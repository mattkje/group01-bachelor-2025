<script setup lang="ts">
import MonteCarloGraph from "@/components/MonteCarloGraph.vue";
import NotificationWidget from "@/components/widgets/NotificationWidget.vue";
import WorkerStatusWidget from "@/components/widgets/WorkerStatusWidget.vue";

import { ref, onMounted, onUnmounted, watch } from 'vue';
import { Zone } from "@/assets/types";
import { fetchAllZones, fetchDoneByForZone } from "@/composables/DataFetcher";
import OverviewTaskSection from "@/components/tasks/OverviewTaskSection.vue";

let selectedZoneObject = ref<Zone | null>(null);
let zones = ref<Zone[]>([]);
let doneBy = ref<string>("");
let zoneId2 = ref<number>(0);
let bestcase = ref<number>(0);
let worstcase = ref<number>(0);

const defaultZone: Zone = {
  id: 0,
  name: "All Zones",
  capacity: 0,
  isPickerZone: false,
  pickerTasks: [],
  tasks: [],
  workers: []
};

const loadAllData = async (zoneId: string) => {
  zones.value = await fetchAllZones();
  if (zoneId) {
    doneBy.value = await fetchDoneByForZone(parseInt(zoneId));
  } else {
    doneBy.value = await fetchDoneByForZone(zones.value[0].id).then(response => response.time);
  }
};

const fetchDoneByInterval = async () => {
  if (selectedZoneObject.value && selectedZoneObject.value.id !== 0) {
    doneBy.value = await fetchDoneByForZone(selectedZoneObject.value.id).then(response => response.time);
  }
};

let intervalId: ReturnType<typeof setInterval> | undefined;

onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search);
  const zoneId = urlParams.get('id');
  loadAllData(zoneId).then(() => {
    if (zones.value.length > 0) {
      if (zoneId) {
        const zone = zones.value.find(zone => zone.id === parseInt(zoneId));
        if (zone) {
          selectedZoneObject.value = zone;
        }
      } else {
        selectedZoneObject.value =  defaultZone;
      }
    }
  });

  // Start the interval for fetching doneBy
  intervalId = setInterval(fetchDoneByInterval, 5000);
});

onUnmounted(() => {
  // Clear the interval when the component is unmounted
  if (intervalId) {
    clearInterval(intervalId);
  }
});

watch(selectedZoneObject, async (newZone) => {
  if (newZone) {
    zoneId2.value = newZone.id;
    doneBy.value = await fetchDoneByForZone(newZone.id).then(response => response.time);
  }

});

function handleTopPointUpdate(value: number) {
  bestcase.value = value;
}

function handleWorstPointUpdate(value: number) {
  worstcase.value = value;
}
</script>

<template>
  <div class="container-container" v-if="selectedZoneObject">
    <div class="header">
      <div class="zone-selector">
        <select class="zone-selector-dropdown" id="zone-dropdown" v-model="selectedZoneObject">
          <option :key="0" :value="defaultZone">All Zones</option>
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
          <div class="day-status-container">
            <WorkerStatusWidget :zone="selectedZoneObject" :key="selectedZoneObject.id" class="status-text-box"/>
            <div class="done-by" v-if="selectedZoneObject.id !== 0">
              <div class="section2">
                <h2>Best Case:</h2>
                <p>{{ bestcase }}</p>
              </div>
              <div class="section2">
                <h2>Zone Done:</h2>
                <p>{{ doneBy }}</p>
              </div>
              <div class="section2">
                <h2>Worst Case:</h2>
                <p>{{ worstcase }}</p>
              </div>
            </div>
          </div>

        </div>
        <div class="monte-carlo-graph-container">
          <MonteCarloGraph
              class="monte-carlo-graph"
              :zone-id="selectedZoneObject.id"
              :key="selectedZoneObject.id"
              @updateTopPoint="handleTopPointUpdate"
              @updateWorstPoint="handleWorstPointUpdate"
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
  max-height: 65px;
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
  max-height: 40%;
  width: 100%;
  gap: 1rem;
  display: flex;
  flex-direction: row;
}

.day-status-container {
  width: 100%;
  height: 100%;
  gap: 1rem;
  display: flex;
  flex-direction: column;
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
  height: 97%;
  border: 1px solid var(--border-1);
  flex-direction: row;
  border-radius: 1rem;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  font-size: 1.2rem;
  color: var(--text-1);
}

.done-by {
  width: 100%;
  height: 100%;
  justify-content: space-between;
  align-items: center;
  font-size: 1.2rem;
  border: 1px solid var(--border-1);
  border-radius: 1rem;
  display: flex;
  flex-direction: row;
  padding: 0.8rem 1.5rem 0.2rem 1.5rem;
}

.done-by h2 {
  color: var(--main-color);
  font-size: 0.8rem;
  font-weight: bolder;
}

.done-by p {
  color: var(--main-color);
  font-size: 1.8rem;
  font-weight: bolder;
  position: relative;
  top: -0.5rem;
}

.section2 {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
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

@media (max-height: 900px) {
  .day-status {
    height: 30%;

  }

  .day-status-container {
    flex-direction: row;
  }

  .done-by {
    max-height: 100%;
    padding: 0 1rem;
    flex-direction: column;
    line-height: 2rem;
  }

  .done-by h2 {
    font-size: 0.75rem;
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