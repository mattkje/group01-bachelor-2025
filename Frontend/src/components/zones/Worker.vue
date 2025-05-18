<script setup lang="ts">
import {onMounted, onUnmounted, ref, watch, watchEffect} from 'vue';
import {License, ActiveTask, TimeTable, Task, PickerTask} from '@/assets/types';
import TaskClass from '@/components/tasks/Task.vue';
import {
  fetchAllActiveTasks, fetchAllPickerTasks,
  fetchAllPickerZones,
  fetchAllTasksForZone,
  fetchSimulationDate,
  fetchSimulationTime,
  fetchAllTimeTables, fetchWorker,
} from '@/composables/DataFetcher';

const POLLING_INTERVAL = 5000;

const props = defineProps<{
  name: string;
  workerId: number;
  licenses: License[];
  availability: boolean;
  zoneId: number;
}>();

const activeTask = ref<ActiveTask | null>(null);
const pickerTask = ref<PickerTask | null>(null);
const qualifiedForAnyTask = ref(true);
const timeTables = ref<TimeTable[]>([]);
const referenceTime = ref<Date | null>(null);
const currentDate = ref<string>('');
let pollingIntervalId: ReturnType<typeof setInterval> | null = null;

const getActiveOrPickerTaskByWorker = async (workerId: number) => {
  try {
    const worker = await fetchWorker(workerId);
    let unknownTypeTasks: any;
    if (await isZonePickerZone(props.zoneId)) {
      unknownTypeTasks = await fetchAllPickerTasks();
      const pickerTask = unknownTypeTasks.find((task: PickerTask) => task.id === worker.currentTaskId);
      return pickerTask || null;
    } else {
      unknownTypeTasks = await fetchAllActiveTasks();
      const activeTask = unknownTypeTasks.find((task: ActiveTask) => task.id === worker.currentTaskId);
      return activeTask || null;
    }

  } catch (error) {
    console.error('Error fetching active task by worker:', error);
    return null;
  }
};

const setTaskBasedOnZoneType = async (workerId: number) => {
  try {
    const worker = await fetchWorker(workerId);
    if (await isZonePickerZone(props.zoneId)) {
      const pickerTasks = await fetchAllPickerTasks();
      pickerTask.value = pickerTasks.find((task: PickerTask) => task.id === worker.currentTaskId);
      activeTask.value = null;
    } else {
      const activeTasks = await fetchAllActiveTasks();
      activeTask.value = activeTasks.find((task: ActiveTask) => task.id === worker.currentTaskId);
      pickerTask.value = null;
    }
  } catch (error) {
    console.error('Error setting task based on zone type:', error);
  }
};

const doesWorkerFulfillAnyTaskLicense = async (zoneId: number): Promise<boolean> => {
  try {
    if (await isZonePickerZone(zoneId)) return true;
    if (props.licenses === null || props.licenses.length === 0) return false;
    const tasks = await fetchAllTasksForZone(zoneId);
    return tasks.some((task: Task) =>
        task.requiredLicense.some((license: License) =>
            props.licenses.some((workerLicense: License) => workerLicense.id === license.id)
        )
    );
  } catch (error) {
    console.error('Error checking task licenses:', error);
    return false;
  }
};

const fetchCurrentTimeFromBackend = async (): Promise<void> => {
  try {
    const timeResponse = await fetchSimulationTime();
    const dateResponse = await fetchSimulationDate();
    currentDate.value = `${dateResponse} ${timeResponse}`;

    const [hours, minutes, seconds] = timeResponse.split(':').map(Number);
    const [year, month, day] = dateResponse.split('-').map(Number);

    referenceTime.value = new Date(year, month - 1, day, hours, minutes, seconds);
  } catch (error) {
    console.error('Error fetching current date and time:', error);
  }
};

const isWorkerPresent = (workerId: number): boolean => {
  if (!referenceTime.value) return false;
  return timeTables.value.some((timeTable: TimeTable) => {
    const realStartTime = new Date(timeTable.realStartTime);
    const realEndTime = new Date(timeTable.realEndTime);
    return (
        timeTable.workerId === workerId &&
        realStartTime <= referenceTime.value! &&
        realEndTime >= referenceTime.value!
    );
  });
};

const doesWorkerHaveUnfinishedActiveTask = (workerId: number): boolean => {
  if (!referenceTime.value) return false;
  if (pickerTask.value && pickerTask.value.worker) {
    return pickerTask.value.worker?.id === workerId && !pickerTask.value.endTime;
  } else if (activeTask.value) {
    return (
        activeTask.value.workers.some((worker) => worker.id === workerId) &&
        (!activeTask.value.endTime || new Date(activeTask.value.endTime) >= referenceTime.value)
    );
  }
};

const isZonePickerZone = async (zoneId: number): Promise<boolean> => {
  try {
    const pickerZones = await fetchAllPickerZones();
    return pickerZones.some((zone: any) => zone.id === zoneId);
  } catch (error) {
    console.error('Error fetching picker zones:', error);
    return false;
  }
};

const startPolling = () => {
  pollingIntervalId = setInterval(async () => {
    try {
      await fetchCurrentTimeFromBackend();
      timeTables.value = await fetchAllTimeTables();
      await setTaskBasedOnZoneType(props.workerId);
      qualifiedForAnyTask.value = await doesWorkerFulfillAnyTaskLicense(props.zoneId);
    } catch (error) {
      console.error('Error during polling:', error);
    }
  }, POLLING_INTERVAL);
};


onMounted(async () => {
  await fetchCurrentTimeFromBackend();
  timeTables.value = await fetchAllTimeTables();
  await setTaskBasedOnZoneType(props.workerId);
  qualifiedForAnyTask.value = await doesWorkerFulfillAnyTaskLicense(props.zoneId);
  startPolling();
});

