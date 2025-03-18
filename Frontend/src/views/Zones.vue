<template>
  <Toolbar title="Zones"/>
      <div class="container">
        <div class="overview">
          <div class="grid">
            <Zone v-for="zone in zones" :key="zone.id" :zone-id="zone.id" :title="zone.name" :workers="getWorkersByZone(zone.id)" @refreshWorkers="fetchAll"/>
          </div>
        </div>
        <WorkerRegistry :workers="workers"  :zones="zones" :taskLessWorkers="taskLessWorkers" @refreshWorkers="fetchAll"/>
      </div>
    </template>

    <script setup lang="ts">
    import { ref, onMounted } from 'vue';
    import Toolbar from "../components/Toolbar.vue";
    import Zone from "../components/zones/Zone.vue";
    import WorkerRegistry from "../components/zones/WorkerRegistry.vue";
    import {provideCompactMode} from "@/compactMode";

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
      workerType: string;
      availability: boolean;
    }

    const zones = ref<Zone[]>([]);
    const workers = ref<Worker[]>([]);
    const taskLessWorkers = ref<Worker[]>([]);

    const getWorkersByZone = (zoneId: number) => {
      return workers.value.filter(worker => worker.zone === zoneId);
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
        taskLessWorkers.value = workers.value.filter(worker => !tasks.some((task: any) => task.workers.some((w: any) => w.id === worker.id)));
      } catch (error) {
        console.error('Failed to fetch worker task:', error);
        taskLessWorkers.value = [];
      }
    };


    // If worker is unavailable, set zone to 0
    const updateWorkerZone = async () => {
      try {
        const unavailableWorkers = workers.value.filter(worker => worker.zone !== 0 && !worker.availability);

        for (const worker of unavailableWorkers) {
          const response = await fetch(`http://localhost:8080/api/workers/${worker.id}/zone/0`, {
            method: 'PUT',
          });

          if (!response.ok) {
            throw new Error(`Failed to update zone for worker ${worker.id}`);
          }
        }

        console.log('Worker zones updated successfully');
        fetchWorkers();
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

    onMounted(() => {
      fetchAll();
    });

    provideCompactMode();
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
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1rem;
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