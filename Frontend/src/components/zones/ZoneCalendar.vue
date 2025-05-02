<script setup lang="ts">
import {onMounted, ref, watch} from "vue";

const date = ref<Date | null>(null);
const dates = ref<{formattedDate: string }[]>([]);
const dateTitle = ref<string | null>(null);

const fetchDateFromBackend = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/simulation/currentDate`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const dateString = await response.json();
    date.value = new Date(dateString);
  } catch (error) {
    console.error('Failed to fetch date:', error);
    date.value = new Date(); // Fallback to current date
  }
};

const generateDates = () => {
  if (date.value) {
    dates.value = [];
    for (let i = 0; i <= 6; i++) {
      const newDate = new Date(date.value);
      newDate.setDate(newDate.getDate() + i);

      const dayName = newDate.toLocaleDateString("en-US", { weekday: "short" });
      const formattedDate = `${dayName} ${newDate.getDate()}`;
      dates.value.push({ formattedDate });
    }
  }
};

const getDateTitle = () => {
  if (date.value) {
    const startDate = new Date(date.value);
    const endDate = new Date(date.value);
    endDate.setDate(startDate.getDate() + 6);

    const startDateFormatted = startDate.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
    });
    const endDateFormatted = endDate.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });

    dateTitle.value = `${startDateFormatted} - ${endDateFormatted}`;
  }
};

watch(date, getDateTitle);
watch(date, generateDates);

onMounted(() => {
  fetchDateFromBackend();
});
</script>

<template>
<div class="calendar-container">
  <h2>{{ dateTitle }}</h2>
  <table>
    <thead>
    <tr>
      <th>Workers</th>
      <th v-for="(dateObj, index) in dates" :key="index">{{ dateObj.formattedDate }}</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>2023-10-05</td>
      <td>Sample Event</td>
    </tr>
    <tr>
      <td>2023-10-06</td>
      <td>Another Event</td>
    </tr>
    </tbody>
  </table>
</div>
</template>

<style scoped>
.calendar-container {
  width: 100%;
  height: 100%;
  margin: 0 3.5rem;
  max-height: calc(100vh - 570px);
  overflow-y: auto;
  flex: 1;
}
</style>