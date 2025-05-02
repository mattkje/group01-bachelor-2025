import {Worker} from "@/assets/types";
import {Zone} from "@/assets/types";
export const fetchAllMonteCarloGraphData = async (zoneId: number) => {
  try {
    const response = await fetch(`http://localhost:8080/api/data/${zoneId}/graph-data`);
    if (!response.ok) throw new Error("Network response was not ok");

    const data = await response.json();
    return {
      realData: data.realData || [0],
      simulationData: data.simulationData || [],
      activeTasks: data.activeTasks || 0,
      currentDate: data.currentDate || "",
    };
  } catch (error) {
    console.error("Failed to fetch all data:", error);
    throw error;
  }
};

export const fetchZone = async (zoneId: number) => {
    try {
        const response = await fetch(`http://localhost:8080/api/zones/${zoneId}`);
        if (!response.ok) {
            throw new Error("Network response was not ok");
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch zone:", error);
        throw error;
    }
};

export const fetchAllZones = async (): Promise<Zone[]> => {
    try {
        const response = await fetch(`http://localhost:8080/api/zones`);
        if (!response.ok) {
            throw new Error("Network response was not ok");
        }
        return await response.json();
    } catch (error) {
        console.error("Failed to fetch zones:", error);
        throw error;
    }
};

export const fetchWorker = async (workerId: string) => {
    try {
        const response = await fetch(`http://localhost:8080/api/workers/${workerId}`);
        if (!response.ok) throw new Error("Network response was not ok");

        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch worker:", error);
        throw error;
    }
}

export const fetchAllWorkers = async () => {
  try {
    const response = await fetch("http://localhost:8080/api/workers");
    if (!response.ok) throw new Error("Network response was not ok");

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Failed to fetch all workers:", error);
    throw error;
  }
}

export const fetchLicenses = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/licenses');
        if (!response.ok) throw new Error('Failed to fetch licenses');
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch all workers:", error);
        throw error;
    }
}

export const fetchAllTasks = async () => {
    try {
        const response = await fetch(`http://localhost:8080/api/tasks`);
        if (!response.ok) {
        throw new Error("Network response was not ok");
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch all workers:", error);
        throw error;
    }
}

export const fetchAllTasksForZone = async (zoneId: number) => {
    try {
        const response = await fetch(`http://localhost:8080/api/zones/${zoneId}/tasks`);
        if (!response.ok) {
            throw new Error("Network response was not ok");
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch all workers:", error);
        throw error;
    }
}

export const fetchAllPickerTasks = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/picker-tasks');
        if (!response.ok) {
            throw new Error("Network response was not ok");
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch all workers:", error);
        throw error;
    }
}

export const fetchAllPickerTasksForZoneNow = async (zoneId: number) => {
    try {
        const response = await fetch(`http://localhost:8080/api/zones/${zoneId}/picker-tasks-now`);
        if (!response.ok) throw new Error("Network response was not ok");

        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch worker:", error);
        throw error;
    }
}
export const fetchActiveTasks = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/active-tasks`);
    if (!response.ok) {
      throw new Error("Network response was not ok");
    }
      const data = await response.json();
      return data;
  } catch (error) {
      console.error("Failed to fetch all workers:", error);
      throw error;
  }
}

export const fetchAllActiveTasksForZone = async (zoneId: number) => {
    try {
        const response = await fetch(`http://localhost:8080/api/zones/${zoneId}/active-tasks`);
        if (!response.ok) {
            throw new Error("Network response was not ok");
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch all workers:", error);
        throw error;
    }
}
export const fetchAllActiveTasksForZoneNow = async (zoneId: number) => {
    try {
        const response = await fetch(`http://localhost:8080/api/zones/${zoneId}/active-tasks-now`);
        if (!response.ok) throw new Error("Network response was not ok");

        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch worker:", error);
        throw error;
    }
}

export const fetchSimulationStatus = async (): Promise<number>  => {
    try {
        const response = await fetch('http://localhost:8080/api/simulation/getStatus');
        if (!response.ok) {
            throw new Error("Network response was not ok");
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch all workers:", error);
        throw error;
    }
}

export const fetchSimulationDate = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/simulation/currentDate');
        if (!response.ok) {
            throw new Error("Network response was not ok");
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch all workers:", error);
        throw error;
    }
}

export const fetchSimulationTime = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/simulation/currentTime');
        if (!response.ok) {
            throw new Error("Network response was not ok");
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Failed to fetch all workers:", error);
        throw error;
    }
}

