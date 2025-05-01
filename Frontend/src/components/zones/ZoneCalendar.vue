<script setup lang="ts">
import {onMounted} from "vue";

let date

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
    date.value = ''; // Fallback to current date
  }
};

onMounted(() => {
  fetchDateFromBackend();
});
</script>

<template>
<div class="calendar-container">
  <h1></h1>
</div>
</template>

<style scoped>
.calendar-container {
  width: 100%;
  height: 100%;
  padding: 20px;
  max-height: calc(100vh - 570px);
  overflow-y: auto;
  flex: 1;
}
</style>