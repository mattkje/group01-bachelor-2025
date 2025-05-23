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
import {fetchAllMonteCarloGraphData} from "@/services/DataFetcher";

ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale);

const props = defineProps<{
  zoneId: number;
}>();

const emit = defineEmits<{
  (event: "updateTopPoint", value: number): void;
  (event: "updateWorstPoint", value: number): void;
}>();

let currentTimeIndex = ref<number>(0);
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

const extendedLabels = Array.from({length: 144}, (_, i) => {
  const hours = Math.floor(i / 6);
  const minutes = (i % 6) * 10;
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
});


const chartData = ref(
    {
      labels: extendedLabels,
      datasets: [
        {
          label: 'Tasks Done Over Time',
          data: [],
          borderColor: 'rgb(131,131,131)',
          tension: 0.01,
          pointRadius: 0,
          fill: false,
        },
      ],
    } as any
);
const chartOptions = ref();
chartOptions.value = {
  responsive: true,
  plugins: {
    legend: {
      display: false,
    },
    horizontalLine: {
      taskCount: taskCount.value,
    },
    verticalLine: {
      currentTimeIndex: currentTimeIndex.value,
    },
  },
  scales: {
    x: {},
    y: {
      beginAtZero: false,
      min: 0,

    },
  },
};

const removeFirstValues = (list: number[], numToRemove: number) => {
  if (numToRemove > list.length) {
    console.error("Cannot remove more elements than the list contains.");
    return;
  }
  for (let i = 0; i < numToRemove; i++) {
    list.shift();
  }
}

