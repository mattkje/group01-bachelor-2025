<script setup lang="ts">
import Sidebar from './components/Sidebar.vue';
</script>

<template>
  <div class="app-container">
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
  justify-content: flex-start;
  align-items: center;
  height: 100vh;
  width: calc(100vw - 1rem);
  background-color: #ffffff; /* Set background to white */
  padding: 0 1rem;
  box-sizing: border-box;
}

.page-border {
  display: flex;
  width: 100%;
  height: 97vh;
  border-radius: 30px;
  border: 1px solid #e0e0e0; /* Add a border */
  overflow: hidden;
  background-color: #ffffff; /* Ensure the inner box is also white */
}

.main-content {
  flex: 1;
  background-color: #ffffff; /* Set background to white */
  padding: 1rem;
  overflow-y: auto; /* Allow vertical scrolling if content overflows */
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