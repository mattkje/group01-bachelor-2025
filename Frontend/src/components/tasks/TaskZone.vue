<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import {ActiveTask, PickerTask} from '@/assets/types';
import {fetchSimulationDate} from "@/composables/DataFetcher";

const props = defineProps<{
  zoneId: number;
  title: string;
  tasks: ActiveTask[] | null;
  pickerTasks: PickerTask[] | null;
}>();

const emit = defineEmits(['add-task']);

const onAddTask = () => {
  emit('add-task', props.zoneId);
};

const collapsed = ref(false);
const currentDate = ref('');
const activeTasks = ref<ActiveTask[]>(props.tasks ? [...props.tasks] : []);
const pickerTasks = ref<PickerTask[]>(props.pickerTasks ? [...props.pickerTasks] : []);

const fetchCurrentDateFromBackend = async () => {
  return await fetchSimulationDate();
};


const activeTasksToday = computed(() =>
    activeTasks.value.filter(task => task.date.toString() === currentDate.value.toString())
);

const pickerTasksToday = computed(() =>
    pickerTasks.value.filter(task => task.date.toString() === currentDate.value.toString())
);

const toggleCollapse = () => {
  collapsed.value = !collapsed.value;
};

// Computed property to sort tasks by status
const sortedTasks = computed(() => {
  return [...activeTasksToday.value].sort((a, b) => {
    const getStatusPriority = (task: ActiveTask) => {
      if (task.startTime && !task.endTime) return 1;
      if (!task.startTime && !task.endTime) return 2;
      if (task.endTime) return 3;
      return 4;
    };
    return getStatusPriority(a) - getStatusPriority(b);
  });
});

// Sort picker tasks by status
const sortedPickerTasks = computed(() => {
  return [...pickerTasksToday.value].sort((a, b) => {
    const getStatusPriority = (task: PickerTask) => {
      if (task.startTime && !task.endTime) return 1;
      if (!task.startTime && !task.endTime) return 2;
      if (task.endTime) return 3;
      return 4;
    };
    return getStatusPriority(a) - getStatusPriority(b);
  });
});

onMounted( () => {
  fetchCurrentDateFromBackend()
    .then(date => {
      currentDate.value = date;
    })
    .catch(error => {
      console.error('Error fetching current date:', error);
    });
});
</script>

<template>
  <div class="rounded-square">
    <div class="title-bar">
      <div class="top-bar">
        <p>{{ title }}</p>
        <router-link v-if="tasks" class="add-task-button" :to="`/zones/${props.zoneId}/tasks`">Manage</router-link>
      </div>
      <div class="task-summary">
        <p v-if="activeTasksToday.length > 0">Tasks: {{ activeTasksToday.length }}</p>
        <p v-else-if="pickerTasksToday.length > 0">Picker Tasks: {{ pickerTasksToday.length }}</p>
        <button class="collapsable-button" @click="toggleCollapse">
          {{ collapsed ? '▲' : '▼' }}
        </button>
      </div>
    </div>

    <div v-show="!collapsed" class="vertical-task-box">
      <div v-if="sortedTasks.length > 0" class="vertical-task-box">
        <div
            v-for="(task, index) in sortedTasks"
            :key="index"
            class="task-item"
            :class="{ 'completed': task.endTime, 'doing': task.startTime && !task.endTime }"
        >
          <div class="task-header">
            <p class="task-name">{{ task.task.name }}</p>
            <p class="task-status">
              Status: {{ task.endTime ? 'Completed' : (task.startTime ? 'Doing' : 'Pending') }}
            </p>
          </div>
          <div class="task-footer">
            <p class="task-due">Due: {{ new Date(task.dueDate).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) }}</p>
          </div>
        </div>
      </div>
      <div v-else-if="sortedPickerTasks.length > 0" class="vertical-task-box">
        <div
            v-for="(task, index) in sortedPickerTasks"
            :key="index"
            class="task-item"
            :class="{ 'completed': task.endTime, 'doing': task.startTime && !task.endTime }"
        >
          <div class="task-header">
            <p class="task-name">{{ task.id }}</p>
            <p class="task-status">
              Status: {{ task.endTime ? 'Completed' : (task.startTime ? 'Doing' : 'Pending') }}
            </p>
          </div>
          <div class="task-footer">
            <p class="task-due">Due: {{ new Date(task.date).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) }}</p>
          </div>
        </div>
      </div>
      <div v-else class="vertical-box" style="text-align: center; margin-top: 1rem; opacity: 0.5">
        <img
            src="/src/assets/icons/check.svg"
            alt="Check"
            style="margin: 1rem auto 10px;width: 50px; height: 50px;"
        />
        <p style="text-align: center; margin-top: 1rem;">No tasks</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
    .rounded-square {
      width: 250px;
      height: min-content;
      border: 1px solid #e5e5e5;
      border-radius: 15px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .title-bar {
      display: flex;
      flex-direction: column;
      background-color: #f5f5f5;
      padding: 1rem;
      font-size: 1.2rem;
      font-weight: bold;
      color: #7b7b7b;
      margin-bottom: 0.5rem;
      border-bottom: 1px solid #e5e5e5;
    }

    .task-summary {
      color: #7b7b7b;
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      align-items: flex-start;
      font-size: 0.8rem;

    }

    .vertical-task-box {
      flex: 1;
      display: flex;
      flex-direction: column;
      transition: max-height 0.3s ease, opacity 0.3s ease;
      max-height: 500px;
      overflow: auto;
    }

    .vertical-task-box[style*="display: none"] {
      max-height: 0;
      opacity: 0;
    }

    .task-item {
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 0.5rem;
      border: 1px solid #e5e5e5;
      border-radius: 9px;
      margin: 0 0.4rem 0.4rem 0.4rem;
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
      margin-top: 0.5rem;
      text-align: right;
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
      margin: 1rem 0;
      background: none;
      color: #7b7b7b;
      font-size: 1rem;
      border: none;
      border-radius: 5px;
      cursor: pointer;
    }

    .add-task-button:hover {
      color: #a4a4a4;
    }

    .collapsable-button {
      background: none;
      border: none;
      cursor: pointer;
      font-size: 1.2rem;
      color: #7b7b7b;
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
    </style>