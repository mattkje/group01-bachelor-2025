<template>
  <div v-if="worker" class="staff-page">
    <div class="worker-image-container">
      <img class="worker-image" src="@/assets/icons/profile.svg" draggable="false"/>
    </div>
    <div class="worker-details">
      <p class="worker-name">{{ worker.name }}</p>
      <p class="worker-info">Zone: {{ getZoneName(worker.zone_id) }}</p>
      <p class="worker-info">Effectiveness: {{ worker.effectiveness }}</p>
      <p class="worker-info">Licenses: {{ worker.licenses.map(license => license.name).join(', ') }}</p>
      <p class="worker-info">Availability: {{ worker.availability ? 'Available' : 'Unavailable' }}</p>
    </div>
    <div class="license-management">
      <select v-model="selectedLicenseId">
        <option v-for="license in availableLicenses" :key="license.id" :value="license.id">
          {{ license.name }}
        </option>
      </select>
      <button @click="addLicense">Add License</button>
      <div v-for="license in worker.licenses" :key="license.id">
        <input type="checkbox" :id="`license-${license.id}`" :value="license.id" v-model="selectedLicenses"/>
        <label :for="`license-${license.id}`">{{ license.name }}</label>
      </div>
      <button @click="removeSelectedLicenses">Remove Selected Licenses</button>
    </div>
    <div class="option-bar">
      <button @click="changeWorkerAvailability" v-if="worker.name !== 'Vincent Holiday'">
        <img src="/src/assets/icons/busy.svg" alt="Edit" />
        <span v-if="!isAvailable">Make Available</span>
        <span v-else>Make Unavailable</span>
      </button>
      <button @click="showDeleteConfirmation = true"  v-if="worker.name !== 'Vincent Holiday'">
        <img src="/src/assets/icons/error.svg" alt="Edit"/>
        <span>Delete Worker</span>
      </button>
    </div>
  </div>
  <div v-else>
    <p>This worker does not exist</p>
  </div>
  <div v-if="showDeleteConfirmation" class="modal">
    <div class="modal-content">
      <p>Are you sure you want to delete this worker?</p>
      <button @click="confirmDelete">Yes</button>
      <button @click="showDeleteConfirmation = false">No</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted} from "vue";
import {useRoute} from "vue-router";
import Toolbar from "../components/Toolbar.vue";
import BarChart from "../components/BarChart.vue";

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
const availableLicenses = ref<License[]>([]);
const selectedLicenseId = ref<number | null>(null);
const selectedLicenses = ref<number[]>([]);
const isAvailable = ref(false);
const zoneTitles = ref<string[]>([]);
const zoneData = ref<number[]>([]);
const showDeleteConfirmation = ref(false);

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

const getRandomProfileImageUrl = (workerId: number) => {
  const gender = workerId % 2 === 0 ? 'men' : 'women';
  const id = workerId % 100;
  return `https://randomuser.me/api/portraits/${gender}/${id}.jpg`;
};


const fetchLicenses = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/licenses');
    if (!response.ok) throw new Error('Failed to fetch licenses');
    availableLicenses.value = await response.json();
  } catch (error) {
    console.error('Error fetching licenses:', error);
  }
};

const addLicense = async () => {
  if (selectedLicenseId.value !== null && worker.value) {
    const licenseToAdd = availableLicenses.value.find(license => license.id === selectedLicenseId.value);
    if (licenseToAdd && !worker.value.licenses.some(license => license.id === licenseToAdd.id)) {
      worker.value.licenses.push(licenseToAdd);
      await addLicensesToWorker(worker.value.id, [licenseToAdd]);
    }
  }
};

const removeSelectedLicenses = async () => {
  if (worker.value) {
    const licensesToRemove = worker.value.licenses.filter(license => selectedLicenses.value.includes(license.id));
    worker.value.licenses = worker.value.licenses.filter(license => !selectedLicenses.value.includes(license.id));
    await removeLicensesFromWorker(worker.value.id, licensesToRemove);
    selectedLicenses.value = [];
  }
};

const addLicensesToWorker = async (workerId: number, licenses: License[]): Promise<void> => {
  try {
    for (const license of licenses) {
      const response = await fetch(`http://localhost:8080/api/workers/${workerId}/license/${license.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      if (!response.ok) throw new Error(`Failed to add license ${license.id} to worker`);
    }
  } catch (error) {
    console.error('Error adding licenses to worker:', error);
  }
};

const changeWorkerAvailability = async () => {
  if (worker.value) {
    worker.value.availability = !worker.value.availability;
    isAvailable.value = worker.value.availability;
    await fetch(`http://localhost:8080/api/workers/${worker.value.id}/availability`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({availability: worker.value.availability}),
    });
  }
};

const confirmDelete = async () => {
  if (worker.value) {
    await fetch(`http://localhost:8080/api/workers/${worker.value.id}`, {
      method: 'DELETE',
    });
    showDeleteConfirmation.value = false;
  }
};

const removeLicensesFromWorker = async (workerId: number, licenses: License[]): Promise<void> => {
  // using addLicensesToWorker and adding a license to the worker that already exists, will delete the license from the worker
  await addLicensesToWorker(workerId, licenses);
};

const getZoneName = (zoneId: number) => {
  const zone = zones.value.find(zone => zone.id === zoneId);
  return zone ? zone.name : 'Unassigned';
};

onMounted(() => {
  fetchWorker();
  fetchZones();
  fetchLicenses();
  fetchWorkerHistory();
});
</script>

<style scoped>
.staff-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  border-radius: 10px;
  position: relative;
}

.option-bar {
  background-color: white;
  padding: 0.5rem 1rem;
  width: 100%;
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.option-bar button {
  display: flex;
  border: #dcdcdc 1px solid;
  border-radius: 0.5rem;
  background-color: white;
  align-items: center;
  padding: 0.5rem 1rem;
  width: 12rem;
  align-content: center;
  justify-content: center;
  cursor: pointer;
}

.option-bar button img {
  width: 30px;
  height: 30px;
  margin-right: 0.5rem;
}

.option-bar button span {
  font-size: 1rem;
}

.profile-picture {
  margin-top: 1.5rem;
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

.license-management {
  margin-top: 1rem;
}

.license-management select {
  margin-right: 1rem;
}

.license-management button {
  margin-right: 1rem;
}

.worker-image-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background-color: #f6f6f6;
  margin-right: 0.5rem;
}

.worker-image {
  width: 120px;
}
</style>