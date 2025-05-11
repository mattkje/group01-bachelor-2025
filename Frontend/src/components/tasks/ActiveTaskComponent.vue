<script setup lang="ts">
import {defineProps, computed} from 'vue';
import TaskDropdown from "@/components/tasks/TaskDropdown.vue";
import {ActiveTask} from "@/assets/types";

const props = defineProps<{ activeTask: ActiveTask }>();

const emit = defineEmits(["taskDeleted", "taskUpdated"]);

const handleTaskDeleted = () => {
  emit("taskDeleted");
};

const handleTaskUpdated = (updatedTask: ActiveTask) => {
  emit("taskUpdated", updatedTask);
};

// Computed property to determine the background color
const taskBackgroundColor = computed(() => {
  const {startTime, endTime} = props.activeTask;
  if (startTime && !endTime) {
    return "#FFFF99"; // Yellow
  } else if (startTime && endTime) {
    return "#CCFFCC"; // Green
  }
  return "#FFF2F2"; // Default color
});
</script>

<template>
  <div class="task-info-box" :style="{ backgroundColor: taskBackgroundColor }">
    <div class="task-header">
      <div class="task-name">{{ props.activeTask.task.name }}</div>
      <TaskDropdown
          v-if="!activeTask.endTime"
          @task-deleted="handleTaskDeleted"
          @task-updated="handleTaskUpdated"
          :activeTask="props.activeTask"
      ></TaskDropdown>
    </div>
    <div class="task-details">
      <div class="workers-info">
        <div>{{ props.activeTask.workers.length }} / {{ props.activeTask.task.maxWorkers }} workers</div>
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
  width: 100%;
  max-width: 300px;
  color: #E77474;
  max-height: 140px;
}

.task-header {
  display: flex;
  justify-content: space-between;
}

.task-name {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
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

@media (max-width: 1400px) {
  .task-info-box {
    max-height: 100px;
  }

  .task-name {
    font-size: 1rem;
  }

  .task-details {
    font-size: 0.8rem;
  }
}
</style>