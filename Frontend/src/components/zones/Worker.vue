<script setup lang="ts">
import {defineProps, ref} from 'vue';

const props = defineProps({
  name: {
    type: String,
    required: true
  },
  licenses: {
    type: Array,
    required: true
  },
  availability: {
    type: Boolean,
    required: true
  }
});

</script>

<template>
  <div class="worker" draggable="true">
    <div class="worker-name">{{ name }}</div>
    <div class="worker-licenses">
      <span v-for="(license, index) in licenses" :key="index" class="license">{{ license.name }}</span>
    </div>
    <hr/>
    <div class="worker-status-container">
      <div>
        <div v-if="availability" class="worker-task">Task: 1</div>
        <div v-if="availability" class="worker-eta">ETA: 1</div>
      </div>
      <div>

        <div v-if="availability" class="worker-status worker-busy">Busy</div>
        <div v-if="!availability" class="worker-status worker-busy">Unavailable</div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.worker {
  border: 1px solid #ccc;
  border-radius: 15px;
  padding: 1rem;
  max-height: 100px;
  margin-bottom: 1rem;
  background-color: transparent;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker:hover {
  background-color: #FFF2F2;
  border: 1px solid #f56e6e;
}

.worker-name {
  line-height: 0.3rem;
  font-size: 1rem;
  font-weight: bold;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-licenses {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.license {
  background-color: #e0e0e0;
  border-radius: 5px;
  padding: 0.2rem 0.5rem;
  font-size: 0.4rem;
  font-weight: bold;
  user-select: none !important;
  -webkit-user-select: none !important;
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
  user-select: none !important;
  -webkit-user-select: none !important;
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
  user-select: none !important;
  -webkit-user-select: none !important;
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
  user-select: none !important;
}
</style>