<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { fetchOverviewData } from "@/composables/DataFetcher";
import { Zone } from "@/assets/types";

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

let intervalId: ReturnType<typeof setInterval> | undefined;

onMounted(() => {
  fetchWorkerStatus();
  intervalId = setInterval(fetchWorkerStatus, 5000); // Fetch every 5 seconds
});

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId);
  }
});
</script>

<template>
  <div class="overview-widget">
    <div class="status-grid">
      <div class="status-group">
        <div class="status-header">
          <span class="status-title">Workers</span>
          <img src="@/assets/icons/staff.svg">
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
          <span class="status-title">Tasks</span>
          <img src="@/assets/icons/tasks.svg">
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
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start !important;
  align-items: flex-start !important;
  font-size: 1.2rem;
  color: var(--text-1);
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
  flex-direction: row;
  gap: 1rem;
  width: 100%;
}

.status-group {
  display: flex;
  flex-direction: column;
  width: calc(50% - 0.5rem);
  height: 100%;
  border: 1px solid var(--border-1);
  border-radius: 1rem;
  padding: 1rem;
}

.status-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--border-1);
}

.status-header img {
  width: 20px;
  height: 20px;
  font-size: 1.5rem;
}


.status-title {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-2);
}

.status-item {
  width: 100%;
  flex-direction: row;
  font-size: 0.1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-label {
  color: var(--text-2);
  font-size: 0.8rem;
}

.status-value {
  color: var(--text-2);
  font-size: 1.2rem;
  font-weight: bold;
}

.status-group hr {
  width: 100%;
  border: none;
  border-top: 1px solid var(--border-1);
  margin: 0.5rem 0;
}


</style>