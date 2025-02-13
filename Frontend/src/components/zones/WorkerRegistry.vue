<script setup lang="ts">
import {ref, computed, defineProps} from 'vue';
import Worker from '@/components/zones/Worker.vue';

const props = defineProps<{
  workers: Worker[];
  zones: { id: number, name: string }[];
}>();

const searchQuery = ref('');
const selectedLicenses = ref([]);
const showAvailableOnly = ref(false);
const showFilters = ref(false);
const selectedZones = ref<number[]>([]);

const licensesList = [
  "Truck License",
  "Forklift License",
  "Safety Training",
  "First Aid Certification"
];

const filteredWorkers = computed(() => {
  return props.workers.filter(worker => {
    const matchesSearch = worker.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesLicenses = selectedLicenses.value.length === 0 || selectedLicenses.value.every(license => worker.licenses.map(l => l.id).includes(license));
    const matchesAvailability = showAvailableOnly.value ? worker.availability : true;
    const shouldIncludeUnavailable = searchQuery.value ? true : worker.availability;
    const matchesZone = selectedZones.value.length === 0 || selectedZones.value.includes(worker.zone);
    return matchesSearch && matchesLicenses && matchesAvailability && shouldIncludeUnavailable && matchesZone;
  });
});
</script>

<template>
  <div class="worker-registry">
    <button @click="showFilters = !showFilters">
      {{ showFilters ? 'Hide Filters' : 'Show Filters' }}
    </button>
    <div v-if="showFilters" class="filters">
      <input v-model="searchQuery" type="text" placeholder="Search by name"/>
      <div>
        <hr/>
        <label>Qualifications:</label>
        <label v-for="(license, index) in licensesList" :key="index" style="display: block;">
          <input type="checkbox" :value="index" v-model="selectedLicenses"/>
          {{ license }}
        </label>
      </div>
      <div>
        <hr/>
        <label>Zones:</label>
        <label v-for="zone in props.zones" :key="zone.id" style="display: block;">
          <input type="checkbox" :value="zone.id" v-model="selectedZones"/>
          {{ zone.name }}
        </label>
        <label>
          <input type="checkbox" :value="0" v-model="selectedZones"/>
          Unassigned
        </label>
      </div>
      <hr/>
      <label>
        <input v-model="showAvailableOnly" type="checkbox"/>
        Available Only
      </label>
    </div>
    <div class="worker-list">
      <Worker v-for="(worker, index) in filteredWorkers"
              :key="index"
              :name="worker.name"
              :zone_id="worker.zone_id"
              :licenses="worker.licenses"
              :availability="worker.availability"
              :class="{ 'unavailable': !worker.availability }"/>
    </div>
  </div>
</template>

<style scoped>
.worker-registry {
  width: 300px;
  height: 100%;
  padding: 1rem;
  border-left: 1px solid #ccc;
  background-color: #f9f9f9;
  box-sizing: border-box;
}

.filters {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.worker-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  height: 100%; /* Set a fixed height */
  overflow-y: auto; /* Enable vertical scrolling */
  flex-grow: 1;
}

.unavailable {
  opacity: 0.5;
}
</style>