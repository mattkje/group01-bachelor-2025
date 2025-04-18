<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref} from "vue";
import { PickerTask, ActiveTask } from "@/assets/types";

const props = defineProps<{
  pickerTask?: PickerTask;
  activeTask?: ActiveTask;
}>();

const emit = defineEmits(["taskDeleted"]);

// Ensure at least one of the props is provided
if (!props.pickerTask && !props.activeTask) {
  throw new Error("Either 'pickerTask' or 'activeTask' must be provided.");
}

const isDropdownOpen = ref(false);

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
    try {
      const response = await fetch(`http://localhost:8080/api/active-tasks/${props.activeTask.id}`, {
        method: 'DELETE',
      });
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      emit("taskDeleted");
    } catch (error) {
      console.error('Failed to delete task:', error);
    }
  } else if (props.pickerTask) {
    try {
      const response = await fetch(`http://localhost:8080/api/picker-tasks/${props.pickerTask.id}`, {
        method: 'DELETE',
      });
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      emit("taskDeleted");
    } catch (error) {
      console.error('Failed to delete task:', error);
    }
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
  <div class="dropdown-container">
    <button class="dropdown-button" @click="toggleDropdown">⋮</button>
    <div
        v-if="isDropdownOpen"
        class="dropdown-menu"
        :class="{ 'dropdown-menu-visible': isDropdownOpen }"
    >
      <div class="dropdown-item">Edit</div>
      <div class="dropdown-item" @click="deleteTask">Delete</div>
    </div>
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