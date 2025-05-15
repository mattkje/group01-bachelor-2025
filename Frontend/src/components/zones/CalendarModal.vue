<script setup lang="ts">
import {onMounted, ref} from "vue";
import {createWorkerSchedule, deleteWorkerSchedule, updateWorkerSchedule} from "@/composables/DataUpdater";

const notification = ref<string | null>(null);

const props = defineProps({
  selectedSchedule: {
    type: Object,
    required: false,
  },
  worker: {
    type: Object,
    required: true,
  },
  date: {
    type: Object,
    required: true,
  }
});

const emit = defineEmits(["schedule-updated"]);

const startTime = ref(
    props.selectedSchedule?.startTime
        ? props.selectedSchedule.startTime.split("T")[1].slice(0, 5)
        : null
);
const endTime = ref(
    props.selectedSchedule?.endTime
        ? props.selectedSchedule.endTime.split("T")[1].slice(0, 5)
        : null
);

const updateSchedule = async () => {
  if (!startTime || !endTime) {
    notification.value = "Both start and end times are required.";
    return;
  } else if (startTime.value >= endTime.value) {
    notification.value = "Start time must be earlier than end time.";
    return;
  }

  if (props.selectedSchedule) {
    const date = props.selectedSchedule.startTime.split("T")[0];
    props.selectedSchedule.startTime = `${date}T${startTime.value}:00`;
    props.selectedSchedule.endTime = `${date}T${endTime.value}:00`;

    try {
      await updateWorkerSchedule(props.selectedSchedule);
      console.log("Schedule updated successfully");
      notification.value = null;
      emit("schedule-updated");
    } catch (error) {
      notification.value = "Failed to update schedule. Please try again.";
    }
  }
};

const createSchedule = async () => {
  if (!startTime || !endTime) {
    notification.value = "Both start and end times are required.";
    return;
  } else if (startTime.value >= endTime.value) {
    notification.value = "Start time must be earlier than end time.";
    return;
  }

  const newSchedule = {
    startTime: `${props.date.isoDate}T${startTime.value}:00`,
    endTime: `${props.date.isoDate}T${endTime.value}:00`,
  };

  try {
    await createWorkerSchedule(props.worker.id, newSchedule);
    notification.value = null;
    emit("schedule-updated");
  } catch (error) {
    notification.value = "Failed to create schedule. Please try again.";
  }
}

const deleteSchedule = async () => {
  if (props.selectedSchedule) {
    const userConfirmed = window.confirm("Are you sure you want to delete this schedule?");
    if (!userConfirmed) {
      return; // Exit if the user cancels
    }

    try {
      await deleteWorkerSchedule(props.selectedSchedule.id);
      notification.value = null;
      emit("schedule-updated");
    } catch (error) {
      notification.value = "Failed to delete schedule. Please try again.";
    }
  }
}

onMounted(() => {
  console.log("Selected Schedule:", props.selectedSchedule);
  console.log("Worker:", props.worker);
  console.log("Date", props.date.isoDate);
});
</script>

<template>
  <div class="modal-overlay">
    <div class="modal-content" @click.stop>
      <h3>Schedule Details</h3>
      <p><strong>Worker:</strong> {{ props.worker.name }}</p>
      <div class="calendar-modal-field">
        <label for="start-time"><strong>Start Time:</strong></label>
        <input id="start-time" type="time" v-model="startTime" />
      </div>
      <div class="calendar-modal-field">
        <label for="end-time"><strong>End Time:</strong></label>
        <input id="end-time" type="time" v-model="endTime" />
      </div>
      <p v-if="notification" class="error-message">{{ notification }}</p>
      <button v-if="!props.selectedSchedule?.startTime && !props.selectedSchedule?.endTime" @click="createSchedule">
        Create Schedule
      </button>
      <div v-else class="calendar-modal-actions">
        <button @click="updateSchedule">Update Schedule</button>
        <button @click="deleteSchedule" style="background-color: #fff" border="1px solid #000">
          <img src="/src/assets/icons/trash.svg" alt="Delete" width="20" height="20"/>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: var(--background-1);
  width: 20%;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal-content button {
  margin-top: 10px;
  padding: 10px 15px;
  background-color: var(--main-color);
  color: var(--text-1);
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-content button:hover {
  background-color: var(--main-color-2);
}

.modal-content h3 {
  margin-bottom: 10px;
  font-size: 24px;
}

.calendar-modal-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  gap: 20px;
}

.error-message {
  color: red;
  margin-top: 10px;
  font-weight: bold;
}

.calendar-modal-field {
  display: flex;
  justify-content: space-between;
  margin: 10px;
}
</style>