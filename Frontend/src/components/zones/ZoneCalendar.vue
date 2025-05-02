<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue";
import {Zone, Worker} from "@/assets/types";
import {fetchSimulationDate} from "@/composables/DataFetcher";

const props = defineProps<{
  zone: Zone;
}>();

const date = ref<Date | null>(null);
const dates = ref<{formattedDate: string, isoDate: string }[]>([]);
const dateTitle = ref<string | null>(null);
const schedules = ref<any[]>([]);
const groupedSchedules = ref<Record<number, any[]>>({});
const todayIsoDate = ref<string | null>(null);

const fetchDateFromBackend = async () => {
  try {
    const dateString = await fetchSimulationDate();
    date.value = new Date(dateString);
    todayIsoDate.value = date.value.toISOString().split("T")[0];
    await fetchSchedulesFromBackend();
  } catch (error) {
    console.error('Failed to fetch date:', error);
    const fallback = new Date();
    date.value = fallback;
    todayIsoDate.value = fallback.toISOString().split("T")[0];
  }
};

const fetchSchedulesFromBackend = async () => {
  try {
    const response = await
        fetch(`http://localhost:8080/api/timetables/one-week/${date.value?.toISOString().split("T")[0]}/${props.zone.id}`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    schedules.value = await response.json();
    groupSchedulesByWorker();
    console.log(groupedSchedules);
  } catch (error) {
    console.error('Failed to fetch schedules:', error);
    schedules.value = [];
  }
}

const groupSchedulesByWorker = () => {
  groupedSchedules.value = schedules.value.reduce((acc: Record<number, any[]>, schedule: any) => {
    if (!acc[schedule.workerId]) {
      acc[schedule.workerId] = [];
    }
    acc[schedule.workerId].push(schedule);
    return acc;
  }, {});
};

const generateDates = () => {
  if (date.value) {
    dates.value = [];
    for (let i = 0; i <= 6; i++) {
      const newDate = new Date(date.value);
      newDate.setDate(newDate.getDate() + i);

      const dayName = newDate.toLocaleDateString("en-US", { weekday: "short" });
      const formattedDate = `${dayName} ${newDate.getDate()}`;
      const isoDate = newDate.toISOString().split("T")[0];

      dates.value.push({ formattedDate, isoDate });
    }
  }
};

const getDateTitle = () => {
  if (date.value) {
    const startDate = new Date(date.value);
    const endDate = new Date(date.value);
    endDate.setDate(startDate.getDate() + 6);

    const startDateFormatted = startDate.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
    });
    const endDateFormatted = endDate.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });

    dateTitle.value = `${startDateFormatted} - ${endDateFormatted}`;
  }
};

const getCellClass = (workerSchedules: any[], isoDate: string): string => {
  const hasSchedule = workerSchedules.some(
      (schedule) => schedule.startTime.split('T')[0] === isoDate
  );
  if (!hasSchedule) return 'empty';
  return isoDate === todayIsoDate.value ? 'has-schedule-today' : 'has-schedule';
};

const navigateWeek = (direction: number) => {
  if (date.value) {
    date.value.setDate(date.value.getDate() + direction * 7);
    generateDates();
    getDateTitle();
    fetchSchedulesFromBackend();
  }
};

const getWorkerNameById = (id: number): string => {
  const worker = props.zone.workers.find(w => w.id === id);
  return worker ? worker.name : `Worker ${id}`;
};

const sortedGroupedSchedules = computed(() => {
  return Object.entries(groupedSchedules.value)
      .sort((a, b) => {
        const nameA = getWorkerNameById(Number(a[0])).toLowerCase();
        const nameB = getWorkerNameById(Number(b[0])).toLowerCase();
        return nameA.localeCompare(nameB);
      });
});

watch(date, getDateTitle);
watch(date, generateDates);

onMounted(() => {
  const today = new Date();
  todayIsoDate.value = today.toISOString().split("T")[0];
  fetchDateFromBackend();
});
</script>

<template>
<div class="calendar-container">
  <div class="top-calendar-navbar">
    <h2>{{ dateTitle }}</h2>
    <div class="top-calendar-navbar-buttons">
      <button @click="navigateWeek(-1)" class="nav-button">←</button>
      <button @click="navigateWeek(1)" class="nav-button">→</button>
    </div>
  </div>
  <table>
    <thead>
    <tr>
      <th></th>
      <th v-for="(dateObj, index) in dates" :key="index">{{ dateObj.formattedDate }}</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="worker in props.zone.workers" :key="worker.id">
      <td>{{ worker.name }}</td>
      <td v-for="(dateObj, index) in dates" :key="index">
        <div :class="['schedule-box', getCellClass(groupedSchedules[worker.id] || [], dateObj.isoDate)]">
          <div
              v-for="schedule in (groupedSchedules[worker.id] || [])"
              :key="schedule.id"
          >
        <span v-if="schedule.startTime.split('T')[0] === dateObj.isoDate">
          {{ new Date(schedule.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
          <br>
          {{ new Date(schedule.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
        </span>
          </div>
        </div>
      </td>
    </tr>
    </tbody>
  </table>
</div>
</template>

<style scoped>
.calendar-container {
  width: 90%;
  height: 100%;
  margin: 0 3.5rem;
  max-height: calc(100vh - 570px);
  overflow-y: auto;
  flex: 1;
}

table {
  width: 100%;
  border-collapse: collapse; /* Ensures borders don't double up */
}

th, td {
  border: 1px solid #E0E0E0;
  padding: 8px;
  text-align: center;
  color: #565656;
}

td {
  height: 80px;
}

thead th:first-child,
tbody td:first-child {
  border-left: none;
}

th:nth-child(2) {
  color: #E77474;
}

thead th {
  position: sticky;
  top: 0;
  z-index: 1;
  background-color: #ffffff;
  border-top: none;
  font-size: 24px;
}

.schedule-box {
  color: #fff;
  border: 1px dashed #CCC;
  border-radius: 20px;
  height: 90px;
  min-height: 60px;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  font-size: 24px;
}

.schedule-box span {
  line-height: 1.2;
  font-weight: bold;
}

.schedule-box.has-schedule {
  background-color: #FFBDBD;
  border: none;
}

.schedule-box.has-schedule-today {
  background-color: #E77474;
  border: none;
}

.schedule-box.empty {
  color: #ffffff;
}

.top-calendar-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.top-calendar-navbar-buttons {
  display: flex;
  gap: 1rem;
  padding-right: 20px;
}

.top-calendar-navbar-buttons button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.5rem;
  color: #E77474;
}
</style>