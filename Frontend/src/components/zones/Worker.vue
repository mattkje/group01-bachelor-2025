<script setup lang="ts">
import {onMounted, ref, computed} from 'vue';
import {License, ActiveTask, Worker, TimeTable} from '@/assets/types';
import axios from "axios";

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

const getTaskByWorker = async (workerId: number) => {
  try {
    const response = await fetch(`http://localhost:8080/api/active-tasks`);
    const tasks = await response.json();
    const workerTask = tasks.find((task: any) => task.workers.some((worker: any) => worker.id === workerId));
    qualified.value = isWorkerQualified(workerTask);
    return workerTask;
  } catch (error) {
    console.error('Failed to fetch worker task:', error);
  }
};

const fetchTasksForZone = async (zoneId: number): Promise<ActiveTask[]> => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${zoneId}/tasks`);
    return await response.json();
  } catch (error) {
    console.error('Failed to fetch tasks for zone:', error);
    return [];
  }
};

const doesWorkerFulfillAnyTaskLicense = async (zoneId: number, worker: Worker): Promise<boolean> => {
  const tasks = await fetchTasksForZone(zoneId);
  return tasks.some(task =>
      task.requiredLicense.some(license =>
          worker.licenses.some((workerLicense: License) => workerLicense.id === license.id)
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

const fetchTimeTables = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/timetables');
    timeTables.value = await response.json();
  } catch (error) {
    console.error('Error fetching time tables:', error);
  }
};

const startPolling = () => {
  fetchTimeTables();
  setInterval(async () => {
    await fetchTimeTables();
    activeTask.value = await getTaskByWorker(props.workerId);
    qualifiedForAnyTask.value = await doesWorkerFulfillAnyTaskLicense(props.zoneId, {
      id: props.workerId,
      licenses: props.licenses
    });
    overtime.value = overtimeOccurance(activeTask.value);
  }, 5000); // Poll every 5 seconds
};

const referenceTime = ref<Date | null>(null);

const fetchCurrentTimeFromBackend = async (): Promise<void> => {
  try {
    const response = await axios.get('http://localhost:8080/api/simulation/time');
    const [hours, minutes, seconds] = response.data.split(':').map(Number);
    const now = new Date();
    referenceTime.value = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hours, minutes, seconds);
  } catch (error) {
    console.error('Error fetching current time:', error);
  }
};

const startFetchingTime = () => {
  fetchCurrentTimeFromBackend();
  setInterval(fetchCurrentTimeFromBackend, 5000); // Fetch time every 5 seconds
};

const isWorkerPresent = (workerId: number): boolean => {
  if (!referenceTime.value) return false;
  return timeTables.value.some((timeTable: TimeTable) => {
    const realStartTime = new Date(timeTable.realStartTime);
    const realEndTime = new Date(timeTable.realEndTime);
    return timeTable.worker.id === workerId && realStartTime <= referenceTime.value! && realEndTime >= referenceTime.value!;
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
const getRandomProfileImageUrl = (workerId: number) => {
  const gender = workerId % 2 === 0 ? 'men' : 'women';
  const id = workerId % 100;
  return `https://randomuser.me/api/portraits/thumb/${gender}/${id}.jpg`;
};

onMounted(async () => {
  activeTask.value = await getTaskByWorker(props.workerId);
  qualifiedForAnyTask.value = await doesWorkerFulfillAnyTaskLicense(props.zoneId, {
    id: props.workerId,
    licenses: props.licenses
  });
  overtime.value = overtimeOccurance(activeTask.value);
});
</script>

<template>
  <div
      :class="['worker-compact', { 'unq-worker-box': !qualifiedForAnyTask && !doesWorkerHaveUnfinishedActiveTask(workerId) && isWorkerPresent(workerId), 'rdy-worker-box': !doesWorkerHaveUnfinishedActiveTask(workerId) && qualifiedForAnyTask && isWorkerPresent(workerId), 'busy-unq-worker-box': doesWorkerHaveUnfinishedActiveTask(workerId) && !qualified && isWorkerPresent(workerId), 'not-present-worker-box': !isWorkerPresent(workerId), 'hover-effect': !activeTask }]"
      :draggable="!activeTask">
    <div class="worker-profile">
      <img class="worker-image" :src="getRandomProfileImageUrl(props.workerId)" draggable="false"/>
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
</template>
<style scoped>
.worker-compact {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #c0c0c0;
  border-radius: 10px;
  max-height: 40px;
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
}

.unq-worker-box:hover {
  animation: none;
  background-color: #ff9292;
  border: 2px solid #ff4b4b;
}

.rdy-worker-box {
  background-color: #bfffab;
}

.rdy-worker-box:hover {
  background-color: #a3ff8f;
}

.busy-unq-worker-box {
  background-color: #ffebc0; /* Yellow for busy and unqualified */
}

.not-present-worker-box {
  background: #ececec;
  opacity: 0.7;
}

.worker-profile {
  display: flex;
  align-items: center;
}

.worker-name {
  font-size: 0.8rem;
  font-weight: bold;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-image {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  margin-right: 0.5rem;
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