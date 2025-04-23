<template>
  <div class="content">
    <div class="overview">
      <h2>Picker Tasks</h2>
      <div class="grid">
        <TaskZone
            v-for="zone in zonesWithPickerTasks"
            :key="zone.id"
            :zoneId="zone.id"
            :title="zone.name"
            :tasks=null
            :picker-tasks="getPickerTasksForZone(zone.id)"
            @add-task="handleAddTask"
        />
      </div>
      <hr>
      <h2>Non-Picker Tasks</h2>
      <div class="grid">
        <TaskZone
          v-for="zone in zonesWithoutPickerTasks"
          :key="zone.id"
          :zoneId="zone.id"
          :title="zone.name"
          :tasks="getTasksForZone(zone.id)"
          :picker-tasks=null
          @add-task="handleAddTask"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import TaskZone from '@/components/tasks/TaskZone.vue';
import {Task, ActiveTask, Zone, PickerTask} from '@/assets/types';
import ZoneClass from "@/components/zones/Zone.vue";
import axios from "axios";

const tasks = ref<Task[]>([]);
const activeTasks = ref<ActiveTask[]>([]);
const pickerTasks = ref<PickerTask[]>([]);
const zones = ref<Zone[]>([]);
const zonesWithPickerTasks = ref<Zone[]>([]);
const zonesWithoutPickerTasks = ref<Zone[]>([]);

const fetchTasks = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/tasks');
    if (!response.ok) throw new Error('Network response was not ok');
    tasks.value = await response.json();
  } catch (error) {
    console.error('Failed to fetch tasks:', error);
  }
};

const fetchActiveTasks = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/active-tasks');
    if (!response.ok) throw new Error('Network response was not ok');
    activeTasks.value = await response.json();
  } catch (error) {
    console.error('Failed to fetch active tasks:', error);
  }
};

const fetchPickerTasks = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/picker-tasks');
    if (!response.ok) throw new Error('Network response was not ok');
    pickerTasks.value = await response.json();
  } catch (error) {
    console.error('Failed to fetch picker tasks:', error);
  }
};

const fetchZones = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/zones');
    zones.value = await response.json();
    filterZonesByPickerTasks();
  } catch (error) {
    console.error('Failed to fetch zones:', error);
  }
};

const filterZonesByPickerTasks = () => {
  zonesWithPickerTasks.value = zones.value.filter(zone => zone.isPickerZone);
  zonesWithoutPickerTasks.value = zones.value.filter(zone => !zone.isPickerZone);
};



onMounted(() => {
  fetchTasks();
  fetchActiveTasks();
  fetchPickerTasks();
  fetchZones();
});

const getTasksForZone = (zoneId: number) => {
  return activeTasks.value.filter(task => task.task.zoneId === zoneId);
};

const getPickerTasksForZone = (zoneId: number) => {
  return pickerTasks.value.filter(task => task.zoneId === zoneId);
};

const handleAddTask = (zoneId: number) => {
  console.log(`Add task to zone ${zoneId}`);
  // Implement logic to add a task to the specified zone
};
</script>

<style scoped>
.content {
  display: flex;
  margin: 0;
  height: calc(100% - 4rem);
}

.overview {
  flex: 1;
  padding: 1rem 0 0 0;
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  justify-items: start;
  gap: 1rem;
  margin: 0 1rem;
}

.overview h2 {
  margin: 0 1rem;
  font-size: 1.5rem;
  color: #505050;
}

hr {
  border: none;
  border-top: 1px solid #ccc;
  margin: 1rem 0;
}
</style>