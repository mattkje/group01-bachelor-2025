<script setup lang="ts">
import {ref, computed, onMounted} from 'vue';
import {Zone, Worker} from "@/assets/types";
import {fetchAllWorkers, fetchAllZones} from "@/composables/DataFetcher";

const workers = ref<Worker[]>([]);
const zones = ref<Zone[]>([]);
const searchQuery = ref('');
const selectedZones = ref<number[]>([]);
const showAvailableOnly = ref(false);

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
    <div class="filters">
      <input v-model="searchQuery" type="text" placeholder="Search by name" class="search-bar"/>
      <div class="filter-group">
        <label v-for="zone in zones" :key="zone.id">
          <input type="checkbox" :value="zone.id" v-model="selectedZones"/>
          {{ zone.name }}
        </label>
        <label>
          <input type="checkbox" :value="0" v-model="selectedZones"/>
          Unassigned
        </label>
      </div>
      <div class="filter-group">
        <label>
          <input v-model="showAvailableOnly" type="checkbox"/>
          Available Only
        </label>
      </div>
    </div>
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
  </div>
</template>

<style scoped>
.staff-page {
  max-height: 95%;
  padding: 2rem;
  overflow-y: auto;
}

.filters {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1rem;
  position: -webkit-sticky;
  position: sticky;
  top: -2rem;
  background-color: var(--background-1);
  z-index: 1000;
  padding: 1rem;
  border-bottom: var(--border-1) 1px solid;
}

.search-bar {
  color: var(--text-1);
  background-color: var(--background-2);
  padding: 0.5rem;
  border: 1px solid var(--border-1);
  border-radius: 4px;
  width: 100%;
}

.filter-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.filter-group label {
  color: var(--text-2s);
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
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.staff-card {
  background-color: var(--background-1);
  border: 1px solid var(--border-1);
  border-radius: 1.5rem;
  padding: 1rem;
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
</style>