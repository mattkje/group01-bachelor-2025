// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import Overview from '../views/Overview.vue';
import Zones from "@/views/Zones.vue";

const routes = [
  { path: '/', component: Overview },
  { path: '/zones', component: Zones }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;