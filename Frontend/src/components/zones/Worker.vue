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
            <div v-if="!available" class="worker-task">Unavailable</div>
            <div v-if="available" class="worker-task">Task: {{ task }}</div>
            <div v-if="available" class="worker-eta">ETA: {{ eta }}</div>
          </div>
        </template>

        <style scoped>
        .worker {
          border: 1px solid #ccc;
          border-radius: 10px;
          padding: 1rem;
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

        .worker-task,
        .worker-eta {
          color: #7B7B7B;
          line-height: 0.2rem;
          margin-top: 0.5rem;
          font-size: 0.5rem;
        }

        hr {
          margin: 0.5rem 0;
          border: none;
          border-top: 1px solid #ccc;
        }
        </style>