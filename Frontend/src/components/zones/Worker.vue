<script setup lang="ts">
        import { defineProps } from 'vue';

        const props = defineProps({
          name: {
            type: String,
            required: true
          },
          licenses: {
            type: Array,
            required: true
          },
          task: {
            type: String,
            required: true
          },
          eta: {
            type: String,
            required: true
          },
          available: {
            type: Boolean,
            required: true
          },
          qualified: {
            type: Boolean,
            required: true
          }
        });

        const licensesList = [
          "Truck License",
          "Forklift License",
          "Safety Training",
          "First Aid Certification"
        ];
        </script>

        <template>
          <div class="worker">
            <div class="worker-name">{{ name }}</div>
            <div class="worker-licenses">
              <span v-for="(license, index) in licenses" :key="index" class="license">{{ licensesList[license] }}</span>
            </div>
            <hr />
            <div class="worker-status-container">
              <div>
                <div v-if="available && !(eta === '0' || task === 'None')" class="worker-task">Task: {{ task }}</div>
                <div v-if="available && !(eta === '0' || task === 'None')" class="worker-eta">ETA: {{ eta }}</div>
              </div>
              <div>
                <div v-if="!qualified" class="worker-status worker-unqualified">Unqualified</div>
                <div v-if="available && (eta === '0' || task === 'None')" class="worker-status worker-Ready">Ready</div>
                <div v-if="available && !(eta === '0' || task === 'None')" class="worker-status worker-busy">Busy</div>
                <div v-if="!available" class="worker-status worker-busy">Unavailable</div>
              </div>

            </div>
          </div>
        </template>

        <style scoped>
        .worker {
          border: 1px solid #ccc;
          border-radius: 10px;
          padding: 1rem;
          max-height: 100px;
          margin-bottom: 1rem;
          background-color: transparent;
        }

        .worker-name {
          line-height: 0.3rem;
          font-size: 1rem;
          font-weight: bold;
        }

        .worker-licenses {
          display: flex;
          flex-wrap: wrap;
          gap: 0.5rem;
          margin-top: 0.5rem;
        }

        .license {
          background-color: #e0e0e0;
          border-radius: 5px;
          padding: 0.2rem 0.5rem;
          font-size: 0.4rem;
          font-weight: bold;
          color: #333;
        }


        .worker-status-container {
          display: flex;
          justify-content: space-between;
        }

        .worker-task,
        .worker-eta {
          color: #7B7B7B;
          line-height: 0.2rem;
          margin-top: 0.5rem;
          font-size: 0.5rem;
        }

        .worker-status {
          background-color: white;
          color: white;
          font-size: 0.6rem;
          border-radius: 0.3rem;
          padding: 5px;
          width: 4rem;
          text-align: center;
          line-height: 0.4rem;
          margin-top: 0.1rem;
        }

        .worker-Ready {
          background-color: #79cc5e;
        }

        .worker-busy {
          background-color: #808080;
        }

        .worker-unqualified {
          background-color: #fab639;
        }

        hr {
          margin: 0.5rem 0;
          border: none;
          border-top: 1px solid #ccc;
        }
        </style>