<script setup lang="ts">
      import { ref, computed, onMounted } from "vue";
      import CreateTaskModal from "./CreateTaskModal.vue";
      import { Task, Worker } from "@/assets/types";
      import Multiselect from "vue-multiselect";

      const props = defineProps<{
        zoneId: number | null;
      }>();

      const emit = defineEmits(["activeTaskAdded", "close", "taskCreated"]);

      const fetchTasksByZoneId = async (zoneId: number) => {
        try {
          const response = await fetch(`http://localhost:8080/api/zones/${zoneId}/tasks`);
          return await response.json();
        } catch (error) {
          console.error("Failed to fetch tasks:", error);
          return [];
        }
      };

      const tasks = ref<Task[]>([]);
      const availableWorkers = ref<Worker[]>([]);
      const fetchTasks = async () => {
        if (props.zoneId) {
          tasks.value = await fetchTasksByZoneId(props.zoneId);
        }
      };

      const fetchAvailableWorkers = async () => {
        availableWorkers.value = await fetchWorkersInZone(props.zoneId);
      };

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

      const addActiveTask = () => {
        if (selectedTaskId.value !== null) {
          fetch(`http://localhost:8080/api/active-tasks/${selectedTaskId.value}`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              duedate: duedate.value,
              starttime: starttime.value,
              endtime: endtime.value,
              workers: workers.value,
              strictstart: strictstart.value,
              date: date.value,
            }),
          })
            .then((response) => {
              if (!response.ok) throw new Error("Network response was not ok");
              return response.json();
            })
            .then(() => {
              emit("activeTaskAdded");
              emit("close");
            })
            .catch((error) => {
              console.error("Failed to add active task:", error);
            });
        }
      };

      const handleTaskCreated = (task: Task) => {
        tasks.value.push(task);
        fetchTasks();
        showCreateTaskModal.value = false;
      };

      const fetchWorkersInZone = async (zoneId: number) => {
        try {
          const response = await fetch(`http://localhost:8080/api/zones/${zoneId}/workers`);
          return await response.json();
        } catch (error) {
          console.error("Failed to fetch workers:", error);
          return [];
        }
      };

      onMounted(() => {
        fetchTasks();
        fetchAvailableWorkers();
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
              <input v-model="date" type="date" />
            </label>
            <label>
              Deadline:
              <input v-model="duedate" type="datetime-local" />
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