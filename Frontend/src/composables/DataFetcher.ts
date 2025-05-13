import {ActiveTask, License, Notification, PickerTask, Task, Worker, Zone} from "@/assets/types";
import { fetchData } from "@/composables/HttpMethods";

export const fetchAllMonteCarloGraphData = async (zoneId: number) => {
    const data = await fetchData<any>(`http://localhost:8080/api/data/${zoneId}/graph-data`);
    return {
        realData: data.realData || [0],
        simulationData: data.simulationData || [],
        activeTasks: data.activeTasks || 0,
        currentDate: data.currentDate || "",
    };
};

export const fetchZone = async (zoneId: number) => {
    return fetchData<Zone>(`http://localhost:8080/api/zones/${zoneId}`);
};

export const fetchAllZones = async (): Promise<Zone[]> => {
    return fetchData<Zone[]>('http://localhost:8080/api/zones');
};

export const fetchAllPickerZones = async (): Promise<Zone[]> => {
    return fetchData<Zone[]>('http://localhost:8080/api/zones/picker-zones');
}

export const fetchWorker = async (workerId: number) => {
    return fetchData<Worker>(`http://localhost:8080/api/workers/${workerId}`);
}

export const fetchWorkersFromActiveTask = async (activeTaskId: number) => {
    return fetchData<Worker[]>(`http://localhost:8080/api/active-tasks/${activeTaskId}/workers`);
}

export const fetchWorkersForZone = async (zoneId: number) => {
    return fetchData<Worker[]>(`http://localhost:8080/api/zones/${zoneId}/workers`);
}
export const fetchAllWorkers = async () => {
    return fetchData<Worker[]>('http://localhost:8080/api/workers');
}

export const fetchLicenses = async () => {
    return fetchData<License[]>('http://localhost:8080/api/licenses');
}

export const fetchTask = async (taskId: number) => {
    return fetchData<Task>(`http://localhost:8080/api/tasks/${taskId}`);
}

export const fetchAllTasks = async () => {
    return fetchData<Task[]>('http://localhost:8080/api/tasks');
}

export const fetchAllTasksForZone = async (zoneId: number) => {
    return fetchData<Task[]>(`http://localhost:8080/api/zones/${zoneId}/tasks`);
}

export const fetchAllPickerTasks = async () => {
    return fetchData<PickerTask[]>('http://localhost:8080/api/picker-tasks');
}

export const fetchPickerTasksForZone = async (zoneId: number) => {
    return fetchData<PickerTask[]>(`http://localhost:8080/api/zones/${zoneId}/picker-tasks`);
}

export const fetchAllPickerTasksForZoneNow = async (zoneId: number) => {
    return fetchData<PickerTask[]>(`http://localhost:8080/api/zones/${zoneId}/picker-tasks-now`);
}

export const fetchAllPickerTasksByZoneForDate = async (zoneId: number , date: string) => {
    return fetchData<PickerTask[]>(`http://localhost:8080/api/zones/${zoneId}/picker-tasks/${date}`);
}

export const fetchActiveTask = async (taskId: number) => {
    return fetchData<ActiveTask>(`http://localhost:8080/api/active-tasks/${taskId}`);
}

export const fetchAllActiveTasks = async () => {
  return fetchData<ActiveTask[]>('http://localhost:8080/api/active-tasks');
}

export const fetchAllActiveTasksForZone = async (zoneId: number) => {
    return fetchData<ActiveTask[]>(`http://localhost:8080/api/zones/${zoneId}/active-tasks`);
}

export const fetchAllActiveTasksForZoneNow = async (zoneId: number) => {
    return fetchData<ActiveTask[]>(`http://localhost:8080/api/zones/${zoneId}/active-tasks-now`);
}
export const fetchAllActiveTasksByZoneForDate = async (zoneId: number ,date: string) => {
    return fetchData<ActiveTask[]>(`http://localhost:8080/api/zones/${zoneId}/active-tasks/${date}`);
}

export const fetchSimulationStatus = async (): Promise<number>  => {
    return fetchData<number>('http://localhost:8080/api/simulation/getStatus');
}

export const fetchSimulationDate = async () => {
    return fetchData<string>('http://localhost:8080/api/simulation/currentDate');
}

export const fetchSimulationTime = async () => {
    return fetchData<string>('http://localhost:8080/api/simulation/currentTime');
}

export const fetchSchedulesFromBackend = async (date: string, zoneId: number) => {
    return fetchData<any>(`http://localhost:8080/api/timetables/one-week/${date}/${zoneId}`);
}

export const fetchTimeTables = async () => {
    return fetchData<any>(`http://localhost:8080/api/timetables`);
}


export const fetchSimulationCount = async () => {
    return fetchData<number>('http://localhost:8080/api/getSimCount');
}

export const fetchPredictionStatus = async () => {
    return fetchData<boolean>('http://localhost:8080/api/getPrediction');
}

export const fetchIntervalId = async () => {
    return fetchData<number>('http://localhost:8080/api/simulation/getIntervalId');
}

export const fetchNotifications = async (zoneId: number) => {
    return fetchData<Notification[]>(`http://localhost:8080/api/error-messages/zone/${zoneId}`);
}

export const fetchOverviewData = async (zoneId: number) => {
    return fetchData<number[]>(`http://localhost:8080/api/zone-data/${zoneId}`);
}
