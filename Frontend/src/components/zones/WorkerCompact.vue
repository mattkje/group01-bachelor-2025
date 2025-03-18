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

onMounted(async () => {
  task.value = await getTaskByWorker(props.workerId);
  qualifiedForAnyTask.value = await doesWorkerFulfillAnyTaskLicense(props.zoneId, { id: props.workerId, licenses: props.licenses });
  overtimeOccurance(task.value) ? overtime.value = true : overtime.value = false;
});
</script>

<template>
  <div :class="['worker-compact', { 'unq-worker-box': !qualifiedForAnyTask && !task, 'rdy-worker-box': !task && qualified && qualifiedForAnyTask, 'busy-unq-worker-box': task && !qualified, 'hover-effect': !task }]" :draggable="!task">
    <div class="worker-name">{{ name }}</div>
    <div class="status-container">

      <img v-if="task" src="/src/assets/icons/busy.svg" class="status-icon" alt="Busy" />
      <img v-if="!task && qualified && qualifiedForAnyTask" src="/src/assets/icons/ready.svg" class="status-icon" alt="Ready" />
      <img v-if="overtime" src="/src/assets/icons/overtime.svg" class="status-icon" alt="Error" />
      <img v-if="!qualified && task" src="/src/assets/icons/warning.svg" class="status-icon" alt="Unqualified" />
      <img v-if="!task && !qualifiedForAnyTask" src="/src/assets/icons/warning-severe.svg" class="status-icon" alt="Unqualified Severe" />
      <div v-if="!task && !qualifiedForAnyTask" class="status-popup">Unqualified</div>
      <div v-if="task && qualified" class="status-popup">Busy</div>
      <div v-if="task && !qualified" class="status-popup">Busy & Unqualified</div>
      <div v-if="overtime" class="status-popup">Error occured</div>
    </div>
  </div>
</template>

<style scoped>
.worker-compact {
  position: relative;
  display: flex;
  justify-content: space-between;
  background-color: #dedede;
  border-radius: 7px;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.hover-effect:hover {
  background-color: #f1f1f1;
  border: 1px solid #8d8d8d;
}

.unq-worker-box {
  background-color: #ffb4b4; /* Red for unqualified and ready */
}

.rdy-worker-box {
  background-color: #bfffab;
}

.busy-unq-worker-box {
  background-color: #ffebc0; /* Yellow for busy and unqualified */
}


.worker-name {
  font-size: 0.8rem;
  font-weight: bold;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.warning-icon {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
}

.status-icon {
  margin-left: 5px;
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