<template>
        <Toolbar title="Tasks"/>
        <div class="content">
          <div class="main-content">
            <h1>Active Tasks</h1>
            <table class="task-table">
              <thead>
                <tr>
                  <th @click="sortTable('name')">Task Name</th>
                  <th @click="sortTable('description')">Description</th>
                  <th @click="sortTable('estimatedTime')">Estimated Time</th>
                  <th @click="sortTable('zone')">Zone</th>
                  <th @click="sortTable('workers')">Workers</th>
                  <th @click="sortTable('requiredLicenses')">Required Licenses</th>
                  <th @click="sortTable('deadline')">Deadline</th>
                  <th @click="sortTable('status')">Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="task in paginatedTasks" :key="task.id">
                  <td>{{ task.name }}</td>
                  <td>{{ task.description }}</td>
                  <td>{{ task.estimatedTime }}</td>
                  <td>{{ task.zone }}</td>
                  <td>{{ task.workers }}</td>
                  <td>{{ task.requiredLicenses }}</td>
                  <td>{{ task.deadline }}</td>
                  <td>{{ task.status }}</td>
                  <td>
                    <button @click="editTask(task)">Edit</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div class="pagination">
              <button @click="prevPage" :disabled="currentPage === 1">Previous</button>
              <span>Page {{ currentPage }} of {{ totalPages }}</span>
              <button @click="nextPage" :disabled="currentPage === totalPages">Next</button>
            </div>
          </div>
          <div class="filter-menu">
            <h3>Filter Tasks</h3>
            <input type="text" v-model="searchQuery" placeholder="Search tasks..." />
            <label for="statusFilter">Status:</label>
            <select v-model="statusFilter" id="statusFilter">
              <option value="">All</option>
              <option value="Pending">Pending</option>
              <option value="In Progress">In Progress</option>
              <option value="Completed">Completed</option>
            </select>
            <h4>Zones</h4>
            <div v-for="zone in zones" :key="zone.id">
              <input type="checkbox" :id="'zone-' + zone.id" :value="zone.id" v-model="selectedZones" />
              <label :for="'zone-' + zone.id">{{ zone.name }}</label>
            </div>
          </div>
        </div>
  <div v-if="showModal" class="modal">
    <div class="modal-content">
      <span class="close" @click="closeModal">&times;</span>
      <h2>Edit Task</h2>
      <form @submit.prevent="saveTask">
        <label for="name">Task Name:</label>
        <input type="text" v-model="currentTask.name" id="name" required />

        <label for="description">Description:</label>
        <input type="text" v-model="currentTask.description" id="description" required />

        <label for="estimatedTime">Estimated Time:</label>
        <input type="text" v-model="currentTask.estimatedTime" id="estimatedTime" required />

        <label for="zone">Zone:</label>
        <input type="number" v-model="currentTask.zone" id="zone" required />

        <label for="workers">Workers:</label>
        <input type="text" v-model="currentTask.workers" id="workers" required />

        <label for="requiredLicenses">Required Licenses:</label>
        <input type="text" v-model="currentTask.requiredLicenses" id="requiredLicenses" required />

        <label for="deadline">Deadline:</label>
        <input type="date" v-model="currentTask.deadline" id="deadline" required />

        <label for="status">Status:</label>
        <input type="text" v-model="currentTask.status" id="status" required />

        <button type="submit">Save</button>
      </form>
    </div>
  </div>
      </template>

      <script setup lang="ts">
      import { ref, computed, onMounted } from 'vue';
      import Toolbar from "@/components/Toolbar.vue";

      interface Task {
        id: number;
        name: string;
        description: string;
        estimatedTime: string;
        zone: number;
        workers: string;
        requiredLicenses: string;
        deadline: string;
        status: string;
      }

      interface Zone {
        id: number;
        name: string;
      }

      const tasks = ref<Task[]>([]);
      const activeTasks = ref<Task[]>([]);
      const zones = ref<Zone[]>([]);

      const currentPage = ref(1);
      const tasksPerPage = 8;
      const searchQuery = ref('');
      const showModal = ref(false);
      const currentTask = ref<Task | null>(null);
      const statusFilter = ref('');
      const selectedZones = ref<number[]>([]);

      const fetchTasks = async () => {
        try {
          const response = await fetch('http://localhost:8080/api/tasks');
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          const data = await response.json();
          tasks.value = data;
        } catch (error) {
          console.error('Failed to fetch tasks:', error);
        }
      };

      const fetchActiveTasks = async () => {
        try {
          const response = await fetch('http://localhost:8080/api/active-tasks');
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          const data = await response.json();
          activeTasks.value = data;
        } catch (error) {
          console.error('Failed to fetch tasks:', error);
        }
      };

      const fetchZones = async () => {
        try {
          const response = await fetch('http://localhost:8080/api/zones');
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          const data = await response.json();
          zones.value = data;
        } catch (error) {
          console.error('Failed to fetch zones:', error);
        }
      };

      onMounted(() => {
        fetchTasks();
        fetchActiveTasks();
        fetchZones();
      });

      const filteredTasks = computed(() => {
        return tasks.value
          .filter(task => task.name.toLowerCase().includes(searchQuery.value.toLowerCase()))
          .filter(task => !statusFilter.value || task.status === statusFilter.value)
          .filter(task => selectedZones.value.length === 0 || selectedZones.value.includes(task.zone));
      });

      const paginatedTasks = computed(() => {
        const start = (currentPage.value - 1) * tasksPerPage;
        const end = start + tasksPerPage;
        return filteredTasks.value.slice(start, end);
      });

      const totalPages = computed(() => {
        return Math.ceil(filteredTasks.value.length / tasksPerPage);
      });

      const nextPage = () => {
        if (currentPage.value < totalPages.value) {
          currentPage.value++;
        }
      };

      const prevPage = () => {
        if (currentPage.value > 1) {
          currentPage.value--;
        }
      };

      const sortTable = (key: keyof Task) => {
        tasks.value.sort((a, b) => {
          if (a[key] < b[key]) return -1;
          if (a[key] > b[key]) return 1;
          return 0;
        });
      };

      const editTask = (task: Task) => {
        currentTask.value = { ...task };
        showModal.value = true;
      };

      const closeModal = () => {
        showModal.value = false;
      };

      const saveTask = () => {
        if (currentTask.value) {
          const index = tasks.value.findIndex(task => task.id === currentTask.value!.id);
          if (index !== -1) {
            tasks.value[index] = { ...currentTask.value };
          }
          closeModal();
        }
      };
      </script>

      <style scoped>
      .content {
        display: flex;
        margin: 0;
        height: calc(100% - 4rem);
      }

      .main-content {
        padding: 1rem;
        flex: 1;
      }

      .filter-menu {
        position: sticky;
        width: 280px;
        height: 100%;
        padding: 1rem;
        border-left: 1px solid #ccc;
        background-color: #f9f9f9;
      }

      .task-table {
        width: 100%;
        border-collapse: separate;
        border-spacing: 0;
        margin-bottom: 1rem;
        border-radius: 8px;
        overflow: hidden;
        border: #e1e1e1 1px solid;
      }

      .task-table th, .task-table td {
        padding: 0.8vh;
        text-align: left;
      }

      .task-table th {
        background-color: #f4f4f4;
        font-weight: bold;
        cursor: pointer;
      }

      .task-table tbody tr {
        transition: background-color 0.3s;
      }

      .task-table tbody tr:hover {
        background-color: #f9f9f9;
      }

      .task-table tbody tr:nth-child(even) {
        background-color: #f8f8f8;
      }

      .pagination {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .modal {
        display: flex;
        justify-content: center;
        align-items: center;
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
      }

      .modal-content {
        background-color: #fff;
        padding: 2rem;
        border-radius: 8px;
        width: 500px;
        max-width: 100%;
      }

      .close {
        position: absolute;
        top: 10px;
        right: 10px;
        font-size: 1.5rem;
        cursor: pointer;
      }
      </style>