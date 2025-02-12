<script setup lang="ts">
      import { defineProps, computed } from 'vue';
      import Worker from "@/components/zones/Worker.vue";

      interface Worker {
        name: string;
        zone: number;
        licenses: number[];
        task: string;
        eta: string;
        available: boolean;
        qualified: boolean;
      }

      const props = defineProps<{
        title: string;
        requiredQualifications: string[];
        workers: Worker[];
      }>();

      const licensesList = [
        "Truck License",
        "Forklift License",
        "Safety Training",
        "First Aid Certification"
      ];

      const isWorkerQualified = (worker: Worker) => {
        const workerLicenses = worker.licenses.map(license => licensesList[license]);
        return props.requiredQualifications.every(qualification => workerLicenses.includes(qualification));
      };

      // Dummy data for task completion times (in minutes) and remaining tasks
      const taskCompletionTimes = [30, 45, 60]; // Example times
      const remainingTasks = 5; // Example remaining tasks

      const totalTaskCompletionTime = computed(() => {
        return taskCompletionTimes.reduce((total, time) => total + time, 0);
      });

      const completionTime = computed(() => {
        const now = new Date();
        now.setMinutes(now.getMinutes() + totalTaskCompletionTime.value);
        return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
      });
      </script>

      <template>
        <div class="rounded-square">
          <div class="title-bar">
            <div class="title-bar-status">
              {{ title }}
              <div class="task-summary">
                Done by: {{ completionTime }}
                <br />
                Tasks: {{ remainingTasks }}
              </div>
            </div>
            <hr>
            <div class="zone-options">
              <button class="icon-button">
                <img src="/src/assets/icons/overview.svg" alt="Assign" />
              </button>
              <button class="icon-button">
                <img src="/src/assets/icons/tasks.svg" alt="Assign" />
              </button>
              <button class="icon-button">
                <img src="/src/assets/icons/simulation.svg" alt="Assign" />
              </button>
              <button v-if="false" class="icon-button bell-icon">
                <img src="/src/assets/icons/bell.svg" alt="Assign" />
              </button>
              <button v-if="true" class="icon-button bell-icon">
                <img src="/src/assets/icons/bellUpdate.svg" alt="Assign" />
              </button>
            </div>
          </div>
          <div class="vertical-box">
            <Worker
              v-for="(worker, index) in workers"
              :key="index"
              :name="worker.name"
              :licenses="worker.licenses"
              :task="worker.task"
              :eta="worker.eta"
              :available="worker.available"
              :qualified="!isWorkerQualified(worker)"
              :class="{ 'unavailable': !worker.available}"
            />
            <p v-if="workers.length === 0" style="text-align: center; margin-top: 1rem;">No workers assigned</p>
          </div>
        </div>
      </template>

      <style scoped>
      .rounded-square {
        width: 280px;
        height: 100%;
        border: 1px solid #ccc;
        border-radius: 15px;
        overflow: hidden;
        display: flex;
        flex-direction: column;
      }

      .title-bar {
        display: flex;
        flex-direction: column;
        background-color: #f5f5f5;
        padding: 1rem;
        font-size: 1.2rem;
        line-height: 0.7rem;
        font-weight: bold;
        color: #7B7B7B;
        border-bottom: 1px solid #ccc;
      }

      .title-bar hr {
        width: 100%;
        margin: 0.5rem 0;
        border: none;
        border-top: 1px solid #ccc;
      }

      .title-bar-status {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        font-size: 1.2rem;
        font-weight: bold;
        color: #7B7B7B;
      }

      .task-summary {
        color: #7B7B7B;
        display: flex;
        line-height: 1rem;
        font-size: 0.7rem;
        flex-direction: column;
        align-items: flex-end;
      }

      .vertical-box {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding: 1rem;
      }

      .unavailable {
        display: none;
      }

      .unqualified {
        color: #f56e6e;
        border-radius: 0.5rem;
        padding: 10px;
        line-height: 0.2rem;
        margin-top: 0.1rem;
      }

      .icon-button {
        background: none;
        border: none;
        cursor: pointer;
        font-size: 1rem;
        width: 20px;
        height: 20px;
        margin-right: 1rem;
        color: #b77979;
      }

      .icon-button img {
        width: 20px;
        height: 20px;
      }

      .icon-button:hover {
        color: #000;
      }

      .bell-icon {
        margin-left: 6rem;
      }
      </style>