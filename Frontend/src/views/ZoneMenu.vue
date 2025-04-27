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
const currentTimeIndex = dataValues.length - 1;



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
  const nextHour = (currentTimeIndex + i) % 24;
  extendedLabels.push(`${nextHour === 0 ? 12 : nextHour % 12}${nextHour < 12 ? 'am' : 'pm'}`);
}

const simulatedDatasets = [];
const baseColor = [150, 150, 150];

for (let i = 0; i < 20; i++) {
  const simulatedValues = [];
  let lastValue = dataValues[dataValues.length - 1]; // Start from the last real data value
  for (let j = 0; j < 40; j++) {
    const increment = Math.random() * 5 + 1; // Random increment between 1 and 5
    lastValue += increment;
    simulatedValues.push(lastValue);
  }

  simulatedDatasets.push({
    label: `Simulated Tasks ${i + 1}`,
    data: [
      ...Array(dataValues.length - 1).fill(null),
      dataValues[dataValues.length - 1], // Match the last real value
      ...simulatedValues
    ],
    borderColor: `rgba(${baseColor[0]}, ${baseColor[1]}, ${baseColor[2]}, 0.2)`, // Semi-transparent color
    tension: 0.1,
    pointRadius: 0,
    fill: false,
  });
}

let bestCaseIndex = 0;
let worstCaseIndex = 0;
let bestCaseValue = -Infinity; // Initialize to a very low value for best case
let worstCaseValue = Infinity; // Initialize to a very high value for worst case

simulatedDatasets.forEach((dataset, index) => {
  const maxValue = Math.max(...dataset.data.filter((value) => value !== null));
  if (maxValue > bestCaseValue) { // Best case is the highest value
    bestCaseValue = maxValue;
    bestCaseIndex = index;
  }
  if (maxValue < worstCaseValue) { // Worst case is the lowest value
    worstCaseValue = maxValue;
    worstCaseIndex = index;
  }
});

// Apply distinct styles to best and worst cases
simulatedDatasets[bestCaseIndex].borderColor = 'rgb(98,183,127)'; // Green for best case
simulatedDatasets[bestCaseIndex].borderWidth = 3;

simulatedDatasets[worstCaseIndex].borderColor = 'rgba(255, 99, 132, 1)'; // Red for worst case
simulatedDatasets[worstCaseIndex].borderWidth = 3;


const data = {
  labels: extendedLabels,
  datasets: [
    {
      label: 'Tasks Done Over Time',
      data: [...dataValues, ...Array(50).fill(null)], // Original data
      borderColor: 'rgb(131,131,131)',
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
      max: Math.max(...simulatedValues) + 100 ,
    },
  },
};

const horizontalLinePlugin = {
  id: 'horizontalLine',
  beforeDraw(chart) {
    const { ctx, chartArea: { top, bottom, left }, scales: { y } } = chart;
    const taskCountValue = chart.config.options.plugins.horizontalLine.taskCount;

    if (taskCountValue !== undefined) {
      const yPosition = y.getPixelForValue(taskCountValue);

      ctx.save();
      ctx.beginPath();
      ctx.moveTo(left, yPosition);
      ctx.lineTo(chart.chartArea.right, yPosition);
      ctx.strokeStyle = 'rgba(255,164,164,0.8)'; // Red color for the line
      ctx.lineWidth = 2;
      ctx.stroke();

      ctx.setLineDash([]); // Reset line dash for text
      ctx.font = '12px Arial';
      ctx.fillStyle = 'rgba(255,164,164,0.8)'; // Text color
      ctx.textAlign = 'center'; // Center text horizontally
      ctx.fillText('Tasks', left - 20, yPosition + 4); // Position text on the y-axis
      ctx.restore();
    }
  },
};

const verticalLinePlugin = {
  id: 'verticalLine',
  beforeDraw(chart) {
    const { ctx, chartArea: { top, bottom, left, right }, scales: { x } } = chart;

    // Calculate the x position for the current time
    const xPosition = x.getPixelForValue(currentTimeIndex);

    if (xPosition >= left && xPosition <= right) {
      ctx.save();
      ctx.beginPath();
      ctx.setLineDash([5, 5]); // Set the line to be dashed (5px dash, 5px gap)
      ctx.moveTo(xPosition, top);
      ctx.lineTo(xPosition, bottom);
      ctx.strokeStyle = 'rgba(121,121,121,0.8)'; // Line color
      ctx.lineWidth = 2;
      ctx.stroke();

      ctx.setLineDash([]); // Reset line dash for text
      ctx.font = '12px Arial';
      ctx.fillStyle = 'rgba(121,121,121,0.8)'; // Text color
      ctx.textAlign = 'center';
      ctx.fillText('Now', xPosition, top - 1); // Position the label slightly above the line
      ctx.restore();
    }
  },
};

ChartJS.register(horizontalLinePlugin, verticalLinePlugin);

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
  <div class="container-container">
  <div class="container" v-if="currentZone">
    <div class="zone-info">
      <div class="zone-menu">
        <h1>Management Dashboard</h1>
        <h2>Zone: {{ currentZone.name }}</h2>
        <p>Number of Workers: {{ currentZone.workers.length }}</p>
        <p>Tasks: {{ currentZone.tasks.length }}</p>
        <button @click="$emit('runSimulation', currentZone.id)">Run Simulation</button>
      </div>
      <div class="workers-container">
        <h3>Workers</h3>
        <div class="workers">
        <div class="worker" v-for="worker in currentZone.workers" :key="worker.id">
          {{ worker.name }}
        </div>
        </div>
      </div>
    </div>
    <div class="vertical-separator"/>
    <div class="monte-carlo-graph">
      <h3>Monte Carlo Simulation Graph</h3>
      <div class="p-4">
        <LineChart :data="data" :options="chartOptions"/>
      </div>
    </div>
  </div>
  <hr>
  <ZoneTasks v-if="currentZone" class="zone-tasks" :zone="currentZone" :tasks="currentZone.tasks" />
  </div>
</template>

<style scoped>
.container-container {
  height: 100vh;
}

.container {
  width: 100%;
  margin: 0;
  padding: 0 20px;
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
  padding: 0 20px;
}

.workers {
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  height: 150px;
  overflow-y: auto;
}

.worker {
  padding: 10px;
  background-color: #f1f1f1;
  height: 45px;
  border-radius: 4px;
  text-align: center;
}

.monte-carlo-graph {
  width: 100%;
  height: 100%;
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

hr {
  margin: 0;
  border: none;
  border-top: 1px solid #e0e0e0;
}

.vertical-separator {
  border-left: 1px solid #e0e0e0;
  height: auto; /* Adjust height dynamically */
  margin: 0;
  align-self: stretch; /* Ensure it stretches to match sibling height */
}

.zone-tasks {
  margin-top: 1.5rem;
}
</style>