<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {useRoute} from 'vue-router';
import {Zone, Task, ActiveTask} from '@/assets/types';
import CreateActiveTask from "@/components/tasks/CreateActiveTask.vue";
import {
  fetchAllActiveTasksForZone,
  fetchAllTasksForZone,
  fetchSimulationDate, fetchZone
} from "@/services/DataFetcher";
import {deleteActiveTask} from "@/services/DataUpdater";

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
const currentDate = ref('');

const loadAllData = async () => {
  currentDate.value = await fetchSimulationDate();
  tasks.value = await fetchAllTasksForZone(parseInt(route.params.id, 10));
  activeTasks.value = await fetchAllActiveTasksForZone(parseInt(route.params.id, 10));
  zone.value = await fetchZone(parseInt(route.params.id, 10));
};

const deleteActiveTaskMethod = async (id: number) => {
  try {
    await deleteActiveTask(id);
    await loadAllData();
  } catch (error) {
    console.error('Error deleting active task:', error);
  }
};

const handleActiveTaskAdded = () => {
  loadAllData();
  closeCreateActiveTask();
};

onMounted(() => {
  loadAllData()
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
              v-for="activeTask in activeTasks.filter(task => !task.endTime && new Date(task.date) >= new Date(currentDate))"
              :key="activeTask.id"
              class="task-item"
          >
            <div class="task-header">
              <div>
                <p class="task-name">{{ activeTask.task.name }}</p>
                <p class="task-status">Status: {{ activeTask.startTime ? "In Progress" : "Pending" }}</p>
                <p class="task-footer">Due: {{ new Date(activeTask.date).toLocaleDateString() }}</p>
              </div>
              <button class="icon-button" @click="deleteActiveTaskMethod(activeTask.id)">
                <img src="@/assets/icons/trash.svg">
              </button>
            </div>
          </div>
          <div class="task-item">
            <div
                class="add-task-button"
                :class="{ rotated: showCreateActiveTask }"
                @click="showCreateActiveTask = !showCreateActiveTask"
            >
              <img src="@/assets/icons/plus.svg" class="plus-icon" alt="Add Task">
            </div>
            <transition name="expand">
              <div v-if="showCreateActiveTask" class="expanded-task-form">
                <CreateActiveTask
                    :zoneId="zone?.id"
                    @close="showCreateActiveTask = false"
                    @activeTaskAdded="handleActiveTaskAdded"
                />
              </div>
            </transition>
          </div>
        </div>
      </div>
      <div class="active-task-list">
        <div class="task-summary">
          <p>Overdue Tasks</p>
        </div>
        <div class="vertical-task-box">
          <div
              v-if="activeTasks"
              v-for="activeTask in activeTasks.filter(task => !task.endTime && new Date(task.date) < new Date(currentDate))"
              :key="activeTask.id"
              class="task-item unfinished"
          >
            <div class="task-header">
              <div>
                <p class="task-name">{{ activeTask.task.name }}</p>
                <p class="task-status">Status: {{ activeTask.startTime ? "In Progress" : "Pending" }}</p>
                <p class="task-footer">Due: {{ new Date(activeTask.date).toLocaleDateString() }}</p>
              </div>
              <button class="icon-button" @click="deleteActiveTaskMethod(activeTask.id)">
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
  color: var(--text-1);
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
  max-height: calc(100vh - 20rem);
}

.task-item {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0.5rem;
  border: 1px solid var(--border-1);
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
  color: var(--text-2);
}

.task-footer {
  font-size: 0.6rem;
  color: var(--text-2);
}

.task-item.doing {
  background-color: #fff3cd;
  color: #181818;
}

.task-item.completed {
  background-color: #d4edda;
  color: #181818;
}

.plus-icon {
  width: 20px;
  height: 20px;
  transition: transform 0.3s ease;
}

.add-task-button {
  margin: 0;
  background: none;
  color: var(--text-1);
  text-align: center;
  font-size: 2rem;
  font-weight: bold;
  border: none;
  cursor: pointer;
  user-select: none;
  transition: transform 0.3s ease;
}

.add-task-button.rotated {
  transform: translateX(calc(-50% + 2rem));
}

.add-task-button.rotated img {
  transform: rotate(45deg);
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

.expand-enter-active, .expand-leave-active {
  transition: max-height 0.3s ease, opacity 0.3s ease;
}

.expand-enter-from, .expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.expand-enter-to, .expand-leave-from {
  max-height: 500px; /* Adjust based on the content's height */
  opacity: 1;
}
</style>