package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/simulation")
public class WorldSimulationController {

    @Autowired
    private WorldSimulation worldSimulation;

    @GetMapping("/time")
    public LocalTime getCurrentTime() {
        return worldSimulation.getCurrentTime();
    }

    @PostMapping("/start")
    public void startSimulation(@RequestParam int simulationTime) throws Exception {
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

    @GetMapping("/currentTime")
    public LocalTime getCurrentSimulationTime() {
        return worldSimulation.getCurrentTime();
    }

    @GetMapping("/currentDate")
    public LocalDate getCurrentDate() {
        return worldSimulation.getCurrentDate();
    }
}
