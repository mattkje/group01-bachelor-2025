export const startSimulationClock = async (simulationTime: number) => {
    try {
        const response = await fetch(`http://localhost:8080/api/simulation/start?simulationTime=${simulationTime}`, {
            method: 'POST',
        });
        if (!response.ok) {
            throw new Error('Error starting simulation');
        }
    } catch (error) {
        console.error('Error starting simulation:', error);
    }
};

export const stopSimulationClock = async () => {
    try {
        const response = await fetch(`http://localhost:8080/api/simulation/pause`, {
            method: 'POST',
        });
        if (!response.ok) {
            throw new Error('Error pausing simulation');
        }
    } catch (error) {
        console.error('Error pausing simulation:', error);
    }
};

export const fastForwardSimulationClock = async (speed: number) => {
    try {
        const response = await fetch(`http://localhost:8080/api/simulation/fastForward?speedMultiplier=${speed}`, {
            method: 'POST',
        });
        if (!response.ok) {
            throw new Error('Error fast forwarding simulation');
        }
    } catch (error) {
        console.error('Error fast forwarding simulation:', error);
    }
};

export const runAllMonteCarloSimulations = async () => {
    try {
        const response = await fetch(`http://localhost:8080/api/monte-carlo`, {
            method: 'GET',
        });

        if (!response.ok) {
            throw new Error('Failed to run simulation');
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Error running simulation:', error);
        return null; // Return null in case of an error
    }
};