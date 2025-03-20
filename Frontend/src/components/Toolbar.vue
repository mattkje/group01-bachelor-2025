<script setup lang="ts">
import {onMounted, ref} from "vue";

    const props = defineProps({
      title: {
        type: String,
        required: true
      },
    });

const currentTime = ref(new Date().toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', hour12: false }));

const updateTime = () => {
  currentTime.value = new Date().toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', hour12: false });
};

let intervalId: number | null = null;

const startClock = () => {
  if (!intervalId) {
    intervalId = setInterval(updateTime, 1000);
  }
};

const stopClock = () => {
  if (intervalId) {
    clearInterval(intervalId);
    intervalId = null;
  }
};

const fastForwardClock = () => {
  if (intervalId) {
    clearInterval(intervalId);
  }
  intervalId = setInterval(updateTime, 500);
};

onMounted(() => {
  startClock();
});
    </script>

    <template>
      <div class="toolbar">
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
        <div class="vertical-separator"/>
        <div class="controls">
          <button @click="startClock">
            <img src="/src/assets/icons/play.svg" alt="Play" />
          </button>
          <button @click="fastForwardClock">
            <img src="/src/assets/icons/skip.svg" alt="Fast Forward" />
          </button>
        </div>
        <div class="vertical-separator"/>
        <div class="clock">
          <p>Time</p>
          <span>{{ currentTime.split(':')[0] }}</span>
          <span class="blink">:</span>
          <span>{{ currentTime.split(':')[1] }}</span>
        </div>
        <div class="vertical-separator"/>
        <div class="clock-done">
          <p>Done By</p>
          <span>Finished</span>
        </div>
      </div>
    </template>

  <style scoped>
  .toolbar {
    display: flex;
    justify-content: flex-end;
    background-color: #ffffff;
    align-items: center;
    padding-right: 1rem;
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
  .vertical-separator {
    border-left: 1px solid #e0e0e0;
    height: 100%;
    margin: 0 1rem;
  }

  .controls {
    display: flex;
    align-items: center;
  }

  .controls button {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1rem;
    width: 30px; /* Ensure consistent width */
    height: 30px; /* Ensure consistent height */
    color: #b77979;
    display: flex;
    margin: 0 0.5rem;
    align-items: center;
    justify-content: center;
  }

  .clock {
    line-height: 1.5rem;
    align-self: center;
  }

  .clock span {
    font-size: 2rem;
    font-weight: bold;
  }

  .clock p {
    font-size: 0.7rem;
    font-weight: bold;
  }

  .clock-done {
    line-height: 1.5rem;
    align-self: center;
  }

  .clock-done span
  {
    color: #c94343;
    font-size: 2rem;
    font-weight: bold;
  }

  .clock-done p {
    color: #c94343;
    font-size: 0.7rem;
    font-weight: bold;
  }

  .blink {
    animation: blink 3s linear infinite;
  }

  @keyframes blink {
    0% {
      opacity: 1;
    }
    20% {
      opacity: 0;
    }
    40% {
      opacity: 1;
    }
  }
  </style>
