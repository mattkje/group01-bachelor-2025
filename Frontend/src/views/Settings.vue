<script setup lang="ts">
import {onMounted, ref, watch} from 'vue';
import {resetToInitialState} from "@/composables/SimulationCommands";
import axios from "axios";
import {setIntervalId, setPredictionStatus} from "@/composables/DataUpdater";
import {fetchIntervalId, fetchPredictionStatus, fetchSimulationCount} from "@/composables/DataFetcher";

const settings = ref({
  notification: true,
  theme: 'light',
  language: 'en',
});


const simCount = ref(10);
const intervals = [
  { id: 0, label: "10 min" },
  { id: 1, label: "30 min" },
  { id: 2, label: "60 min" },
];
const selectedIntervalId = ref(0);
const predictions = ref(true);

async function updateSimCount() {
  try {
    await axios.post('http://localhost:8080/api/setSimCount', null, {
      params: { simCount: simCount.value },
    });
    console.log('SIM_COUNT updated successfully');
  } catch (error) {
    console.error('Error updating SIM_COUNT:', error);
  }
}

async function updatePrediction() {
  await setPredictionStatus(predictions.value);
}

watch(simCount, (newValue, oldValue) => {
  if (newValue < 1) {
    simCount.value = 1;
    console.log("simCount cannot be less than 1. Resetting to 1.");
    return;
  }
  console.log(`simCount changed from ${oldValue} to ${newValue}`);
  updateSimCount();
});

watch(predictions, (newValue, oldValue) => {
  if (newValue === oldValue) {
    return;
  }
  updatePrediction();
})

const resetSimulations = async () => {
  await resetToInitialState();
  window.location.reload();
};

const updateSimulationInterval = async () => {
  await setIntervalId(selectedIntervalId.value);
};

watch(selectedIntervalId, (newValue, oldValue) => {
  console.log(`Simulation interval changed from ${oldValue} to ${newValue}`);
  updateSimulationInterval();
});


onMounted(async () => {
  simCount.value = await fetchSimulationCount();
  predictions.value = await fetchPredictionStatus();
  selectedIntervalId.value = await fetchIntervalId();
});
</script>

<template>
  <div class="settings">
    <h1>Settings</h1>
    <div>
      <div class="sim-interval-container">
        <label for="sim-interval">Simulation Interval</label>
        <div class="input-with-text">
          <select
              id="sim-interval"
              v-model="selectedIntervalId"
              class="sim-interval-input"
          >
            <option v-for="interval in intervals" :key="interval.id" :value="interval.id">
              {{ interval.label }}
            </option>
          </select>
          <span class="explanation-text">Defines the time gap between simulations.</span>
        </div>
      </div>
      <div class="sim-count-container">
        <label for="sim-count">Simulation Count</label>
        <div class="input-with-text">
          <input
              id="sim-count"
              type="number"
              v-model="simCount"
              class="sim-count-input"
              min="1"
          />
          <span class="explanation-text">Specifies the number of simulations to run.</span>
        </div>
      </div>
      <div class="disable-predictions-container">
        <label for="disable-predictions">Predictions</label>
        <div class="input-with-text">
          <label class="switch">
            <input
                id="disable-predictions"
                type="checkbox"
                v-model="predictions"
                @change="updatePrediction"
            />
            <span class="slider"></span>
          </label>
          <span class="explanation-text">Run World simulations with monte-carlo predictions.</span>
        </div>
      </div>
      <button type="button" class="reset-simulations" @click="resetSimulations">
        Restore Initial State
        <span class="tooltip">This will reset all data to the initial state</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.settings {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 2rem;
  margin: 0 auto;
}

h1 {
  font-size: 1.8rem;
  color: var(--text-1);
  text-align: center;
  margin-bottom: 1.5rem;
}

.sim-interval-container,
.sim-count-container {
  margin-bottom: 1.5rem;
}

.disable-predictions-container {
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

label {
  display: block;
  font-size: 0.9rem;
  color: var(--text-2);
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.sim-interval-input,
.sim-count-input,
.disable-predictions-switch {
  width: 100%;
  padding: 0.5rem;
  font-size: 1rem;
  border: 1px solid var(--border-1);
  border-radius: 4px;
  background-color: var(--background-2);
  color: var(--text-1);
  transition: border-color 0.3s;
}

.sim-interval-input::-webkit-inner-spin-button,
.sim-count-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  appearance: none;
  background-color: var(--background-2);
  color: var(--text-1);
  border-radius: 4px;
}

.sim-interval-input:focus::-webkit-inner-spin-button,
.sim-count-input:focus::-webkit-inner-spin-button {
  background-color: var(--main-color);
  color: var(--background-1);
}

.sim-interval-input:focus,
.sim-count-input:focus {
  border-color: var(--main-color);
  border-width: 2px;
  outline: none;
}

button {
  display: inline-block;
  padding: 0.7rem 1.5rem;
  font-size: 1rem;
  color: var(--text-inverse);
  background-color: var(--main-color);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: var(--main-color-2);
}

.reset-simulations {
  position: relative;
  margin-top: 1rem;
}

.tooltip {
  width: 150px;
  visibility: hidden;
  background-color: var(--background-2);
  color: var(--text-1);
  text-align: center;
  border-radius: 5px;
  padding: 0.5rem;
  position: absolute;
  bottom: 150%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
  opacity: 0;
  transition: opacity 0.3s;
}

.reset-simulations:hover .tooltip,
.disable-predictions-container:hover .tooltip {
  visibility: visible;
  opacity: 1;
}

.input-with-text {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.explanation-text {
  font-size: 0.8rem;
  color: var(--text-2);
}

.switch {
  position: relative;
  display: inline-block;
  width: 34px;
  height: 20px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--border-1);
  transition: 0.4s;
  border-radius: 20px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 14px;
  width: 14px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: var(--main-color);
}

input:checked + .slider:before {
  transform: translateX(14px);
}
</style>