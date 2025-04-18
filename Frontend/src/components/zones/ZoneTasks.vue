<script setup lang="ts">
import {ref, computed, onMounted} from 'vue';
import ActiveTaskComponent from "@/components/tasks/ActiveTaskComponent.vue";
import {ActiveTask, Zone, PickerTasks} from "@/assets/types";
import PickerTaskComponent from "@/components/tasks/PickerTaskComponent.vue";

interface Task {
  id: number;
  name: string;
  description: string;
  requiredLicense: { name: string }[];
  maxTime: number;
  minTime: number;
  zoneId: number;
  minWorkers: number;
  maxWorkers: number;
}



const props = defineProps<{
  zone: Zone;
}>();

const tasks = ref<Task[]>([]);
const activeTasks = ref<ActiveTask[]>([]);
const pickerTasks = ref<PickerTasks[]>([]);
const zones = ref<Zone[]>([]);

const currentPage = ref(1);
const tasksPerPage = 9;
const showModal = ref(false);

const fetchTasks = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/tasks');
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    tasks.value = await response.json();
  } catch (error) {
    console.error('Failed to fetch tasks:', error);
  }
};

const fetchActiveTasks = async (id: number) => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${id}/active-tasks-now`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    activeTasks.value = await response.json();
    activeTasks.value.sort((a, b) => a.id - b.id);
  } catch (error) {
    console.error('Failed to fetch active tasks:', error);
  }
};

const fetchPickerTasks = async (id: number) => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${id}/picker-tasks-now`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    pickerTasks.value = await response.json();
    pickerTasks.value.sort((a, b) => a.id - b.id);
  } catch (error) {
    console.error('Failed to fetch picker tasks:', error);
  }
};

const fetchZones = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/zones');
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    zones.value = await response.json();
  } catch (error) {
    console.error('Failed to fetch zones:', error);
  }
};

onMounted(() => {
  fetchTasks();
  if (props.zone.isPickerZone) {
    fetchPickerTasks(props.zone.id);
  } else {
    fetchActiveTasks(props.zone.id);
  }
  fetchZones();

  console.log(activeTasks);
});

const filteredTasks = computed(() => {
  return tasks.value.filter(task => task.zoneId === props.zone.id);
});

const getWorkersForTask = (taskId: number) => {
  const activeTask = activeTasks.value.find(task => task.id === taskId);
  if (activeTask) {
    const uniqueWorkers = new Set(activeTask.workers.map(worker => worker.id));
    return Array.from(uniqueWorkers).map(id => activeTask.workers.find(worker => worker.id === id));
  }
  return [];
};

const paginatedTasks = computed(() => {
  const start = (currentPage.value - 1) * tasksPerPage;
  const end = start + tasksPerPage;
  return filteredTasks.value.slice(start, end);
});

const totalPages = computed(() => {
  return Math.ceil(filteredTasks.value.length / tasksPerPage);
});

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const taskDeleted = () => {
  fetchTasks();
  if (props.zone.isPickerZone) {
    fetchPickerTasks(props.zone.id);
  } else {
    fetchActiveTasks(props.zone.id);
  }
  fetchZones();
};
</script>

<template>
  <div class="content">
    <h2 v-if="!zone.isPickerZone">Active Tasks</h2>
    <h2 v-if="zone.isPickerZone">Picker Tasks</h2>
    <div class="activeTaskContainer">
      <div class="placeholder" v-if="activeTasks.length === 0 && pickerTasks.length === 0">No tasks remaining...</div>
      <active-task-component
          v-if="!zone.isPickerZone"
          v-for="activeTask in activeTasks"
          :key="activeTask.id"
          :active-task="activeTask"
          @task-deleted="taskDeleted"/>
      <picker-task-component
          v-if="zone.isPickerZone"
          v-for="pickerTask in pickerTasks"
          :key="pickerTask.id"
          :picker-task="pickerTask"
          @task-deleted="taskDeleted"/>
    </div>
    <div class="pagination" v-if="activeTasks.length > 0">
      <button @click="prevPage" :disabled="currentPage === 1">Previous</button>
      <span>Page {{ currentPage }} of {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages">Next</button>
    </div>
  </div>
</template>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin: 0 3.5rem;
  height: calc(100vh - 4rem);
}

.activeTaskContainer {
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 10px;
  height: 100%;
  flex: 1;
  overflow-y: auto;
}
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  bottom: 10px;
  width: 100%;
  padding: 10px;
  max-width: calc(100% - 2rem);
  margin-top: auto;
}

ul {
  list-style-type: none;
  padding: 0;
}

ul li {
  background-color: #f0f0f0;
  padding: 0.5em;
  border-radius: 4px;
  margin-bottom: 0.5em;
}

ul li::before {
  content: '';
  display: inline-block;
  width: 1em;
  margin-left: -1em;
}

.placeholder {
  font-size: 1.2rem;
  color: #888;
  text-align: center;
}
</style>