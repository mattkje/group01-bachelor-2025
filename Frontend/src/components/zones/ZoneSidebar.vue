<script setup lang="ts">
import { ref } from 'vue';
import overviewIcon from '@/assets/icons/overview.svg';
import tasksIcon from '@/assets/icons/tasks.svg';
import simulationIcon from '@/assets/icons/simulation.svg';

const tabs = ref([
  { name: 'Overview', icon: overviewIcon },
  { name: 'Tasks', icon: tasksIcon },
  { name: 'History', icon: simulationIcon },
]);

const activeTab = ref(tabs.value[0].name);
const emit = defineEmits(['tab-selected']);

const props = defineProps<{
  title: string;
}>();

const selectTab = (tabName: string) => {
  activeTab.value = tabName;
  emit('tab-selected', tabName);
};
</script>

<template>
  <div class="sidebar">
    <button class="close-button" @click="$emit('close')">&times;</button>
    <span class="logo-text">{{ title }}</span>
    <div class="tabs">
      <div
          v-for="tab in tabs"
          :key="tab.name"
          :class="['tab', { 'active-tab': tab.name === activeTab }]"
          @click="selectTab(tab.name)"
      >
        <img :src="tab.icon" alt="" class="tab-icon" />
        <span>{{ tab.name }}</span>
      </div>
    </div>
  </div>
</template>


<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 250px;
  background-color: #ffffff;
  color: #646464;
  padding: 1rem;
  border-right: #e0e0e0 1px solid;
}

.logo {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
}

.logo-icon {
  width: 40px;
  height: 40px;
  margin-right: 0.5rem;
}

.logo-text {
  padding-top: 0.3rem;
  width: 100%;
  text-align: center;
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
  line-height: 1rem;
  font-weight: bold;
}

.tabs {
  flex: 1;
}

.tab {
  display: flex;
  align-items: center;
  padding: 0.5rem 0;
  cursor: pointer;
  color: #7B7B7B;
  text-decoration: none;
}

.tab:hover {
  border-radius: 10px;
  background-color: #f0f0f0; /* Light grey background on hover */
}

.tab-icon {
  width: 20px;
  height: 20px;
  margin-right: 0.5rem;
  margin-left: 0.5rem;
}

.separator {
  margin-top: 1rem;
  border-top: 1px solid #7f8c8d;
  padding-top: 1rem;
}

.settings-button {
  border: none;
  padding-top: 1rem;
  margin-top: 1rem;
  display: flex;
  align-items: center;
  cursor: pointer;
  background: none;
  color: inherit;
}

hr {
  margin-top: 1rem;
  margin-bottom: 1rem;
  border: 0;
  border-top: 1px solid #e0e0e0;
}

.settings-button img {
  margin-right: 0.5rem;
  width: 20px;
  height: 20px;
}

.active-tab {
  background-color: #FFF2F2;
  border-radius: 10px;
  color: #E77474;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.8rem;
  color: #646464;

  cursor: pointer;
  align-self: flex-start;
}

.close-button:hover {
  color: #e77474;
}
</style>