<template>
      <Toolbar title="Zones"/>
      <div class="container">
        <div class="overview">
          <div class="grid">
            <Zone v-for="zone in zones" :key="zone.id" :title="zone.name" :workers="getWorkersByZone(zone.id)"/>
          </div>
        </div>
        <WorkerRegistry :workers="workers" />
      </div>
    </template>

    <script setup lang="ts">
    import { ref, onMounted } from 'vue';
    import Toolbar from "../components/Toolbar.vue";
    import Zone from "../components/zones/Zone.vue";
    import WorkerRegistry from "../components/zones/WorkerRegistry.vue";

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

    onMounted(() => {
      fetchZones();
      fetchWorkers();
    });
    </script>

    <style scoped>
    .container {
      display: flex;
    }

    .overview {
      flex: 1;
      padding: 1rem;
      max-height: 85vh; /* Set a fixed height */
      overflow-y: auto; /* Enable vertical scrolling */
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