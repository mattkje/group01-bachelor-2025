<template>
  <div class="overview">
    <div class="large-rounded-square" v-for="n in 4" :key="n">
      <p>Widget {{ n }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import Toolbar from "@/components/Toolbar.vue";
</script>

<style scoped>
.overview {
  padding: 2rem;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.large-rounded-square {
  width: 450px;
  height: 300px;
  border: 1px solid #e5e5e5;
  border-radius: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
  font-size: 1.2rem;
  color: #7B7B7B;
}
</style>