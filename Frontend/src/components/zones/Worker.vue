<script setup lang="ts">
import {onMounted, ref} from 'vue';

interface License {
  id: number;
  name: string;
}

const props = defineProps<{
  name: string;
  workerId: number;
  licenses: License[];
  availability: boolean;
}>();

const task = ref('');
const qualified = ref(false);

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

const isWorkerQualified = (task: any) => {
  if (task) {
    return task.task.requiredLicense.every((license: any) => props.licenses.some((workerLicense: License) => workerLicense.id === license.id));
  } else {
    return true;
  }
};

onMounted(async () => {
  task.value = await getTaskByWorker(props.workerId);
});
</script>

<template>
  <div :class="['worker', { 'unq-worker-box': !qualified , 'hover-effect': !task }]" :draggable="!task">
    <div class="worker-name">{{ name }}</div>
    <div class="worker-licenses">
      <span v-for="(license, index) in licenses" :key="index" class="license">{{ license.name }}</span>
    </div>
    <hr/>
    <div class="worker-status-container">
      <div v-if="task">
        <div v-if="availability" class="worker-task">Task: {{ task.task.name }}</div>
        <div v-if="availability" class="worker-eta">
          ETA: {{ task.eta ? task.eta : 'unavailable' }}
        </div>
      </div>
      <div>

        <div v-if="availability && task" class="worker-status worker-busy">Busy</div>
        <div v-if="availability && !task" class="worker-status worker-Ready">Ready</div>
        <div v-if="availability && task && !qualified" class="worker-status worker-unqualified">Unqualified</div>
        <div v-if="!availability" class="worker-status worker-busy">Unavailable</div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.worker {
  border: 1px solid #ccc;
  border-radius: 15px;
  padding: 1rem;
  max-height: 100px;
  margin-bottom: 1rem;
  background-color: transparent;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.hover-effect:hover {
  background-color: #f1f1f1;
  border: 1px solid #8d8d8d;
}

.unq-worker-box {
  border: 1px solid #fab639;
}

.worker-name {
  line-height: 0.3rem;
  font-size: 1rem;
  font-weight: bold;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-licenses {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.license {
  background-color: #e0e0e0;
  border-radius: 5px;
  padding: 0.2rem 0.5rem;
  font-size: 0.4rem;
  font-weight: bold;
  user-select: none !important;
  -webkit-user-select: none !important;
  color: #333;
}

.worker-status-container {
  display: flex;
  justify-content: space-between;
}

.worker-task,
.worker-eta {
  color: #7B7B7B;
  line-height: 0.2rem;
  margin-top: 0.5rem;
  font-size: 0.5rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-status {
  background-color: white;
  color: white;
  font-size: 0.6rem;
  border-radius: 0.3rem;
  padding: 5px;
  width: 4rem;
  text-align: center;
  line-height: 0.4rem;
  margin-top: 0.1rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-Ready {
  background-color: #79cc5e;
}

.worker-busy {
  background-color: #808080;
}

.worker-unqualified {
  background-color: #fab639;
}

hr {
  margin: 0.5rem 0;
  border: none;
  border-top: 1px solid #ccc;
  user-select: none !important;
}
</style>