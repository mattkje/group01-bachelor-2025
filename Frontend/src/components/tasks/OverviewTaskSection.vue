<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue';
import ActiveTaskComponent from "@/components/tasks/ActiveTaskComponent.vue";
import {ActiveTask, PickerTask, Zone} from "@/assets/types";
import {
  fetchAllActiveTasksByZoneForDate,
  fetchAllPickerTasksByZoneForDate,
  fetchSimulationDate
} from "@/composables/DataFetcher";
import PickerTaskComponent from "@/components/tasks/PickerTaskComponent.vue";
import ZoneCalendar from "@/components/zones/ZoneCalendar.vue";

const selectedTab = ref('Current');
const selectedOption = ref('best case');
const displayEstimations = ref(false);

const tabs = ['Current', 'Completed', 'Schedule'];
const dropdownOptions = ['best case', 'average case', 'worst case'];

const props = defineProps<{
  zone: Zone;
}>();

const tasks = ref<ActiveTask[]>([]);
const pickerTasks = ref<PickerTask[]>([]);
const currentDate = ref<string>('');
const hasTasks = ref(false);
const hasPickerTasks = ref(false);
const loading = ref(true);

onMounted(async () => {
  await handleZoneChange();
});

const handleZoneChange = async () => {
  loading.value = true;
  currentDate.value = await loadCurrentDate();

  console.log('Current date:', currentDate.value);

  console.log(props.zone.name);
  if (props.zone.isPickerZone) {
    await loadPickerTasksForZone();
    hasPickerTasks.value = pickerTasks.value.length > 0;
  } else {
    await loadTasksForZone();
    hasTasks.value = tasks.value.length > 0;
  }
  loading.value = false;
};

const loadCurrentDate = async () => {
  return await fetchSimulationDate();
};

const loadTasksForZone = async () => {
  try {
    tasks.value = await fetchAllActiveTasksByZoneForDate(props.zone.id, currentDate.value);
  } catch (error) {
    console.error('Failed to fetch tasks for zone:', error);
  }
};

const loadPickerTasksForZone = async () => {
  try {
    pickerTasks.value = await fetchAllPickerTasksByZoneForDate(props.zone.id, currentDate.value);
  } catch (error) {
    console.error('Failed to fetch picker tasks for zone:', error);
  }
};


const tasksWithoutEndTime = computed(() => {
  return tasks.value.filter(task => !task.endTime);
});
const tasksWithEndTime = computed(() => {
  return tasks.value.filter(task => task.endTime);
});
const pickerTasksWithoutEndTime = computed(() => {
  return pickerTasks.value.filter(task => !task.endTime);
});
const pickerTasksWithEndTime = computed(() => {
  return pickerTasks.value.filter(task => task.endTime);
});

// Watch for changes to the zone prop
watch(() => props.zone, async () => {
  await handleZoneChange();
});


</script>
<template>
  <div class="container">
    <div class="toolbar">
      <div class="tabs">
        <button
            v-for="tab in tabs"
            :key="tab"
            :class="{ active: selectedTab === tab }"
            @click="selectedTab = tab"
        >
          {{ tab }}
        </button>
      </div>
    </div>
    <div class="content">
      <div class="task-content" v-if="selectedTab === 'Current' || selectedTab === 'Completed'">
        <div class="controls">
          <select v-model="selectedOption">
            <option v-for="option in dropdownOptions" :key="option" :value="option">
              {{ option }}
            </option>
          </select>

          <div class="switch">
            <label>
              <input type="checkbox" v-model="displayEstimations" />
              <span class="text-span">Display estimations</span>
            </label>
          </div>
        </div>
        <div v-if="selectedTab === 'Completed'" class="task-container">
          <template v-if="tasksWithEndTime.length > 0 || pickerTasksWithEndTime.length > 0">
            <ActiveTaskComponent
                v-for="(task, index) in tasksWithEndTime"
                :key="index"
                :active-task="task"
            />
            <PickerTaskComponent
                v-for="(task, index) in pickerTasksWithEndTime"
                :key="index"
                :picker-task="task"
            />
          </template>
          <p style="margin-top: 20vh" v-else>No completed tasks</p>
        </div>
        <div v-else-if="selectedTab === 'Current'" class="task-container">
          <template v-if="tasksWithoutEndTime.length > 0 || pickerTasksWithoutEndTime.length > 0">
            <ActiveTaskComponent
                v-for="(task, index) in tasksWithoutEndTime"
                :key="index"
                :active-task="task"
            />
            <PickerTaskComponent
                v-for="(task, index) in pickerTasksWithoutEndTime"
                :key="index"
                :picker-task="task"
            />
          </template>
          <p style="margin-top: 20vh" v-else>No current tasks</p>
        </div>
      </div>
      <div v-else-if="selectedTab === 'Schedule'" class="task-container">
        <ZoneCalendar :zone="zone" class="bottom-box-right"/>
      </div>
    </div>
  </div>

</template>

<style scoped>
.container {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.content {
  width: 100%;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.task-container {
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
  justify-content: center;
  align-content: center;
  justify-items: center;
  text-align: center;
  overflow: auto;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #ddd;
}

.tabs {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
}

.tabs button {
  padding: 0.7rem;
  width: 7rem;
  border-radius: 0.7rem;
  border: none;
  background: none;
  cursor: pointer;
}

.tabs button.active {
  color: var(--main-color);
  background-color: var(--main-color-3);
}

.controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-left: auto;
  margin-bottom: 1rem;
}

.controls select {
  padding: 0.5rem;
  border-radius: 0.7rem;
  border: 1px solid var(--border-1);
  background-color: var(--background-2);
}

.controls select:focus {
  outline: none;
  border-color: var(--main-color);
}

.switch {
  display: flex;
  align-items: center;
  gap: 5px;
}

.switch input[type="checkbox"] {
  width: 20px;
  height: 20px;
  cursor: pointer;
}

.switch label {
  display: flex;
  align-items: center;
  gap: 5px;
}

hr {
  margin: 0;
  border: none;
  border-top: 1px solid #ddd;
}

.text-span {
  font-size: 0.8rem;
  color: var(--text-1);
}

.task-content {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

@media (max-width: 1400px) {

  .task-container {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }
  .tabs button {
    width: 5rem;
  }
  .controls select {
    width: 100px;
  }
  .controls {
    gap: 0.5rem;
  }
}
</style>