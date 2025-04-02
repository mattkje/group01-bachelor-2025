<script setup lang="ts">
import {onMounted, ref, inject, computed, defineEmits} from "vue";
import axios from 'axios';

const props = defineProps({
  title: {
    type: String,
    required: true
  },
});

const isSpinning = ref(false);
let completionTime = ref(null);
let isPlaying = ref(false);
const activeTab = inject('activeTab', ref('Overview'));
const isPaused = ref(false);
const state = ref(0); // 0: stopped, 1: running, 2: paused

const fetchPausedState = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/simulation/getStatus');
    isPaused.value = response.data;
    if (state.value === 0) {
      isPlaying.value = false;
      isPaused.value = false
    }
    if (state.value === 1) {
      isPlaying.value = true;
      isPaused.value = false
    }
    if (state.value === 2) {
      isPlaying.value = true;
      isPaused.value = true
    }
    else {
      console.error('Invalid state value:', state.value);
    }
  } catch (error) {
    console.error('Error fetching paused state:', error);
  }
};

const simulatedTime = ref(new Date());
const fetchCurrentTimeFromBackend = async () => {
  const response = await axios.get('http://localhost:8080/api/simulation/currentTime');
  return response.data;
};

const fetchCurrentDateFromBackend = async () => {
  const response = await axios.get('http://localhost:8080/api/simulation/currentDate');
  return response.data;
};

const currentTime = ref('');
const currentDate = ref('');

const updateCurrentTime = async () => {
  currentTime.value = await fetchCurrentTimeFromBackend();
  currentDate.value = await fetchCurrentDateFromBackend();
};
const updateSimulatedTime = () => {
  simulatedTime.value = new Date(simulatedTime.value.getTime() + 60000); // Advance by 1 minute
};

let intervalId: number | null = null;

let speedIndex = 0;
const speeds = [5/10, 5/2, 5/3, 5];

const startClock = async () => {
  isPlaying.value = true;
  try {
    await axios.post('http://localhost:8080/api/simulation/start', null, {
      params: {simulationTime: 2}
    });
  } catch (error) {
    console.error('Error starting simulation:', error);
  }
};

const stopClock = async () => {
  isPaused.value = !isPaused.value;
  try {
    await axios.post('http://localhost:8080/api/simulation/pause');
  } catch (error) {
    console.error('Error pausing simulation:', error);
  }
};

const fastForwardClock = async () => {
  if (intervalId) {
    clearInterval(intervalId);
  }
  speedIndex = (speedIndex + 1) % speeds.length;
  try {
    await axios.post('http://localhost:8080/api/simulation/fastForward', null, {
      params: {speedMultiplier: speeds[speedIndex]}
    });
  } catch (error) {
    console.error('Error fast forwarding simulation:', error);
  }
};

const runAllMonteCarloSimulations = async () => {
  isSpinning.value = true;
  try {
    const response = await fetch(`http://localhost:8080/api/monte-carlo`, {
      method: 'GET',
    });

    if (!response.ok) {
      throw new Error('Failed to run simulation');
    }
    const result = await response.json();

    let latestTimes = {};
    for (const zoneId in result) {
      const times = result[zoneId].filter(time => !time.includes('ERROR'));
      if (times.length > 0) {
        const currentTime = times[0];
        if (!latestTimes[zoneId]) {
          latestTimes[zoneId] = currentTime;
        } else {
          const [latestHours, latestMinutes] = latestTimes[zoneId].split(':').map(Number);
          const [currentHours, currentMinutes] = currentTime.split(':').map(Number);

          if (currentHours > latestHours || (currentHours === latestHours && currentMinutes > latestMinutes)) {
            latestTimes[zoneId] = currentTime;
          }
        }
      }
    }

    console.log(latestTimes);
  } catch (error) {
    console.error('Error running simulation:', error);
  } finally {
    await new Promise(resolve => setTimeout(resolve, 1000));
    isSpinning.value = false;
  }
};const isFinished = computed(() => {
  if (completionTime.value === null) {
    return false;
  }
  const [hours, minutes] = completionTime.value.split(':').map(Number);
  const [currentHours, currentMinutes] = currentTime.value.split(':').map(Number);

  return hours < currentHours || (hours === currentHours && minutes < currentMinutes);
});

const dateText = computed(() => {
  if (!currentDate.value) {
    return '00/00/0000';
  }
  console.log(currentDate.value);
  return new Date(currentDate.value).toLocaleDateString();
});

onMounted(() => {
  fetchPausedState();
  updateCurrentTime();
  setInterval(updateCurrentTime, 1000);
});
</script>

