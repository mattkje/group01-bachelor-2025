<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';

interface License {
  id: number;
  name: string;
}

interface Task {
  id: number;
  requiredLicense: License[];
}

interface Worker {
  id: number;
  licenses: License[];
}

const props = defineProps<{
  name: string;
  workerId: number;
  licenses: License[];
  availability: boolean;
  zoneId: number;
}>();

const task = ref<Task | null>(null);
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

const fetchTasksForZone = async (zoneId: number): Promise<Task[]> => {
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
  if (!task || !task.eta || task.task.maxTime) return false;
  return task.task.maxTime < task.eta;
};

const getRandomProfileImageUrl = (workerId: number, isToon: boolean) => {
  if (isToon) {
    return `https://joesch.moe/api/v1/${workerId}`;
  } else {
    const gender = workerId % 2 === 0 ? 'men' : 'women';
    const id = workerId % 100;
    return `https://randomuser.me/api/portraits/med/${gender}/${id}.jpg`;
  }
};
onMounted(async () => {
  task.value = await getTaskByWorker(props.workerId);
  qualifiedForAnyTask.value = await doesWorkerFulfillAnyTaskLicense(props.zoneId, { id: props.workerId, licenses: props.licenses });
  overtimeOccurance(task.value) ? overtime.value = true : overtime.value = false;
});
</script>

<template>
  <div :class="['worker-compact', { 'rdy-worker-box': !task, 'hover-effect': !task }]" :draggable="!task">
    <div class="worker-profile">
      <img class="worker-image" :src="getRandomProfileImageUrl(workerId, false)" />
      <div class="worker-name">{{ name }}</div>
    </div>
    <div class="status-container">
      <div v-if="overtime" class="status-popup">Error occured</div>
    </div>
  </div>
</template>

<style scoped>
.worker-compact {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ececec;
  opacity: 0.5;
  border-radius: 10px;
  max-height: 40px;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-compact:hover {
  background-color: #dcdcdc;
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
  opacity: 1;
}

.busy-unq-worker-box {
  background-color: #ffebc0; /* Yellow for busy and unqualified */
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