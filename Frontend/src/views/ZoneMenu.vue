<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {Zone} from "@/assets/types";
import {useRoute} from "vue-router";

import ZoneTasks from "@/components/zones/ZoneTasks.vue";
import MonteCarloGraph from "@/components/MonteCarloGraph.vue";


let currentZone = ref<Zone | null>(null);
const route = useRoute();

const fetchZone = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${route.params.id}`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const data: Zone = await response.json();
    currentZone.value = data;
  } catch (error) {
    console.error('Failed to fetch zone:', error);
  }
};

onMounted(() => {
  fetchZone();
});
</script>

<template>
  <div class="container-container">
  <div class="container" v-if="currentZone">
    <div class="zone-info">
      <div class="zone-menu">
        <h1>Management Dashboard</h1>
        <h2>Zone: {{ currentZone.name }}</h2>
        <p>Number of Workers: {{ currentZone.workers.length }}</p>
        <p>Tasks: {{ currentZone.tasks.length }}</p>
        <button @click="$emit('runSimulation', currentZone.id)">Run Simulation</button>
      </div>
      <div class="workers-container">
        <h3>Workers</h3>
        <div class="workers">
        <div class="worker" v-for="worker in currentZone.workers" :key="worker.id">
          {{ worker.name }}
        </div>
        </div>
      </div>
    </div>
    <div class="vertical-separator"/>
    <MonteCarloGraph :zone-id="currentZone.id" />
  </div>
  <hr>
  <ZoneTasks v-if="currentZone" class="zone-tasks" :zone="currentZone" :tasks="currentZone.tasks" />
  </div>
</template>

<style scoped>
.container-container {
  height: 100vh;
}

.container {
  width: 100%;
  margin: 0;
  padding: 0 20px;
  display: flex;
  flex-direction: row;
  gap: 20px;
}

.zone-menu {
  padding: 20px;
  border-radius: 8px;
}

.zone-menu h2 {
  margin-bottom: 10px;
}

.zone-menu p {
  margin: 5px 0;
}

.zone-menu button {
  margin-top: 10px;
  padding: 10px 15px;
  background-color: #E77474;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.zone-menu button:hover {
  background-color: #9d4d4d;
}

.zone-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0 20px;
}

.workers {
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  height: 150px;
  overflow-y: auto;
}

.worker {
  padding: 10px;
  background-color: #f1f1f1;
  height: 45px;
  border-radius: 4px;
  text-align: center;
}

.monte-carlo-graph {
  width: 100%;
  height: 100%;
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
}

.monte-carlo-graph h3 {
  margin-bottom: 10px;
}

canvas {
  width: 100%;
}

hr {
  margin: 0;
  border: none;
  border-top: 1px solid #e0e0e0;
}

.vertical-separator {
  border-left: 1px solid #e0e0e0;
  height: auto; /* Adjust height dynamically */
  margin: 0;
  align-self: stretch; /* Ensure it stretches to match sibling height */
}

.zone-tasks {
  margin-top: 1.5rem;
}
</style>