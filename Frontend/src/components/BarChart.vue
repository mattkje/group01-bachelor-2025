<template>
  <div>
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { Bar } from "vue-chartjs";
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from "chart.js";

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

const props = defineProps<{
  zoneLabels: String[];
  data: Number[];

}>();

const chartData = ref({
  labels: props.zoneLabels,
  datasets: [
    {
      label: "Hours Worked",
      backgroundColor: "#E77474",
      data: props.data
    }
  ]
});

const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false
});
</script>

<style scoped>
div {
  height: 400px;
}
</style>