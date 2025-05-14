import {License, Task, Worker} from "@/assets/types";
import {deleteData, putData, postData, postWithParams} from "@/composables/HttpMethods";

export const addLicensesToWorker = async (workerId: number, licenses: License[]): Promise<void> => {
  try {
    for (const license of licenses) {
      await putData(`http://localhost:8080/api/workers/${workerId}/license/${license.id}`);
    }
  } catch (error) {
    console.error('Error adding licenses to worker:', error);
  }
};

export const updateWorkerAvailability = async (worker: Worker, isAvailable: boolean) => {
  if (worker) {
    worker.availability = !worker.availability;
    isAvailable = worker.availability;

    await putData(`http://localhost:8080/api/workers/${worker.id}/availability`, {
      availability: worker.availability,
    });
  }
};

export const updateWorkerSchedule = async (schedule: any) => {
  if (schedule.id) {
    await putData(`http://localhost:8080/api/timetables/${schedule.id}`, schedule);
  }
};

export const createWorkerSchedule = async (workerId: number, schedule: any) => {
  if (schedule && workerId) {
    await postData(`http://localhost:8080/api/timetables/${workerId}`, schedule);
  }
};

export const deleteWorkerSchedule = async (scheduleId: number) => {
  if (scheduleId) {
    await deleteData(`http://localhost:8080/api/timetables/${scheduleId}`);
  }
}

export const deleteWorker = async (worker: Worker) => {
  if (worker) {
    await deleteData(`http://localhost:8080/api/workers/${worker.id}`);
  }
};

export const deletePickerTask = async (pickerTaskId: number) => {
    if (pickerTaskId) {
      await deleteData(`http://localhost:8080/api/active-tasks/${pickerTaskId}`);
    }
};

export const updatePickerTask = async (pickerTaskId: number, zoneId: number) => {
    if (pickerTaskId) {
      await putData(`http://localhost:8080/api/picker-tasks/${pickerTaskId}/zone/${zoneId}`);
    }
}

export const deleteActiveTask = async (activeTaskId: number) => {
    if (activeTaskId) {
      await deleteData(`http://localhost:8080/api/active-tasks/${activeTaskId}`);
    }
};

export const updateActiveTask = async (activeTaskId: number) => {
    if (activeTaskId) {
      await putData(`http://localhost:8080/api/active-tasks/${activeTaskId}`);
    }
}

export const createTask = async (zoneId: number, newTask: Task) => {
  if (zoneId) {
    await postData(`http://localhost:8080/api/tasks/${zoneId}`, newTask);
  }
};

export const createActiveTask = async (taskId: number, newActiveTask: any) => {
  if (taskId) {
    await postData(`http://localhost:8080/api/active-tasks/${taskId}`, newActiveTask);
  }
}

export const setPredictionStatus = async (paramData: any) => {
  await postWithParams(`http://localhost:8080/api/setPrediction`, {
    ["prediction"]: paramData,
  });
};

export const setIntervalId = async (intervalId: any) => {
  await postWithParams(`http://localhost:8080/api/simulation/setIntervalId`, {
    ["intervalId"]: intervalId,
  });
};

export const createWorker = async (worker: Worker) => {
    if (worker) {
        await postData(`http://localhost:8080/api/workers`, worker);
    }
}
