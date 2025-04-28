<template>
  <div class="overview">
    <MonteCarloGraph class="monte-carlo-graph" :zone-id="99" />
    <div class="day-status">
      <div class="status-text-box">
        <p>Tasks Remaining:</p>
        <p>5</p>
      </div>
      <div class="status-text-box">
        <p>Tasks In Completed:</p>
        <p>2</p>
      </div>
      <div class="status-text-box">
        <p>Workers Available:</p>
        <p>3</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import MonteCarloGraph from "@/components/MonteCarloGraph.vue";
</script>

<style scoped>
.overview {
  height: 100%;
  padding: 2rem;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.monte-carlo-graph {
  width: 50%;
  height: 50%;
  border-radius: 2rem;
  border: 1px solid #e5e5e5;
}

.day-status {
  height: 20%;
  border: 1px solid #e5e5e5;
  flex-direction: row;
  border-radius: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.2rem;
  gap: 2rem;
  color: #7B7B7B;
}

.status-text-box {
  margin: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
</style>