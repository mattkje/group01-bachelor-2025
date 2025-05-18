<script setup lang="ts">
import {ref, computed, onMounted} from 'vue';
import {Zone, Worker} from "@/assets/types";
import {fetchAllWorkers, fetchAllZones} from "@/services/DataFetcher";
import {createWorker} from "@/services/DataUpdater";

const workers = ref<Worker[]>([]);
const zones = ref<Zone[]>([]);
const searchQuery = ref('');
const selectedZones = ref<number[]>([]);
const showAvailableOnly = ref(false);
const showAddWorkerPopup = ref(false);
const newWorkerName = ref('');

const addWorker = async () => {
  if (newWorkerName.value.trim()) {

    const newWorker: Worker = {
      id: null, // The ID is auto-generated, so no need to set it here
      name: newWorkerName.value,
      efficiency: 1,
      zone: 0,
      dead: false,
      availability: true,
      currentTaskId: null,
      licenses: [],
    };
    await createWorker(newWorker);
    newWorkerName.value = '';
    showAddWorkerPopup.value = false;
  }
};

const loadAllData = async () => {
  zones.value = await fetchAllZones()
  workers.value = await fetchAllWorkers()
};

const getZoneName = (zoneId: number) => {
  const zone = zones.value.find(zone => zone.id === zoneId);
  return zone ? zone.name : 'Unassigned';
};

const filteredWorkers = computed(() => {
  return workers.value.filter((worker: Worker) => {
    const matchesSearch = worker.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesZone = selectedZones.value.length === 0 || selectedZones.value.includes(worker.zone);
    const matchesAvailability = showAvailableOnly.value ? worker.availability : true;
    return matchesSearch && matchesZone && matchesAvailability;
  });
});

onMounted(() => {
  loadAllData();
});
</script>

<template>
  <div class="staff-page">
    <div class="content">
      <div class="staff-cards">
        <div v-for="worker in filteredWorkers" :key="worker.id" class="staff-card">
          <div class="card-header">
            <div class="left-items">
              <h3>{{ worker.name }}</h3>
            </div>
            <router-link :to="`/workers/${worker.id}`" class="edit-link">
              <img src="@/assets/icons/edit.svg" alt="Edit" width="20" height="20"/>
            </router-link>
          </div>
          <hr>
          <div class="card-body">
            <p><strong>Zone:</strong> {{ getZoneName(worker.zone) }}</p>
            <p><strong>Licenses:</strong> {{ worker.licenses.map(license => license.name).join(', ') }}</p>
            <p><strong>Availability:</strong> {{ worker.availability ? 'Available' : 'Unavailable' }}</p>
          </div>
        </div>
      </div>
      <div class="filters">
        <div class="filter-header">
          <input v-model="searchQuery" type="text" placeholder="Search by name" class="search-bar"/>
          <button class="add-worker-btn" @click="showAddWorkerPopup = true">Add Worker</button>
        </div>
        <div class="filter-group">
          <h4>Zones</h4>
          <div class="filter-options">
            <select v-model="selectedZones" multiple class="multi-select-dropdown">
              <option v-for="zone in zones" :key="zone.id" :value="zone.id">
                {{ zone.name }}
              </option>
            </select>
            <label>
              <input type="checkbox" :value="0" v-model="selectedZones"/>
              Unassigned
            </label>
          </div>
        </div>
        <div class="filter-group">
          <h4>Availability</h4>
          <label>
            <input v-model="showAvailableOnly" type="checkbox"/>
            Available Only
          </label>
        </div>
      </div>
    </div>
    <div v-if="showAddWorkerPopup" class="popup-overlay" @click.self="showAddWorkerPopup = false">
      <div class="popup">
        <h3>Add Worker</h3>
        <input v-model="newWorkerName" type="text" placeholder="Full Name" class="popup-input"/>
        <div class="popup-actions">
          <button @click="addWorker">Add</button>
          <button @click="showAddWorkerPopup = false">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.staff-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.content {
  height: 100%;
  display: flex;
  flex-direction: row;
}

.filters {
  padding: 1rem;
  min-width: 20%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-left: 1px solid var(--border-1);
}

.filter-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  height: 3rem;
  align-items: center;
}


.search-bar {
  color: var(--text-1);
  background-color: var(--background-2);
  padding: 1rem;
  height: 3rem;
  border: 1px solid var(--border-1);
  border-radius: 1rem;
  width: 100%;
}

.multi-select-dropdown {
  width: 100%;
  height: 100%;
  padding: 0.5rem;
  border: 1px solid var(--border-1);
  border-radius: 4px;
  background-color: var(--background-2);
  color: var(--text-1);
  font-size: 1rem;
}

.multi-select-dropdown:focus {
  outline: none;
  border-color: var(--main-color);
}

.filter-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.filter-group label {
  color: var(--text-2);
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.filter-group input[type="checkbox"] {
  appearance: none;
  -webkit-appearance: none;
  background-color: var(--background-2);
  border: 1px solid var(--border-1);
  width: 1rem;
  height: 1rem;
  margin-right: 0.5rem;
  border-radius: 0.25rem;
  display: inline-block;
  position: relative;
}

.filter-group input[type="checkbox"]:checked {
  background-color: var(--main-color);
  border-color: var(--main-color);
}

.filter-group input[type="checkbox"]::after {
  content: '';
  position: absolute;
  top: 0.2rem;
  left: 0.35rem;
  width: 0.3rem;
  height: 0.6rem;
  border: solid #fff;
  border-width: 0 0.2rem 0.2rem 0;
  transform: rotate(45deg);
  display: none;
}

.filter-group input[type="checkbox"]:checked::after {
  display: block;
}

.staff-cards {
  height: 100%;
  width: 100%;
  padding: 1rem;
  gap: 1rem;
  display: flex;
  justify-content: flex-start;
  align-content: flex-start;
  flex-wrap: wrap;
  overflow-y: auto;
}

.staff-card {
  background-color: var(--background-1);
  border: 1px solid var(--border-1);
  border-radius: 1.5rem;
  padding: 1rem;
  max-height: 170px;
  width: calc(33.333% - 1rem);
}

.staff-card hr {
  border: 1px solid var(--main-color);
  border-radius: 10rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.card-body p {
  margin: 0.5rem 0;
}

.edit-link {
  background: none;
  border: none;
  border-radius: 1rem;
  cursor: pointer;
}

.edit-link img {
  filter: brightness(1);
  transition: filter 0.3s;
}

.edit-link:hover img {
  filter: brightness(0.5);
}

.profile-portrait {
  border-radius: 50%;
  width: 50px;
  margin: 0;
  border: 2px solid var(--border-1);
}

.card-body strong {
  color: var(--text-1);
}

.card-body p {
  color: var(--text-2);
  font-size: 0.8rem;
  line-height: 1.1;
}


.left-items {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.popup {
  background-color: var(--background-1);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 300px;
}

.popup-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 1rem;
}

.add-worker-btn {
  background-color: var(--main-color);
  color: white;
  height: 3rem;
  padding: 0.3rem 0.5rem;
  border: none;
  border-radius: 1rem;
  cursor: pointer;
}
</style>