<script setup lang="ts">
    import { ref } from 'vue';

    const date = ref(""); // Store the selected date

    const buttons = [
      { label: "Run WorldSim", action: () => runWorldSimulation(date.value) },
      { label: "Start Model", action: () => startModel() },
      { label: "Create CSV", action: () => createCSV() },
    ];

    const runWorldSimulation = (date: string) => {
      if (!date) {
        alert("Please select a date before running the simulation.");
        return;
      }
      fetch(`http://localhost:8080/api/generate-active-tasks/${date}/1`)
        .then(response => {
          if (response.ok) {
            alert("World simulation started successfully!");
          } else {
            alert("Failed to start world simulation.");
          }
        })
        .catch(error => {
          alert("An error occurred while starting the world simulation.");
          console.error(error);
        });
    };

    const startModel = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/ml/start-model');
        if (response.ok) {
          alert("Model started successfully!");
        } else {
          alert("Failed to start model.");
        }
      } catch (error) {
        alert("An error occurred while starting the model.");
        console.error(error);
      }
    };

    const createCSV = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/ml/create-csv');
        if (response.ok) {
          alert("CSV created successfully!");
        } else {
          alert("Failed to create CSV.");
        }
      } catch (error) {
        alert("An error occurred while creating the CSV.");
        console.error(error);
      }
    };

    const handleButtonClick = (action: () => void) => {
      action();
    };
    </script>

    <template>
      <div class="control-panel">
        <h1>Control Panel</h1>
        <div class="date-picker">
          <label for="date">Select Date for world sim:</label>
          <input id="date" type="date" v-model="date" />
        </div>
        <div class="button-grid">
          <button
            v-for="(button, index) in buttons"
            :key="index"
            class="grid-button"
            @click="handleButtonClick(button.action)"
          >
            {{ button.label }}
          </button>
        </div>
      </div>
    </template>

    <style scoped>
    .control-panel {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      height: 100%;
    }

    .date-picker {
      margin-bottom: 20px;
    }

    .button-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
      gap: 10px;
      width: 50%;
    }

    .grid-button {
      padding: 10px;
      font-size: 16px;
      background-color: #E77474;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      transition: background-color 0.3s ease;
    }

    .grid-button:hover {
      background-color: #9d4d4d;
    }
    </style>