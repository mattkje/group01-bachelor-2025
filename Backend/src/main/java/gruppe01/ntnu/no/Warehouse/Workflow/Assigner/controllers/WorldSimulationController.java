package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;

@RestController
@RequestMapping("/api/simulation")
public class WorldSimulationController {

    @Autowired
    private WorldSimulation worldSimulation;

    @PostMapping("/start")
    public void startSimulation(@RequestParam int simulationTime) throws InterruptedException {
        worldSimulation.runWorldSimulation(simulationTime);
    }

    @PostMapping("/pause")
    public void pauseSimulation() {
        //worldSimulation.pauseSimulation();
    }

    @PostMapping("/fastForward")
    public void fastForwardSimulation(@RequestParam int speedMultiplier) {
        //worldSimulation.fastForwardSimulation(speedMultiplier);
    }
}
