<script setup lang="ts">
      import { defineProps } from 'vue';
      import Worker from "@/components/zones/Worker.vue";

      interface Worker {
        name: string;
        zone: number;
        licenses: number[];
        task: string;
        eta: string;
        available: boolean;
      }

      const props = defineProps<{
        title: string;
        workers: Worker[];
      }>();

      </script>

      <template>
        <div class="rounded-square">
          <div class="title-bar">
            {{ title }}
          </div>
          <div class="vertical-box">
            <Worker v-for="(worker, index) in workers" :key="index" :name="worker.name" :licenses="worker.licenses" :task="worker.task" :eta="worker.eta" :available="worker.available" :class="{ 'unavailable': !worker.available }"/>
          </div>
        </div>
      </template>

      <style scoped>
      .rounded-square {
        width: 300px;
        height: 300px;
        border: 1px solid #ccc;
        border-radius: 15px;
        overflow: hidden;
        display: flex;
        flex-direction: column;
      }

      .title-bar {
        background-color: transparent;
        padding: 1rem;
        text-align: left;
        font-size: 1.2rem;
        font-weight: bold;
        color: #7B7B7B;
        border-bottom: 1px solid #ccc;
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
      </style>