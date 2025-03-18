<script setup lang="ts">
import {ref, computed, onMounted} from 'vue';

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

interface Worker {
  id: number;
  name: string;
}

interface ActiveTask {
  id: number;
  workers: Worker[];
}

interface Zone {
  id: number;
  name: string;
}

const props = defineProps<{
  zone: Zone;
}>();

const tasks = ref<Task[]>([]);
const activeTasks = ref<ActiveTask[]>([]);
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

const fetchActiveTasks = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/active-tasks');
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    activeTasks.value = await response.json();
  } catch (error) {
    console.error('Failed to fetch active tasks:', error);
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
  fetchActiveTasks();
  fetchZones();
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

const sortTable = (key: string) => {
  tasks.value.sort((a, b) => {
    const aValue = a[key as keyof Task];
    const bValue = b[key as keyof Task];
    if (aValue < bValue) return -1;
    if (aValue > bValue) return 1;
    return 0;
  });
};
</script>


<template>
  <div class="content">
    <div class="main-content">
      <h1>Active Tasks</h1>
      <table class="task-table">
        <thead>
        <tr>
          <th @click="sortTable('name')">Task Name</th>
          <th @click="sortTable('requiredLicense')">Qualifications</th>
          <th @click="sortTable('estimatedTime')">Duration</th>
          <th @click="sortTable('workers')">Workers</th>
          <th @click="sortTable('workerSlots')">Worker slots</th>
          <th @click="sortTable('description')">Info</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="task in paginatedTasks" :key="task.id">
          <td>{{ task.name }}</td>
          <td>
            <ul>
              <li v-for="license in task.requiredLicense" :key="license.name">{{ license.name }}</li>
            </ul>
          </td>
          <td>{{ (task.maxTime + task.minTime) / 2 }} minutes</td>
          <td>
            <ul>
              <li v-for="worker in getWorkersForTask(task.id)" :key="worker.id">
                <router-link :to="`/worker?id=${worker.id}`">{{ worker.name }}</router-link>
              </li>
            </ul>
          </td>
          <td>{{ task.minWorkers }} - {{ task.maxWorkers }}</td>
          <td>
            <button @click="showModal = true">i</button>
          </td>
        </tr>
        </tbody>
      </table>
      <div class="pagination">
        <button @click="prevPage" :disabled="currentPage === 1">Previous</button>
        <span>Page {{ currentPage }} of {{ totalPages }}</span>
        <button @click="nextPage" :disabled="currentPage === totalPages">Next</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content {
  display: flex;
  margin: 0;
  height: calc(100% - 4rem);
}

.main-content {
  padding: 1rem;
  flex: 1;
  overflow-y: auto;
}

.filter-menu {
  position: sticky;
  width: 280px;
  height: 100%;
  padding: 1rem;
  border-left: 1px solid #ccc;
  background-color: #f9f9f9;
}

.task-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin-bottom: 1rem;
  border-radius: 8px;
  overflow: hidden;
  border: #e1e1e1 1px solid;
}

.task-table th, .task-table td {
  padding: 0.8vh;
  text-align: left;
}

.task-table th {
  background-color: #f4f4f4;
  font-weight: bold;
  cursor: pointer;
}

.task-table tbody tr {
  transition: background-color 0.3s;
}

.task-table tbody tr:hover {
  background-color: #f9f9f9;
}

.task-table tbody tr:nth-child(even) {
  background-color: #f8f8f8;
}

.task-table button {
  padding: 0.25rem 0.5rem;
  background-color: #E77474;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.task-table button:hover {
  background-color: #9d4d4d;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-content {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  width: 500px;
  max-width: 100%;
}

.close {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 1.5rem;
  cursor: pointer;
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
</style>