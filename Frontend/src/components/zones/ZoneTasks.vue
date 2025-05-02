<script setup lang="ts">
import {ref, onMounted} from 'vue';
import ActiveTaskComponent from "@/components/tasks/ActiveTaskComponent.vue";
import {ActiveTask, Zone, PickerTask, Task} from "@/assets/types";
import PickerTaskComponent from "@/components/tasks/PickerTaskComponent.vue";
import {
  fetchAllActiveTasksForZoneNow,
  fetchAllPickerTasksForZoneNow,
  fetchAllTasks,
  fetchAllZones
} from "@/composables/DataFetcher";


const props = defineProps<{
  zone: Zone;
}>();

const tasks = ref<Task[]>([]);
const activeTasks = ref<ActiveTask[]>([]);
const pickerTasks = ref<PickerTask[]>([]);
const zones = ref<Zone[]>([]);

const fetchTasks = async () => {
  tasks.value = await fetchAllTasks();
};

const fetchActiveTasks = async () => {
  activeTasks.value = await fetchAllActiveTasksForZoneNow(parseInt(props.zone.id.toString()));
  activeTasks.value.sort((a, b) => a.id - b.id);
};

const loadPickerTasks = async () => {
  pickerTasks.value = await fetchAllPickerTasksForZoneNow(parseInt(props.zone.id.toString()));
  pickerTasks.value.sort((a, b) => a.id - b.id);
};

const loadAll = async () => {
  zones.value = await fetchAllZones();
};

onMounted(() => {
  fetchTasks();
  if (props.zone.isPickerZone) {
    loadPickerTasks();
  } else {
    fetchActiveTasks();
  }
  loadAll();
});


const updateTasks = () => {
  fetchTasks();
  if (props.zone.isPickerZone) {
    loadPickerTasks();
  } else {
    fetchActiveTasks();
  }
  loadAll();
};
</script>

<template>
  <div class="content">
    <h2 v-if="!zone.isPickerZone">Active Tasks</h2>
    <h2 v-if="zone.isPickerZone">Picker Tasks</h2>
    <div class="activeTaskContainer">
      <div class="placeholder" v-if="activeTasks.length === 0 && pickerTasks.length === 0">No tasks remaining...</div>
      <active-task-component
          v-if="!zone.isPickerZone"
          v-for="activeTask in activeTasks"
          :key="activeTask.id"
          :active-task="activeTask"
          @task-deleted="updateTasks"
          @task-updated="updateTasks"/>
      <picker-task-component
          v-if="zone.isPickerZone"
          v-for="pickerTask in pickerTasks"
          :key="pickerTask.id"
          :picker-task="pickerTask"
          @task-deleted="updateTasks"
          @task-updated="updateTasks"/>
    </div>
  </div>
</template>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  margin: 0 3.5rem;
  height: 100%;
}

.activeTaskContainer {
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  gap: 10px;
  max-height: calc(100vh - 570px);
  overflow-y: auto;
  flex: 1;
}

ul {
  list-style-type: none;
  padding: 0;
}

ul li {
  background-color: #f0f0f0;
  padding: 0.5em;
  border-radius: 4px;
  margin-bottom: 0.5em;
}

ul li::before {
  content: '';
  display: inline-block;
  width: 1em;
  margin-left: -1em;
}

.placeholder {
  font-size: 1.2rem;
  color: #888;
  text-align: center;
}
</style>