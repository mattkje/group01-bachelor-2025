<script setup lang="ts">
import {computed, ref, onMounted} from 'vue';
import WorkerCompact from '@/components/zones/Worker.vue';
import ZoneMenu from "@/components/zones/ZoneMenu.vue";

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
  name: string;
  licenses: License[];
  availability: boolean;
}

const props = defineProps<{
  title: string;
  zoneId: number;
  workers: Worker[];
}>();

const emit = defineEmits(['refreshWorkers']);

const showPopup = ref(false);
const selectedZone = ref({id: 0, name: ''});
const isDraggingOver = ref(false);
const tasks = ref<Task[]>([]);
const hasTasks = ref(false);
const isSpinning = ref(false);
const remainingTasks = computed(() => tasks.value.length);
let completionTime = ref(null);

const openPopup = () => {
  selectedZone.value = {id: props.zoneId, name: props.title};
  showPopup.value = true;
};

const closePopup = () => {
  showPopup.value = false;
};


const runMonteCarloSimulation = async () => {
  isSpinning.value = true;
  try {
    const response = await fetch(`http://localhost:8080/api/monte-carlo/zones/${props.zoneId}`, {
      method: 'GET',
    });

    if (!response.ok) {
      throw new Error('Failed to run simulation');
    }
    const result = await response.json()
    console.log(result);
    if (result.length < 2) {
      completionTime = result[0];
    }

    emit('refreshWorkers');
  } catch (error) {
    console.error('Error running simulation:', error);
  } finally {
    // wait for 1 second to show the spinning animation
    await new Promise(resolve => setTimeout(resolve, 1000));
    isSpinning.value = false;
  }
};

const fetchTasksForZone = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${props.zoneId}/tasks`);
    tasks.value = await response.json();
  } catch (error) {
    console.error('Failed to fetch tasks for zone:', error);
  }
};

const isWorkerQualifiedForAnyTask = (worker: Worker) => {
  return tasks.value.some(task =>
      task.requiredLicense.every(license =>
          worker.licenses.some(workerLicense => workerLicense.id === license.id)
      )
  );
};

onMounted(async () => {
  await fetchTasksForZone();
  hasTasks.value = tasks.value.length > 0;
});

const onDragStart = (event: DragEvent, worker: Worker) => {
  event.dataTransfer?.setData('worker', JSON.stringify(worker));
};

const onDrop = async (event: DragEvent) => {
  const worker = JSON.parse(event.dataTransfer?.getData('worker') || '{}');

  try {
    const response = await fetch(`http://localhost:8080/api/workers/${worker.id}/zone/${props.zoneId}`, {
      method: 'PUT',
    });

    if (!response.ok) {
      throw new Error('Failed to update worker zone');
    }

    console.log('Worker zone updated successfully');

    emit('refreshWorkers');
  } catch (error) {
    console.error('Error updating worker zone:', error);
  }

  isDraggingOver.value = false;
};

const onDragOver = (event: DragEvent) => {
  event.preventDefault();
  isDraggingOver.value = true;
};

const onDragLeave = () => {
  isDraggingOver.value = false;
};
</script>

<template>
  <div class="rounded-square">
    <div class="title-bar">
      <div class="title-bar-status">
        {{ title }}
        <div class="task-summary">
          Done by: {{ completionTime }}
          <br/>
          Tasks: {{ remainingTasks }}
        </div>
      </div>
      <hr>
      <div class="zone-options">
        <button class="icon-button" @click="openPopup">
          <img src="/src/assets/icons/overview.svg" alt="Assign"/>
        </button>
        <button class="icon-button">
          <img src="/src/assets/icons/tasks.svg" alt="Assign"/>
        </button>
        <button class="icon-button" @click="runMonteCarloSimulation">
          <img :class="{ 'spin-animation': isSpinning }" src="/src/assets/icons/simulation.svg" alt="Assign"/>
        </button>
        <button v-if="false" class="icon-button bell-icon">
          <img src="/src/assets/icons/bell.svg" alt="Assign"/>
        </button>
        <button v-if="true" class="icon-button bell-icon">
          <img src="/src/assets/icons/bellUpdate.svg" alt="Assign"/>
        </button>
      </div>
    </div>
    <div v-if="hasTasks" class="vertical-box" @drop="onDrop" @dragover="onDragOver" @dragleave="onDragLeave">
      <WorkerCompact
          v-for="(worker, index) in workers"
          :key="index"
          :name="worker.name"
          :worker-id="worker.id"
          :licenses="worker.licenses"
          :availability="worker.availability"
          :qualified="isWorkerQualifiedForAnyTask(worker)"
          :zone-id="props.zoneId"
          :class="{ 'unavailable': !worker.availability }"
          @dragstart="(event) => onDragStart(event, worker)"
      />
      <div v-if="isDraggingOver" class="on-drop-worker-box"/>
      <div v-if="workers.length === 0" class="vertical-box" style="text-align: center; margin-top: 1rem;">
        <img
            src="/src/assets/icons/warning.svg"
            alt="Check"
            style="margin: auto; margin-top: 1rem; width: 50px; height: 50px;"
        />
        <p style="text-align: center; margin-top: 1rem;">
          No workers assigned
        </p>
      </div>
    </div>
    <div v-else class="vertical-box" style="opacity: 0.5">
      <img
          src="/src/assets/icons/check.svg"
          alt="Check"
          style="margin: 10px auto; margin-top: 1rem; width: 50px; height: 50px;"
      />
      <p style="text-align: center;">
        All tasks completed
      </p>

    </div>
  </div>
  <ZoneMenu v-if="showPopup" :zone="selectedZone" @close="closePopup"/>
</template>

<style scoped>
.rounded-square {
  width: 280px;
  height: 100%;
  border: 1px solid #e5e5e5;
  border-radius: 15px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.on-drop-worker-box {
  height: 45px;
  width: 100%;
  background-color: #ececec;
  border-radius: 7px;
  pointer-events: none;
}

.title-bar {
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  padding: 1rem;
  font-size: 1.2rem;
  line-height: 0.7rem;
  font-weight: bold;
  color: #7B7B7B;
  border-bottom: 1px solid #e5e5e5;
}

.title-bar hr {
  width: 100%;
  margin: 0.5rem 0;
  border: none;
  border-top: 1px solid #ccc;
}

.title-bar-status {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  font-size: 1.2rem;
  font-weight: bold;
  color: #7B7B7B;
}

.task-summary {
  color: #7B7B7B;
  display: flex;
  line-height: 1rem;
  font-size: 0.7rem;
  flex-direction: column;
  align-items: flex-end;
}

.vertical-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1rem;
}

.unavailable {
  display: none;
}

.unqualified {
  color: #f56e6e;
  border-radius: 0.5rem;
  padding: 10px;
  line-height: 0.2rem;
  margin-top: 0.1rem;
}

.icon-button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  width: 20px;
  height: 20px;
  margin-right: 1rem;
  color: #b77979;
}

.icon-button img {
  width: 20px;
  height: 20px;
}

.icon-button:hover {
  color: #000;
}

.bell-icon {
  margin-left: 6rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.spin-animation {
  animation: spin 1s ease-in-out infinite;
}
</style>