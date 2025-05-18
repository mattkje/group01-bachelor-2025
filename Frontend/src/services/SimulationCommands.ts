import {fetchData, postData, runCommand} from "@/services/HttpMethods";

/**
 * Starts the simulation clock.
 *
 * @param simulationTime the time to simulate in minutes
 * @param simCount the number of simulations to run
 */
export const startSimulationClock = async (simulationTime: number, simCount: number) => {
    await postData(`http://localhost:8080/api/simulation/start?simulationTime=${simulationTime}&simCount=${simCount}`);
};

/**
 * Pauses the simulation clock.
 */
export const pauseSimulationClock = async () => {
    await postData(`http://localhost:8080/api/simulation/pause`);
};

/**
 * Stops the simulation clock. (Different from pause because it completely aborts the simulation)
 */
export const stopSimulationClock = async () => {
    await postData(`http://localhost:8080/api/simulation/stop`);
};

/**
 * Fast-forwards the simulation clock.
 *
 * @param speed the speed multiplier for fast-forwarding
 */
export const fastForwardSimulationClock = async (speed: number) => {
    await postData(`http://localhost:8080/api/simulation/fastForward?speedMultiplier=${speed}`);
};

/**
 * Runs a Monte Carlo simulation for all zones.
 *
 * @returns the result of the simulation
 */
export const runAllMonteCarloSimulations = async () => {
    return await fetchData<any>(`http://localhost:8080/api/monte-carlo`);
};

/**
 * Runs a Monte Carlo simulation for a specific zone.
 *
 * @param zoneId the ID of the zone to simulate
 * @returns the result of the simulation
 */
export const runMonteCarloSimulationForZone = async (zoneId: number) => {
    return await fetchData<any>(`http://localhost:8080/api/monte-carlo/zones/${zoneId}`);
};

/**
 * Moves a worker to a different zone.
 *
 * @param workerId the ID of the worker to move
 * @param zoneId the ID of the zone to move the worker to
 */
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

/**
 * Resets the simulation to its initial state.
 */
export const resetToInitialState = async () => {
    await runCommand(`http://localhost:8080/api/resetSim`);
}