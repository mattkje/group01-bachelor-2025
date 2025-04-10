<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {Zone} from "@/assets/types";
import {useRoute} from "vue-router";
import {Line} from "vue-chartjs";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale,
} from 'chart.js';

import ZoneTasks from "@/components/zones/ZoneTasks.vue";


// Register chart components
ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale);

// Components
const LineChart = Line;

// Monte Carlo Simulation: Estimate Pi
const numPoints = 10000;
const insideCircle = ref(0);
const estimates = ref<number[]>([]);

for (let i = 1; i <= numPoints; i++) {
  const x = Math.random();
  const y = Math.random();
  if (x * x + y * y <= 1) insideCircle.value++;

  estimates.value.push((4 * insideCircle.value) / i);
}
const labels = Array.from({length: 24}, (_, i) => `${i === 0 ? 12 : i % 12}${i < 12 ? 'am' : 'pm'}`);
const dataValues = [0, 5, 12, 15, 20, 25, 30, 35, 36, 40, 45];
const dummyClockTimeNow = 0; // 0 - 24



// Generate simulated values that always increase
const simulatedValues = [];
let lastValue = dataValues[dataValues.length - 1]; // Start from the last real data value
for (let i = 0; i < 30; i++) {
  const increment = Math.random() * 5 + 1; // Random increment between 1 and 5
  lastValue += increment;
  simulatedValues.push(lastValue);
}

const taskCount = Math.max(...simulatedValues);

// Extend labels for the simulated hours
const extendedLabels = [...labels];
for (let i = 1; i <= simulatedValues.length; i++) {
  const nextHour = (dummyClockTimeNow + i) % 24;
  extendedLabels.push(`${nextHour === 0 ? 12 : nextHour % 12}${nextHour < 12 ? 'am' : 'pm'}`);
}

const simulatedDatasets = [];
const baseColor = [150, 150, 150];

for (let i = 0; i < 20; i++) {
  const simulatedValues = [];
  let lastValue = dataValues[dataValues.length - 1]; // Start from the last real data value
  for (let j = 0; j < 60; j++) {
    const increment = Math.random() * 5 + 1; // Random increment between 1 and 5
    lastValue += increment;
    simulatedValues.push(lastValue);
  }

  simulatedDatasets.push({
    label: `Simulated Tasks ${i + 1}`,
    data: [...Array(dataValues.length).fill(null), ...simulatedValues], // Simulated data
    borderColor: `rgba(${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, 0.5)`,
    tension: 0.1,
    borderDash: [3, 0.4],
    pointRadius: 0,
    fill: false,
  });
}

const data = {
  labels: extendedLabels,
  datasets: [
    {
      label: 'Tasks Done Over Time',
      data: [...dataValues, ...Array(50).fill(null)], // Original data
      borderColor: 'rgba(255, 99, 132, 1)',
      tension: 0.1,
      pointRadius: 0,
      fill: false,
    },
    ...simulatedDatasets, // Add all simulated datasets
  ],
};

const chartOptions = {
  responsive: true,
  plugins: {
    legend: {
      display: false, // Hide the legend
    },
    horizontalLine: {
      taskCount: taskCount, // Pass the taskCount value
    },
  },
  scales: {
    x: {},
    y: {
      beginAtZero: false,
      min: 0,
      max: Math.max(...simulatedValues) + 50 ,
    },
  },
};

const horizontalLinePlugin = {
  id: 'horizontalLine',
  beforeDraw(chart) {
    const { ctx, chartArea: { top, bottom, left, right }, scales: { y } } = chart;
    const taskCountValue = chart.config.options.plugins.horizontalLine.taskCount;

    if (taskCountValue !== undefined) {
      const yPosition = y.getPixelForValue(taskCountValue);

      ctx.save();
      ctx.beginPath();
      ctx.moveTo(left, yPosition);
      ctx.lineTo(right, yPosition);
      ctx.strokeStyle = 'rgba(255,164,164,0.8)'; // Red color for the line
      ctx.lineWidth = 2;
      ctx.stroke();
      ctx.restore();
    }
  },
};

ChartJS.register(horizontalLinePlugin);

let currentZone = ref<Zone | null>(null);
const route = useRoute();

const fetchZone = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${route.params.id}`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const data: Zone = await response.json();
    currentZone.value = data;
  } catch (error) {
    console.error('Failed to fetch zone:', error);
  }
};

onMounted(() => {
  fetchZone();
});
</script>

<template>
  <div class="container" v-if="currentZone">
    <div class="zone-info">
      <div class="zone-menu">
        <h2>{{ currentZone.name }}</h2>
        <p>Zone ID: {{ currentZone.id }}</p>
        <p>Number of Workers: {{ currentZone.workers.length }}</p>
        <p>Tasks: {{ currentZone.tasks.length }}</p>
        <button @click="$emit('runSimulation', currentZone.id)">Run Simulation</button>
      </div>
      <div class="workers">
        <h3>Workers</h3>
        <ul>
          <li v-for="worker in currentZone.workers" :key="worker.id">{{ worker.name }}</li>
        </ul>
      </div>
    </div>
    <div class="monte-carlo-graph">
      <h3>Monte Carlo Simulation Graph</h3>
      <div class="p-4">
        <LineChart :data="data" :options="chartOptions"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 100%;
  margin: 0;
  padding: 20px;
  display: flex;
  flex-direction: row;
  gap: 20px;
}

.zone-menu {
  padding: 20px;
  border-radius: 8px;
}

.zone-menu h2 {
  margin-bottom: 10px;
}

.zone-menu p {
  margin: 5px 0;
}

.zone-menu button {
  margin-top: 10px;
  padding: 10px 15px;
  background-color: #E77474;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.zone-menu button:hover {
  background-color: #9d4d4d;
}

.zone-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}

.workers {
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
}

.workers h3 {
  margin-bottom: 10px;
}

.workers ul {
  list-style: none;
  padding: 0;
}

.workers li {
  margin: 5px 0;
  padding: 5px;
  background-color: #f1f1f1;
  border-radius: 4px;
}

.monte-carlo-graph {
  width: 100%;
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
}

.monte-carlo-graph h3 {
  margin-bottom: 10px;
}

canvas {
  width: 100%;
}

</style>