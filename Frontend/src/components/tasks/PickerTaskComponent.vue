<script setup lang="ts">
import { PickerTask } from '@/assets/types';
import TaskDropdown from "@/components/tasks/TaskDropdown.vue";
import { computed } from "vue";

const props = defineProps<{ pickerTask: PickerTask }>();

const emit = defineEmits(["taskDeleted", "taskUpdated"]);

const handleTaskDeleted = () => {
  emit("taskDeleted");
};

const handleTaskUpdated = () => {
  emit("taskUpdated");
};

const taskBackgroundColor = computed(() => {
  const { startTime, endTime } = props.pickerTask;
  if (startTime && !endTime) {
    return "#FFFF99";
  } else if (startTime && endTime) {
    return "#D4EDD9";
  }
  return "#FFF2F2";
});

const taskTextColor = computed(() => {
  const { startTime, endTime } = props.pickerTask;
  if (startTime && !endTime) {
    return "#B3B36B"; // Slightly darker yellow
  } else if (startTime && endTime) {
    return  "var(--text-1)"; // Slightly darker green
  }
  return "#E77474"; // Slightly darker red
});
</script>

<template>
  <div class="task-info-box" :style="{ backgroundColor: taskBackgroundColor, color: taskTextColor }">
    <div class="task-header">
      <div class="task-name">{{ props.pickerTask.id }}</div>
      <TaskDropdown v-if="!pickerTask.endTime" :pickerTask="props.pickerTask" @task-deleted="handleTaskDeleted" @task-updated="handleTaskUpdated"></TaskDropdown>
    </div>
    <div class="task-details">
      <div class="workers-info">
        <div>{{ Math.floor(props.pickerTask.linesAmount) }} items</div>
        <div v-if="!props.pickerTask.endTime">ETA: {{ Math.floor(props.pickerTask.time / 60) }} min</div>
        <div v-else>{{ Math.floor(props.pickerTask.time / 60) }} min</div>
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
  width: 100%;
  max-width: 300px;
  max-height: 140px;
  position: relative;
}

.task-name {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
  text-align: left;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-align: left;
}

.task-details {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  text-align: left;
}

.workers-info {
  display: flex;
  flex-direction: column;
  font-weight: bold;
  text-align: left;
}

.task-zone {
  align-self: flex-end;
}
</style>