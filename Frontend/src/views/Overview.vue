<script setup lang="ts">
import MonteCarloGraph from "@/components/MonteCarloGraph.vue";

import {ref, onMounted} from 'vue';
import {Zone} from "@/assets/types";

let selectedZoneId = ref<number>(1);
let zones = ref<Zone[]>([]);

const fetchZones = async () => {
  try {
    const response = await fetch("http://localhost:8080/api/zones");
    zones.value = await response.json();
  } catch (error) {
    console.error("Failed to fetch zones:", error);
  }
};

onMounted(() => {
  fetchZones();
});
</script>

<template>
  <div class="container-container">
    <div class="overview">
      <div class="monte-carlo-graph-container">
        <h2>Monte Carlo Simulation</h2>
        <div class="zone-selector">
          <label for="zone-dropdown">Select Zone:</label>
          <select id="zone-dropdown" v-model="selectedZoneId">
            <option :value="0">All Zones</option>
            <option v-for="zone in zones" :key="zone.id" :value="zone.id">
              {{ zone.name }}
            </option>
          </select>
        </div>
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
      <h2>Tasks</h2>
      <div class="tasks">
        <p>Task 1</p>
        <p>Task 2</p>
        <p>Task 3</p>
        <p>Task 4</p>
        <p>Task 5</p>
      </div>
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

.overview {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.monte-carlo-graph-container {
  width: 100%;
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  border-radius: 2rem;
  border: 1px solid #e5e5e5;
}

.monte-carlo-graph {
  border-radius: 2rem;
}

.day-status {
  height: 20%;
  border: 1px solid #e5e5e5;
  flex-direction: row;
  border-radius: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.2rem;
  gap: 2rem;
  color: #7B7B7B;
}

.status-text-box {
  margin: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.zone-selector {
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.zone-selector label {
  font-size: 1rem;
}

.zone-selector select {
  padding: 0.5rem;
  border-radius: 0.5rem;
  border: 1px solid #dcdcdc;
  font-size: 1rem;
}

.tasks-container {
  width: 100%;
  height: 100%;
  border: 1px solid #e5e5e5;
  flex-direction: row;
  border-radius: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.2rem;
  gap: 2rem;
  color: #7B7B7B;
}


</style>