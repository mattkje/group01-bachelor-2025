// Composable: useSimulationTime.ts
  import { ref, onMounted, onUnmounted } from "vue";
  import {fetchSimulationTime, fetchSimulationDate, fetchDoneBy} from "@/composables/DataFetcher";

  export function useSimulationTime() {
    const currentTime = ref("00:00");
    const currentDate = ref("00/00/0000");
    const completionTime = ref("00:00");
    let intervalId: ReturnType<typeof setInterval> | null = null;

    const updateTime = async () => {
      try {
        currentTime.value = await fetchSimulationTime();
        currentDate.value = await fetchSimulationDate();
        completionTime.value = await fetchDoneBy().then(response => response.time);
      } catch (error) {
      }
    };

    const startUpdatingTime = () => {
      updateTime();
      intervalId = setInterval(updateTime, 1000);
    };

    const stopUpdatingTime = () => {
      if (intervalId) {
        clearInterval(intervalId);
        intervalId = null;
      }
    };

    onMounted(startUpdatingTime);
    onUnmounted(stopUpdatingTime);

    return { currentTime, currentDate, completionTime, startUpdatingTime, stopUpdatingTime };
  }