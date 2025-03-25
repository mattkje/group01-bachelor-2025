<script setup lang="ts">
import { onMounted, ref, inject, computed } from "vue";

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

const simulatedTime = ref(new Date());
const currentTime = computed(() => {
  return simulatedTime.value.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', hour12: false });
});

const updateSimulatedTime = () => {
  simulatedTime.value = new Date(simulatedTime.value.getTime() + 60000); // Advance by 1 minute
};

let intervalId: number | null = null;

const startClock = () => {
  isPlaying.value = true;
  if (!intervalId) {
    intervalId = setInterval(updateSimulatedTime, 1000); // Update every second
  }
};

const stopClock = () => {
  isPlaying.value = false;
  if (intervalId) {
    clearInterval(intervalId);
    intervalId = null;
  }
};

let speedIndex = 0;
const speeds = [1000, 500, 333, 200]; // Corresponding to 1x, 2x, 3x, 5x speeds

const fastForwardClock = () => {
  if (intervalId) {
    clearInterval(intervalId);
  }
  speedIndex = (speedIndex + 1) % speeds.length;
  intervalId = setInterval(updateSimulatedTime, speeds[speedIndex]);
};

const runAllMonteCarloSimulations = async () => {
  isSpinning.value = true;
  try {
    const response = await fetch(`http://localhost:8080/api/monte-carlo/zones/1`, {
      method: 'GET',
    });

    if (!response.ok) {
      throw new Error('Failed to run simulation');
    }
    const result = await response.json();
    console.log(result);
    if (result.length < 2) {
      completionTime = result[0];
    } else {
      notification.value = true;
      let notifications: string[] = result;
      notificationMessage.value = notifications;
    }
    emit('refreshWorkers');
  } catch (error) {
    console.error('Error running simulation:', error);
  } finally {
    // wait for 1 second to show the spinning animation
    await new Promise(resolve => setTimeout(resolve, 1000));
    isSpinning.value = false;
  }
};

const isFinished = computed(() => {
  if (completionTime.value === null) {
    return false;
  }
  const [hours, minutes] = completionTime.value.split(':').map(Number);
  const [currentHours, currentMinutes] = currentTime.value.split(':').map(Number);

  return hours < currentHours || (hours === currentHours && minutes < currentMinutes);
});

onMounted(() => {
  startClock();
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
      <img :class="{ 'spin-animation': isSpinning }" src="/src/assets/icons/simulation.svg" alt="Assign" />
    </button>
    <button v-if="false" class="toolbar-item">
      <img src="/src/assets/icons/bell.svg" alt="Assign" />
    </button>
    <button v-if="true" class="toolbar-item">
      <img src="/src/assets/icons/bellUpdate.svg" alt="Assign" />
    </button>
    <div class="vertical-separator"/>
    <div class="controls">
      <button v-if="!isPlaying" @click="startClock">
        <img src="/src/assets/icons/play.svg" alt="Play" />
      </button>
      <button v-else @click="stopClock">
        <img src="/src/assets/icons/pause.svg" alt="Pause" />
      </button>
      <button @click="fastForwardClock">
        <img src="/src/assets/icons/skip.svg" alt="Fast Forward" />
      </button>
    </div>
    <div class="vertical-separator"/>
    <div class="clock">
      <p>Time</p>
      <span>{{ currentTime.split(':')[0] }}</span>
      <span class="blink">:</span>
      <span>{{ currentTime.split(':')[1] }}</span>
    </div>
    <div class="vertical-separator"/>
    <div class="clock-done">
      <p>Done By</p>
      <span v-if="completionTime !== null ">{{ completionTime }}</span>
      <span v-if="isFinished">Finished</span>
      <span v-else>Error</span>
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
  border-bottom: 1px solid #e0e0e0;
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
  line-height: 1.5rem;
  align-self: center;
}

.clock span {
  color: #7B7B7B;
  font-size: 2rem;
  font-weight: bold;
}

.clock p {
  color: #7B7B7B;
  font-size: 0.7rem;
  font-weight: bold;
}

.clock-done {
  line-height: 1.5rem;
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

.blink {
  animation: blink 3s linear infinite;
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