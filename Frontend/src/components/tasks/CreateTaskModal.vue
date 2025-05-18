<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import Multiselect from "vue-multiselect";
import "vue-multiselect/dist/vue-multiselect.min.css";
import {fetchLicenses} from "@/services/DataFetcher";
import {createTask} from "@/services/DataUpdater";
import {License, Task} from "@/assets/types";


const emit = defineEmits(["taskCreated"]);

const props = defineProps<{
  zoneId: number | null;
}>();

const newTask = reactive<Task>({
  id: null,
  zoneId: null,
  name: "",
  description: "",
  maxTime: 0,
  minTime: 0,
  maxWorkers: 0,
  minWorkers: 0,
  requiredLicense: [],
});

const handleCreateTask = async () => {
  try {
    await createTask(props.zoneId!, newTask);
    emit("taskCreated", newTask);
  } catch (error) {
    console.error("Failed to create task:", error);
  }
};

const availableLicenses = ref<License[]>([]);

onMounted(async () => {
  availableLicenses.value = await fetchLicenses();
});
</script>

<template>
  <div class="modal-overlay">
    <div class="modal-content">
      <h2>Create Task</h2>
      <label>
        Task Name:
        <input v-model="newTask.name" type="text"/>
      </label>
      <label>
        Description:
        <input v-model="newTask.description" type="text"/>
      </label>
      <label>
        Max Time:
        <input v-model="newTask.maxTime" type="number"/>
      </label>
      <label>
        Min Time:
        <input v-model="newTask.minTime" type="number"/>
      </label>
      <label>
        Max Workers:
        <input v-model="newTask.maxWorkers" type="number"/>
      </label>
      <label>
        Min Workers:
        <input v-model="newTask.minWorkers" type="number"/>
      </label>
      <label>
        Required Licenses:
        <Multiselect
            v-model="newTask.requiredLicense"
            :options="availableLicenses"
            :multiple="true"
            :close-on-select="false"
            label="name"
            track-by="id"
            placeholder="Select required licenses"
        />
      </label>
      <div class="modal-actions">
        <button @click="$emit('close')">Cancel</button>
        <button @click="handleCreateTask">Create</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  width: 400px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

::v-deep(.multiselect__content) {
  background-color: #E77474;
}

::v-deep(.multiselect__option) {
  background-color: #ffffff;
  color: #000000;
}

::v-deep(.multiselect__tag) {
  background-color: #E77474;
}

label {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

textarea {
  width: 100%;
  resize: none;
}
</style>