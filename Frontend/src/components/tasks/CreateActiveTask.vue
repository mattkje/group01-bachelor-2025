<script setup lang="ts">
import {ref, computed, onMounted} from "vue";
import CreateTaskModal from "./CreateTaskModal.vue";
import {Task, Worker} from "@/assets/types";
import {fetchAllTasksForZone, fetchWorkersForZone} from "@/composables/DataFetcher";
import {createActiveTask} from "@/composables/DataUpdater";

const props = defineProps<{
  zoneId: number | null;
}>();

const emit = defineEmits(["activeTaskAdded", "close", "taskCreated"]);

const tasks = ref<Task[]>([]);
const availableWorkers = ref<Worker[]>([]);

const showCreateTaskModal = ref(false);
const selectedTaskId = ref<number | null>(null);

const filteredTasks = computed(() =>
    tasks.value.filter((task) => task.zoneId === props.zoneId)
);

// New fields for the popup
const duedate = ref("");
const starttime = ref("");
const endtime = ref("");
const workers = ref<Worker[]>([]);
const strictstart = ref(false);
const date = ref("");

const addActiveTask = async () => {
  if (selectedTaskId.value !== null) {
    try {
      await createActiveTask(selectedTaskId.value, {
        duedate: duedate.value,
        starttime: starttime.value,
        endtime: endtime.value,
        workers: workers.value,
        strictstart: strictstart.value,
        date: date.value,
      });
      emit("activeTaskAdded");
      emit("close");
    } catch (error) {
      console.error("Failed to add active task:", error);
    }
  }
};

const handleTaskCreated = (task: Task) => {
  tasks.value.push(task);
  loadData();
  showCreateTaskModal.value = false;
};

const loadData = async () => {
  tasks.value = await fetchAllTasksForZone(props.zoneId);
  availableWorkers.value = await fetchWorkersForZone(props.zoneId);
};

onMounted(async () => {
  await loadData();
});
</script>

<template>
  <div class="modal-overlay">
    <div class="modal-content">
      <h2>Select Task</h2>
      <label>
        Available Tasks:
        <select v-model="selectedTaskId">
          <option v-for="task in filteredTasks" :key="task.id" :value="task.id">
            {{ task.name }}
          </option>
        </select>
      </label>
      <label>
        Date:
        <input v-model="date" type="date"/>
      </label>
      <label>
        Deadline:
        <input v-model="duedate" type="datetime-local"/>
      </label>
      <div class="modal-actions">
        <button @click="addActiveTask" :disabled="!selectedTaskId">Add Task</button>
        <button @click="showCreateTaskModal = true">Create New Task</button>
      </div>
    </div>
    <CreateTaskModal
        v-if="showCreateTaskModal"
        :zoneId="props.zoneId"
        @close="showCreateTaskModal = false"
        @taskCreated="handleTaskCreated"
    />
  </div>
</template>

<style scoped>
.modal-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 1.5rem;
  width: 400px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

label {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
</style>