// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import Overview from '../views/Overview.vue';
import Zones from "../views/Zones.vue";
import Tasks from "../views/Tasks.vue";
import Staff from "../views/Staff.vue";
import Simulation from "../views/Simulation.vue";
import Info from "../views/Info.vue";
import WorkerProfile from "../views/WorkerProfile.vue";

const routes = [
  { path: '/', component: Overview },
  { path: '/zones', component: Zones },
  { path: '/staff', component: Staff },
  { path: '/tasks', component: Tasks },
  { path: '/simulation', component: Simulation},
  { path: '/info', component: Info},
  { path: '/worker', component: WorkerProfile},
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;