<template>
  <div v-if="worker" class="page-content">
    <div class="left-content">
      <div class="staff-page">
        <div class="worker-image-container">
          <img class="worker-image" src="@/assets/icons/profile.svg" draggable="false"/>
        </div>
        <div class="worker-details-container">
          <div class="worker-details">
            <p class="worker-name">{{ worker.name }}</p>
            <p class="worker-status">{{ workerStatus }}</p>
            <Multiselect
                v-model="worker.licenses"
                :options="availableLicenses"
                :multiple="true"
                :close-on-select="false"
                label="name"
                track-by="id"
                placeholder="Select required licenses"
            />
            <p class="worker-info">
           <span
               class="availability-indicator"
               :class="{ 'available': worker.availability, 'unavailable': !worker.availability }">
           </span>
              {{ worker.availability ? 'Available' : 'Unavailable' }} - Working from 9:00 AM to 5:00 PM
            </p>
          </div>
        </div>
      </div>
      <div class="calendar-container">
        Calendar goes here
      </div>
    </div>
    <div class="graph-container">
      Graph goes here
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
import {ActiveTask, PickerTask} from "@/assets/types";

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
  activeTask?: ActiveTask;
  pickerTask?: PickerTask;
  zone?: Zone;
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

import { computed } from "vue";
import Multiselect from "vue-multiselect";

const workerStatus = computed(() => {
  if (worker.value) {
    if (worker.value.activeTask) {
      return `${worker.value.activeTask.task.name} at (${getZoneName(worker.value.zone_id)}) zone`;
    } else if (worker.value.pickerTask) {
      return `Picking task number ${worker.value.pickerTask.id} at zone ${getZoneName(worker.value.zone_id)}`;
    } else {
      return `Waiting for work at ${getZoneName(worker.value.zone_id)} zone`;
    }
  }
  return "No worker data available";
});

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
.left-content {
  width: 50%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  border-right: #E0E0E0 1px solid;
}

.page-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  padding: 2rem;
}

.calendar-container {
  min-height: 60%;
  height: 100%;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 1rem;;
  border-top: #E0E0E0 1px solid;
  margin-right: 2rem;
}

.staff-page {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  position: relative;
  width: 100%;
  max-height: 40%;
  margin-bottom: 40px;
}

.graph-container {
  min-width: 50%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
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
  display: flex;
  align-items: center;
  font-size: 1rem;
  color: #555;
}

.availability-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 0.5rem;
}

.available {
  background-color: green;
}

.unavailable {
  background-color: red;
}

::v-deep(.multiselect__tag) {
  background-color: #E77474 !important;
  color: white;
  border-radius: 5px;
}

::v-deep(.multiselect__tags) {
  border: none !important;
  box-shadow: none !important;
}

::v-deep(.multiselect) {
  max-width: 500px;
  width: 100%;
}

::v-deep(.multiselect__content) {
  max-width: 300px;
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
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background-color: #f6f6f6;
  margin-right: 0.5rem;
}

.worker-image {
  width: 80px;
}
</style>