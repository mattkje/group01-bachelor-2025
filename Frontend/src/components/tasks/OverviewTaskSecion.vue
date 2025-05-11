<script setup lang="ts">
import { ref } from 'vue';
import ActiveTaskComponent from "@/components/tasks/ActiveTaskComponent.vue";
import {ActiveTask, License, PickerTask, Worker, Zone} from '@/assets/types';

const selectedTab = ref('Completed');
const selectedOption = ref('best case');
const displayEstimations = ref(false);

const tabs = ['Completed', 'Current', 'Overdue'];
const dropdownOptions = ['best case', 'average case', 'worst case'];

const props = defineProps<{
  zoneId: number;
}>();


</script>

<template>
  <div class="container">
    <div class="toolbar">
      <!-- Tabs on the left -->
      <div class="tabs">
        <button
            v-for="tab in tabs"
            :key="tab"
            :class="{ active: selectedTab === tab }"
            @click="selectedTab = tab"
        >
          {{ tab }}
        </button>
      </div>

      <!-- Dropdown and switch on the right -->
      <div class="controls">
        <select v-model="selectedOption">
          <option v-for="option in dropdownOptions" :key="option" :value="option">
            {{ option }}
          </option>
        </select>

        <div class="switch">
          <label>
            <input type="checkbox" v-model="displayEstimations" />
            <span class="text-span">Display estimations</span>
          </label>
        </div>
      </div>
    </div>
    <hr />
    <div class="content">
      <div v-if="selectedTab === 'Completed'">
        <p>Completed tasks content goes here.</p>
      </div>
      <div v-else-if="selectedTab === 'Current'">
        <ActiveTaskComponent active-task="active" />

      </div>
      <div v-else-if="selectedTab === 'Overdue'">
        <p>Overdue tasks content goes here.</p>
      </div>
    </div>
  </div>

</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
}

.content {
  margin-top: 10px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #ddd;
}

.tabs button {
  margin-right: 10px;
  padding: 5px 10px;
  border: none;
  background: none;
  cursor: pointer;
}

.tabs button.active {
  font-weight: bold;
  border-bottom: 2px solid #007bff;
}

.controls {
  display: flex;
  align-items: center;
  gap: 15px;
}

.switch label {
  display: flex;
  align-items: center;
  gap: 5px;
}

hr {
  margin: 0;
  border: none;
  border-top: 1px solid #ddd;
}

.text-span {
  font-size: 0.8rem;
  color: var(--text-1);
}
</style>