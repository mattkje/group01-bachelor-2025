<script setup lang="ts">
import {ActiveTask, PickerTask} from "@/assets/types";
import {computed, ref} from "vue";

const props = defineProps<{ task: PickerTask | ActiveTask }>();
const emit = defineEmits(["taskUpdated"]);

const updatedTask = ref({ ...props.task });

const isPickerTask = computed(() => "weight" in props.task);

const saveChanges = () => {
  emit("taskUpdated", updatedTask.value);
};
</script>

<template>
  <div class="modal-overlay">
    <div class="modal-content">
      <h2>Edit Task</h2>
      <label v-if="!isPickerTask">
        Due Date:
        <input v-model="updatedTask.dueDate" type="date" />
      </label>
      <label v-if="!isPickerTask">
        Strict Start:
        <input v-model="updatedTask.strictStart" type="checkbox" />
      </label>
      <label v-if="isPickerTask">
        Zone ID:
        <input v-model="updatedTask.zoneId" type="number" />
      </label>
      <label v-if="isPickerTask">
        Date:
        <input v-model="updatedTask.date" type="date" />
      </label>
      <label v-if="isPickerTask">
        Distance:
        <input v-model="updatedTask.distance" type="number" />
      </label>
      <label v-if="isPickerTask">
        Pack Amount:
        <input v-model="updatedTask.packAmount" type="number" />
      </label>
      <label v-if="isPickerTask">
        Lines Amount:
        <input v-model="updatedTask.linesAmount" type="number" />
      </label>
      <label v-if="isPickerTask">
        Weight:
        <input v-model="updatedTask.weight" type="number" />
      </label>
      <label v-if="isPickerTask">
        Volume:
        <input v-model="updatedTask.volume" type="number" />
      </label>
      <label v-if="isPickerTask">
        Average Height:
        <input v-model="updatedTask.avgHeight" type="number" />
      </label>
      <div class="modal-actions">
        <button @click="$emit('close')">Cancel</button>
        <button @click="saveChanges">Save</button>
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

label {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}
</style>