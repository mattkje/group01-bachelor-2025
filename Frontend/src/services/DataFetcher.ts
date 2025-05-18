import {ActiveTask, License, Notification, NotificationDone, PickerTask, Task, Worker, Zone} from "@/assets/types";
import {fetchData} from "@/services/HttpMethods";

/**
 * Fetches all Monte Carlo graph data for a given zone ID.
 *
 * @param zoneId - The ID of the zone for which to fetch the data.
 */
export const fetchAllMonteCarloGraphData = async (zoneId: number) => {
    const data = await fetchData<any>(`http://localhost:8080/api/data/${zoneId}/graph-data`);
    return {
        realData: data.realData || [0],
        simulationData: data.simulationData || [],
        activeTasks: data.activeTasks || 0,
        currentDate: data.currentDate || "",
    };
};

/**
 * Fetches Zone for a given zone ID.
 *
 * @param zoneId - The ID of the zone to fetch.
 * @return A promise that resolves to the Zone object.
 */

export const fetchZone = async (zoneId: number) => {
    return fetchData<Zone>(`http://localhost:8080/api/zones/${zoneId}`);
};

/**
 * Fetches all zones.
 *
 * @return A promise that resolves to an array of Zone objects.
 */
export const fetchAllZones = async (): Promise<Zone[]> => {
    return fetchData<Zone[]>('http://localhost:8080/api/zones');
};

/**
 * Fetches all picker zones.
 *
 * @return A promise that resolves to an array of Zone objects.
 */
export const fetchAllPickerZones = async (): Promise<Zone[]> => {
    return fetchData<Zone[]>('http://localhost:8080/api/zones/picker-zones');
}

/**
 * Fetches all zones with active tasks.
 *
 * @return A promise that resolves to an array of Zone objects.
 */
export const fetchWorker = async (workerId: number) => {
    return fetchData<Worker>(`http://localhost:8080/api/workers/${workerId}`);
}

/**
 * Fetches all workers from an active task.
 *
 * @param activeTaskId - The ID of the active task to fetch workers from.
 */
export const fetchWorkersFromActiveTask = async (activeTaskId: number) => {
    return fetchData<Worker[]>(`http://localhost:8080/api/active-tasks/${activeTaskId}/workers`);
}

/**
 * Fetches all workers from a zone.
 *
 * @param zoneId - The ID of the zone to fetch workers from.
 * @returns A promise that resolves to an array of Worker objects.
 */
export const fetchWorkersForZone = async (zoneId: number) => {
    return fetchData<Worker[]>(`http://localhost:8080/api/zones/${zoneId}/workers`);
}

/**
 * Fetches all workers.
 *
 * @returns A promise that resolves to an array of Worker objects.
 */
export const fetchAllWorkers = async () => {
    return fetchData<Worker[]>('http://localhost:8080/api/workers');
}

/**
 * Fetches all licenses.
 *
 * @return A promise that resolves to an array of License objects.
 */
export const fetchLicenses = async () => {
    return fetchData<License[]>('http://localhost:8080/api/licenses');
}

/**
 * Fetches a task by its ID.
 *
 * @param taskId - The ID of the task to fetch.
 */
export const fetchTask = async (taskId: number) => {
    return fetchData<Task>(`http://localhost:8080/api/tasks/${taskId}`);
}

/**
 * Fetches all tasks.
 *
 * @return A promise that resolves to an array of Task objects.
 */
export const fetchAllTasks = async () => {
    return fetchData<Task[]>('http://localhost:8080/api/tasks');
}

/**
 * Fetches all tasks for a zone.
 *
 * @param zoneId - The ID of the zone to fetch tasks for.
 * @returns A promise that resolves to an array of Task objects.
 */
export const fetchAllTasksForZone = async (zoneId: number) => {
    return fetchData<Task[]>(`http://localhost:8080/api/zones/${zoneId}/tasks`);
}

/**
 * Fetches all picker tasks.
 *
 * @returns A promise that resolves to an array of PickerTask objects.
 */
export const fetchAllPickerTasks = async () => {
    return fetchData<PickerTask[]>('http://localhost:8080/api/picker-tasks');
}

/**
 * Fetches all picker tasks for a zone.
 *
 * @param zoneId - The ID of the zone to fetch picker tasks for.
 * @returns A promise that resolves to an array of PickerTask objects.
 */
export const fetchPickerTasksForZone = async (zoneId: number) => {
    return fetchData<PickerTask[]>(`http://localhost:8080/api/zones/${zoneId}/picker-tasks`);
}

/**
 * Fetches all picker tasks for a zone at the current time.
 *
 * @param zoneId - The ID of the zone to fetch picker tasks for.
 */
export const fetchAllPickerTasksForZoneNow = async (zoneId: number) => {
    return fetchData<PickerTask[]>(`http://localhost:8080/api/zones/${zoneId}/picker-tasks-now`);
}

/**
 * Fetches all picker tasks for a zone on a specific date.
 *
 * @param zoneId - The ID of the zone to fetch picker tasks for.
 * @param date - The date to fetch picker tasks for.
 * @returns A promise that resolves to an array of PickerTask objects.
 */
export const fetchAllPickerTasksByZoneForDate = async (zoneId: number, date: string) => {
    return fetchData<PickerTask[]>(`http://localhost:8080/api/zones/${zoneId}/picker-tasks/${date}`);
}

