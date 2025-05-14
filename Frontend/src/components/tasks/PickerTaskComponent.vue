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
  const {startTime, endTime} = props.pickerTask;
  if (startTime && !endTime) {
    return "var(--main-color-3)"; // Yellow
  } else if (startTime && endTime) {
    return "var(--busy-color-2)"; // Green
  }
  return "var(--background-2)";
});

</script>

<template>
  <div class="task-info-box" :style="{ backgroundColor: taskBackgroundColor }">
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
  border: 1px solid var(--border-1);
  border-radius: 0.7rem;
  padding: 0.3rem;
  display: flex;
  flex-direction: column;
  width: 100%;
  color: var(--text-1);
}

.task-header {
  display: flex;
  justify-content: space-between;
}

.task-name {
  font-size: 0.8rem;
  margin-bottom: 0.2rem;
  text-align: left;
}

.task-details {
  display: flex;
  justify-content: space-between;
  font-size: 0.6rem;
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

@media (max-width: 1400px) {
  .task-info-box {
    max-height: 100px;
  }

  .task-name {
    font-size: 0.7rem;
  }

  .task-details {
    font-size: 0.6rem;
  }
}
</style>