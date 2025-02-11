<template>
        <Toolbar title="Zones"/>
        <div class="container">
          <div class="overview">
            <div class="grid">
              <Zone v-for="index in 6" :key="index" :title="`Zone ${index}`" :workers="getWorkersByZone(index)" />
            </div>
          </div>
          <WorkerRegistry :workers="dummyWorkers" />
        </div>
      </template>

      <script setup lang="ts">
      import Toolbar from "@/components/Toolbar.vue";
      import Zone from "@/components/zones/Zone.vue";
      import WorkerRegistry from "@/components/zones/WorkerRegistry.vue";

      const dummyWorkers = [
        { name: "John Doe", zone: 1, licenses: [0, 1], task: "Building", eta: "2 hours", available: true },
        { name: "Jane Smith", zone: 1, licenses: [2], task: "", eta: "", available: false },
        { name: "Alice Johnson", zone: 2, licenses: [1, 3], task: "Managing", eta: "1 hour", available: true },
        { name: "Bob Brown", zone: 2, licenses: [0], task: "Inspecting", eta: "3 hours", available: true },
        { name: "Charlie Davis", zone: 3, licenses: [1, 2], task: "Repairing", eta: "4 hours", available: false },
        { name: "Diana Evans", zone: 3, licenses: [3], task: "Supervising", eta: "2 hours", available: true },
        { name: "Eve Foster", zone: 4, licenses: [0, 2], task: "Welding", eta: "5 hours", available: true },
        { name: "Frank Green", zone: 4, licenses: [1], task: "Painting", eta: "1 hour", available: false },
        { name: "Grace Harris", zone: 5, licenses: [2, 3], task: "Cleaning", eta: "3 hours", available: true },
        { name: "Hank Irving", zone: 5, licenses: [0], task: "Transporting", eta: "2 hours", available: true },
        { name: "Ivy Johnson", zone: 6, licenses: [1, 3], task: "Organizing", eta: "4 hours", available: false },
        { name: "Jack King", zone: 6, licenses: [2], task: "Surveying", eta: "1 hour", available: true },
        { name: "Karen Lee", zone: 1, licenses: [0, 1, 2], task: "Planning", eta: "2 hours", available: true },
        { name: "Leo Martin", zone: 2, licenses: [3], task: "Executing", eta: "3 hours", available: false },
        { name: "Mona Nelson", zone: 3, licenses: [0, 2], task: "Monitoring", eta: "5 hours", available: true }
      ];

      const getWorkersByZone = (zoneIndex) => {
        return dummyWorkers.filter(worker => worker.zone === zoneIndex);
      };
      </script>

      <style scoped>
      .container {
        display: flex;
      }

      .overview {
        flex: 1;
        padding: 2rem;
        max-height: 85vh; /* Set a fixed height */
        overflow-y: auto; /* Enable vertical scrolling */
      }

      .grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 2rem;
      }

      .worker-registry {
        position: sticky;
        width: 300px;
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