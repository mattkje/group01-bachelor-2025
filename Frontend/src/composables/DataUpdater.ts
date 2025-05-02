import { Ref } from "vue";
import {License, Worker} from "@/assets/types";
import axios from "axios";

export const addLicensesToWorker = async (workerId: number, licenses: License[]): Promise<void> => {
  try {
    for (const license of licenses) {
      const response = await fetch(`http://localhost:8080/api/workers/${workerId}/license/${license.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      if (!response.ok) throw new Error(`Failed to add license ${license.id} to worker`);
    }
  } catch (error) {
    console.error('Error adding licenses to worker:', error);
  }
};

export const updateWorkerAvailability = async (worker: Worker, isAvailable: boolean) => {
  if (worker) {
    worker.availability = !worker.availability;
    isAvailable = worker.availability;

    await fetch(`http://localhost:8080/api/workers/${worker.id}/availability`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ availability: worker.availability }),
    });
  }
};

export const deleteWorker = async (worker: Worker) => {
  if (worker) {
    await fetch(`http://localhost:8080/api/workers/${worker.id}`, {
      method: 'DELETE',
    });
  }
};

export const deleteActiveTask = async (activeTaskId: number) => {
  try {
    await fetch(`http://localhost:8080/api/active-tasks/${activeTaskId}`, {
      method: 'DELETE',
    });
  } catch (error) {
    console.error('Error deleting active task:', error);
  }
};