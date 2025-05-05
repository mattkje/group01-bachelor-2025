<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {License, ActiveTask, TimeTable, Task} from '@/assets/types';
import TaskClass from "@/components/tasks/Task.vue";
import {
  fetchAllActiveTasks, fetchAllPickerZones,
  fetchAllTasksForZone, fetchSimulationDate,
  fetchSimulationTime,
  fetchTimeTables
} from "@/composables/DataFetcher";

const props = defineProps<{
  name: string;
  workerId: number;
  licenses: License[];
  availability: boolean;
  zoneId: number;
}>();

const activeTask = ref<ActiveTask | null>(null);
const qualified = ref(false);
const qualifiedForAnyTask = ref(false);
const overtime = ref(false);


const getActiveTaskByWorker = async (workerId: number) => {
  const activeTasks = await fetchAllActiveTasks();
  const workerTask = activeTasks.find((task: any) => task.workers.some((worker: any) => worker.id === workerId));
  qualified.value = isWorkerQualified(workerTask);
  return workerTask;
};

const doesWorkerFulfillAnyTaskLicense = async (zoneId: number): Promise<boolean> => {
  if (await isZonePickerZone(zoneId)) return true;
  const tasks = await fetchAllTasksForZone(zoneId);
  return tasks.some((task: Task) =>
      task.requiredLicense.some((license: License) =>
          props.licenses.some((workerLicense: License) => workerLicense.id === license.id)
      )
  );
};

const isWorkerQualified = (task: any) => {
  if (props.zoneId === 0) return true;
  if (task) {
    return task.task.requiredLicense.every((license: any) => props.licenses.some((workerLicense: License) => workerLicense.id === license.id));
  } else {
    return true;
  }
};

const overtimeOccurance = (task: any) => {
  if (!task || !task.eta || !task.task.maxTime) return false;
  return task.task.maxTime < task.eta;
};

const timeTables = ref<TimeTable[]>([]);


const startPolling = () => {
  fetchTimeTables();
  setInterval(async () => {
    timeTables.value = await fetchTimeTables();
    activeTask.value = await getActiveTaskByWorker(props.workerId);
    qualifiedForAnyTask.value = await doesWorkerFulfillAnyTaskLicense(props.zoneId);
    overtime.value = overtimeOccurance(activeTask.value);
  }, 5000);
};

const referenceTime = ref<Date | null>(null);

const fetchCurrentTimeFromBackend = async (): Promise<void> => {
  try {
    const timeResponse = await fetchSimulationTime();
    const dateResponse = await fetchSimulationDate();

    const [hours, minutes, seconds] = timeResponse.split(':').map(Number);
    const [year, month, day] = dateResponse.split('-').map(Number);

    referenceTime.value = new Date(year, month - 1, day, hours, minutes, seconds);
  } catch (error) {
    console.error('Error fetching current date and time:', error);
  }
};

const startFetchingTime = () => {
  fetchCurrentTimeFromBackend();
  setInterval(fetchCurrentTimeFromBackend, 5000);
};

const isWorkerPresent = (workerId: number): boolean => {
  if (!referenceTime.value) return false;
  return timeTables.value.some((timeTable: TimeTable) => {
    const realStartTime = new Date(timeTable.realStartTime);
    const realEndTime = new Date(timeTable.realEndTime);
    return timeTable.workerId === workerId && realStartTime <= referenceTime.value! && realEndTime >= referenceTime.value!;
  });
};

const doesWorkerHaveUnfinishedActiveTask = (workerId: number): boolean => {
  if (!referenceTime.value) return false;
  if (!activeTask.value) return false;
  return activeTask.value.workers.some(worker => worker.id === workerId) && (!activeTask.value.endTime || new Date(activeTask.value.endTime) >= referenceTime.value);
};

onMounted(() => {
  startFetchingTime();
  startPolling();
});
const isZonePickerZone = async (zoneId: number): Promise<boolean> => {
  const pickerZones = await fetchAllPickerZones();
  return pickerZones.some((zone: any) => zone.id === zoneId);
};

onMounted(async () => {
  activeTask.value = await getActiveTaskByWorker(props.workerId);
  qualifiedForAnyTask.value = await doesWorkerFulfillAnyTaskLicense(props.zoneId);
  overtime.value = overtimeOccurance(activeTask.value);
});
</script>

