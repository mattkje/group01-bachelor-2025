<script setup lang="ts">
import {defineProps, computed} from 'vue';
import TaskDropdown from "@/components/tasks/TaskDropdown.vue";
import {ActiveTask} from "@/assets/types";

const props = defineProps<{ activeTask: ActiveTask; estimate: boolean }>();

const emit = defineEmits(["taskDeleted", "taskUpdated"]);

const handleTaskDeleted = () => {
  emit("taskDeleted");
};

const handleTaskUpdated = (updatedTask: ActiveTask) => {
  emit("taskUpdated", updatedTask);
};

// Computed property to determine the background color
const taskBackgroundColor = computed(() => {
  const {startTime, endTime, mcStartTime, mcEndTime, dueDate} = props.activeTask;

  if (props.estimate) {
    if (dueDate && mcEndTime && new Date(dueDate) < new Date(mcEndTime)) {
      return "var(--main-color-3)"; // Orange for missed deadline
    }

    if (mcStartTime && mcEndTime) {
      return "var(--background-2)";
    } else {
      return "var(--main-color-3)";
    }
  }
  if (startTime && !endTime) {
    return "var(--busy-color-2)";
  } else if (startTime && endTime) {
    return "var(--main-color-3)";
  }
  return "var(--background-2)";
});

const formattedTimes = computed(() => {
  const formatTime = (time: string | null) => {
    if (!time) return "N/A";
    const date = new Date(time);
    return date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
  };

  return {
    mcStartTime: formatTime(props.activeTask.mcStartTime),
    mcEndTime: formatTime(props.activeTask.mcEndTime),
  };
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
        <div
            v-if=" estimate &&props.activeTask.dueDate && props.activeTask.mcEndTime && new Date(props.activeTask.dueDate) < new Date(props.activeTask.mcEndTime)">
          Deadline missed
        </div>
        <div v-else>
          {{ props.activeTask.workers.length }} / {{ props.activeTask.task.maxWorkers }} workers
        </div>
        <div v-if="props.estimate">
          <div v-if="props.activeTask.mcStartTime && props.activeTask.mcEndTime">
            Estimated: {{ formattedTimes.mcStartTime }} - {{ formattedTimes.mcEndTime }},
          </div>
          <div v-else>
            Task Estimated to not complete
          </div>
        </div>
        <div v-else>
          {{
            props.activeTask.dueDate
                ? 'Due: ' + new Date(props.activeTask.dueDate).toLocaleTimeString([], {
              hour: '2-digit',
              minute: '2-digit'
            })
                : 'No deadline'
          }}
        </div>
      </div>
      <div class="task-zone">Zone {{ props.activeTask.task.zoneId }}</div>
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
  max-height: 75px;
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