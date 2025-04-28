<script setup lang="ts">
import { Line } from "vue-chartjs";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale,
} from "chart.js";
import {onMounted, ref} from "vue";
import {Zone} from "@/assets/types";

// Register chart components
ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale);

const props = defineProps<{
  zoneId: number;
}>();

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
const dataValues = [0, 20, 30, 45, 50, 65, 65, 75];
const currentTimeIndex = dataValues.length - 1;


const taskCount = 190; // Random task count between last value and last value + 100

// Generate simulated values that stop increasing when reaching the task count
const simulatedValues = [];
let lastValue = dataValues[dataValues.length - 1]; // Start from the last real data value
for (let i = 0; i < 30; i++) {
  if (lastValue >= taskCount) {
    simulatedValues.push(taskCount); // Ensure the value doesn't exceed the task count
  } else {
    const increment = Math.random() * 9 + 1; // Random increment between 1 and 5
    lastValue += increment;
    if (lastValue > taskCount) {
      lastValue = taskCount; // Cap the value at the task count
    }
    simulatedValues.push(lastValue);
  }
}

// Extend labels for the simulated hours
// Extend labels for the simulated hours, ensuring only 24 hours are included
const extendedLabels = Array.from({ length: 24 }, (_, i) => `${i === 0 ? 12 : i % 12}${i < 12 ? 'am' : 'pm'}`);

const simulatedDatasets = [];
const baseColor = [150, 150, 150];

for (let i = 0; i < 50; i++) {
  const simulatedValues = [];
  let lastValue = dataValues[dataValues.length - 1]; // Start from the last real data value
  while (lastValue < taskCount) {
    const increment = Math.random() * 20 + 1; // Random increment between 1 and 5
    lastValue += increment;
    if (lastValue > taskCount) {
      lastValue = taskCount; // Cap the value at the task count
    }
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

let bestCaseIndex = -1;
let worstCaseIndex = -1;
let bestCaseValue = Infinity; // Start with the smallest possible index
let worstCaseValue = -Infinity; // Start with the largest possible index

simulatedDatasets.forEach((dataset, index) => {
  const firstCompletionIndex = dataset.data.findIndex((value) => value >= taskCount);

  if (firstCompletionIndex !== -1) {
    // Update best case (earliest completion)
    if (firstCompletionIndex < bestCaseValue) {
      bestCaseValue = firstCompletionIndex;
      bestCaseIndex = index;
    }
    // Update worst case (latest completion)
    if (firstCompletionIndex > worstCaseValue) {
      worstCaseValue = firstCompletionIndex;
      worstCaseIndex = index;
    }
  }
});

// Apply distinct styles to best and worst cases
if (bestCaseIndex !== -1) {
  simulatedDatasets[bestCaseIndex].borderColor = 'rgb(126,196,177)'; // Green for best case
  simulatedDatasets[bestCaseIndex].borderWidth = 3;
}

if (worstCaseIndex !== -1) {
  simulatedDatasets[worstCaseIndex].borderColor = 'rgba(255, 99, 132, 1)'; // Red for worst case
  simulatedDatasets[worstCaseIndex].borderWidth = 3;
}

// Calculate regression data starting from the end of dataValues
const regressionData = [];
const startIndex = dataValues.length - 1; // Start from the last index of dataValues
const regressionSlope = 10; // Example slope for the regression line
const regressionIntercept = dataValues[startIndex];

for (let i = 0; i < simulatedValues.length; i++) {
  const y = regressionSlope * i + regressionIntercept;
  regressionData.push(y);
}

// Add regression line dataset
const regressionLine = {
  label: 'Regression Line',
  data: [...Array(startIndex).fill(null), ...regressionData], // Align with simulated data
  borderColor: 'rgb(126, 161, 196)',
  tension: 0.1,
  pointRadius: 0,
  fill: false,
};

simulatedDatasets.push(regressionLine); // Add regression line to datasets



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

const fetchZone = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${props.zoneId}`);
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
  <div class="monte-carlo-graph">
    <p>
      <span class="color-indicator best-case"></span> Best Case:
    </p>
    <p>
      <span class="color-indicator probable-case"></span> Probable Case:
    </p>
    <p>
      <span class="color-indicator worst-case"></span> Worst Case:
    </p>
    <Line :data="data" :options="chartOptions" />
  </div>
</template>

<style scoped>
.color-indicator {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 5px;
}

.best-case {
  background-color: rgb(126, 196, 177); /* Green */
}

.probable-case {
  background-color: rgb(126, 161, 196);
}

.worst-case {
  background-color: rgba(255, 99, 132, 1); /* Red */
}

.monte-carlo-graph {
  width: 100%;
  height: 100%;
  background-color: #ffffff;
  padding: 20px;
  border-radius: 8px;
}

canvas {
  width: 100%;
}
</style>