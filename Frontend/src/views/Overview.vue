<script setup lang="ts">
import MonteCarloGraph from "@/components/MonteCarloGraph.vue";

import {ref, onMounted} from 'vue';
import {Zone} from "@/assets/types";
import {fetchAllZones} from "@/composables/DataFetcher";
import OverviewTaskSecion from "@/components/tasks/OverviewTaskSecion.vue";

let selectedZoneId = ref<number>(1);
let zones = ref<Zone[]>([]);

const loadAllData = async () => {
  zones.value = await fetchAllZones()
};

onMounted(() => {
  loadAllData();
});
</script>

<template>
  <div class="container-container">
    <div class="overview">
      <div class="header">
        <h1>Dashboard</h1>
        <div class="vertical-separator"></div>
        <div class="zone-selector">
          <select class="zone-selector-dropdown" id="zone-dropdown" v-model="selectedZoneId">
            <option :value="0">All Zones</option>
            <option v-for="zone in zones" :key="zone.id" :value="zone.id">
              {{ zone.name }}
            </option>
          </select>
        </div>
      </div>
      <div class="monte-carlo-graph-container">
        <MonteCarloGraph class="monte-carlo-graph" :zone-id="selectedZoneId" :key="selectedZoneId"/>
      </div>
      <div class="day-status">
        <div class="status-text-box">
          <p>Tasks Remaining:</p>
          <p>5</p>
        </div>
        <div class="status-text-box">
          <p>Tasks In Completed:</p>
          <p>2</p>
        </div>
        <div class="status-text-box">
          <p>Workers Present:</p>
          <p>3</p>
        </div>
      </div>
      <div class="day-status">
        <div class="status-text-box">
          <p>Tasks Overdue:</p>
          <p>0</p>
        </div>
        <div class="status-text-box">
          <p>Workers dead:</p>
          <p>2</p>
        </div>
        <div class="status-text-box">
          <p>Workers unavailable:</p>
          <p>3</p>
        </div>
      </div>
    </div>
    <div class="tasks-container">
      <OverviewTaskSecion :zone-id="selectedZoneId"></OverviewTaskSecion>
    </div>
  </div>
</template>

<style scoped>
.container-container {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 2rem;
  padding: 2rem;
}

.vertical-separator {
  width: 1px;
  height: 30px;
  background-color: var(--border-1);
  margin: 0.6rem 0.8rem;
}

.overview {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.header {
  display: flex;
}

.monte-carlo-graph-container {
  width: 100%;
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  border-radius: 2rem;
  border: 1px solid var(--border-1);
}

.monte-carlo-graph {
  border-radius: 2rem;
}

.day-status {
  height: 20%;
  border: 1px solid var(--border-1);
  flex-direction: row;
  border-radius: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.2rem;
  gap: 2rem;
  color: var(--text-1);
}

.status-text-box {
  margin: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.zone-selector {
  display: flex;
  align-items: center;
}

.zone-selector label {
  font-size: 1rem;
}

.zone-selector select {
  padding: 0.5rem;
  border-radius: 0.5rem;
  font-size: 1rem;
}

.zone-selector-dropdown {
  background-color: transparent;
  color: var(--text-1);
  border: 1px solid transparent;
  border-radius: 0.5rem;
}
.tasks-container {
  width: 100%;
  height: 100%;
  border: 1px solid var(--border-1);
  flex-direction: row;
  border-radius: 2rem;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  font-size: 1.2rem;
  color: var(--text-1);
}


</style>