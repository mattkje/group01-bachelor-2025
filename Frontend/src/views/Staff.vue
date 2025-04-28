<script setup lang="ts">
          import { ref, computed, onMounted } from 'vue';
          import { Zone, Worker } from "@/assets/types";

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
            return workers.value.filter((worker: Worker) => {
              const matchesSearch = worker.name.toLowerCase().includes(searchQuery.value.toLowerCase());
              const matchesZone = selectedZones.value.length === 0 || selectedZones.value.includes(worker.zone);
              const matchesAvailability = showAvailableOnly.value ? worker.availability : true;
              return matchesSearch && matchesZone && matchesAvailability;
            });
          });

          const getEfficiencyColor = (efficiency: number) => {
            if (efficiency < 0.95) {
              return '#ff5d5d';
            } else if (efficiency < 1.05 && efficiency >= 0.95) {
              return '#f6aa3b';
            } else {
              return '#79cc5e';
            }
          };

          const getRandomProfileImageUrl = (workerId: number) => {
            const gender = workerId % 2 === 0 ? 'men' : 'women';
            const id = workerId % 100;
            return `https://randomuser.me/api/portraits/${gender}/${id}.jpg`;
          };

          const editWorker = (worker: Worker) => {
            // Implement the logic to edit the worker's information
            console.log('Edit worker:', worker);
          };

          onMounted(() => {
            fetchWorkers();
            fetchZones();
          });
          </script>

          <template>
            <div class="staff-page">
              <div class="filters">
                <input v-model="searchQuery" type="text" placeholder="Search by name" class="search-bar"/>
                <div class="filter-group">
                  <label v-for="zone in zones" :key="zone.id">
                    <input type="checkbox" :value="zone.id" v-model="selectedZones"/>
                    {{ zone.name }}
                  </label>
                  <label>
                    <input type="checkbox" :value="0" v-model="selectedZones"/>
                    Unassigned
                  </label>
                </div>
                <div class="filter-group">
                  <label>
                    <input v-model="showAvailableOnly" type="checkbox"/>
                    Available Only
                  </label>
                </div>
              </div>
              <div class="staff-cards">
                <div v-for="worker in filteredWorkers" :key="worker.id" class="staff-card">
                  <div class="card-header">
                    <div class="left-items">
                      <p :style="{ backgroundColor: getEfficiencyColor(worker.efficiency) }" class="efficiency-score">{{ worker.efficiency }}</p>

                      <h3>{{ worker.name }}</h3>
                    </div>
                    <router-link :to="`/worker?id=${worker.id}`" class="edit-link">
                      <img src="@/assets/icons/edit.svg" alt="Edit" width="20" height="20"/>
                    </router-link>
                  </div>
                  <hr>
                  <div class="card-body">
                    <p><strong>Zone:</strong> {{ getZoneName(worker.zone) }}</p>
                    <p><strong>Licenses:</strong> {{ worker.licenses.map(license => license.name).join(', ') }}</p>
                    <p><strong>Availability:</strong> {{ worker.availability ? 'Available' : 'Unavailable' }}</p>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <style scoped>
          .staff-page {
            max-height: 95%;
            padding: 2rem;
            overflow-y: auto;
          }

          .filters {
            display: flex;
            flex-direction: column;
            gap: 1rem;
            margin-bottom: 1rem;
            position: -webkit-sticky;
            position: sticky;
            top: -2rem;
            background-color: white;
            z-index: 1000;
            padding: 1rem;
            border-bottom: #e1e1e1 1px solid;
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

          .staff-cards {
            display: flex;
            flex-wrap: wrap;
            gap: 1rem;
          }

          .staff-card {
            background-color: #fff;
            border: 1px solid #e1e1e1;
            border-radius: 1.5rem;
            padding: 1rem;
            width: calc(33.333% - 1rem);
          }

          .staff-card hr {
            border: 1px solid #e57575;
            border-radius: 10rem;
          }

          .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
          }

          .card-body p {
            margin: 0.5rem 0;
          }

          .edit-link {
            background: none;
            border: none;
            border-radius: 1rem;
            cursor: pointer;
          }

          .edit-link img {
            filter: brightness(1);
            transition: filter 0.3s;
          }

          .edit-link:hover img {
            filter: brightness(0.5);
          }

          .profile-portrait {
            border-radius: 50%;
            width: 50px;
            margin: 0;
            border: 2px solid #e1e1e1;
          }

          .card-body strong {
            color: #595959;
          }

          .card-body p {
            color: #919191;
            font-size: 0.8rem;
            line-height: 1.1;
          }

          .efficiency-score {
            background-color: #e57575;
            color: white;
            text-align: center;
            width: 40px;
            height: 40px;
            padding: 0.5rem;
            border-radius: 50%;
            font-size: 0.8rem;
            font-weight: 900;
            margin: 0;
            display: flex;
            align-items: center;
            justify-content: center;
          }

          .worker-image-container {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background-color: #f6f6f6;
            margin-right: 0.5rem;
          }

          .worker-image {
            width: 30px;
            height: 30px;
          }

          .left-items {
            display: flex;
            align-items: center;
            gap: 0.5rem;
          }
          </style>