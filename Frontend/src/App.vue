<script setup lang="ts">
import Sidebar from './components/Sidebar.vue';
import Toolbar from "@/components/Toolbar.vue";
</script>

<template>
  <div class="app-container">
    <Toolbar class="toolbar" title="Simulator"/>
    <div class="page-border">
      <Sidebar />
      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  height: 100vh;
  width: 100%;
  padding: 1rem;
  box-sizing: border-box;
}

.page-border {
  display: flex;
  width: 100%;
  height: 100%;
  border-radius: 1.5rem;
  border: 1px solid #e0e0e0;
  overflow: hidden;
  background-color: #ffffff;
}

.main-content {
  flex: 1;
  padding: 0;
}

.toolbar {
  width: 100%;
  height: 5rem;
  background-color: #ffffff;
  border-radius: 1.5rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  padding: 0 1rem;
  position: relative;
}

header {
  line-height: 1.5;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>