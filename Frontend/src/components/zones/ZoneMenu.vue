<script setup lang="ts">
import { ref } from 'vue';
import ZoneSidebar from "@/components/zones/ZoneSidebar.vue";
import ZoneTasks from "@/components/zones/ZoneTasks.vue";

interface Zone {
  id: number;
  name: string;
  data: string;
}

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
        <ZoneSidebar :title="zone.name" @close="$emit('close')" @tab-selected="handleTabSelected" />
        <div class="popup-content">
          <div v-if="activeTab === 'Overview'">Overview Content</div>
          <div v-if="activeTab === 'Tasks'"><ZoneTasks :zone="zone"/></div>
          <div v-if="activeTab === 'History'">History Content</div>
        </div>
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
  width: 100%;
  height: 100%;
  background-color: #fff;
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