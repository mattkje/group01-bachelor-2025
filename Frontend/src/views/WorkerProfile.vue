<template>
  <Toolbar title="Worker Profile" />
  <div v-if="worker" class="staff-page">
    <img src="../assets/icons/worker.svg" alt="Profile Picture" class="profile-picture" />
    <div class="worker-details">
      <p class="worker-name">{{ worker.name }}</p>
      <p class="worker-info">Zone: {{ getZoneName(worker.zone) }}</p>
      <p class="worker-info">Effectiveness: {{ worker.effectiveness }}</p>
      <p class="worker-info">Licenses: {{ worker.licenses.map(license => license.name).join(', ') }}</p>
      <p class="worker-info">Availability: {{ worker.availability ? 'Available' : 'Unavailable' }}</p>
    </div>
    <BarChart :zone-labels="zoneTitles" :data="zoneData" />
  </div>
  <div v-else>
    <p>This worker does not exist</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import Toolbar from "../components/Toolbar.vue";
import BarChart from "../components/BarChart.vue";
import Zone from "@/components/zones/Zone.vue";


interface Zone {
  id: number;
  name: string;
}

interface License {
  id: number;
  name: string;
}

interface Worker {
  id: number;
  name: string;
  zone_id: number;
  effectiveness: number;
  licenses: License[];
  availability: boolean;
  profilePicture?: string;
}

const route = useRoute();
const worker = ref<Worker | null>(null);
const zones = ref<Zone[]>([]);

const zoneTitles = ref<string[]>([]);
const zoneData = ref<number[]>([]);

const fetchWorker = async () => {
  const workerId = route.query.id; // Extract ID from ?id=1
  if (!workerId) {
    console.error("Worker ID not provided in URL.");
    return;
  }

  try {
    const response = await fetch(`http://localhost:8080/api/workers/${workerId}`);
    if (!response.ok) throw new Error("Failed to fetch worker");
    worker.value = await response.json();
  } catch (error) {
    console.error("Failed to fetch worker:", error);
  }
};



const fetchWorkerHistory = () => {
  zoneTitles.value = ["Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5"];
  zoneData.value = [10, 0, 0, 40, 50];
};

const fetchZones = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/zones');
    zones.value = await response.json();
  } catch (error) {
    console.error('Failed to fetch zones:', error);
  }
};

const getZoneName = (zoneId: number) => {
  const zone = zones.value.find(zone => zone.id === zoneId);
  return zone ? zone.name : 'Unassigned';
};

onMounted(() => {
  fetchWorker();
  fetchZones();
  fetchWorkerHistory();
});
</script>

<style scoped>
.staff-page {
  display: flex;
  align-items: center;
  padding: 2rem;
  background-color: #f9f9f9;
  border-radius: 10px;
  position: relative;
}

.profile-picture {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  margin-right: 2rem;
  object-fit: cover;
}

.worker-details {
  display: flex;
  flex-direction: column;
}

.worker-name {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.worker-info {
  font-size: 1rem;
  color: #555;
  margin-bottom: 0.25rem;
}
</style>