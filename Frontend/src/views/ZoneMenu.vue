<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {Zone} from "@/assets/types";
import {useRoute} from "vue-router";

import ZoneTasks from "@/components/zones/ZoneTasks.vue";
import MonteCarloGraph from "@/components/MonteCarloGraph.vue";
import ZoneCalendar from "@/components/zones/ZoneCalendar.vue";


let currentZone = ref<Zone | null>(null);
let activeTab = ref< 'tasks' | 'calendar'>('tasks');
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

const parseLocalDate = (dateString: string): Date => {
  return new Date(`${dateString}T00:00:00`);
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
        <h1>{{ currentZone.name }} Zone</h1>
        <div style="display: flex; justify-content: space-between">
        <p>{{ currentZone.workers.length }} Workers | {{ currentZone.tasks.length }} Tasks</p>
        <button @click="$emit('runSimulation', currentZone.id)">Run Simulation</button>
        </div>
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
    <MonteCarloGraph :zone-id="currentZone.id" :date="parsedDate"/>
  </div>
  <hr>
    <div class="tabs">
      <button :class="{ active: activeTab === 'tasks' }" @click="activeTab = 'tasks'">Tasks</button>
      <button :class="{ active: activeTab === 'calendar' }" @click="activeTab = 'calendar'">Calendar</button>
    </div>
    <div v-if="activeTab === 'tasks'">
      <ZoneTasks v-if="currentZone" :zone="currentZone" :tasks="currentZone.tasks" />
    </div>
    <div v-if="activeTab === 'calendar'">
      <ZoneCalendar v-if="currentZone" :zone="currentZone"/>
    </div>
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

.tabs {
  display: flex;
  position: relative;
  top: -34px;
}

.tabs button {
  padding: 10px 15px;
  background-color: transparent;
  border: none;
  cursor: pointer;
}

.tabs button:hover {
  background-color: rgba(0, 0, 0, 0.05);
  transition: ease 0.2s;
}

.tabs button.active {
  background-color: #E77474;
  color: white;
  border-color: #E77474;
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
  background-color: rgba(0, 0, 0, 0.05);
  padding: 20px;
  border-radius: 8px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  height: 150px;
  overflow-y: auto;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05); /* Inner border effect */
}

.worker {
  padding: 10px;
  background-color: #ffffff;
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

.workers-container {
  margin-bottom: 50px;
}
</style>