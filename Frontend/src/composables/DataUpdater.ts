import {License, Worker} from "@/assets/types";
import { deleteData , putData } from "@/composables/HttpMethods";

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

export const deleteWorker = async (worker: Worker) => {
  if (worker) {
    await deleteData(`http://localhost:8080/api/workers/${worker.id}`);
  }
};

export const deleteActiveTask = async (activeTaskId: number) => {
    if (activeTaskId) {
      await deleteData(`http://localhost:8080/api/active-tasks/${activeTaskId}`);
    }
};