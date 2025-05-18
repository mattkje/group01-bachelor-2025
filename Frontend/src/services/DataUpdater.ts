import {License, Task, Worker} from "@/assets/types";
import {deleteData, putData, postData, postWithParams} from "@/services/HttpMethods";

/**
 * Adds licenses to a worker.
 *
 * @param workerId the ID of the worker to whom the licenses will be added
 * @param licenses an array of licenses to be added
 */
export const addLicensesToWorker = async (workerId: number, licenses: License[]): Promise<void> => {
    try {
        for (const license of licenses) {
            await putData(`http://localhost:8080/api/workers/${workerId}/license/${license.id}`);
        }
    } catch (error) {
        console.error('Error adding licenses to worker:', error);
    }
};

/**
 * Updates the availability of a worker.
 *
 * @param worker the worker whose availability will be updated
 * @param isAvailable the new availability status
 */
export const updateWorkerAvailability = async (worker: Worker, isAvailable: boolean) => {
    if (worker) {
        worker.availability = !worker.availability;
        isAvailable = worker.availability;

        await putData(`http://localhost:8080/api/workers/${worker.id}/availability`, {
            availability: worker.availability,
        });
    }
};

/**
 * Updates the worker's schedule.
 *
 * @param schedule the schedule to be updated
 */
export const updateWorkerSchedule = async (schedule: any) => {
    if (schedule.id) {
        await putData(`http://localhost:8080/api/timetables/${schedule.id}`, schedule);
    }
};

/**
 * Creates a new schedule for a worker.
 *
 * @param workerId the ID of the worker for whom the schedule will be created
 * @param schedule the schedule to be created
 */
export const createWorkerSchedule = async (workerId: number, schedule: any) => {
    if (schedule && workerId) {
        await postData(`http://localhost:8080/api/timetables/${workerId}`, schedule);
    }
};

/**
 * Deletes a worker's schedule.
 *
 * @param scheduleId the ID of the schedule to be deleted
 */
export const deleteWorkerSchedule = async (scheduleId: number) => {
    if (scheduleId) {
        await deleteData(`http://localhost:8080/api/timetables/${scheduleId}`);
    }
}

/**
 * Deletes a worker.
 *
 * @param worker the worker to be deleted
 */
export const deleteWorker = async (worker: Worker) => {
    if (worker) {
        await deleteData(`http://localhost:8080/api/workers/${worker.id}`);
    }
};

/**
 * Deletes a picker task by its ID.
 *
 * @param pickerTaskId the ID of the picker task to be deleted
 */
export const deletePickerTask = async (pickerTaskId: number) => {
    if (pickerTaskId) {
        await deleteData(`http://localhost:8080/api/active-tasks/${pickerTaskId}`);
    }
};

/**
 * Updates a picker task with a new zone ID.
 *
 * @param pickerTaskId the ID of the picker task to be updated
 * @param zoneId the new zone ID to be set
 */
export const updatePickerTask = async (pickerTaskId: number, zoneId: number) => {
    if (pickerTaskId) {
        await putData(`http://localhost:8080/api/picker-tasks/${pickerTaskId}/zone/${zoneId}`);
    }
}

/**
 * Deletes an active task by its ID.
 *
 * @param activeTaskId the ID of the active task to be deleted
 */
export const deleteActiveTask = async (activeTaskId: number) => {
    if (activeTaskId) {
        await deleteData(`http://localhost:8080/api/active-tasks/${activeTaskId}`);
    }
};

/**
 * Updates the active task with a new ID.
 *
 * @param activeTaskId the ID of the active task to be updated
 */
export const updateActiveTask = async (activeTaskId: number) => {
    if (activeTaskId) {
        await putData(`http://localhost:8080/api/active-tasks/${activeTaskId}`);
    }
}

/**
 * Creates a new task.
 *
 * @param zoneId the ID of the zone where the task will be created
 * @param newTask the task to be created
 */
export const createTask = async (zoneId: number, newTask: Task) => {
    if (zoneId) {
        await postData(`http://localhost:8080/api/tasks/${zoneId}`, newTask);
    }
};

/**
 * Creates a new active task.
 *
 * @param taskId the ID of the task to be activated
 * @param newActiveTask the active task to be created
 */
export const createActiveTask = async (taskId: number, newActiveTask: any) => {
    if (taskId) {
        await postData(`http://localhost:8080/api/active-tasks/${taskId}`, newActiveTask);
    }
}

/**
 * Sets the prediction status.
 *
 * @param paramData the prediction data to be set
 */
export const setPredictionStatus = async (paramData: any) => {
    await postWithParams(`http://localhost:8080/api/setPrediction`, {
        ["prediction"]: paramData,
    });
};

/**
 * Sets the interval ID.
 *
 * @param intervalId the ID of the interval to be set
 */
export const setIntervalId = async (intervalId: any) => {
    await postWithParams(`http://localhost:8080/api/simulation/setIntervalId`, {
        ["intervalId"]: intervalId,
    });
};

/**
 * Creates a new worker.
 *
 * @param worker the worker to be created
 */
export const createWorker = async (worker: Worker) => {
    if (worker) {
        await postData(`http://localhost:8080/api/workers`, worker);
    }
}