onUnmounted(() => {
  if (pollingIntervalId) clearInterval(pollingIntervalId);
});
</script>

<template>
  <div class="worker-task-container">
    <div
        :class="['worker-compact', { 'unq-worker-box': !qualifiedForAnyTask && !doesWorkerHaveUnfinishedActiveTask(workerId) && isWorkerPresent(workerId),'busy-worker-box': doesWorkerHaveUnfinishedActiveTask(workerId) && qualifiedForAnyTask && isWorkerPresent(workerId), 'busy-unq-worker-box': doesWorkerHaveUnfinishedActiveTask(workerId) && !qualifiedForAnyTask && isWorkerPresent(workerId), 'not-present-worker-box': !isWorkerPresent(workerId), 'hover-effect': !activeTask }]"
        :draggable="!activeTask && !pickerTask">
      <div class="worker-profile">
        <div class="worker-image-container">
          <img class="worker-image" src="@/assets/icons/profile.svg" draggable="false"/>
        </div>
        <div class="worker-name">{{ props.name }}</div>
      </div>
      <div class="status-container">
        <img :draggable="!activeTask && !pickerTask"
             v-if="!doesWorkerHaveUnfinishedActiveTask(workerId) && qualifiedForAnyTask && isWorkerPresent(workerId)"
             src="/src/assets/icons/ready.svg" class="status-icon" alt="Ready"/>
        <img :draggable="!activeTask && !pickerTask"
             v-if="!doesWorkerHaveUnfinishedActiveTask(workerId) && !qualifiedForAnyTask && isWorkerPresent(workerId)"
             src="/src/assets/icons/warning-severe.svg" class="status-icon" alt="Unqualified Severe"/>
        <TaskClass
            v-if="activeTask"
            :active-task="activeTask"
            :requiredLicenses="activeTask.task.requiredLicense"
            :qualified="qualifiedForAnyTask"
            :current-date="currentDate"
            :picker-task="null"/>

        <TaskClass
            v-if="pickerTask"
            :active-task="null"
            :requiredLicenses="[]"
            :qualified="true"
            :current-date="currentDate"
            :picker-task="pickerTask"/>
        <div v-if="!doesWorkerHaveUnfinishedActiveTask(workerId) && !qualifiedForAnyTask && isWorkerPresent(workerId)"
             class="status-popup">
          Unqualified
        </div>
        <div v-if="doesWorkerHaveUnfinishedActiveTask(workerId) && qualifiedForAnyTask && isWorkerPresent(workerId)"
             class="status-popup">Busy
        </div>
        <div v-if="doesWorkerHaveUnfinishedActiveTask(workerId) && !qualifiedForAnyTask && isWorkerPresent(workerId)"
             class="status-popup">Busy & Unqualified
        </div>
        <div v-if="!doesWorkerHaveUnfinishedActiveTask(workerId) && qualifiedForAnyTask && isWorkerPresent(workerId)"
             class="status-popup">Ready
        </div>
        <div v-if="!isWorkerPresent(workerId)" class="status-popup">Not Present</div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.worker-task-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.worker-compact {
  width: 100%;
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--background-backdrop);
  border-radius: 10px;
  max-height: 30px;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}


@keyframes pulse-border {
  0% {
    border-color: var(--main-color);
    box-shadow: 0 0 2px #ff4b4b;
  }
  50% {
    border-color: var(--main-color-2);
    box-shadow: 0 0 0 #ff4b4b;
  }
  100% {
    border-color: var(--main-color);
    box-shadow: 0 0 2px #ff4b4b;
  }
}

.unq-worker-box {
  background-color: var(--main-color-3);
  border: 2px solid var(--main-color);
  animation: pulse-border 2s infinite;
  border-radius: 10px;
}

.unq-worker-box:hover {
  animation: none;
  background-color: var(--main-color);
  border: 2px solid #ff4b4b;
}

.busy-worker-box {
  background-color: var(--busy-color);
  border-radius: 10px
}

.busy-worker-box:hover {
  background-color: var(--busy-color-2);
}

.busy-unq-worker-box {
  background-color: var(--yellow-color);
}

.not-present-worker-box {
  background: var(--background-2);
  border-radius: 10px;
  opacity: 0.7;
}

.not-present-worker-box:hover {
  border: 2px solid var(--border-1);
}

.worker-profile {
  display: flex;
  align-items: center;
}

.worker-name {
  font-size: 0.6rem;
  font-weight: bold;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-image-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: var(--background-1);
  margin-right: 0.5rem;
}

.worker-image {
  max-width: 70%;
  max-height: 70%;
}

.warning-icon {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
}

.status-icon {
  margin-top: 7px;
  width: 20px;
  height: 20px;
}

.status-popup {
  display: none;
  position: absolute;
  top: -25px;
  right: 0;
  background-color: var(--background-1);
  color: var(--text-1);
  padding: 5px;
  border-radius: 3px;
  font-size: 0.7rem;
  white-space: nowrap;
}

.worker-compact:hover .status-popup {
  display: block;
}

.worker-status-container {
  display: flex;
  justify-content: space-between;
  margin-top: 0.5rem;
}

.worker-status {
  background-color: var(--background-1);
  color: var(--text-1);
  font-size: 0.5rem;
  border-radius: 0.2rem;
  padding: 0.2rem;
  width: 3rem;
  text-align: center;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-ready {
  background-color: #79cc5e;
}

.worker-busy {
  background-color: #808080;
}

.worker-unqualified {
  background-color: #fa7d39;
}

.worker-unavailable {
  background-color: #ff4d4d;
}
</style>