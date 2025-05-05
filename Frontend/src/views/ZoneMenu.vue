<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {Zone} from "@/assets/types";
import {useRoute} from "vue-router";

import ZoneTasks from "@/components/zones/ZoneTasks.vue";
import MonteCarloGraph from "@/components/MonteCarloGraph.vue";
import ZoneCalendar from "@/components/zones/ZoneCalendar.vue";
import {fetchZone} from "@/composables/DataFetcher";


let currentZone = ref<Zone | null>(null);
let activeTab = ref<'tasks' | 'calendar'>('tasks');
const route = useRoute();
const isSpinning = ref(false);

const loadZone = async () => {
  try {
    currentZone.value = await fetchZone(Number(route.params.id));
  } catch (error) {
    console.error("Error loading zone:", error);
  }
};

const runSimulation = async () => {
  isSpinning.value = true;
  try {
    await new Promise((resolve) => setTimeout(resolve, 1000));
    // Simulate a delay for the simulation process
    // Here you would call your simulation function
  } catch (error) {
    console.error("Error running simulation:", error);
  } finally {
    isSpinning.value = false;
  }
};

onMounted(() => {
  loadZone();
});
</script>

<template>
  <div class="container-container">
    <div class="container" v-if="currentZone">
      <div class="zone-info">
        <div class="zone-menu">
          <h1 style="transform: translateX(-2px)">{{ currentZone.name }}</h1>
          <div class="zone-menu-info">
            <div class="zone-options">
              <button class="toolbar-item" @click="runSimulation" title="Run Simulation">
                <img :class="{ 'spin-animation': isSpinning }" src="/src/assets/icons/simulation.svg" alt="Assign"/>
              </button>
              <router-link class="toolbar-item" title="Task Menu" :to="`/zones/${currentZone.id}/tasks`">
                <img src="/src/assets/icons/tasks.svg" alt="Assign"/>
              </router-link>
            </div>
            <p>{{ currentZone.workers.length }} Workers | {{ currentZone.tasks.length }} Tasks</p>
          </div>
        </div>
        <hr>
        <div class="workers-container">
          <h3>Workers</h3>
          <div class="workers">
            <div class="worker" v-for="worker in currentZone.workers" :key="worker.id">
              <div class="worker-image-container">
                <img class="worker-image" src="@/assets/icons/profile.svg" draggable="false"/>
              </div>
              {{ worker.name }}
            </div>
          </div>
        </div>
      </div>
      <div class="vertical-separator"/>
      <MonteCarloGraph class="monte-carlo-graph" :zone-id="currentZone.id"/>
    </div>
    <hr>
    <div class="tabs">
      <button :class="{ active: activeTab === 'tasks' }" @click="activeTab = 'tasks'">Tasks</button>
      <button :class="{ active: activeTab === 'calendar' }" @click="activeTab = 'calendar'">Calendar</button>
    </div>
    <div v-if="activeTab === 'tasks'">
      <ZoneTasks v-if="currentZone" :zone="currentZone" :tasks="currentZone.tasks"/>
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
  display: flex;
  flex-direction: row;

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
  padding: 1rem;
}

.zone-menu h1 {
  font-size: 1.5rem;
  margin: 0;
}

.zone-menu h2 {
  margin-bottom: 10px;
}

.zone-menu p {
  margin: 5px 0;
}


.zone-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.workers {
  display: grid;
  grid-template-columns: repeat(2, minmax(150px, 1fr));
  gap: 10px;
  overflow-y: auto;
}

.worker {
  display: flex;
  flex-direction: row;
  padding: 10px;
  align-items: center;
  background-color: #f1f1f1;
  border-radius: 1rem;
  text-align: left;
  font-size: 0.8rem;
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
  padding: 1rem;
}

.toolbar-item {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  width: 30px;
  height: 30px;
  margin-right: 1rem;
  color: #b77979;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-item img {
  width: 100%;
  height: auto;
}

.toolbar-item:hover {
  color: #000;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.spin-animation {
  animation: spin 1s ease-in-out infinite;
}

.zone-menu-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 1rem;
}

.worker-image-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #f8f8f8;
  margin-right: 0.5rem;
}

.worker-image {
  max-width: 70%;
  max-height: 70%;
}

.zone-options {
  display: flex;
  align-items: center;
}

.monte-carlo-graph {
  min-height: calc(50vh - 100px);
  height: 50%;
  max-height: 100%;
}
</style>