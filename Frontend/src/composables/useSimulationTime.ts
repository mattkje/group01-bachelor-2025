import {ref, onMounted, onUnmounted} from "vue";
import {fetchSimulationTime, fetchSimulationDate, fetchDoneBy} from "@/services/DataFetcher";

interface NotificationDoneObject {
    time: string;
}

interface SimulationTimeComposable {
    currentTime: ReturnType<typeof ref<string>>;
    currentDate: ReturnType<typeof ref<string>>;
    completionTime: ReturnType<typeof ref<string>>;
    startUpdatingTime: () => Promise<void>;
    stopUpdatingTime: () => void;
}

/**
 * Composable to manage simulation time and date.
 */
export function useSimulationTime(): SimulationTimeComposable {
    const currentTime = ref<string>("00:00");
    const currentDate = ref<string>("00/00/0000");
    const completionTime = ref<string>("00:00");
    let intervalId: ReturnType<typeof setInterval> | null = null;

    const updateTime = async () => {
        try {
            currentTime.value = await fetchSimulationTime();
            currentDate.value = await fetchSimulationDate();
            const notificationDoneObject: NotificationDoneObject = await fetchDoneBy();
            completionTime.value = notificationDoneObject.time;
        } catch (error) {
            console.log("Could not fetch date, Retrying...");
            currentTime.value = "00:00";
        }
    };

    const startUpdatingTime = async () => {
        await updateTime();
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

    return {currentTime, currentDate, completionTime, startUpdatingTime, stopUpdatingTime};
}