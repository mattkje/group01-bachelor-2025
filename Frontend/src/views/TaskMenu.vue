<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Zone, Task, ActiveTask } from '@/assets/types';
import axios from 'axios';
import CreateActiveTask from "@/components/tasks/CreateActiveTask.vue";

const showCreateActiveTask = ref(false);

const openCreateActiveTask = () => {
  showCreateActiveTask.value = true;
};

const closeCreateActiveTask = () => {
  showCreateActiveTask.value = false;
};

const emit = defineEmits(['add-task']);
const route = useRoute();

const zone = ref<Zone | null>(null);
const tasks = ref<Task[] | null>(null);
const activeTasks = ref<ActiveTask[] | null>(null);
const currentDate = ref(new Date());

const fetchCurrentDateFromBackend = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/simulation/currentDate');
    currentDate.value = new Date(response.data);
  } catch (error) {
    console.error('Error fetching current date:', error);
    throw error;
  }
};

const fetchTasks = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/api/zones/${route.params.id}/tasks`);
    tasks.value = response.data;
  } catch (error) {
    console.error('Failed to fetch tasks:', error);
  }
};

const fetchActiveTasks = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/api/zones/${route.params.id}/active-tasks`);
    activeTasks.value = response.data;
  } catch (error) {
    console.error('Failed to fetch tasks:', error);
  }
};

const fetchThisZone = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/api/zones/${route.params.id}`);
    zone.value = response.data;
  } catch (error) {
    console.error('Failed to fetch zone:', error);
  }
};

const deleteActiveTask = async (activeTaskId: number) => {
  try {
    await axios.delete(`http://localhost:8080/api/active-tasks/${activeTaskId}`);
    fetchActiveTasks();
  } catch (error) {
    console.error('Failed to delete active task:', error);
  }
};

const handleActiveTaskAdded = () => {
  fetchActiveTasks();
  closeCreateActiveTask();
};

onMounted(() => {
  fetchThisZone();
  fetchTasks();
  fetchActiveTasks();
  fetchCurrentDateFromBackend()
});
</script>

<template>
  <div class="container">
    <h1>{{ zone?.name || 'Loading...' }}</h1>
    <div class="task-tabs">
      <div class="active-task-list">
        <div class="task-summary">
          <p>Active Tasks</p>
        </div>
        <div class="vertical-task-box">
          <div
            v-if="activeTasks"
            v-for="activeTask in activeTasks.filter(task => !task.endTime && new Date(task.date) >= currentDate)"
            :key="activeTask.id"
            class="task-item"
          >
            <div class="task-header">
              <div>
                <p class="task-name">{{ activeTask.task.name }}</p>
                <p class="task-status">Status: {{ activeTask.startTime ? "In Progress" : "Pending" }}</p>
                <p class="task-footer">Due: {{ new Date(activeTask.date).toLocaleDateString() }}</p>
              </div>
              <button class="icon-button" @click="deleteActiveTask(activeTask.id)">
                <img src="@/assets/icons/trash.svg">
              </button>
            </div>
          </div>
          <div class="task-item" @click="openCreateActiveTask">
            <div class="add-task-button">
              +
            </div>
          </div>
          <CreateActiveTask
              v-if="showCreateActiveTask"
              :zoneId="zone?.id"
              @close="closeCreateActiveTask"
              @activeTaskAdded="handleActiveTaskAdded"
          />
        </div>
      </div>
      <div class="active-task-list">
        <div class="task-summary">
          <p>Overdue Tasks</p>
        </div>
        <div class="vertical-task-box">
          <div
              v-if="activeTasks"
              v-for="activeTask in activeTasks.filter(task => !task.endTime && new Date(task.date) < currentDate)"
              :key="activeTask.id"
              class="task-item unfinished"
          >
            <div class="task-header">
              <div>
                <p class="task-name">{{ activeTask.task.name }}</p>
                <p class="task-status">Status: {{ activeTask.startTime ? "In Progress" : "Pending" }}</p>
                <p class="task-footer">Due: {{ new Date(activeTask.date).toLocaleDateString() }}</p>
              </div>
              <button class="icon-button" @click="deleteActiveTask(activeTask.id)">
                <img src="@/assets/icons/trash.svg">
              </button>
            </div>
          </div>
          <div
              v-if="activeTasks && activeTasks.filter(task => !task.endTime && new Date(task.date) < currentDate).length === 0">
            <h2>No unfinished tasks</h2>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 100%;
  display: flex;
  flex-direction: column;
  padding: 1rem;
}

.task-tabs {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  gap: 3rem;
  margin-top: 1rem;
}

.task-summary {
  color: #7b7b7b;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
  font-size: 1rem;

}

.vertical-task-box {
  width: 500px;
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: max-height 0.3s ease, opacity 0.3s ease;
  overflow: auto;
}

.task-item {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0.5rem;
  border: 1px solid #e5e5e5;
  border-radius: 9px;
  margin: 0 0 0.4rem 0;
  font-size: 0.8rem;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-name {
  font-weight: bold;
  font-size: 0.8rem;
}

.task-status {
  font-size: 0.6rem;
  color: #505050;
}

.task-footer {
  font-size: 0.6rem;
  color: #505050;;
}

.task-item.doing {
  background-color: #fff3cd;
  color: #181818;
}

.task-item.completed {
  background-color: #d4edda;
  color: #181818;
}

.add-task-button {
  margin: 0;
  background: none;
  color: #7b7b7b;
  text-align: center;
  font-size: 2rem;
  font-weight: bold;
  border: none;
  cursor: pointer;
  user-select: none;
}

.add-task-button:hover {
  color: #a4a4a4;
}

.icon-button {
  background: none;
  border: none;
  cursor: pointer;
}

.icon-button img {
  width: 20px;
  height: 20px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-bar p {
  margin: 0;
  font-size: 1.2rem;
  font-weight: bold;
}

.task-item.unfinished {
  background-color: #f8d7da;
  color: #721c24;
}
</style>