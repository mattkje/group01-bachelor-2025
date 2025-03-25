<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import Worker from '@/components/zones/RegistryWorker.vue';

interface License {
  id: number;
  name: string;
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
  taskLessWorkers: Worker[];
}>();

const emit = defineEmits(['refreshWorkers']);

const isDraggingOver = ref(false);
const showBusy = ref(false);
const showUnavailable = ref(false);
const searchQuery = ref('');


const filteredWorkers = computed(() => {
  let workers: Worker[];
  if (!showBusy.value) {
    workers = props.taskLessWorkers;
  } else {
    workers = props.workers;
  }
  if (!showUnavailable.value) {
    workers = workers.filter(worker => worker.availability);
  }
  if (searchQuery.value) {
    workers = workers.filter(worker =>
        worker.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    );
  }
  // Sort workers so that non-busy workers are shown at the top
  workers = workers.sort((a, b) => {
    if (a.availability && !b.availability) return -1;
    if (!a.availability && b.availability) return 1;
    return 0;
  });
  return workers;
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
        Workers
      </div>
      <hr/>
      <div class="filter-box">
        <p class="filter-text">Show busy workers</p>
        <input type="checkbox" v-model="showBusy"/>
      </div>
      <div class="filter-box">
        <p class="filter-text">Show unavailable workers</p>
        <input type="checkbox" v-model="showUnavailable"/>
      </div>
      <div class="search-box">
        <input type="text" v-model="searchQuery" placeholder="Search workers..."/>
      </div>
    </div>
    <div class="vertical-box" @drop="onDrop" @dragover="onDragOver" @dragleave="onDragLeave">
      <Worker
          v-for="(worker, index) in filteredWorkers"
          :key="index"
          :name="worker.name"
          :worker-id="worker.id"
          :availability="worker.availability"
          :zone-id="props.zoneId"
          :licenses="worker.licenses"
          @dragstart="(event) => onDragStart(event, worker)"
      />
      <div v-if="isDraggingOver" class="on-drop-worker-box"/>
    </div>
  </div>
</template>

<style scoped>
.rounded-square {
  width: 280px;
  border-left: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
}

.on-drop-worker-box {
  height: 45px;
  width: 100%;
  border-radius: 7px;
  pointer-events: none;
}

.title-bar {
  display: flex;
  flex-direction: column;
  
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

.vertical-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: scroll;
  padding: 1rem 1rem 4.5rem;
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
  animation: spin 1s ease-in-out;
}

.filter-box {
  display: flex;
  align-items: center;
  margin-top: 1rem;
}

.filter-text {
  font-size: 0.8rem;
  color: #7B7B7B;
  margin: auto 1rem auto 0;
}

.search-box {
  margin-top: 1rem;
}

.search-box input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 5px;
}
</style>