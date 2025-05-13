<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {ActiveTask, License, PickerTask, Task, Worker} from '@/assets/types';
import {fetchActiveTask, fetchSimulationDate, fetchTask, fetchWorkersFromActiveTask} from "@/composables/DataFetcher";

const props = defineProps<{
  activeTask: ActiveTask | null;
  pickerTask: PickerTask | null;
  requiredLicenses: License[];
  qualified: boolean;
  currentDate: string;
}>();

const activeTask = ref<ActiveTask | null>(null);
const pickerTask = ref<PickerTask | null>(null);
const isTaskOverdue = ref(false);

onMounted(async () => {
  if (props.activeTask) {
    activeTask.value = props.activeTask;

    isTaskOverdue.value = activeTask.value.dueDate
        ? activeTask.value.dueDate.toString() < new Date(props.currentDate).toDateString()
        : false;
  } else if (props.pickerTask) {
    pickerTask.value = props.pickerTask;
  }

});
</script>

<template>
  <div class="task-compact">
    <div class="task-details">
      <div v-if="activeTask" :class="['task-name', {'overdue': isTaskOverdue }]">{{
          props.activeTask.task.name
        }}
      </div>
      <div v-else-if="pickerTask" class="task-name" :class="['task-name', { 'overdue': isTaskOverdue } ]">
        {{ props.pickerTask.id }}
      </div>
      <img :draggable="!activeTask && !pickerTask"
           v-if="!qualified"
           src="/src/assets/icons/warning.svg" class="status-icon" alt="Unqualified"/>
      <img v-if="isTaskOverdue" src="/src/assets/icons/overtime.svg" class="status-icon"/>
      <img src="/src/assets/icons/busy.svg" class="status-icon-rotating"
           alt="Error"/>
    </div>
  </div>
</template>

<style scoped>
.task-compact {
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  margin: auto;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.status-icon {
  width: 20px;
  height: 20px;
  margin-right: 0.2rem;
}

.status-icon-rotating {
  width: 20px;
  height: 20px;
  animation: spin 1.5s ease-in-out infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.overdue {
  color: var(--main-color) !important;
}

.task-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-name {
  margin-right: 0.5rem;
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