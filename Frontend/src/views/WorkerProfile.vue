<script setup lang="ts">
import {ref, onMounted} from "vue";
import {useRoute} from "vue-router";
import {computed} from "vue";
import Multiselect from "vue-multiselect";
import {License, Worker, Zone} from "@/assets/types";
import {fetchWorker, fetchAllZones, fetchLicenses} from "@/services/DataFetcher";
import {deleteWorker, updateWorkerAvailability} from "@/services/DataUpdater";

const route = useRoute();
const worker = ref<Worker | null>(null);
const zones = ref<Zone[]>([]);
const availableLicenses = ref<License[]>([]);
const showDeleteConfirmation = ref(false);

const loadWorker = async () => {
  try {
    worker.value = await fetchWorker(parseInt(Array.isArray(route.params.id) ? route.params.id[0] : route.params.id));
  } catch (error) {
    console.error("Failed to fetch worker:", error);
    console.error("Failed to fetch worker:", error);
  }
};

const loadZones = async () => {
  try {
    zones.value = await fetchAllZones()
  } catch (error) {
    console.error('Failed to fetch zones:', error);
  }
};

const workerStatus = computed(() => {
  return "No worker data available";
});

const loadLicenses = async () => {
  try {
    availableLicenses.value = await fetchLicenses();
  } catch (error) {
    console.error('Error fetching licenses:', error);
  }
};

const addLicensesToWorker = async (workerId: number, licenses: License[]): Promise<void> => {
  await addLicensesToWorker(workerId, licenses);
};

const changeWorkerAvailability = async (worker: Worker, isAvailable: boolean) => {
  await updateWorkerAvailability(worker, isAvailable);
};

const confirmDelete = async () => {
  if (worker.value) {
    await deleteWorker(worker.value);
    showDeleteConfirmation.value = false;
  }
};

onMounted(() => {
  loadWorker();
  loadZones();
  loadLicenses();
});
</script>
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
      <div class="option-bar">
        <button @click="changeWorkerAvailability(worker, !worker.availability)">
          <img src="@/assets/icons/edit.svg" alt="Change Availability"/>
          <span>{{ worker.availability ? 'Set Unavailable' : 'Set Available' }}</span>
        </button>
        <button @click="showDeleteConfirmation = true">
          <img src="@/assets/icons/trash.svg" alt="Delete Worker"/>
          <span>Delete Worker</span>
        </button>
      </div>
    </div>
    <div class="graph-container">
      <div class="calendar-placeholder">
        <p>Room for a calendar or graph here</p>
      </div>
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
<style scoped>
.left-content {
  width: 50%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  border-right: var(--border-1) 1px solid;
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
  border-top: var(--border-1) 1px solid;
  margin-right: 2rem;
}

.calendar-placeholder {
  min-height: 60%;
  height: 100%;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 1rem;
  border: var(--border-1) 1px dashed;
  margin-right: 2rem;
  color: var(--text-2);
  font-style: italic;
  background-color: var(--background-2);
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
   background-color: var(--background-1);
   padding: 1rem;
   width: 100%;
   display: flex;
   gap: 1.5rem;
   margin-top: 1rem;
   justify-content: center;
   border-radius: 0.5rem;
 }

.option-bar button {
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 0.5rem;
  background-color: var(--main-color);
  color: white;
  font-size: 1rem;
  font-weight: bold;
  padding: 0.75rem 1.5rem;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s ease;
}

.option-bar button:hover {
  background-color: var(--main-color-2);
  transform: scale(1.05);
}

.option-bar button img {
  width: 24px;
  height: 24px;
  margin-right: 0.5rem;
  filter: brightness(0) invert(1);
}

.option-bar button span {
  display: inline-block;
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
  color: var(--text-1);
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

::v-deep(.multiselect__dropdown) {
  background-color: var(--background-1) !important;
  border: 1px solid var(--border-1);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

::v-deep(.multiselect__tag) {
  background-color: var(--main-color) !important;
  color: white !important;
  border-radius: 5px;
}

::v-deep(.multiselect__tags) {
  background-color: var(--background-1) !important;
  border: none !important;
  box-shadow: none !important;
}

::v-deep(.multiselect) {
  background-color: var(--background-1) !important;
  max-width: 500px;
  width: 100%;
}

::v-deep(.multiselect__content) {
  background-color: var(--background-1) !important;
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
  aspect-ratio: 1;
  border-radius: 50%;
  background-color: var(--background-2);
  margin-right: 0.5rem;
}

.worker-image {
  width: 80px;
}

.modal {
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

.modal-content {
  background-color: white;
  padding: 2rem;
  border-radius: 0.5rem;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.modal-content button {
  margin: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}



.modal-content button:first-of-type {
  background-color: var(--main-color);
  color: white;
}

.modal-content button:first-of-type:hover {
  background-color: var(--main-color-2);
}

.modal-content button:last-of-type {
  background-color: gray;
  color: white;
}

.modal-content button:last-of-type:hover {
  background-color: darkgray;
}
</style>