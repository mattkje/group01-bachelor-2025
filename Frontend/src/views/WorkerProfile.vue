<script setup lang="ts">
import {ref, onMounted, Ref} from "vue";
import {useRoute} from "vue-router";
import {computed} from "vue";
import Multiselect from "vue-multiselect";
import {License, Worker, Zone} from "@/assets/types";
import {fetchWorker, fetchAllZones, fetchLicenses} from "@/composables/DataFetcher";
import {deleteWorker, updateWorkerAvailability} from "@/composables/DataUpdater";

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
  padding: 0.5rem 1rem;
  width: 100%;
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.option-bar button {
  display: flex;
  border: var(--border-1) 1px solid;
  border-radius: 0.5rem;
  background-color: var(--main-color);
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
</style>