<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { useRoute } from 'vue-router';
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
  import cpIcon from '../assets/icons/cp.svg';
  import cpIconSelected from '../assets/icons/cpSelected.svg';

  const tabs = ref([
    { name: 'Overview', icon: overviewIcon, iconSelected: overviewIconSelected, path: '/' },
    { name: 'Zones', icon: zonesIcon, iconSelected: zonesIconSelected, path: '/zones' },
    { name: 'Tasks', icon: tasksIcon, iconSelected: tasksIconSelected, path: '/tasks' },
    { name: 'Staff', icon: staffIcon, iconSelected: staffIconSelected, path: '/staff' },
    { separator: true },
    { name: 'Control Panel', icon: cpIcon, iconSelected: cpIconSelected, path: '/controlpanel' },
    { name: 'Info', icon: infoIcon, iconSelected: infoIconSelected, path: '/info' },

  ]);

  const route = useRoute();

  const activeTab = computed(() => {
    return tabs.value.find(tab => tab.path === route.path) || { path: '/', name: 'Overview', icon: overviewIcon, iconSelected: overviewIconSelected };
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
          active-class="active-tab"
          exact-active-class="exact-active-tab"
        >
          <img :src="tab.path === activeTab.path ? tab.iconSelected : tab.icon" alt="" class="tab-icon" />
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
      <img :src="route.path === '/settings' ? settingsIconSelected : settingsIcon" class="tab-icon" />
      <span>Settings</span>
    </router-link>
  </div>
</template>
  <style scoped>
  .sidebar {
    display: flex;
    flex-direction: column;
    width: 250px;
    background-color: #ffffff;
    color: #646464;
    padding: 1rem;
    border-right: #e0e0e0 1px solid;
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
    width: 20px;
    height: 20px;
    margin-right: 0.5rem;
    margin-left: 0.5rem;
  }

  .settings-button {
    border: none;
    padding: 0.5rem;
    display: flex;
    align-items: center;
    cursor: pointer;
    background: none;
    color: inherit;
  }

  .settings-button:hover{
    border-radius: 10px;
    background-color: #f0f0f0; /* Light grey background on hover */
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

  .active-tab, .exact-active-tab {
    background-color: #FFF2F2;
    border-radius: 10px;
    color: #E77474;
  }
  </style>