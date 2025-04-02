<template>
      <div class="container">
        <div class="overview">
          <div class="grid">
            <ZoneClass v-for="zone in zones" :key="zone.id" :zone-id="zone.id" :title="zone.name" :workers="getWorkersByZone(zone.id)" @refreshWorkers="fetchAll"/>
          </div>
        </div>
        <WorkerRegistry :workers="workers"  :zones="zones" :taskLessWorkers="taskLessWorkers" @refreshWorkers="fetchAll" title="Unassigned" :zone-id="0"/>
      </div>
    </template>

    <script setup lang="ts">
    import { ref, onMounted } from 'vue';
    import ZoneClass from "../components/zones/Zone.vue";
    import WorkerRegistry from "../components/zones/WorkerRegistry.vue";
    import {Zone, Worker} from "@/assets/types";

    const zones = ref<Zone[]>([]);
    const workers = ref<Worker[]>([]);
    const taskLessWorkers = ref<Worker[]>([]);

    const getWorkersByZone = (zoneId: number): Worker[] => {
      return workers.value.filter((worker: Worker) => worker.zone === zoneId);
    };

    const fetchZones = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/zones');
        zones.value = await response.json();
      } catch (error) {
        console.error('Failed to fetch zones:', error);
      }
    };

    const fetchWorkers = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/workers');
        workers.value = await response.json();
      } catch (error) {
        console.error('Failed to fetch workers:', error);
      }
    };

    const getTaskLessWorkers = async (): Promise<void> => {
      try {
        const response = await fetch(`http://localhost:8080/api/active-tasks`);
        const tasks = await response.json();
        taskLessWorkers.value = workers.value.filter((worker: Worker) => !tasks.some((task: any) => task.workers.some((w: any) => w.id === worker.id)));
      } catch (error) {
        console.error('Failed to fetch worker task:', error);
        taskLessWorkers.value = [];
      }
    };

   const repopulateWorkersAfterActiveTaskUpdate = async () => {
     try {
       const response = await fetch('http://localhost:8080/api/active-tasks');
       const tasks = await response.json();

       workers.value.forEach((worker: Worker) => {
         const isAssigned = tasks.some((task: any) => task.workers.some((w: any) => w.id === worker.id));
         if (!isAssigned) {
           worker.zone_id = 0; // Move unassigned workers to zone 0
         }
       });

       console.log('Workers repopulated after active task update');
     } catch (error) {
       console.error('Failed to repopulate workers after active task update:', error);
     }
   };

    const moveUnavailableWorkers = () => {
      workers.value.forEach((worker: Worker) => {
        if (!worker.availability) {
          worker.zone_id = 0;
        }
      });
    };

    const updateWorkerZone = async () => {
      try {
        moveUnavailableWorkers();
        console.log('Worker zones updated successfully');
        await fetchWorkers();
      } catch (error) {
        console.error('Error updating worker zones:', error);
      }
    };

    const fetchAll = async () => {
      await fetchZones();
      await fetchWorkers();
      await getTaskLessWorkers();
      await updateWorkerZone();
    };

    const refreshWorkersEveryXMinutes = async () => {
      await repopulateWorkersAfterActiveTaskUpdate();
    };

    onMounted(() => {
      fetchAll();
      setInterval(refreshWorkersEveryXMinutes, 5000); // Emit every 5 seconds
    });
    
    </script>

    <style scoped>
    .container {
      display: flex;
      height: 100%;
    }

    .overview {
      flex: 1;
      padding: 1rem 1rem 0 1rem;
      height: calc(100vh - 120px);
      overflow-y: auto;
    }

    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
      gap: 1rem;
      margin-bottom: 1rem;
    }

    .worker-registry {
      position: sticky;
      width: 280px;
      max-height: 100%;
      padding: 1rem;
      border-left: 1px solid #ccc;
      background-color: #f9f9f9;
    }

    .toolbar {
      position: sticky;
      top: 0;
      width: 100%;
      z-index: 1000;
    }
    </style>