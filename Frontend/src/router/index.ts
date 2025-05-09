// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import Overview from '../views/Overview.vue';
import Zones from "../views/Zones.vue";
import Tasks from "../views/Tasks.vue";
import Staff from "../views/Staff.vue";
import Settings from "../views/Settings.vue";
import Info from "../views/Info.vue";
import WorkerProfile from "../views/WorkerProfile.vue";
import ZoneMenu from "../views/ZoneMenu.vue";
import TaskMenu from "../views/TaskMenu.vue";

const routes = [
  { path: '/', component: Overview },
  { path: '/zones', component: Zones },
  { path: '/staff', component: Staff },
  { path: '/tasks', component: Tasks },
  { path: '/settings', component: Settings},
  { path: '/info', component: Info},
  { path: '/workers/:id', component: WorkerProfile},
  { path: '/zones/:id', component: ZoneMenu },
  { path: '/zones/:id/tasks', component: TaskMenu },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;