function generateChartData() {
  currentTimeIndex.value = dataValues.value.length - 1;
  console.log(currentTimeIndex.value);
  taskCount.value = activeTasks.value;
  simulatedDatasets.value = [];

  const baseColor = 'rgba(150, 150, 150, 0.2)';

  const maxLength = 143;

  // Remove first values from dataValues if they are the same as the last value
  ListOfListsOfValues.value.forEach((monteCarloSimulationList, index) => {
    if (monteCarloSimulationList.length + dataValues.value.length > maxLength) {
      removeFirstValues(monteCarloSimulationList, monteCarloSimulationList.length + dataValues.value.length - maxLength);
      // Add last value of dataValues to be the first value of the simulation list
      monteCarloSimulationList.unshift(dataValues.value[dataValues.value.length - 1]);
    }

    // Remove last values from the monteCarloSimulationList if they are the same as the last value
    while (
        monteCarloSimulationList.length > 1 &&
        monteCarloSimulationList[monteCarloSimulationList.length - 1] === monteCarloSimulationList[monteCarloSimulationList.length - 2]
        ) {
      monteCarloSimulationList.pop();
    }

    simulatedDatasets.value.push({
      label: `Case ${index + 1}`,
      data: [...Array(dataValues.value.length - 1).fill(null), ...monteCarloSimulationList],
      borderColor: baseColor,
      tension: 0.1,
      pointRadius: 0,
      borderWidth: 1,
      borderDash: [4, 2],
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
  let topPointXValue = 0;
  let worstPointXValue = 0;

  let bestCaseValueList = [];
  if (bestCaseIndex !== -1) {
    const increment = (bestCaseValue - lastDataValue) / ListOfListsOfValues.value[bestCaseIndex].length;
    let cumulativeValue = lastDataValue;
    bestCaseValueList = ListOfListsOfValues.value[bestCaseIndex].map(() => {
      cumulativeValue += increment;
      return cumulativeValue;
    });
  }
  if (bestCaseValueList.length > 0) {
    const maxIndex = bestCaseValueList.indexOf(Math.max(...bestCaseValueList));
    topPointXValue = dataValues.value.length + maxIndex
    emit("updateTopPoint", bestCaseValueList[maxIndex]);
  } else {
    emit("updateTopPoint", 0);
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

  if (worstCaseValueList.length > 0) {
    const minIndex = worstCaseValueList.indexOf(Math.max(...worstCaseValueList));
    worstPointXValue = dataValues.value.length + minIndex
    emit("updateWorstPoint",  worstCaseValueList[minIndex]);
  } else {
    emit("updateWorstPoint", 0);
  }


  // Add a new line for the best case
  if (bestCaseIndex !== -1) {
    simulatedDatasets.value.push({
      label: 'Best Case Line',
      data: [null, ...Array(dataValues.value.length - 2).fill(null), lastDataValue, ...bestCaseValueList],
      borderColor: 'rgb(39,194,191)',
      borderWidth: 1,
      borderDash: [4, 2],
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
      borderWidth: 1,
      borderDash: [4, 2],
      tension: 0,
      pointRadius: 0,
      fill: false,
    });
  }

  const cappedDataValues = dataValues.value.map(value =>
      value > taskCount.value ? taskCount.value : value
  );

  chartData.value = {
    labels: extendedLabels,
    datasets: [
      {
        label: 'Tasks Done Over Time',
        data: [...cappedDataValues, ...Array(144 - cappedDataValues.length).fill(null)],
        borderColor: 'rgb(131,131,131)',
        tension: 0.01,
        borderWidth: 2,
        pointRadius: 0,
        fill: true,
        backgroundColor: 'rgba(39,194,191,0.2)',
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
        max: taskCount.value + Math.round(taskCount.value * 0.2),
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
        ctx.lineWidth = 1;
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

      // Draw the current time vertical line
      if (currentTimeIndex.value !== undefined && currentTimeIndex.value >= 0) {
        const xPosition = x.getPixelForValue(currentTimeIndex.value);

        if (xPosition >= left && xPosition <= right) {
          ctx.save();
          ctx.beginPath();
          ctx.moveTo(xPosition, top);
          ctx.lineTo(xPosition, bottom);
          ctx.strokeStyle = 'rgba(121,121,121,0.3)';
          ctx.lineWidth = 1;
          ctx.stroke();

          ctx.setLineDash([]);
          ctx.font = '8px Istok Web';
          ctx.fillStyle = 'rgba(121,121,121,0.8)';
          ctx.textAlign = 'center';
          ctx.fillText('Now', xPosition, top - 1);
          ctx.restore();
        }
      }

      if (topPointXValue !== undefined && topPointXValue >= 0) {
        const xPosition = x.getPixelForValue(topPointXValue);

        if (xPosition >= left && xPosition <= right) {
          ctx.save();
          ctx.beginPath();
          ctx.setLineDash([4, 2]);
          ctx.moveTo(xPosition, top);
          ctx.lineTo(xPosition, bottom);
          ctx.strokeStyle = 'rgba(39,194,191,0.8)'; // Green color for best case
          ctx.lineWidth = 1;
          ctx.stroke();

          ctx.setLineDash([]);
          ctx.restore();
        }

      }

      if (worstPointXValue !== undefined && worstPointXValue >= 0) {
        const xPosition = x.getPixelForValue(worstPointXValue);

        if (xPosition >= left && xPosition <= right) {
          ctx.save();
          ctx.beginPath();
          ctx.setLineDash([4, 2]);
          ctx.moveTo(xPosition, top);
          ctx.lineTo(xPosition, bottom);
          ctx.strokeStyle = 'rgba(255, 99, 132, 0.3)';
          ctx.lineWidth = 1;
          ctx.stroke();

          ctx.setLineDash([]);
          ctx.restore();
        }

      }
    },
  };


  ChartJS.unregister(horizontalLinePlugin, verticalLinePlugin);
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

onMounted(async () => {
  await fetchData();
  startPolling(pollingInterval);
});

onUnmounted(() => {
  stopPolling();
});
</script>

<template>
  <div class="monte-carlo-graph">

    <div class="monte-carlo-graph">
      <Line :data="chartData" :options="chartOptions"/>
    </div>
    <div class="color-indicator-container">
      <p>
        <span class="color-indicator-line now-line"></span> Now
      </p>
      <p>
        <span class="color-indicator-dotted-line best-finish"></span> Optimistic Finish
      </p>
      <p>
        <span class="color-indicator-dotted-line worst-finish"></span> Pessimistic Finish
      </p>
      <p>
        <span class="color-indicator-dotted-line simulation-line"></span> Simulation
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

.color-indicator-container p {
  font-size: 0.7rem;
  margin: 0;
}

.best-case {
  background-color: rgb(39, 194, 191); /* Green */
}

.probable-case {
  background-color: rgb(126, 161, 196);
}

.worst-case {
  background-color: var(--main-color);
}

.best-finish {
  border-bottom: 2px dashed rgb(39, 194, 191);
}

.worst-finish {
  border-bottom: 2px dashed rgb(255, 99, 132); /* Red */
}

.color-indicator-dotted-line {
  display: inline-block;
  width: 20px;
  margin-right: 5px;
}

.color-indicator-line {
  display: inline-block;
  width: 20px;
  height: 2px;
  margin-right: 5px;
}

.now-line {
  background-color: #8e8e8e;
}

.simulation-line {
  border-bottom: 2px dashed rgb(181, 181, 181); /* Red */
}

.monte-carlo-graph {
  width: 100%;
  height: 90%;
  background-color: var(--background-1, #f0f0f0);
  border-radius: 8px;
  color: var(--text-1, #000); /* Fallback to black */
}

canvas {
  width: 100%;
}

.color-indicator-container {
  height: 10%;
  display: flex;
  justify-content: flex-start;
  gap: 2rem;
}
</style>
