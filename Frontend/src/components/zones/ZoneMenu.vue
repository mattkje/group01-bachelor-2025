<script setup lang="ts">
import { ref } from 'vue';
import ZoneTasks from "@/components/zones/ZoneTasks.vue";
import { Zone } from "@/assets/types";

const props = defineProps<{ zone: Zone }>();
const activeTab = ref('Overview');

const handleTabSelected = (tabName: string) => {
  activeTab.value = tabName;
};
</script>

<template>
  <div class="popup-background" @click="$emit('close')">
    <div class="popup" @click.stop>
      <div class="popup-body">
        <button class="close-button" @click="$emit('close')">&times;</button>
        <ZoneTasks :zone="zone"/>
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
  width: 70%;
  height: 70%;
  background-color: #fff;
  border-radius: 2rem;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.popup-header {
  margin-bottom: 1rem;
}

.popup-body {
  display: flex;
  flex-direction: row;
  padding: 2rem;
  flex: 1;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.8rem;
  color: #646464;

  cursor: pointer;
  align-self: flex-start;
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