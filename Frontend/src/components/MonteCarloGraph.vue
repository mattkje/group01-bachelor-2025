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
import {fetchAllMonteCarloGraphData} from "@/composables/DataFetcher";

ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale);

const props = defineProps<{
  zoneId: number;
}>();

let currentTimeIndex = 0;
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
const taskCount = ref(0);
const dataValues = ref<number[]>([0]);
const ListOfListsOfValues = ref<number[][]>([]);
const simulatedDatasets = ref<any[]>([]);
const isDataReady = ref(false);

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
  simulatedDatasets.value = [];

  const baseColor = 'rgba(150, 150, 150, 0.3)';

  ListOfListsOfValues.value.forEach((list, index) => {
    simulatedDatasets.value.push({
      label: `Case ${index + 1}`,
      data: [...Array(dataValues.value.length - 1).fill(null), ...list],
      borderColor: baseColor,
      tension: 0.1,
      pointRadius: 0,
      borderWidth: 1.5,
      fill: false,
    });
  });

  let bestCaseIndex = -1;
  let worstCaseIndex = -1;
  let bestCaseValue = -Infinity;
  let worstCaseValue = Infinity;

  ListOfListsOfValues.value.forEach((list, index) => {
    const lastValue = list[list.length - 1] || 0;
    const listLength = list.length;

    // Determine best case
    if (
        lastValue > bestCaseValue ||
        (lastValue === bestCaseValue && listLength < ListOfListsOfValues.value[bestCaseIndex]?.length)
    ) {
      bestCaseValue = lastValue;
      bestCaseIndex = index;
    }

    // Determine worst case
    if (
        lastValue < worstCaseValue ||
        (lastValue === worstCaseValue && listLength > ListOfListsOfValues.value[worstCaseIndex]?.length)
    ) {
      worstCaseValue = lastValue;
      worstCaseIndex = index;
    }
  });

  const lastDataValue = dataValues.value[dataValues.value.length - 1] || 0;

  let bestCaseValueList = [];
  if (bestCaseIndex !== -1) {
    const increment = (bestCaseValue - lastDataValue) / ListOfListsOfValues.value[bestCaseIndex].length;
    let cumulativeValue = lastDataValue;
    bestCaseValueList = ListOfListsOfValues.value[bestCaseIndex].map(() => {
      cumulativeValue += increment;
      return cumulativeValue;
    });
  }

  let worstCaseValueList = [];
  if (worstCaseIndex !== -1) {
    const increment = (worstCaseValue - lastDataValue) / ListOfListsOfValues.value[worstCaseIndex].length;
    let cumulativeValue = lastDataValue;
    worstCaseValueList = ListOfListsOfValues.value[worstCaseIndex].map(() => {
      cumulativeValue += increment;
      return cumulativeValue;
    });
  }


  // Add a new line for the best case
  if (bestCaseIndex !== -1) {
    simulatedDatasets.value.push({
      label: 'Best Case Line',
      data: [null, ...Array(dataValues.value.length - 2).fill(null), lastDataValue, ...bestCaseValueList],
      borderColor: 'rgb(126,196,177)',
      borderWidth: 2,
      tension: 0,
      pointRadius: 0,
      fill: false,
    });
  }

  // Add a new line for the worst case
  if (worstCaseIndex !== -1) {
    simulatedDatasets.value.push({
      label: 'Worst Case Line',
      data: [null, ...Array(dataValues.value.length - 2).fill(null), lastDataValue, ...worstCaseValueList],
      borderColor: 'rgba(255, 99, 132, 1)',
      borderWidth: 2,
      tension: 0,
      pointRadius: 0,
      fill: false,
    });
  }
  const extendedLabels = Array.from({length: 144}, (_, i) => {
    const hours = Math.floor(i / 6);
    const minutes = (i % 6) * 10;
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
  });

  const regressionData = [];
  const startIndex = dataValues.value.length - 1;
  const regressionSlope = 1.7;
  const regressionIntercept = dataValues[startIndex];


  const regressionLine = {
    label: 'Regression Line',
    data: [...Array(startIndex).fill(null), ...regressionData],
    borderColor: 'rgb(126, 161, 196)',
    tension: 0.1,
    pointRadius: 0,
    fill: false,
  };

  simulatedDatasets.value.push(regressionLine);

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
      ...simulatedDatasets.value,
    ],
  };

  chartOptions.value = {
    responsive: true,
    plugins: {
      legend: {
        display: false,
      },
      horizontalLine: {
        taskCount: taskCount.value,
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
        ctx.strokeStyle = 'rgba(255,164,164,0.8)';
        ctx.lineWidth = 2;
        ctx.stroke();

        ctx.setLineDash([]);
        ctx.font = '12px Arial';
        ctx.fillStyle = 'rgba(255,164,164,0.8)';
        ctx.textAlign = 'center';
        ctx.fillText('Tasks', left - 20, yPosition + 4);
        ctx.restore();
      }
    },
  };

  const verticalLinePlugin = {
    id: 'verticalLine',
    beforeDraw(chart) {
      const {ctx, chartArea: {top, bottom, left, right}, scales: {x}} = chart;

      const xPosition = x.getPixelForValue(currentTimeIndex);

      if (xPosition >= left && xPosition <= right) {
        ctx.save();
        ctx.beginPath();
        ctx.setLineDash([5, 5]);
        ctx.moveTo(xPosition, top);
        ctx.lineTo(xPosition, bottom);
        ctx.strokeStyle = 'rgba(121,121,121,0.8)';
        ctx.lineWidth = 2;
        ctx.stroke();

        ctx.setLineDash([]);
        ctx.font = '12px Arial';
        ctx.fillStyle = 'rgba(121,121,121,0.8)';
        ctx.textAlign = 'center';
        ctx.fillText('Now', xPosition, top - 1);
        ctx.restore();
      }
    },
  };
  ChartJS.register(horizontalLinePlugin, verticalLinePlugin);
}

const pollingInterval = 5000;

const fetchData = async () => {
  try {
    const data = await fetchAllMonteCarloGraphData(props.zoneId);
    dataValues.value = data.realData;
    ListOfListsOfValues.value = data.simulationData;
    activeTasks.value = data.activeTasks;
    date.value = data.currentDate;

    isDataReady.value = dataValues.value.length > 1 && ListOfListsOfValues.value.length > 1;
  } catch (error) {
    console.error("Error fetching data:", error);
  }
};

let pollingTimer: ReturnType<typeof setInterval> | null = null;

function startPolling(interval: number) {
  if (pollingTimer) {
    clearInterval(pollingTimer);
  }
  pollingTimer = setInterval(fetchData, interval);
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer);
    pollingTimer = null;
  }
}

onMounted(() => {
  startPolling(pollingInterval);
});

onUnmounted(() => {
  stopPolling();
});
</script>

<template>
  <div class="monte-carlo-graph">

    <div v-if="isDataReady" class="monte-carlo-graph">
      <Line :data="chartData" :options="chartOptions"/>
    </div>
    <div v-else>
      <p>Loading data...</p>
    </div>
    <div class="color-indicator-container">
      <p>
        <span class="color-indicator best-case"></span> Best Case
      </p>
      <p>
        <span class="color-indicator probable-case"></span> Probable Case
      </p>
      <p>
        <span class="color-indicator worst-case"></span> Worst Case
      </p>
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
  border-radius: 8px;
}

canvas {
  width: 100%;
}

.color-indicator-container {
  display: flex;
  justify-content: flex-start;
  gap: 2rem;
}
</style>