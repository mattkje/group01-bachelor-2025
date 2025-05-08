package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.SimulationService;
import org.springframework.web.bind.annotation.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ExecutionException;

/**
 * WorldSimulationController handles HTTP requests related to world simulation operations.
 * It provides endpoints to start, pause, stop, and manage the simulation.
 */
@RestController
@RequestMapping("/api/simulation")
public class WorldSimulationController {

    private final WorldSimulation worldSimulation;
    private final SimulationService simulationService;

    /**
     * Constructor for WorldSimulationController.
     *
     * @param worldSimulation The service to run world simulations.
     */
    public WorldSimulationController(WorldSimulation worldSimulation, SimulationService simulationService) {
        this.worldSimulation = worldSimulation;
        this.simulationService = simulationService;
    }

    @PostMapping("/start")
    public void startSimulation(@RequestParam int simulationTime, @RequestParam int simCount) throws Exception {
        if (simCount != 0) {
            simulationService.setSimCount(simCount);
        }

        worldSimulation.runWorldSimulation(simulationTime, LocalDate.now());
    }

    @PostMapping("/pause")
    public void pauseSimulation() throws InterruptedException, IOException, ExecutionException {
        worldSimulation.pauseSimulation();
    }

    @PostMapping("/stop")
    public void stopSimulation() throws InterruptedException, IOException, ExecutionException {
        worldSimulation.stopSimulation();
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

    @GetMapping("/CurrentDateTime")
    public LocalDateTime getCurrentDateTime() {
        return worldSimulation.getCurrentDateTime();
    }
}
