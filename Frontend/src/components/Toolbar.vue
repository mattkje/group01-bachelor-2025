<script setup lang="ts">
    const props = defineProps({
      title: {
        type: String,
        required: true
      },
    });
    </script>

    <template>
      <div v-if="title === 'Zones'" class="toolbar">
        <div class="toolbar-title">{{ props.title }}</div>
        <button class="toolbar-item">
          <img src="/src/assets/icons/overview.svg" alt="Assign" />
        </button>
        <button class="toolbar-item">
          <img src="/src/assets/icons/tasks.svg" alt="Assign" />
        </button>
        <button class="toolbar-item">
          <img src="/src/assets/icons/simulation.svg" alt="Assign" />
        </button>
        <button v-if="false" class="toolbar-item">
          <img src="/src/assets/icons/bell.svg" alt="Assign" />
        </button>
        <button v-if="true" class="toolbar-item">
          <img src="/src/assets/icons/bellUpdate.svg" alt="Assign" />
        </button>
      </div>
      <div v-if="title === 'Tasks'" class="toolbar">
        <div class="toolbar-title">{{ props.title }}</div>
        <button class="toolbar-item">
          <img src="/src/assets/icons/edit.svg" alt="Assign" />
        </button>
        <button class="toolbar-item">
          <img src="/src/assets/icons/trash.svg" alt="Assign" />
        </button>
        <button class="toolbar-item">
          <img src="/src/assets/icons/add.svg" alt="Assign" />
        </button>
        <button v-if="false" class="toolbar-item">
          <img src="/src/assets/icons/bell.svg" alt="Assign" />
        </button>
        <button v-if="true" class="toolbar-item">
          <img src="/src/assets/icons/bellUpdate.svg" alt="Assign" />
        </button>
      </div>
      <div v-if="title === 'Staff'" class="toolbar">
        <div class="toolbar-title">{{ props.title }}</div>
      </div>
      <div v-if="title === 'Overview'" class="toolbar">
        <div class="toolbar-title">{{ props.title }}</div>

      </div>
      <div v-if="title === 'Simulation'" class="toolbar">
        <div class="toolbar-title">{{ props.title }}</div>
        <button class="toolbar-item">
          <img src="/src/assets/icons/edit.svg" alt="Assign" />
        </button>
        <button class="toolbar-item">
          <img src="/src/assets/icons/trash.svg" alt="Assign" />
        </button>
        <button class="toolbar-item">
          <img src="/src/assets/icons/add.svg" alt="Assign" />
        </button>
        <button v-if="false" class="toolbar-item">
          <img src="/src/assets/icons/bell.svg" alt="Assign" />
        </button>
        <button v-if="true" class="toolbar-item">
          <img src="/src/assets/icons/bellUpdate.svg" alt="Assign" />
        </button>
      </div>
      <div v-if="title === 'Worker Profile'" class="toolbar">
        <div class="toolbar-title">{{ props.title }}</div>
      </div>
    </template>

  <style scoped>
  .toolbar {
    display: flex;
    justify-content: flex-end;
    background-color: #ffffff;
    padding: 1rem;
    height: 4rem;
    border-bottom: 1px solid #e0e0e0;
    position: relative;
  }

  .toolbar-title {
    position: absolute;
    left: 1rem;
    font-size: 1.5rem;
    font-weight: bold;
  }

  .toolbar-item {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1rem;
    width: 30px; /* Ensure consistent width */
    height: 30px; /* Ensure consistent height */
    margin-right: 1rem;
    color: #b77979;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .toolbar-item img {
    width: 100%; /* Make the image take the full width of the button */
    height: auto; /* Maintain aspect ratio */
  }

  .toolbar-item:hover {
    color: #000;
  }
  </style>
