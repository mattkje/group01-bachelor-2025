<script setup lang="ts">
  import { onMounted, ref, computed } from 'vue';
  import {License, Task, Worker} from '@/assets/types';
  import axios from 'axios';

  const props = defineProps<{
    taskId: number;
    name: string;
    requiredLicenses: License[];
    zoneId: number;
  }>();

  const task = ref<Task | null>(null);
  const workers = ref<Worker[]>([]);
  const isTaskOverdue = ref(false);
  const connectionString = ref<string>('');

  const fetchTaskDetails = async (taskId: number) => {
    try {
      const response = await axios.get(`http://localhost:8080/api/tasks/${taskId}`);
      task.value = response.data;
      isTaskOverdue.value = new Date(task.value.maxTime) < new Date();
    } catch (error) {
      console.error('Failed to fetch task details:', error);
    }
  };

  const fetchWorkersForTask = async (taskId: number) => {
    try {
      const response = await axios.get(`http://localhost:8080/api/tasks/${taskId}/workers`);
      workers.value = response.data;
      createConnectionString();
    } catch (error) {
      console.error('Failed to fetch workers for task:', error);
    }
  };

  const createConnectionString = () => {
    if (task.value) {
      connectionString.value = workers.value.map(worker => `${worker.name} is working on ${task.value!.name}`).join(', ');
    }
  };

  onMounted(() => {
    fetchTaskDetails(props.taskId);
    fetchWorkersForTask(props.taskId);
  });
  </script>

  <template>
    <div :class="['task-compact', { 'overdue-task-box': isTaskOverdue }]">
      <div class="task-details">
        <div class="task-name">{{ props.name }}</div>
        <div class="task-status">{{ isTaskOverdue ? 'Overdue' : 'On Time' }}</div>
      </div>
      <div class="workers-container">
        <div v-for="worker in workers" :key="worker.id" class="worker">
          <div class="worker-name">{{ worker.name }}</div>
        </div>
      </div>
      <div class="connection-string">{{ connectionString }}</div>
    </div>
  </template>

  <style scoped>
  .task-compact {
    position: relative;
    display: flex;
    flex-direction: column;
    background-color: #ececec;
    border-radius: 10px;
    max-height: 40px;
    padding: 0.5rem;
    margin-bottom: 0.5rem;
    user-select: none !important;
    -webkit-user-select: none !important;
  }

  .overdue-task-box {
    background-color: #ffcccc;
    border: 2px solid #ff4b4b;
  }

  .task-details {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .task-name {
    font-size: 0.5rem;
    color: #505050;
    font-weight: bold;
  }

  .task-status {
    font-size: 0.6rem;
    color: #ff4b4b;
  }

  .workers-container {
    display: flex;
    flex-wrap: wrap;
    margin-top: 0.5rem;
  }

  .worker {
    display: flex;
    align-items: center;
    margin-right: 0.5rem;
  }

  .worker-image {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    margin-right: 0.5rem;
  }

  .worker-name {
    font-size: 0.8rem;
    font-weight: bold;
  }

  .connection-string {
    margin-top: 1rem;
    font-size: 0.8rem;
    color: #333;
  }
  </style>