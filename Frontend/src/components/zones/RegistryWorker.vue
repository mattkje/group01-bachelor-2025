<script setup lang="ts">
import {License} from "@/assets/types";

const props = defineProps<{
  name: string;
  workerId: number;
  licenses: License[];
  availability: boolean;
  zoneId: number;
}>();

const getRandomProfileImageUrl = (workerId: number, isToon: boolean) => {
  if (isToon) {
    return `https://joesch.moe/api/v1/${workerId}`;
  } else {
    const gender = workerId % 2 === 0 ? 'men' : 'women';
    const id = workerId % 100;
    return `https://randomuser.me/api/portraits/thumb/${gender}/${id}.jpg`;
  }
};

</script>

<template>
  <div class="worker-compact rdy-worker-box">
    <div class="worker-profile">
      <div class="worker-image-container">
        <img v-if="false" class="worker-image" :src="getRandomProfileImageUrl(workerId, false)" />
        <img v-else class="worker-image" src="@/assets/icons/profile.svg" />
      </div>
      <div class="worker-name">{{ name }}</div>
    </div>
  </div>
</template>

<style scoped>
.worker-compact {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ececec;
  opacity: 0.5;
  border-radius: 10px;
  max-height: 30px;
  padding: 0.5rem;
  margin-bottom: 0.5rem;
  user-select: none !important;
  -webkit-user-select: none !important;
}

.worker-compact:hover {
  background-color: #dcdcdc;
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

.busy-unq-worker-box {
  background-color: #ffebc0; /* Yellow for busy and unqualified */
}

.worker-profile {
  display: flex;
  align-items: center;
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
  background-color: #f6f6f6;
  margin-right: 0.5rem;
}

.worker-image {
  max-width: 70%;
  max-height: 70%;
}

.warning-icon {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
}

.status-icon {
  margin-top: 7px;
  width: 20px;
  height: 20px;
}

.status-popup {
  display: none;
  position: absolute;
  top: -25px;
  right: 0;
  background-color: #333;
  color: #fff;
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
  background-color: white;
  color: white;
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