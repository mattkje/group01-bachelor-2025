<script setup lang="ts">
import {defineProps, onBeforeUnmount, onMounted, ref} from 'vue';
import { PickerTask } from '@/assets/types';
import TaskDropdown from "@/components/tasks/TaskDropdown.vue";

const props = defineProps<{ pickerTask: PickerTask }>();

const emit = defineEmits(["taskDeleted", "taskUpdated"]);

const handleTaskDeleted = () => {
  emit("taskDeleted");
};

const handleTaskUpdated = () => {
  emit("taskUpdated");
};
</script>

<template>
  <div class="task-info-box">
    <div class="task-header">
    <div class="task-name">{{ props.pickerTask.id}}</div>
      <TaskDropdown :pickerTask="props.pickerTask" @task-deleted="handleTaskDeleted" @task-updated="handleTaskUpdated"></TaskDropdown>
    </div>
    <div class="task-details">
      <div class="workers-info">
        <div>{{ Math.floor(props.pickerTask.weight / 1000) }} kg</div>
        <div>ETA: {{ props.pickerTask.time }}</div>
      </div>
      <div class="task-zone">Zone {{ props.pickerTask.zoneId }}</div>
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
  position: relative;
}

.task-name {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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