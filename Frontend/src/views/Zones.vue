<script setup lang="ts">
import {ref, onMounted} from 'vue';
import ZoneClass from "../components/zones/Zone.vue";
import WorkerRegistry from "../components/zones/WorkerRegistry.vue";
import {Zone, Worker, ActiveTask} from "@/assets/types";
import {fetchAllWorkers, fetchAllZones, fetchAllActiveTasks} from "@/services/DataFetcher";

const zones = ref<Zone[]>([]);
const workers = ref<Worker[]>([]);
const activeTasks = ref<ActiveTask[]>([]);
const taskLessWorkers = ref<Worker[]>([]);
const zonesWithPickerTasks = ref<Zone[]>([]);
const zonesWithoutPickerTasks = ref<Zone[]>([]);


const getWorkersByZone = (zoneId: number): Worker[] => {
  return workers.value.filter((worker: Worker) => worker.zone === zoneId);
};

const loadZones = async () => {
  try {
    zones.value = await fetchAllZones();
    filterZonesByPickerTasks();
  } catch (error) {
    console.error("Error loading zone:", error);
  }
};

const loadWorkers = async () => {
  try {
    workers.value = await fetchAllWorkers()
  } catch (error) {
    console.error("Error loading workers:", error);
  }
};

const loadActiveTasks = async () => {
  try {
    activeTasks.value = await fetchAllActiveTasks();
  } catch (error) {
    console.error('Failed to fetch active tasks:', error);
  }
};

const filterZonesByPickerTasks = () => {
  zonesWithPickerTasks.value = zones.value.filter(zone => zone.isPickerZone);
  zonesWithoutPickerTasks.value = zones.value.filter(zone => !zone.isPickerZone);
};

const extractTaskLessWorkers = () => {
  taskLessWorkers.value = workers.value.filter((worker: Worker) =>
      !activeTasks.value.some((task: ActiveTask) =>
          task.workers.some((w: Worker) => w.id === worker.id)
      )
  );
};

const moveUnavailableWorkers = () => {
  workers.value.forEach((worker: Worker) => {
    if (!worker.availability) {
      worker.zone = 0;
    }
  });
};

const updateWorkerZone = async () => {
  try {
    moveUnavailableWorkers();
    await loadZones();
  } catch (error) {
    console.error('Error updating worker zones:', error);
  }
};

const fetchAll = async () => {
  try {
    const [tasks, zonesData, workersData] = await Promise.all([
      loadActiveTasks(),
      loadZones(),
      loadWorkers()
    ]);
    extractTaskLessWorkers();
    await updateWorkerZone();
  } catch (error) {
    console.error('Error fetching data:', error);
  }
};


onMounted(() => {
  fetchAll();
});

</script>
<template>
  <div class="container">
    <div class="overview">
      <h2>Picker Zones</h2>
      <div class="grid">
        <!-- this should only display zones with no pickerTasks -->
        <ZoneClass v-for="zone in zonesWithPickerTasks" :key="zone.id" :zone-id="zone.id" :title="zone.name"
                   :workers="getWorkersByZone(zone.id)" @refreshWorkers="fetchAll"/>
      </div>
      <hr>
      <h2>Non-Picker Zones</h2>
      <div class="grid">
        <!-- this should only display zones with pickerTasks -->
        <ZoneClass v-for="zone in zonesWithoutPickerTasks" :key="zone.id" :zone-id="zone.id" :title="zone.name"
                   :workers="getWorkersByZone(zone.id)" @refreshWorkers="fetchAll"/>
      </div>
    </div>
    <WorkerRegistry :workers="workers" :zones="zones" :taskLessWorkers="taskLessWorkers" @refreshWorkers="fetchAll"
                    title="Unassigned" :zone-id="0"/>
  </div>
</template>


<style scoped>
.container {
  display: flex;
  height: 100%;
}

.overview {
  flex: 1;
  padding: 1rem 0 0 0;
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.overview h2 {
  margin: 0 1rem;
  font-size: 1.5rem;
  color: var(--text-2);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  justify-items: start;
  gap: 1rem;
  margin: 0 1rem;
}

hr {
  border: none;
  border-top: 1px solid var(--border-1);
  margin: 1rem 0;
}
</style>