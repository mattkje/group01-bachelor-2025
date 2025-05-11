<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {fetchOverviewData} from "@/composables/DataFetcher";
import {Zone} from "@/assets/types";


const props = defineProps<{
  zone: Zone;
}>();

const workersPresent = ref(0);
const pendingTasks = ref(0);
const completedTasks = ref(0);

const fetchWorkerStatus = async () => {
  const listOfNumbers: number[] = await fetchOverviewData(props.zone.id);
  workersPresent.value = listOfNumbers[0];
  pendingTasks.value = listOfNumbers[1];
  completedTasks.value = listOfNumbers[2];
};

onMounted(async () => {
  await fetchWorkerStatus();
})
</script>

<template>
  <div class="overview-widget">
    <h2>Worker Status</h2>
    <div class="status-grid">
      <hr>
      <div class="status-item">
        <span class="status-label">Workers Present:</span>
        <span class="status-value">{{ workersPresent }}</span>
      </div>
      <hr>
      <div class="status-item">
        <span class="status-label">Pending Tasks:</span>
        <span class="status-value">{{ pendingTasks }}</span>
      </div>
      <hr>
      <div class="status-item">
        <span class="status-label">Completed Tasks:</span>
        <span class="status-value">{{ completedTasks }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overview-widget {
  margin: 0 !important;
  width: 100%;
  height: 100%;
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
  justify-content: space-between;
  width: 100%;
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

.status-grid hr {
  width: 100%;
  border: none;
  border-top: 1px solid var(--border-1);
  margin: 0.5rem 0;
}
</style>