<template>
  <Toolbar title="Overview" />
  <div class="overview">
    <pre>{{ asciiArt }}</pre>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import Toolbar from "@/components/Toolbar.vue";
import AsciiArt from "@/assets/ascii.txt";

const asciiArt = ref('');

onMounted(async () => {
  const response = await fetch(AsciiArt);
  asciiArt.value = await response.text();
});
</script>

<style scoped>
.overview {
  padding: 2rem;
  white-space: pre-wrap;
  font-size: 0.4rem;
  overflow-y: auto;
}
</style>