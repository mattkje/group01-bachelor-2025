<script setup lang="ts">
import MonteCarloGraph from "@/components/MonteCarloGraph.vue";
import NotificationWidget from "@/components/widgets/NotificationWidget.vue";
import WorkerStatusWidget from "@/components/widgets/WorkerStatusWidget.vue";

import {ref, onMounted} from 'vue';
import {Zone} from "@/assets/types";
import {fetchAllZones} from "@/composables/DataFetcher";
import OverviewTaskSection from "@/components/tasks/OverviewTaskSection.vue";

let selectedZoneObject = ref<Zone | null>(null);
let zones = ref<Zone[]>([]);

const loadAllData = async () => {
  zones.value = await fetchAllZones();
};

onMounted(() => {

  loadAllData().then(() => {
    if (zones.value.length > 0) {
      const urlParams = new URLSearchParams(window.location.search);
      const zoneId = urlParams.get('id');
      if (zoneId) {
        const zone = zones.value.find(zone => zone.id === parseInt(zoneId));
        if (zone) {
          selectedZoneObject.value = zone;
        }
      } else {
        selectedZoneObject.value = zones.value[0];
      }
    }
  });


});
</script>

<template>
  <div class="container-container" v-if="selectedZoneObject">
    <div class="header">
      <h1>Dashboard</h1>
      <div class="vertical-separator"></div>
      <div class="zone-selector">
        <select class="zone-selector-dropdown" id="zone-dropdown" v-model="selectedZoneObject">
          <option :key="0" :value="{ id: 0 }">All Zones</option>
          <option v-for="zone in zones" :key="zone.id" :value="zone">
            {{ zone.name }}
          </option>
        </select>
      </div>
    </div>
    <div class="overview-widget-area">
      <div class="overview">

        <div class="day-status">
          <NotificationWidget :zone="selectedZoneObject" :key="selectedZoneObject.id" class="status-text-box"/>
          <WorkerStatusWidget :zone="selectedZoneObject" :key="selectedZoneObject.id" class="status-text-box"/>
        </div>
        <div class="monte-carlo-graph-container">
          <MonteCarloGraph
              class="monte-carlo-graph"
              :zone-id="selectedZoneObject.id"
              :key="selectedZoneObject.id"
          />
        </div>
      </div>
      <div class="tasks-container">
        <OverviewTaskSection :zone="selectedZoneObject" :zone-id="selectedZoneObject.id"></OverviewTaskSection>
      </div>
    </div>
  </div>
  <div v-else>
    <p>Loading...</p>
  </div>
</template>

<style scoped>
.container-container {
  height: calc(90vh - 4rem);
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 2rem;
}

.vertical-separator {
  width: 1px;
  height: 30px;
  background-color: var(--border-1);
  margin: 0.6rem 0.8rem;
}

.overview {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.overview-widget-area {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: row;
  gap: 1rem;
}

.header {
  width: 100%;
  align-content: center;
  justify-content: flex-start;
  display: flex;
  border: 1px solid var(--border-1);
  border-radius: 1rem;
  padding: 0.5rem;
}

.monte-carlo-graph-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  border-radius: 1rem;
  padding: 0.7rem;
  border: 1px solid var(--border-1);
}

.monte-carlo-graph {
  border-radius: 2rem;
}

.day-status {
  height: 100%;
  max-height: 25vh;
  width: 100%;
  gap: 1rem;
  display: flex;
  flex-direction: row;
}

.status-text-box {
  margin: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.zone-selector {
  display: flex;
  align-items: center;
}

.zone-selector label {
  font-size: 1rem;
}

.zone-selector select {
  padding: 0.5rem;
  border-radius: 0.5rem;
  font-size: 1rem;
}

.zone-selector-dropdown {
  background-color: transparent;
  color: var(--text-1);
  border: 1px solid transparent;
  border-radius: 0.5rem;
}

.tasks-container {
  width: 100%;
  height: 100%;
  border: 1px solid var(--border-1);
  flex-direction: row;
  border-radius: 1rem;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  font-size: 1.2rem;
  color: var(--text-1);
}

@media (max-width: 1400px) {

  .tasks-container {
    width: 50%;
    min-width: 20%;
  }

  .overview {
    flex-direction: column;
  }

}

@media (max-height: 800px) {
  .day-status {
    height: 30%;
  }

  .overview {
    flex-direction: column;
    overflow-y: auto;
    height: 100%;
  }

  .monte-carlo-graph-container {
    height: 65%;
  }
}

@media (min-height: 1000px) {
  .day-status {
    height: auto;
  }

}


</style>