/**
 * Fetches active tasks by its ID.
 *
 * @return A promise that resolves to an array of ActiveTask objects.
 */
export const fetchActiveTask = async (activeTaskId: number) => {
    return fetchData<ActiveTask>(`http://localhost:8080/api/active-tasks/${activeTaskId}`);
}

/**
 * Fetches all active tasks.
 *
 * @return A promise that resolves to an array of ActiveTask objects.
 */
export const fetchAllActiveTasks = async () => {
    return fetchData<ActiveTask[]>('http://localhost:8080/api/active-tasks');
}

/**
 * Fetches all active tasks for a zone.
 *
 * @param zoneId - The ID of the zone to fetch active tasks for.
 * @returns A promise that resolves to an array of ActiveTask objects.
 */
export const fetchAllActiveTasksForZone = async (zoneId: number) => {
    return fetchData<ActiveTask[]>(`http://localhost:8080/api/zones/${zoneId}/active-tasks`);
}

/**
 * Fetches all active tasks for a zone at the current time.
 *
 * @param zoneId - The ID of the zone to fetch active tasks for.
 * @returns A promise that resolves to an array of ActiveTask objects.
 */
export const fetchAllActiveTasksForZoneNow = async (zoneId: number) => {
    return fetchData<ActiveTask[]>(`http://localhost:8080/api/zones/${zoneId}/active-tasks-now`);
}

/**
 * Fetches all active tasks for a zone on a specific date.
 *
 * @param zoneId - The ID of the zone to fetch active tasks for.
 * @param date - The date to fetch active tasks for.
 * @returns A promise that resolves to an array of ActiveTask objects.
 */
export const fetchAllActiveTasksByZoneForDate = async (zoneId: number, date: string) => {
    return fetchData<ActiveTask[]>(`http://localhost:8080/api/zones/${zoneId}/active-tasks/${date}`);
}

/**
 * Fetches simulation status.
 *
 * @returns A promise that resolves to a number representing the simulation status.
 */
export const fetchSimulationStatus = async (): Promise<number> => {
    return fetchData<number>('http://localhost:8080/api/simulation/getStatus');
}

/**
 * Fetches simulation date.
 *
 * @returns A promise that resolves to a string representing the simulation date.
 */
export const fetchSimulationDate = async () => {
    return fetchData<string>('http://localhost:8080/api/simulation/currentDate');
}

/**
 * Fetches simulation time.
 *
 * @returns A promise that resolves to a string representing the simulation time.
 */
export const fetchSimulationTime = async () => {
    return fetchData<string>('http://localhost:8080/api/simulation/currentTime');
}

/**
 * Fetches schedules for a specific date and zone ID.
 *
 * @param date - The date for which to fetch schedules (format: YYYY-MM-DD).
 * @param zoneId - The ID of the zone for which to fetch schedules.
 */
export const fetchTimetable = async (date: string, zoneId: number) => {
    return fetchData<any>(`http://localhost:8080/api/timetables/one-week/${date}/${zoneId}`);
}

/**
 * Fetches all schedules.
 *
 * @returns A promise that resolves to an array of schedules.
 */
export const fetchAllTimeTables = async () => {
    return fetchData<any>(`http://localhost:8080/api/timetables`);
}

/**
 * Fetches the number of simulations to be run.
 *
 * @returns A promise that resolves to a number representing the simulation count.
 */
export const fetchSimulationCount = async () => {
    return fetchData<number>('http://localhost:8080/api/getSimCount');
}

/**
 * Fetches the prediction status.
 *
 * @returns A promise that resolves to a boolean indicating the prediction status.
 */
export const fetchPredictionStatus = async () => {
    return fetchData<boolean>('http://localhost:8080/api/getPrediction');
}

/**
 * Fetches the interval ID.
 *
 * @returns A promise that resolves to a number representing the interval ID.
 */
export const fetchIntervalId = async () => {
    return fetchData<number>('http://localhost:8080/api/simulation/getIntervalId');
}

/**
 * Fetches all notifications for a specific zone.
 *
 * @param zoneId - The ID of the zone for which to fetch notifications.
 */
export const fetchNotifications = async (zoneId: number) => {
    return fetchData<Notification[]>(`http://localhost:8080/api/error-messages/zone/${zoneId}`);
}

/**
 * Fetches all data for the overview page.
 *
 * @param zoneId - The ID of the zone for which to fetch overview data.
 */
export const fetchOverviewData = async (zoneId: number) => {
    return fetchData<number[]>(`http://localhost:8080/api/zone-data/${zoneId}`);
}

/**
 * Fetches when the simulation is predicted to end.
 *
 * @returns A promise that resolves to a string representing the end time.
 */
export const fetchDoneBy = async () => {
    return fetchData<NotificationDone>(`http://localhost:8080/api/error-messages/done-by`);
}

/**
 * Fetches the done-by time for a specific zone.
 *
 * @param zoneId - The ID of the zone for which to fetch the done-by time.
 */
export const fetchDoneByForZone = async (zoneId: number) => {
    return fetchData<string>(`http://localhost:8080/api/error-messages/done-by/${zoneId}`);
}

/**
 * Fetches the speed of the simulation.
 *
 * @returns A promise that resolves to a number representing the speed.
 */
export const fetchSpeed = async () => {
    return fetchData<number>(`http://localhost:8080/api/simulation/getSpeed`);
}
