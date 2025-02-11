<script setup lang="ts">
    import { ref } from 'vue';
    import overviewIcon from '../assets/icons/overview.svg';
    import zonesIcon from '../assets/icons/zones.svg';
    import staffIcon from '../assets/icons/staff.svg';
    import tasksIcon from '../assets/icons/tasks.svg';
    import simulationIcon from '../assets/icons/simulation.svg';
    import graphIcon from '../assets/icons/graph.svg';


    const tabs = ref([
      { name: 'Overview', icon: 'overview-icon', path: '/' },
      { name: 'Zones', icon: 'zones-icon', path: '/zones' },
      { name: 'Staff', icon: 'staff-icon', path: '/staff' },
      { name: 'Tasks', icon: 'tasks-icon', path: '/tasks' },
      { name: 'Simulation', icon: 'simulation-icon', path: '/simulation' },
      { name: 'Graph', icon: 'graph-icon', path: '/graph' },
    ]);
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
            <i :class="tab.icon"></i>
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
  text-decoration: none;
}

.tab:hover {
  border-radius: 10px;
  background-color: #f0f0f0; /* Light grey background on hover */
}

.tab i {
  margin-right: 0.5rem;
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