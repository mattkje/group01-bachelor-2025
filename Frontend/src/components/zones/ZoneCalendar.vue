<script setup lang="ts">
import {Zone} from "@/assets/types";
import FullCalendar from "@fullcalendar/vue3";
import resourceTimelinePlugin from "@fullcalendar/resource-timeline";
import interactionPlugin from "@fullcalendar/interaction";

const props = defineProps<{
  zone: Zone;
  date: Date;
}>();

const handleDateClick = (arg: any) => {
  alert('Date clicked: ' + arg.dateStr);
};

const resources = [
  { id: '1', title: 'Alice' },
  { id: '2', title: 'Bob' },
  { id: '3', title: 'Charlie' },
  { id: '4', title: 'Håkon' },
  { id: '5', title: 'Diana'},
];

const events = [
  { id: '1', resourceId: '1', title: '08:00 - 12:00', start: '2025-05-01', end: '2025-05-01' },
  { id: '2', resourceId: '2', title: '10:00 - 14:00', start: '2025-05-01', end: '2025-05-01' },
  { id: '3', resourceId: '3', title: '13:00 - 17:00', start: '2025-05-01', end: '2025-05-01' },
];

const calendarOptions = {
  plugins: [resourceTimelinePlugin, interactionPlugin],
  initialView: 'resourceTimelineWeek',
  resources,
  events,
  dateClick: handleDateClick,
  slotDuration: '24:00:00',
  slotLabelFormat: [
    { weekday: 'short', day: 'numeric' }
  ],
  initialDate: props.date,
  resourceAreaHeaderContent: 'Workers',
  height: 'auto',
};

</script>

<template>
  <div class="calendar-container">
    <FullCalendar
      :options="calendarOptions"
    />
  </div>
</template>

<style scoped>
.calendar-container {
  width: 100%;
  height: 100%;
  padding: 20px;
  max-height: calc(100vh - 570px);
  overflow-y: auto;
  flex: 1;
}

/* Optional: change background and hover states */
::v-deep .fc-button-primary {
  background-color: transparent;
  color: #E77474;
  border: 1px solid #E77474;
}

::v-deep .fc-button-primary:hover {
  background-color: #E77474;
  color: #fff;
}

::v-deep .fc-today-button:disabled {
  background-color: #E77474;
  color: white;
  border-color: #E77474;
  opacity: 1; /* Optional: override default dimmed look */
  cursor: default;
}

::v-deep .fc-timeline-event {
  height: 60px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  background-color: #E77474;
  color: white;
  border-radius: 6px;
  text-align: center;
}

</style>