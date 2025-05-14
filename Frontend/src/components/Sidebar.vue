<script setup lang="ts">
import {ref, computed} from 'vue';
import {useRoute} from 'vue-router';
import overviewIcon from '../assets/icons/factory.svg';
import zonesIcon from '../assets/icons/zones.svg';
import staffIcon from '../assets/icons/staff.svg';
import tasksIcon from '../assets/icons/tasks.svg';
import settingsIcon from '../assets/icons/settings.svg';
import infoIcon from '../assets/icons/info.svg';
import overviewIconSelected from '../assets/icons/factorySelected.svg';
import zonesIconSelected from '../assets/icons/zonesSelected.svg';
import staffIconSelected from '../assets/icons/staffSelected.svg';
import tasksIconSelected from '../assets/icons/tasksSelected.svg';
import settingsIconSelected from '../assets/icons/settingsSelected.svg';
import infoIconSelected from '../assets/icons/infoSelected.svg';

type TabItem = {
  name: string;
  icon: string;
  iconSelected: string;
  path: string;
  separator?: never;
} | {
  separator: true;
  name?: never;
  icon?: never;
  iconSelected?: never;
  path?: never;
}

const tabs = ref<TabItem[]>([
  {name: 'Dashboard', icon: overviewIcon, iconSelected: overviewIconSelected, path: '/'},
  {name: 'Zones', icon: zonesIcon, iconSelected: zonesIconSelected, path: '/zones'},
  {name: 'Tasks', icon: tasksIcon, iconSelected: tasksIconSelected, path: '/tasks'},
  {name: 'Staff', icon: staffIcon, iconSelected: staffIconSelected, path: '/staff'},
  {separator: true},
  {name: 'Info', icon: infoIcon, iconSelected: infoIconSelected, path: '/info'},
]);

const route = useRoute();

const activeTab = computed(() => {
  const firstSegment = `/${route.path.split('/')[1]}`;
  return tabs.value.find(tab => tab.path === firstSegment)?.path || tabs.value[0].path;
});

</script>

<template>
  <div class="sidebar">
    <div class="tabs">
      <template v-for="(tab, index) in tabs" :key="index">
        <hr v-if="tab.separator">
        <router-link
            v-else
            :to="tab.path"
            class="tab"
            :class="{ 'active-tab': tab.path === activeTab }"
            active-class="active-tab"
            exact-active-class="exact-active-tab"
        >
          <img :src="tab.path === activeTab ? tab.iconSelected : tab.icon" alt="" class="tab-icon"/>
          <span>{{ tab.name }}</span>
        </router-link>
      </template>
    </div>
    <hr>
    <router-link
        to="/settings"
        class="tab"
        active-class="active-tab"
        exact-active-class="exact-active-tab"
    >
      <img :src="route.path === '/settings' ? settingsIconSelected : settingsIcon" class="tab-icon" alt="No"/>
      <span>Settings</span>
    </router-link>
  </div>
</template>
<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  width: 250px;
  background-color: var(--background-1);
  padding: 1rem;
  border-right: var(--border-1) 1px solid;
}

.tabs {
  flex: 1;
}

.tab {
  display: flex;
  align-items: center;
  padding: 0.5rem 0;
  cursor: pointer;
  color: var(--text-2);
  text-decoration: none;
  border-radius: 0.7rem;
}

.tab:hover {
  background-color: var(--background-2);
}

.tab-icon {
  width: 20px;
  height: 20px;
  margin-right: 0.5rem;
  margin-left: 0.5rem;
}

hr {
  margin-top: 1rem;
  margin-bottom: 1rem;
  border: 0;
  border-top: 1px solid var(--border-1);
}

.settings-button img {
  margin-right: 0.5rem;
  width: 20px;
  height: 20px;
}

.active-tab, .exact-active-tab {
  background-color: var(--main-color-3);
  border-radius: 10px;
  color: var(--main-color);
}

@media (max-width: 1400px) {
  .tab span {
    display: none;
  }

  .tab {
    width: 2.5rem;
    height: 2.5rem;
    padding: 0.5rem;
    border-radius: 0.7rem;
    justify-content: center;
  }

  .tab-icon {
    margin-right: 0;
    margin-left: 0;
    width: 25px;
    height: 25px;
  }

  .sidebar {
    width: min-content;
    padding: 0.5rem;
  }

}

</style>