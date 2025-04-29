<script setup lang="ts">
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
} from "chart.js";
import {onMounted, onUnmounted, ref, watch} from "vue";
import {ActiveTask, WorldSimObject, Zone} from "@/assets/types";

// Register chart components
ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale);

const props = defineProps<{
  zoneId: number;
}>();

let currentTimeIndex = 0;
// Monte Carlo Simulation: Estimate Pi
const numPoints = 10000;
const insideCircle = ref(0);
const estimates = ref<number[]>([]);
const date = ref(null);

for (let i = 1; i <= numPoints; i++) {
  const x = Math.random();
  const y = Math.random();
  if (x * x + y * y <= 1) insideCircle.value++;

  estimates.value.push((4 * insideCircle.value) / i);
}

const activeTasks = ref<number>(0);

const taskCount = ref(0); // Initialize taskCount to 0

const dataValues = ref<number[]>([0]);
const isDataReady = ref(false); // Flag to track readiness

// Watch for changes in dataValues
watch(dataValues, (newValue) => {
  if (newValue.length > 1) {
    isDataReady.value = true;

    generateChartData();
  }
});

const chartData = ref();
const chartOptions = ref();

function generateChartData() {
  currentTimeIndex = dataValues.value.length - 1;
  taskCount.value = activeTasks.value;
   // Get the maximum value from dataValues
// Generate simulated values that stop increasing when reaching the task count
  const simulatedValues = [];
  let lastValue = dataValues[dataValues.value.length - 1]; // Start from the last real data value
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
  const extendedLabels = Array.from({length: 144}, (_, i) => {
    const hours = Math.floor(i / 6); // Each hour has 6 intervals of 10 minutes
    const minutes = (i % 6) * 10; // Calculate the minute mark
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
  });

  const simulatedDatasets = [];
  const baseColor = [150, 150, 150];

  for (let i = 0; i < 50; i++) {
    const simulatedValues = [];
    let lastValue = dataValues[dataValues.value.length - 1]; // Start from the last real data value
    while (lastValue < taskCount) {
      const increment = Math.random() * 3; // Random increment between 1 and 5
      lastValue += increment;
      if (lastValue > taskCount) {
        lastValue = taskCount; // Cap the value at the task count
      }
      simulatedValues.push(lastValue);
    }

    simulatedDatasets.push({
      label: `Simulated Tasks ${i + 1}`,
      data: [
        ...Array(dataValues.value.length - 1).fill(null),
        dataValues[dataValues.value.length - 1], // Match the last real value
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
  const startIndex = dataValues.value.length - 1; // Start from the last index of dataValues
  const regressionSlope = 1.7; // Example slope for the regression line
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


  chartData.value = {
    labels: extendedLabels,
    datasets: [
      {
        label: 'Tasks Done Over Time',
        data: [...dataValues.value, ...Array(144 - dataValues.value.length).fill(null)],
        borderColor: 'rgb(131,131,131)',
        tension: 0.01,
        pointRadius: 0,
        fill: false,
      },
      ...simulatedDatasets,
    ],
  };


  chartOptions.value = {
    responsive: true,
    plugins: {
      legend: {
        display: false, // Hide the legend
      },
      horizontalLine: {
        taskCount: taskCount.value, // Pass the taskCount value
      },
    },
    scales: {
      x: {},
      y: {
        beginAtZero: false,
        min: 0,
        max: taskCount.value + 20,
      },
    },
  };


  const horizontalLinePlugin = {
    id: 'horizontalLine',
    beforeDraw(chart) {
      const {ctx, chartArea: {top, bottom, left}, scales: {y}} = chart;
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
      const {ctx, chartArea: {top, bottom, left, right}, scales: {x}} = chart;

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

}

const fetchDateFromBackend = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/simulation/currentDate`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const data: string = await response.json();
    date.value = data;
  } catch (error) {
    console.error('Failed to fetch date:', error);
    date.value = ''; // Fallback to an empty string if the fetch fails
  }

  await fetchActiveTasks();
};

const fetchActiveTasks = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/zones/${props.zoneId}/${date.value}`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const data: number = await response.json();
    activeTasks.value = data;
  } catch (error) {
    console.error('Failed to fetch active tasks:', error);
  }
  await fetchRealData();
  pollingTimer = setInterval(fetchRealData, pollingInterval);
};

const pollingInterval = 5000; // Poll every 5 seconds
let pollingTimer: ReturnType<typeof setInterval> | null = null;

const fetchRealData = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/data/${props.zoneId}/values`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const data: number[] = await response.json();

    // Update dataValues and regenerate chart data
    if (data && JSON.stringify(data) !== JSON.stringify(dataValues.value)) {
      dataValues.value = data || [0];
      isDataReady.value = dataValues.value.length > 1;

      const now = new Date();
      const currentHour = now.getHours();
      const currentMinute = now.getMinutes();
      currentTimeIndex = currentHour * 6 + Math.floor(currentMinute / 10); // Calculate the index based on time

      generateChartData(); // Regenerate chart data
    }
  } catch (error) {
    console.error('Failed to fetch real data:', error);
  }
};


onMounted( () => {
   fetchDateFromBackend();
});

onUnmounted(() => {
  if (pollingTimer) {
    clearInterval(pollingTimer);
  }
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
    <div v-if="isDataReady" class="monte-carlo-graph">
      <Line :data="chartData" :options="chartOptions"/>
    </div>
    <div v-else>
      <p>Loading data...</p>
    </div>
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