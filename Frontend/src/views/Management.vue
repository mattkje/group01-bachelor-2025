<template>
  <div class="tabs-container">
    <div class="tabs-header">
      <button
          v-for="tab in tabs"
          :key="tab"
          :class="['tab-button', { active: activeTab === tab }]"
          @click="activeTab = tab"
      >
        {{ tab }}
      </button>
    </div>
    <div class="tabs-content">
      <div v-if="activeTab === 'Tasks'">
        <div class="item-container">
          <div class="parent-cell">
            <div class="cell">Task Name</div>
            <div class="cell">Description</div>
            <div class="cell">Min - Max Time</div>
            <div class="cell">Min - Max Workers</div>
            <div class="cell">Zone ID</div>
          </div>
          <div class="item" v-for="task in tasks" :key="task.id">
            <div class="cell">{{ task.name }}</div>
            <div class="cell">{{ task.description }}</div>
            <div class="cell">{{ task.minTime }} - {{ task.maxTime }}</div>
            <div class="cell">{{ task.minWorkers }} - {{ task.maxWorkers }}</div>
            <div class="cell">{{ task.zoneId }}</div>
          </div>
        </div>
      </div>
      <div v-if="activeTab === 'Zones'">
        <div>
          <div v-for="zone in zones" :key="zone.id">{{ zone.name }}</div>
        </div>
      </div>
      <div v-if="activeTab === 'Workers'">
        <div>
          <div v-for="worker in workers" :key="worker.id">{{ worker.name }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {Task, Worker, Zone} from "@/assets/types";

const tabs = ['Tasks', 'Workers', 'Zones'];
const activeTab = ref('Tasks');
const tasks = ref<Array<Task>>([]);
const workers = ref<Array<Worker>>([]);
const zones = ref<Array<Zone>>([]);

function fetchData() {
  fetch('http://localhost:8080/api/tasks')
    .then(response => response.json())
    .then(data => {
      tasks.value = data;
    })
    .catch(error => console.error('Error fetching tasks:', error));

  fetch('http://localhost:8080/api/workers')
    .then(response => response.json())
    .then(data => {
      workers.value = data;
    })
    .catch(error => console.error('Error fetching workers:', error));

  fetch('http://localhost:8080/api/zones')
    .then(response => response.json())
    .then(data => {
      zones.value = data;
    })
    .catch(error => console.error('Error fetching zones:', error));
}

onMounted(() => {
  fetchData();
})
</script>

<style scoped>
.tabs-container {
  width: 100%;
  height: 100%;
  margin: 0 auto;
  font-family: Arial, sans-serif;
  border-radius: 0 8px 8px 0;
  overflow-y: auto;
}

.tabs-header {
  display: flex;
  background-color: #f9f9f9;
  border-bottom: 1px solid #ddd;
}

.tab-button {
  flex: 1;
  padding: 10px 15px;
  text-align: center;
  cursor: pointer;
  background: none;
  border: none;
  font-size: 16px;
  font-weight: bold;
  color: #555;
  transition: background-color 0.3s, color 0.3s;
}

.tab-button:hover {
  background-color: #f0f0f0;
}

.tab-button.active {
  background-color: #e77474;
  color: white;
}

.tabs-content {
  padding: 20px;
  background-color: white;
}

.tabs-content h2 {
  margin-top: 0;
  color: #e77474;
}

.item {
  display: table-row;
  border-bottom: 1px solid #ddd;
}

.item-container {
  display: table;
  width: 100%;
  border-collapse: collapse;
}

.cell {
  display: table-cell;
  padding: 10px;
  text-align: left;
  color: 1px solid #ddd;
}

.parent-cell {
  display: table-header-group;
  background-color: #f9f9f9;
}
</style>