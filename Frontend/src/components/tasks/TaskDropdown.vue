<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref} from "vue";
import {PickerTask, ActiveTask} from "@/assets/types";
import EditActiveTaskModal from "@/components/tasks/EditActiveTaskModal.vue";
import {deleteActiveTask, deletePickerTask, updateActiveTask, updatePickerTask} from "@/services/DataUpdater";

const props = defineProps<{
  pickerTask?: PickerTask;
  activeTask?: ActiveTask;
}>();

const emit = defineEmits(["taskDeleted", "taskUpdated"]);

// Ensure at least one of the props is provided
if (!props.pickerTask && !props.activeTask) {
  throw new Error("Either 'pickerTask' or 'activeTask' must be provided.");
}

const isDropdownOpen = ref(false);
const isEditModalOpen = ref(false);

const openEditModal = (event: MouseEvent) => {
  event.stopPropagation();
  isEditModalOpen.value = true;
  toggleDropdown();
};

const closeEditModal = () => {
  isEditModalOpen.value = false;
};

const handleTaskUpdated = (updatedTask: PickerTask | ActiveTask) => {
  updateTask(updatedTask);
  closeEditModal();
}

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value;
};

const closeDropdown = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  if (!target.closest('.dropdown-container')) {
    isDropdownOpen.value = false;
  }
};

const deleteTask = async () => {
  if (props.activeTask) {
    await deleteActiveTask(props.activeTask.id);
    emit("taskDeleted");
  } else if (props.pickerTask) {
    await deletePickerTask(props.pickerTask.id);
    emit("taskDeleted");
  }
};

const updateTask = async (updatedTask: PickerTask | ActiveTask) => {
  console.log(updatedTask);
  if (props.activeTask) {
    await updateActiveTask(updatedTask.id);
    emit("taskUpdated");
  } else if (props.pickerTask && 'zoneId' in updatedTask) {
    await updatePickerTask(props.pickerTask.id, updatedTask.zoneId);
    emit("taskUpdated");
  }
};

const promptDeleteConfirmation = () => {
  const confirmation = confirm("Are you sure you want to delete this task?");
  if (confirmation) {
    deleteTask();
  }
};

onMounted(() => {
  document.addEventListener('click', closeDropdown);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', closeDropdown);
});
</script>

<template>
  <div>
    <div class="dropdown-container">
      <button class="dropdown-button" @click="toggleDropdown">⋮</button>
      <div
          v-if="isDropdownOpen"
          class="dropdown-menu"
          :class="{ 'dropdown-menu-visible': isDropdownOpen }"
      >
        <div class="dropdown-item" @click="openEditModal($event)">Edit</div>
        <div class="dropdown-item" @click="promptDeleteConfirmation">Delete</div>
      </div>
    </div>
    <EditActiveTaskModal
        v-if="isEditModalOpen"
        :task="props.pickerTask || props.activeTask"
        @close="closeEditModal"
        @task-updated="handleTaskUpdated"
    />
  </div>
</template>

<style scoped>

.dropdown-container {
  position: relative;
}

.dropdown-button {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
}

.dropdown-menu {
  position: absolute;
  top: 20px;
  left: 0;
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  z-index: 10;
  opacity: 0;
  transform: translateY(-10px);
  transition: opacity 0.3s ease, transform 0.3s ease;
  pointer-events: none;
}

.dropdown-menu-visible {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.dropdown-item {
  padding: 0.5rem 1rem;
  cursor: pointer;
}

.dropdown-item:hover {
  background-color: #f0f0f0;
}
</style>