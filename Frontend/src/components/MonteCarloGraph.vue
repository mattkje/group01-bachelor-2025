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
  let lastValue = dataValues.value[dataValues.value.length - 1] || 0;


  // This is a placeholder for the simulation data \/
  // const case1 = [lastValue, ...Array.from({ length: 25 }, (_, i) => Math.min(lastValue + Math.floor(i / 12), 13, taskCount.value))];
  // const case2 = [lastValue, ...Array.from({ length: 50 }, (_, i) => Math.min(lastValue + Math.floor(i / 10), 13, taskCount.value))];
 // const case3 = [lastValue, ...Array.from({ length: 95 }, (_, i) => Math.min(lastValue + Math.floor(i / 12), 13, taskCount.value))];
  //const case4 = [lastValue, ...Array.from({ length: 32 }, (_, i) => Math.min(lastValue + Math.floor(i / 15), 13, taskCount.value))];
  //const case5 = [lastValue, ...Array.from({ length: 68 }, (_, i) => Math.min(lastValue + Math.floor(i / 11), 13,taskCount.value))];
  //ListOfListsOfValues.value.push(case1);
 // ListOfListsOfValues.value.push(case2);
 // ListOfListsOfValues.value.push(case3);
 // ListOfListsOfValues.value.push(case4);
//  ListOfListsOfValues.value.push(case5);
  // Down to here /\

  const baseColor = 'rgba(150, 150, 150, 0.3)';

  ListOfListsOfValues.value.forEach((list, index) => {
    simulatedDatasets.value.push({
      label: `Case ${index + 1}`,
      data: [...Array(dataValues.value.length - 1).fill(null), ...list],
      borderColor: baseColor,
      tension: 0.1,
      pointRadius: 0,
      fill: false,
    });
  });

  let bestCaseIndex = -1;
  let worstCaseIndex = -1;
  let bestCaseValue = Infinity;
  let worstCaseValue = -Infinity;

  simulatedDatasets.value.forEach((dataset, index) => {
    const firstCompletionIndex = dataset.data.findIndex((value) => value >= taskCount.value);

    if (firstCompletionIndex !== -1) {
      if (firstCompletionIndex < bestCaseValue) {
        bestCaseValue = firstCompletionIndex;
        bestCaseIndex = index;
      }
      if (firstCompletionIndex > worstCaseValue) {
        worstCaseValue = firstCompletionIndex;
        worstCaseIndex = index;
      }
    }
  });

  if (bestCaseIndex !== -1) {
    simulatedDatasets.value[bestCaseIndex].borderColor = 'rgb(126,196,177)';
    simulatedDatasets.value[bestCaseIndex].borderWidth = 3;
  }

  if (worstCaseIndex !== -1) {
    simulatedDatasets.value[worstCaseIndex].borderColor = 'rgba(255, 99, 132, 1)';
    simulatedDatasets.value[worstCaseIndex].borderWidth = 3;
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
  await fetchSimulationData();
  pollingTimer = setInterval(fetchAllData, pollingInterval);
};

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
    date.value = '';
  }
  await fetchActiveTasks();
};


const fetchAllData = async () => {
  await fetchRealData();
  await fetchSimulationData();
};

const pollingInterval = 5000;
let pollingTimer: ReturnType<typeof setInterval> | null = null;

const fetchRealData = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/data/${props.zoneId}/values`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const data: number[] = await response.json();

    if (data && JSON.stringify(data) !== JSON.stringify(dataValues.value)) {
      dataValues.value = data || [0];
      isDataReady.value = dataValues.value.length > 1;

      const now = new Date();
      const currentHour = now.getHours();
      const currentMinute = now.getMinutes();
      currentTimeIndex = currentHour * 6 + Math.floor(currentMinute / 10);

      generateChartData();
    }
  } catch (error) {
    console.error('Failed to fetch real data:', error);
  }
};

const fetchSimulationData = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/data/${props.zoneId}/mcvalues`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const data: number[][] = await response.json();
    if (data && JSON.stringify(data) !== JSON.stringify(ListOfListsOfValues.value)) {
      ListOfListsOfValues.value = data || [];
      isDataReady.value = ListOfListsOfValues.value.length > 1;

      generateChartData();
    }

  } catch (error) {
    console.error('Failed to fetch simulation data:', error);
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
  padding: 20px;
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