<template>
  <div class="worker-task-container">
    <div
        :class="['worker-compact', { 'unq-worker-box': !qualifiedForAnyTask && !doesWorkerHaveUnfinishedActiveTask(workerId) && isWorkerPresent(workerId), 'rdy-worker-box': !doesWorkerHaveUnfinishedActiveTask(workerId) && qualifiedForAnyTask && isWorkerPresent(workerId), 'busy-unq-worker-box': doesWorkerHaveUnfinishedActiveTask(workerId) && !qualified && isWorkerPresent(workerId), 'not-present-worker-box': !isWorkerPresent(workerId), 'hover-effect': !activeTask }]"
        :draggable="!activeTask">
      <div class="worker-profile">
        <div class="worker-image-container">
          <img class="worker-image" src="@/assets/icons/profile.svg" draggable="false"/>
        </div>
        <div class="worker-name">{{ props.name }}</div>
      </div>
      <div class="status-container">
        <img :draggable="!activeTask" v-if="doesWorkerHaveUnfinishedActiveTask(workerId) && isWorkerPresent(workerId)" src="/src/assets/icons/busy.svg"
             class="status-icon" alt="Busy"/>
        <img :draggable="!activeTask" v-if="!doesWorkerHaveUnfinishedActiveTask(workerId) && qualifiedForAnyTask && isWorkerPresent(workerId)"
             src="/src/assets/icons/ready.svg" class="status-icon" alt="Ready"/>
        <img :draggable="!activeTask" v-if="overtime" src="/src/assets/icons/overtime.svg" class="status-icon"
             alt="Error"/>
        <img :draggable="!activeTask" v-if="!qualified && doesWorkerHaveUnfinishedActiveTask(workerId) && isWorkerPresent(workerId)"
             src="/src/assets/icons/warning.svg" class="status-icon" alt="Unqualified"/>
        <img :draggable="!activeTask" v-if="!doesWorkerHaveUnfinishedActiveTask(workerId) && !qualifiedForAnyTask && isWorkerPresent(workerId)"
             src="/src/assets/icons/warning-severe.svg" class="status-icon" alt="Unqualified Severe"/>
        <div v-if="!doesWorkerHaveUnfinishedActiveTask(workerId) && !qualifiedForAnyTask && isWorkerPresent(workerId)" class="status-popup">
          Unqualified
        </div>
        <div v-if="doesWorkerHaveUnfinishedActiveTask(workerId) && qualified && isWorkerPresent(workerId)" class="status-popup">Busy</div>
        <div v-if="doesWorkerHaveUnfinishedActiveTask(workerId) && !qualified && isWorkerPresent(workerId)" class="status-popup">Busy & Unqualified</div>
        <div v-if="!doesWorkerHaveUnfinishedActiveTask(workerId) && qualifiedForAnyTask && isWorkerPresent(workerId)" class="status-popup">Ready</div>
        <div v-if="!isWorkerPresent(workerId)" class="status-popup">Not Present</div>
        <div v-if="overtime" class="status-popup">Error occurred</div>
      </div>
    </div>
    <TaskClass
        v-if="doesWorkerHaveUnfinishedActiveTask(workerId) && activeTask"
        :task-id="activeTask.id"
        :name="activeTask.task.name"
        :requiredLicenses="activeTask.task.requiredLicense"
        :zone-id="props.zoneId"
    />
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
  background-color: #c0c0c0;
  border-radius: 10px 0 0 10px;
  max-height: 30px;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}

@keyframes pulse-border {
  0% {
    border-color: #ff4b4b;
    box-shadow: 0 0 2px #ff4b4b;
  }
  50% {
    border-color: #ffb4b4;
    box-shadow: 0 0 0 #ff4b4b;
  }
  100% {
    border-color: #ff4b4b;
    box-shadow: 0 0 2px #ff4b4b;
  }
}

.unq-worker-box {
  background-color: #ffcccc; /* Red for unqualified and ready */
  border: 2px solid #ff4b4b;
  animation: pulse-border 2s infinite;
  border-radius: 10px;
}

.unq-worker-box:hover {
  animation: none;
  background-color: #ff9292;
  border: 2px solid #ff4b4b;
}

.rdy-worker-box {
  background-color: #bfffab;
  border-radius: 10px
}

.rdy-worker-box:hover {
  background-color: #a3ff8f;
}

.busy-unq-worker-box {
  background-color: #ffebc0;
}

.not-present-worker-box {
  background: #ececec;
  border-radius: 10px;
  opacity: 0.7;
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
  background-color: #f6f6f6;
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
  background-color: #333;
  color: #fff;
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
  background-color: white;
  color: white;
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