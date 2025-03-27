<script setup lang="ts">
import { ref } from 'vue';
const settings = ref({
  notification: true,
  theme: 'light',
  language: 'en',
});

const saveSettings = () => {
  // Save settings logic here
  console.log('Settings saved:', settings.value);
};

const resetSettings = () => {
  settings.value = {
    notification: true,
    theme: 'light',
    language: 'en',
  };
};
</script>

<template>
  <div class="settings">
    <h1>Settings</h1>
    <form @submit.prevent="saveSettings">
      <div class="form-group">
        <label for="notification">Enable Notifications</label>
        <input type="checkbox" id="notification" v-model="settings.notification" />
      </div>
      <div class="form-group">
        <label for="theme">Theme</label>
        <select id="theme" v-model="settings.theme">
          <option value="light">Light</option>
          <option value="dark">Dark</option>
        </select>
      </div>
      <div class="form-group">
        <label for="language">Language</label>
        <select id="language" v-model="settings.language">
          <option value="en">English</option>
          <option value="es">Spanish</option>
          <option value="fr">French</option>
        </select>
      </div>
      <button type="submit">Save Settings</button>
      <button type="button" @click="resetSettings">Reset to Default</button>
    </form>
  </div>
</template>

<style scoped>
.settings {
  padding: 2rem;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
}

input[type="checkbox"] {
  margin-right: 0.5rem;
}

select {
  display: block;
  width: 100%;
  padding: 0.5rem;
  margin-top: 0.5rem;
}

button {
  margin-right: 1rem;
  padding: 0.5rem 1rem;
}
</style>