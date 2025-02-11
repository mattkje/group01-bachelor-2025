<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { useRoute } from 'vue-router';
  import overviewIcon from '../assets/icons/overview.svg';
  import zonesIcon from '../assets/icons/zones.svg';
  import staffIcon from '../assets/icons/staff.svg';
  import tasksIcon from '../assets/icons/tasks.svg';
  import simulationIcon from '../assets/icons/simulation.svg';
  import graphIcon from '../assets/icons/overview.svg';
  import overviewIconSelected from '../assets/icons/overviewSelected.svg';
  import zonesIconSelected from '../assets/icons/zonesSelected.svg';
  import staffIconSelected from '../assets/icons/staffSelected.svg';
  import tasksIconSelected from '../assets/icons/tasksSelected.svg';
  import simulationIconSelected from '../assets/icons/simulationSelected.svg';
  import graphIconSelected from '../assets/icons/overviewSelected.svg';

  const tabs = ref([
    { name: 'Overview', icon: overviewIcon, iconSelected: overviewIconSelected, path: '/' },
    { name: 'Zones', icon: zonesIcon, iconSelected: zonesIconSelected, path: '/zones' },
    { name: 'Staff', icon: staffIcon, iconSelected: staffIconSelected, path: '/staff' },
    { name: 'Tasks', icon: tasksIcon, iconSelected: tasksIconSelected, path: '/tasks' },
    { name: 'Simulation', icon: simulationIcon, iconSelected: simulationIconSelected, path: '/simulation' },
    { name: 'Graph', icon: graphIcon, iconSelected: graphIconSelected, path: '/graph' },
  ]);

  const route = useRoute();

  const activeTab = computed(() => {
    return tabs.value.find(tab => tab.path === route.path);
  });
  </script>

  <template>
    <div class="sidebar">
      <div class="logo">
        <img src="@/assets/logo.png" alt="Logo" class="logo-icon" />
        <span class="logo-text">Warehouse&nbsp;Workflow<br>Assigner™</span>
      </div>
      <div class="tabs">
        <router-link
          v-for="(tab, index) in tabs"
          :key="index"
          :to="tab.path"
          class="tab"
          :class="{ separator: index === 4 }"
          active-class="active-tab"
          exact-active-class="exact-active-tab"
        >
          <img :src="tab.path === activeTab.path ? tab.iconSelected : tab.icon" alt="" class="tab-icon" />
          <span>{{ tab.name }}</span>
        </router-link>
      </div>
      <button class="settings-button">
        <i class="settings-icon"></i>
        <span>Settings</span>
      </button>
    </div>
  </template>

  <style scoped>
  .sidebar {
    display: flex;
    flex-direction: column;
    height: 100vh;
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
    font-size: 1.1rem;
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
    text-decoration: none
  }

  .tab:hover {
    border-radius: 10px;
    background-color: #f0f0f0; /* Light grey background on hover */
  }

  .tab-icon {
    width: 15px;
    height: 15px;
    margin-right: 0.5rem;
    margin-left: 0.5rem;
  }

  .separator {
    margin-top: 1rem;
    border-top: 1px solid #7f8c8d;
    padding-top: 1rem;
  }

  .settings-button {
    display: flex;
    align-items: center;
    margin-top: auto;
    padding: 0.5rem 0;
    cursor: pointer;
    background: none;
    border: none;
    color: inherit;
  }

  .settings-button i {
    margin-right: 0.5rem;
  }

  .active-tab, .exact-active-tab {
    background-color: #FFF2F2;
    border-radius: 10px;
    color: #E77474;
  }
  </style>