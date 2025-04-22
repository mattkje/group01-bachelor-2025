<script setup lang="ts">
import {computed, ref, onMounted, onUnmounted} from 'vue';
import WorkerClass from '@/components/zones/Worker.vue';
import NotificationBubble from "@/components/notifications/NotificationBubble.vue";
import {License, PickerTask, Task, Worker, Zone} from '@/assets/types';

const props = defineProps<{
  title: string;
  zoneId: number;
  workers: Worker[];
}>();

const emit = defineEmits(['refreshWorkers']);

const showPopup = ref(false);
const selectedZone = ref<Zone | null>(null);
const isDraggingOver = ref(false);
const tasks = ref<Task[]>([]);
const pickerTasks = ref<PickerTask[]>([]);
const hasTasks = ref(false);
const hasPickerTasks = ref(false);
const isSpinning = ref(false);
const remainingTasks = computed(() => tasks.value.length);
const remainingPickerTasks = computed(() => pickerTasks.value.length);
let completionTime = ref<string>('');
const showNotificationBubble = ref(false);
const notificationMessage = ref<string[]>([]);
const notification = ref(false);
const isPickerZone = ref(false);


const getThisZone = async (): Promise<Zone | null> => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${props.zoneId}`);
    if (!response.ok) {
      throw new Error('Failed to fetch zone');
    }
    const zone: Zone = await response.json();
    return zone;
  } catch (error) {
    console.error('Error fetching zone:', error);
    return null;
  }
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
    if (result.length < 3) {
      completionTime = result[0];
    } else {
      notification.value = true;
      let notifications: string[] = result;
      notificationMessage.value = notifications;
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

const isWorkerQualifiedForAnyTask = async (worker: Worker) => {
  if (isPickerZone.value) {
    return true;
  }
  return tasks.value.some((task: Task) =>
    task.requiredLicense.every((license: License) =>
      worker.licenses.some((workerLicense: License) => workerLicense.id === license.id)
    )
  );
};

const fetchPickerTasksForZone = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${props.zoneId}/picker-tasks`);
    pickerTasks.value = await response.json();
  } catch (error) {
    console.error('Failed to fetch picker tasks for zone:', error);
  }
};

onMounted(async () => {
  if ((await getThisZone()).isPickerZone) {
    isPickerZone.value = true;
    await fetchPickerTasksForZone();
    hasPickerTasks.value = pickerTasks.value.length > 0;
  }
  else {
    await fetchTasksForZone();
    hasTasks.value = tasks.value.length > 0;
  }
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

const toggleNotificationBubble = () => {
  showNotificationBubble.value = !showNotificationBubble.value;
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
          <p v-if="remainingTasks"> Tasks: {{ remainingTasks }}</p>
          <p v-if="remainingPickerTasks"> Picker Tasks: {{ remainingPickerTasks }}</p>
        </div>
      </div>
      <hr>
      <div class="zone-options">
        <div class="zone-option">
          <router-link :to="`/zones/${props.zoneId}`" class="icon-button">
              <img src="/src/assets/icons/zones.svg" alt="Assign"/>
          </router-link>
          <div class="status-popup">Tasks</div>
        </div>
        <div class="zone-option">
          <button class="icon-button" @click="runMonteCarloSimulation">
            <img :class="{ 'spin-animation': isSpinning }" src="/src/assets/icons/simulation.svg" alt="Assign"/>
          </button>
          <div class="status-popup">Simulate zone</div>
        </div>
        <div v-if="notification" class="zone-option">
          <button class="icon-button bell-icon" @mouseenter="toggleNotificationBubble"
                  @mouseleave="toggleNotificationBubble">
            <img src="/src/assets/icons/bellUpdate.svg" alt="Assign"/>
          </button>
          <div class="status-popup">Notifications</div>
        </div>
        <div v-else class="zone-option">
          <button class="icon-button bell-icon">
            <img src="/src/assets/icons/bell.svg" alt="Assign"/>
          </button>
          <div class="status-popup">Notifications</div>
        </div>
      </div>
    </div>
      <div v-if="hasTasks || hasPickerTasks" class="vertical-worker-box" @drop="onDrop" @dragover="onDragOver" @dragleave="onDragLeave">
        <WorkerClass
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
      <div v-else class="vertical-box" style="text-align: center; margin-top: 1rem; opacity: 0.5">
        <img
            src="/src/assets/icons/check.svg"
            alt="Check"
            style="margin: 10px auto; margin-top: 1rem; width: 50px; height: 50px;"
        />
        <p style="text-align: center; margin-top: 1rem;">
          All tasks completed
        </p>
      </div>
    <NotificationBubble v-if="showNotificationBubble" :messages="notificationMessage"/>
  </div>
</template>

<style scoped>
.rounded-square {
  width: 250px;
  height: 100%;
  border: 1px solid #e5e5e5;
  border-radius: 15px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.on-drop-worker-box {
  height: 30px;
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

.horizontal-box {
  flex: 1;
  display: flex;
  flex-direction: row;
  overflow-y: scroll;
  padding: 1rem 1rem 4.5rem;
}

.task-summary {
  color: #7B7B7B;
  display: flex;
  line-height: 1rem;
  font-size: 0.7rem;
  flex-direction: column;
  align-items: flex-end;
}

.vertical-worker-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1rem;
}

.vertical-task-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1rem;
}

.unavailable {
  display: none;
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

.zone-option:hover .status-popup {
  display: block;
}


.unqualified {
  color: #f56e6e;
  border-radius: 0.5rem;
  padding: 10px;
  line-height: 0.2rem;
  margin-top: 0.1rem;
}

.zone-options {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.zone-option {
  position: relative;
  align-content: center;
  justify-content: center;
}

.zone-option a {
  text-decoration: none;
  padding: 0;
  margin: 0.2rem 0 0 0;
  display: block;
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
  width: 15px;
  height: 15px;
}

.icon-button:hover {
  color: #000;
}

.bell-icon {
  margin-left: 8.5rem;
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