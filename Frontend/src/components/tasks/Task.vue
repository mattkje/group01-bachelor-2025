<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import {License, Task, Worker} from '@/assets/types';
  import {fetchTask, fetchWorkersForTask} from "@/composables/DataFetcher";

  const props = defineProps<{
    taskId: number;
    name: string;
    requiredLicenses: License[];
    zoneId: number;
  }>();

  const task = ref<Task | null>(null);
  const workers = ref<Worker[]>([]);
  const isTaskOverdue = ref(false);

  onMounted(async () => {
    task.value = await fetchTask(props.taskId)
    workers.value = await fetchWorkersForTask(props.taskId)
  });
  </script>

  <template>
    <div :class="['task-compact', { 'overdue-task-box': isTaskOverdue }]">
      <div class="task-details">
        <div class="task-name">{{ props.name }}</div>
        <div class="task-status">{{ isTaskOverdue ? 'Overdue' : 'On Time' }}</div>
      </div>
    </div>
  </template>

  <style scoped>
  .task-compact {
    position: relative;
    display: flex;
    flex-direction: column;
    background-color: var(--background-backdrop);
    border-radius: 0 10px 10px 0;
    max-height: 30px;
    padding: 0 0.2rem;
    margin-bottom: 0.5rem;
    user-select: none !important;
    -webkit-user-select: none !important;
  }

  .overdue-task-box {
    background-color: var(--main-color-3);
    border: 2px solid var(--main-color);
  }

  .task-details {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .task-name {
    font-size: 0.5rem;
    color: var(--text-1);
    font-weight: bold;
  }

  .task-status {
    font-size: 0.6rem;
    color: var(--main-color);
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
    color: var(--text-1);
  }
  </style>