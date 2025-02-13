<template>
        <Toolbar title="Staff" />
        <div class="staff-page">
          <div class="filters">
            <input v-model="searchQuery" type="text" placeholder="Search by name" class="search-bar" />
            <div class="filter-group">
              <label v-for="zone in zones" :key="zone.id">
                <input type="checkbox" :value="zone.id" v-model="selectedZones" />
                {{ zone.name }}
              </label>
              <label>
                <input type="checkbox" :value="0" v-model="selectedZones" />
                Unassigned
              </label>
            </div>
            <div class="filter-group">
              <label>
                <input v-model="showAvailableOnly" type="checkbox" />
                Available Only
              </label>
            </div>
          </div>
          <table class="staff-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Zone</th>
                <th>Effectiveness</th>
                <th>Licenses</th>
                <th>Availability</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="worker in filteredWorkers" :key="worker.id">
                <td>{{ worker.name }}</td>
                <td>{{ getZoneName(worker.zone) }}</td>
                <td>{{ worker.effectiveness }}</td>
                <td>{{ worker.licenses.map(license => license.name).join(', ') }}</td>
                <td>{{ worker.availability ? 'Available' : 'Unavailable' }}</td>
                <td>
                  <button @click="editWorker(worker)">Edit</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>

      <script setup lang="ts">
      import { ref, computed, onMounted } from 'vue';
      import Toolbar from "@/components/Toolbar.vue";

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
      }

      const workers = ref<Worker[]>([]);
      const zones = ref<Zone[]>([]);
      const searchQuery = ref('');
      const selectedZones = ref<number[]>([]);
      const showAvailableOnly = ref(false);

      const fetchWorkers = async () => {
        try {
          const response = await fetch('http://localhost:8080/api/workers');
          workers.value = await response.json();
        } catch (error) {
          console.error('Failed to fetch workers:', error);
        }
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

      const filteredWorkers = computed(() => {
        return workers.value.filter(worker => {
          const matchesSearch = worker.name.toLowerCase().includes(searchQuery.value.toLowerCase());
          const matchesZone = selectedZones.value.length === 0 || selectedZones.value.includes(worker.zone);
          const matchesAvailability = showAvailableOnly.value ? worker.availability : true;
          return matchesSearch && matchesZone && matchesAvailability;
        });
      });

      const editWorker = (worker: Worker) => {
        // Implement the logic to edit the worker's information
        console.log('Edit worker:', worker);
      };

      onMounted(() => {
        fetchWorkers();
        fetchZones();
      });
      </script>

      <style scoped>
      .staff-page {
        padding: 2rem;
      }

      .filters {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        margin-bottom: 1rem;
      }

      .search-bar {
        padding: 0.5rem;
        border: 1px solid #ccc;
        border-radius: 4px;
        width: 100%;
      }

      .filter-group {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
      }

      .filter-group label {
        display: flex;
        align-items: center;
        gap: 0.25rem;
      }


      .staff-table {
        width: 100%;
        border-collapse: separate;
        border-spacing: 0;
        margin-bottom: 1rem;
        border-radius: 8px;
        border: #e1e1e1 1px solid;
      }

      .staff-table th, .staff-table td {
        padding: 0.8vh;
        text-align: left;
      }

      .staff-table th {
        background-color: #f4f4f4;
        font-weight: bold;
        cursor: pointer;
      }

      .staff-table tbody tr {
        transition: background-color 0.3s;
      }

      .staff-table tbody tr:hover {
        background-color: #f9f9f9;
      }

      .staff-table tbody tr:nth-child(even) {
        background-color: #f8f8f8;
      }


      .staff-table button {
        padding: 0.25rem 0.5rem;
        background-color: #E77474;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
      }

      .staff-table button:hover {
        background-color: #9d4d4d;
      }
      </style>