<template>
  <div class="toolbar">
    <div class="toolbar-title">
      <div class="logo">
        <img src="@/assets/icons/wws.svg" alt="Logo" class="logo-icon"/>
        <span class="logo-text">Warehouse&nbsp;Workflow<br><span class="regular-font">Simulator™</span></span>
      </div>
    </div>
    <button class="toolbar-item" @click="runAllMonteCarloSimulations">
      <img :class="{ 'spin-animation': isSpinning }" src="/src/assets/icons/simulation.svg" alt="Assign"/>
    </button>
    <button v-if="false" class="toolbar-item">
      <img src="/src/assets/icons/bell.svg" alt="Assign"/>
    </button>
    <button v-if="true" class="toolbar-item">
      <img src="/src/assets/icons/bellUpdate.svg" alt="Assign"/>
    </button>
    <div class="vertical-separator"/>
    <div class="controls">
      <button v-if="!isPlaying" @click="startClock">
        <img src="/src/assets/icons/play.svg" alt="Play"/>
      </button>
      <button v-else @click="stopClock">
        <img v-if="isPaused" src="/src/assets/icons/play.svg" alt="Play"/>
        <img v-else src="/src/assets/icons/pause.svg" alt="Pause"/>
      </button>
      <button class="ff-arrow" @click="fastForwardClock">
        <img v-if="speedIndex === 0" src="/src/assets/icons/ff1x.svg" alt="Fast Forward"/>
        <img v-else-if="speedIndex === 1" src="/src/assets/icons/ff2x.svg" alt="Fast Forward"/>
        <img v-else-if="speedIndex === 2" src="/src/assets/icons/ff5x.svg" alt="Fast Forward"/>
        <img v-else src="/src/assets/icons/ff10x.svg" alt="Fast Forward"/>
      </button>
    </div>
    <div class="vertical-separator"/>
    <button class="date-clock">
      <p>Date</p>
      <div class="clock-time">
        <span>{{ dateText }}</span>
      </div>
    </button>
    <div class="vertical-separator"/>
    <div class="clock">
      <p>Time</p>
      <div v-if="currentTime" class="clock-time">
        <span>{{ currentTime.split(':')[0] }}</span>
        <span class="blink">:</span>
        <span>{{ currentTime.split(':')[1] }}</span>
      </div>
      <div v-else class="clock-time">
        <span>00:00</span>
      </div>
    </div>
    <div class="vertical-separator"/>
    <div class="clock-done">
      <p v-if="!isFinished">Done By</p>
      <div class="clock-time">
        <span v-if="completionTime !== null ">{{ completionTime }}</span>
        <span v-else>{{ isFinished ? 'Done' : '00:00' }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: flex-end;
  background-color: #ffffff;
  align-items: center;
  padding-right: 1rem;
  height: 4rem;
  position: relative;
}

.toolbar-title {
  position: absolute;
  left: 1rem;
  font-size: 1.5rem;
  font-weight: bold;
}

.toolbar-item {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  width: 30px; /* Ensure consistent width */
  height: 30px; /* Ensure consistent height */
  margin-right: 1rem;
  color: #b77979;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-item img {
  width: 100%; /* Make the image take the full width of the button */
  height: auto; /* Maintain aspect ratio */
}

.toolbar-item:hover {
  color: #000;
}

.vertical-separator {
  border-left: 1px solid #e0e0e0;
  height: 100%;
  margin: 0 1rem;
}

.controls {
  display: flex;
  align-items: center;
}

.controls button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  width: 30px; /* Ensure consistent width */
  height: 30px; /* Ensure consistent height */
  color: #b77979;
  display: flex;
  margin: 0 0.5rem;
  align-items: center;
  justify-content: center;
}

.clock {
  display: flex;
  flex-direction: column;
  line-height: 1.5rem;
  width: 6rem;
  align-content: center;
  align-self: center;
}

.date-clock {
  display: flex;
  background: none;
  border: none;
  flex-direction: column;
  line-height: 1.5rem;
  width: 6rem;
  align-content: center;
  align-self: center;
}

.date-clock:hover {
  background-color: #f0f0f0;
  border-radius: 10px;
}

.clock-time {
  display: flex;
  justify-content: center;
}

.clock span, .date-clock span {
  color: #7B7B7B;
  font-size: 2rem;
  font-weight: bold;
}

.clock p, .date-clock p {
  color: #7B7B7B;
  font-size: 0.7rem;
  font-weight: bold;
}

.date-clock span {
  color: #7B7B7B;
  font-size: 1rem;
  font-weight: bold;
}

.clock p, .date-clock p {
  color: #7B7B7B;
  font-size: 0.7rem;
  font-weight: bold;
}

.clock-done {
  display: flex;
  flex-direction: column;
  line-height: 1.5rem;
  align-content: center;
  align-self: center;
}

.clock-done span {
  color: #d97c7c;
  font-size: 2rem;
  font-weight: bold;
}

.clock-done p {
  color: #d97c7c;
  font-size: 0.7rem;
  font-weight: bold;
}

.logo {
  display: flex;
  align-items: center;
}

.logo-icon {
  width: 50px;
  margin-right: 0.5rem;
}

.logo-text {
  padding-top: 0.3rem;
  font-size: 0.9rem;
  line-height: 1rem;
  font-weight: bold;
}

.date-text {
  color: #7B7B7B;
  font-size: 1.2rem !important;
  font-weight: bold;
}

.blink {
  animation: blink 3s linear infinite;
}

.ff-arrow img {
  width: 30px;
  height: 30px;
  margin-bottom: 0.2rem;
}

@keyframes blink {
  0% {
    opacity: 1;
  }
  20% {
    opacity: 0;
  }
  40% {
    opacity: 1;
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.spin-animation {
  animation: spin 1s ease-in-out infinite;
}
</style>