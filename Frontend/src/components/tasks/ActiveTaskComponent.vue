<script setup lang="ts">
  import { defineProps } from 'vue';

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
    task: Task;
  }

  const props = defineProps<{ activeTask: ActiveTask }>();
  </script>

  <template>
    <div class="task-info-box">
      <div class="task-name">{{ props.activeTask.task.name}}</div>
      <div class="task-details">
        <div class="workers-info">
          <div>{{ props.activeTask.workers.length }} / {{ props.activeTask.task.maxWorkers}} workers</div>
          <div>ETA: {{ props.activeTask.task.maxTime }}</div>
        </div>
        <div class="task-zone">Zone {{ props.activeTask.task.zoneId }}</div>
      </div>
    </div>
  </template>

  <style scoped>
  .task-info-box {
    border: 1px solid #e5e5e5;
    border-radius: 10px;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    width: 200px;
    background-color: #FFF2F2;
    color: #E77474;
    max-height: 120px;
  }

  .task-name {
    font-size: 1.2rem;
    font-weight: bold;
    margin-bottom: 0.5rem;
  }

  .task-details {
    display: flex;
    justify-content: space-between;
    font-size: 0.9rem;
  }

  .workers-info {
    display: flex;
    flex-direction: column;
    font-weight: bold;
  }

  .task-zone {
    align-self: flex-end;
  }
  </style>