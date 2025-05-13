<script setup lang="ts">
import {License, Worker} from "@/assets/types";

const props = defineProps<{
  worker: Worker;
  busyWorkers: Worker[];
}>();

const isWorkerBusy = (worker: Worker): boolean => {
  return !props.busyWorkers.some((busyWorker: Worker) => busyWorker.id === worker.id);
};

</script>

<template>
  <div
      :class="{
     'worker-compact': true,
     'rdy-worker-box': !isWorkerBusy(props.worker),
     'busy-worker-box': isWorkerBusy(props.worker),
   }"
      :draggable="props.worker.availability && !isWorkerBusy(props.worker)"
      :style="{ cursor: isWorkerBusy(props.worker) || !props.worker.availability ? 'not-allowed' : 'grab' }"
  >
    <div class="worker-profile">
      <div class="worker-image-container">
        <img class="worker-image" src="@/assets/icons/profile.svg"/>
      </div>
      <div class="worker-name">{{ worker.name }}</div>
      <img v-if="!props.worker.availability" class="warning-icon" src="@/assets/icons/error.svg"/>
      <img v-else-if="isWorkerBusy(props.worker)" class="status-icon" src="@/assets/icons/busy.svg"/>
      <img v-else-if="!isWorkerBusy(props.worker)" class="status-icon" src="@/assets/icons/ready.svg"/>

    </div>
  </div>
</template>

<style scoped>
.worker-compact {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--background-2);
  opacity: 0.5;
  border-radius: 10px;
  max-height: 30px;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}

@keyframes pulse-border {
  0% {
    border-color: #ff4b4b;
    box-shadow: 0 0 2px #ff4b4b;
  }
  50% {
    border-color: #ffb4b4;
    box-shadow: 0 0 0 #ff4b4b;
  }
  100% {
    border-color: #ff4b4b;
    box-shadow: 0 0 2px #ff4b4b;
  }
}

.unq-worker-box {
  background-color: #ffcccc; /* Red for unqualified and ready */
  border: 2px solid #ff4b4b;
  animation: pulse-border 2s infinite;
}

.unq-worker-box:hover {
  animation: none;
  background-color: #ff9292;
  border: 2px solid #ff4b4b;
}

.rdy-worker-box {
  opacity: 1;
}

.busy-worker-box {
  background-color: #cccccc; /* Grey for busy and ready */
}

.busy-unq-worker-box {
  background-color: #ffebc0; /* Yellow for busy and unqualified */
}

.worker-profile {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between !important;
}

.worker-name {
  font-size: 0.6rem;
  font-weight: bold;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-image-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: var(--background-1);
  margin-right: 0.5rem;
}

.worker-image {
  max-width: 70%;
  max-height: 70%;
}


.status-icon {
  margin-left: auto;
  width: 20px;
  height: 20px;
  filter: saturate(0);
}

.warning-icon {
  margin-left: auto;
  width: 20px;
  height: 20px;
  filter: saturate(0);
  opacity: 0.5;
}

.status-popup {
  display: none;
  position: absolute;
  top: -25px;
  right: 0;
  background-color: var(--background-1);
  color: var(--text-1);
  padding: 5px;
  border-radius: 3px;
  font-size: 0.7rem;
  white-space: nowrap;
}

.worker-compact:hover .status-popup {
  display: block;
}

.worker-status-container {
  display: flex;
  justify-content: space-between;
  margin-top: 0.5rem;
}

.worker-status {
  background-color: var(--background-1);
  color: var(--text-1);
  font-size: 0.5rem;
  border-radius: 0.2rem;
  padding: 0.2rem;
  width: 3rem;
  text-align: center;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-ready {
  background-color: #79cc5e;
}

.worker-busy {
  background-color: #808080;
}

.worker-unqualified {
  background-color: #fa7d39;
}

.worker-unavailable {
  background-color: #ff4d4d;
}
</style>