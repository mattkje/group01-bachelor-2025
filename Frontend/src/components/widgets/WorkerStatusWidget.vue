<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {fetchOverviewData} from "@/composables/DataFetcher";
import {Zone} from "@/assets/types";


const props = defineProps<{
  zone: Zone;
}>();

const workersToday = ref(0);
const workersPresent = ref(0);
const pendingTasks = ref(0);
const completedTasks = ref(0);

const fetchWorkerStatus = async () => {
  const listOfNumbers: number[] = await fetchOverviewData(props.zone.id);
  workersToday.value = listOfNumbers[0];
  pendingTasks.value = listOfNumbers[1];
  completedTasks.value = listOfNumbers[2];
  workersPresent.value = listOfNumbers[3];
};

onMounted(async () => {
  await fetchWorkerStatus();
})
</script>

<template>
  <div class="overview-widget">
    <h2>Day Status</h2>
    <div class="status-grid">
      <div class="status-group">
        <div class="status-header">
          <img src="@/assets/icons/staff.svg">
          <span class="status-title">Workers</span>
        </div>
        <div class="status-item">
          <span class="status-label">Present:</span>
          <span class="status-value">{{ workersPresent }}</span>
        </div>
        <div class="status-item">
          <span class="status-label">Today:</span>
          <span class="status-value">{{ workersToday }}</span>
        </div>
      </div>
      <div class="status-group">
        <div class="status-header">
          <img src="@/assets/icons/tasks.svg">
          <span class="status-title">Tasks</span>
        </div>
        <div class="status-item">
          <span class="status-label">Pending:</span>
          <span class="status-value">{{ pendingTasks }}</span>
        </div>
        <div class="status-item">
          <span class="status-label">Completed:</span>
          <span class="status-value">{{ completedTasks }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overview-widget {
  margin: 0 !important;
  width: 100%;
  max-height: 35vh;
  border: 1px solid var(--border-1);
  border-radius: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: flex-start !important;
  align-items: flex-start !important;
  font-size: 1.2rem;
  color: var(--text-1);
  padding: 1rem;
  box-sizing: border-box;
}

.overview-widget h2 {
  font-size: 1.2rem;
  align-self: flex-start;
  color: var(--text-1);
  margin-bottom: 1rem;
}

.status-grid {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  width: 100%;
}

.status-group {
  margin-bottom: 1rem;
}

.status-header {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  border-bottom: 1px solid var(--border-1);
}

.status-header img {
  font-size: 1.5rem;
  margin-right: 0.5rem;
}

.status-title {
  font-size: 1rem;
  font-weight: bold;
  color: var(--text-1);
}

.status-item {
  display: flex;
  justify-content: space-between;
  font-size: 1rem;
  width: 100%;
}

.status-label {
  color: var(--text-2);
  font-size: 0.8rem;
}

.status-value {
  color: var(--main-color);
}

.status-group hr {
  width: 100%;
  border: none;
  border-top: 1px solid var(--border-1);
  margin: 0.5rem 0;
}
</style>