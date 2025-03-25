<template>
      <div class="notification-bubble">
        <div v-for="(notification, index) in parsedMessages" :key="index" class="notification-item">
          <p class="notification-title">{{ notification.title }}</p>
          <p class="notification-reason">{{ notification.reason }}</p>
        </div>
      </div>
    </template>

    <script setup lang="ts">
    import {computed} from 'vue';

    const props = defineProps<{ messages: string[] }>();

    const parsedMessages = computed(() => {
      return props.messages.slice(1).map(message => {
        const parts = message.split('-');
        const content = parts.length > 1 ? parts[1].trim() : message;
        const [title, reason] = content.split('!');
        return {title: title.trim(), reason: reason.trim()};
      });
    });
    </script>

    <style scoped>
    .notification-bubble {
      position: absolute;
      margin-top: 100px;
      border-radius: 5px;
      padding: 10px;
      z-index: 1000;
    }

    .notification-item {
      border: 1px solid #ddd;
      background-color: #FFF2F2;
      border-radius: 5px;
      padding: 10px;
      margin-bottom: 10px;
    }

    .notification-title {
      font-weight: bold;
      margin-bottom: 5px;
    }

    .notification-reason {
      margin: 0;
    }
    </style>