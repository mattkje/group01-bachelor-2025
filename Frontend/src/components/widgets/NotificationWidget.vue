<script setup lang="ts">
import {ErrorCodes, Notification, Zone} from "@/assets/types";
import {fetchNotifications} from "@/composables/DataFetcher";
import {onMounted, onUnmounted, ref, watch} from "vue";

const props = defineProps<{
  zone: Zone;
}>();

const notifications = ref<Notification[]>([]);
const messages = ref<string[]>([]);
const zoneId = ref<number>(0);
let intervalId: ReturnType<typeof setInterval> | null = null;

const loadAndHandleNotifications = async () => {

  notifications.value = await fetchNotifications(props.zone.id);
  messages.value = [];
  // Parse the notifications ","
  notifications.value.forEach(notification => {
    const parsedMessage = notification.message.split(',').map((message: string) => message.trim().replace(/\[|\]/g, ''));

    // Also split message by ";" if it exists
    parsedMessage.forEach(message => {
      const parts = message.split(':');
      zoneId.value = notification.zoneId;
      identifyNotificationType(parts, notification.zoneId);
    });
  });
};

const identifyNotificationType = (parsedMessage: string[], zoneId: number) => {
  if (parsedMessage.length < 2) {
    return;
  }
  const zone = `Error Zone ${zoneId}`;
  const error = ErrorCodes.get(parseInt(parsedMessage[0])) || "Unknown Error";
  const details = parsedMessage.slice(1).filter(item => item !== "null").join(" | ");

  messages.value.push([zone, error, "Task(s): " + details].filter(Boolean).join(" | "));
};

// Watch for changes in the zone and reload notifications every 5 seconds
watch(() => props.zone, async (newZone) => {
  if (intervalId) {
    clearInterval(intervalId);
  }
  await loadAndHandleNotifications();
  intervalId = setInterval(loadAndHandleNotifications, 5000);
}, { immediate: true });

onMounted(() => {
  intervalId = setInterval(loadAndHandleNotifications, 5000);
});

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId);
  }
});

</script>

<template>
  <div class="overview-widget">
    <router-link class="notification" :to="`/zones/${zoneId}/tasks`" v-if="messages.length > 0">
      <div
        v-for="message in messages"
        :key="message"
        class="notification-item"
      >
        <img  src="@/assets/icons/info.svg" alt="Notification Icon" />
      <p>{{ message }}</p>
      </div>
    </router-link>
    <div class="no-notifications" v-else>
      <p>No Notifications</p>
    </div>
  </div>
</template>

<style scoped>
.overview-widget {
  margin: 0 !important;
  width: 100%;
  height: 100%;
  border: 1px solid var(--border-1);
  flex-direction: column;
  border-radius: 1rem;
  display: flex;
  justify-content: flex-start !important;
  align-items: flex-start !important;
  font-size: 1.2rem;
  color: var(--text-1);
  padding: 0 1rem;
  overflow: auto;
  position: relative; /* Required for the pseudo-element */
}

.overview-widget::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2rem;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0), var(--background-1));
  pointer-events: none;
}

.overview-widget h2 {
  font-size: 1.2rem;
  align-self: flex-start;
  color: var(--text-1);
  margin-bottom: 0.5rem;
}

.notification {
  padding: 0;
  width: 100%;
  text-decoration: none;
  overflow: auto;
}

.notification-item {
  margin-top: 1rem;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  gap: 0.5rem;
  background-color: var(--background-2);
  border: 1px solid var(--border-2);
  border-radius: 0.3rem;
  padding: 0.2rem;
  font-size: 0.7rem;
  transition: background-color 0.1s ease 0.1s, border 0.1s ease 0.1s;
}

.notification-item:hover {
  background-color: var(--main-color-3);
  border: 2px solid var(--main-color);
  cursor: pointer;
}

.notification-item p {
  margin: 0;
  color: var(--text-2);
}

.notification-item img {
  width: 20px;
  height: 20px;
}

.no-notifications {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  font-size: 1rem;
  color: var(--text-2);
}

</style>