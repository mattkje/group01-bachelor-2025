<script setup lang="ts">

import ZoneSidebar from "@/components/zones/ZoneSidebar.vue";

interface Zone {
  id: number;
  name: string;
  data: string;
}

const props = defineProps<{ zone: Zone }>();
</script>

<template>
  <div class="popup-background" @click="$emit('close')">
    <div class="popup" @click.stop>

      <div class="popup-body">
        <ZoneSidebar :title="zone.name" @close="$emit('close')" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.popup-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.popup {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80%;
  height: 90%;
  background-color: #fff;
  border-radius: 10px;
  border: #e1e1e1 solid 1px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.popup-header {
  margin-bottom: 1rem;
}

.popup-body {
  display: flex;
  flex: 1;
}

.sidebar {
  width: 250px;
  border-right: 1px solid #e1e1e1;
  margin-right: 1rem;
}

.popup-content {
  flex: 1;
  font-size: 1rem;
  overflow-y: auto;
}
</style>