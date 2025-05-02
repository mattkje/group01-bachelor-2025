import { fetchData, postData } from "@/composables/HttpMethods";

export const startSimulationClock = async (simulationTime: number) => {
    await postData(`http://localhost:8080/api/simulation/start?simulationTime=${simulationTime}`);
};

export const stopSimulationClock = async () => {
    await postData(`http://localhost:8080/api/simulation/pause`);
};

export const fastForwardSimulationClock = async (speed: number) => {
    await postData(`http://localhost:8080/api/simulation/fastForward?speedMultiplier=${speed}`);
};

export const runAllMonteCarloSimulations = async () => {
    return await fetchData<any>(`http://localhost:8080/api/monte-carlo`);
};

export const runMonteCarloSimulationForZone = async (zoneId: number) => {
    return await fetchData<any>(`http://localhost:8080/api/monte-carlo/zones/${zoneId}`);
};

export const updateWorkerZone = async (workerId: number, zoneId: number) => {
    try {
        const response = await fetch(`http://localhost:8080/api/workers/${workerId}/zone/${zoneId}`, {
            method: 'PUT',
        });
        if (!response.ok) {
            throw new Error('Error updating worker zone');
        }
    } catch (error) {
        console.error('Error moving worker:', error);
    }
};