package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/simulation")
public class WorldSimulationController {

    @Autowired
    private WorldSimulation worldSimulation;

    @PostMapping("/start")
    public void startSimulation(@RequestParam int simulationTime) throws Exception {
        worldSimulation.runWorldSimulation(simulationTime, LocalDate.now());
    }

    @PostMapping("/pause")
    public void pauseSimulation() throws InterruptedException, IOException {
        worldSimulation.pauseSimulation();
    }

    @GetMapping("/getStatus")
    public int getSimulationStatus() {
        return worldSimulation.getPauseStatus();
    }

    @PostMapping("/fastForward")
    public void fastForwardSimulation(@RequestParam double speedMultiplier) {
        worldSimulation.changeSimulationSpeed(speedMultiplier);
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
