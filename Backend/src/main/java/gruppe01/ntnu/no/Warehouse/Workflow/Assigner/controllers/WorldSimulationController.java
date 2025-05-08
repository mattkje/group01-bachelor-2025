package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "WorldSimulationController", description = "Controller for managing world simulations")
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

    @Operation(
            summary = "Start the world simulation",
            description = "Starts the world simulation with the specified simulation time and count."
    )
    @PostMapping("/start")
    public void startSimulation(
            @Parameter(description = "Simulation time in minutes")
            @RequestParam int simulationTime,
            @Parameter(description = "Number of Monte-Carlo simulations to run")
            @RequestParam int simCount) throws Exception {
        if (simCount > 0) {
            simulationService.setSimCount(simCount);
        }

        worldSimulation.runWorldSimulation(simulationTime, LocalDate.now());
    }

    @Operation(
            summary = "Pause the world simulation",
            description = "Pauses the world simulation."
    )
    @PostMapping("/pause")
    public void pauseSimulation() throws InterruptedException, IOException, ExecutionException {
        worldSimulation.pauseSimulation();
    }

    @Operation(
            summary = "Stop the world simulation",
            description = "Stops the world simulation."
    )
    @PostMapping("/stop")
    public void stopSimulation() throws InterruptedException, IOException, ExecutionException {
        worldSimulation.stopSimulation();
    }

    @Operation(
            summary = "Get the status of the world simulation",
            description = "Retrieves the current status of the world simulation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved simulation status"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getStatus")
    public ResponseEntity<Integer> getSimulationStatus() {
        return ResponseEntity.ok(worldSimulation.getPauseStatus());
    }

    @Operation(
            summary = "Fast forward the world simulation",
            description = "Changes the speed of the world simulation."
    )
    @PostMapping("/fastForward")
    public void fastForwardSimulation(
            @Parameter(description = "Speed multiplier for the simulation")
            @RequestParam double speedMultiplier) {
        worldSimulation.changeSimulationSpeed(speedMultiplier);
    }

    @Operation(
            summary = "Get the current simulation time",
            description = "Retrieves the current simulation time."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved current simulation time"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/currentTime")
    public ResponseEntity<LocalTime> getCurrentSimulationTime() {
        return ResponseEntity.ok(worldSimulation.getCurrentTime());
    }

    @Operation(
            summary = "Get the current simulation date",
            description = "Retrieves the current simulation date."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved current simulation date"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/currentDate")
    public ResponseEntity<LocalDate> getCurrentDate() {
        return ResponseEntity.ok(worldSimulation.getCurrentDate());
    }

    @Operation(
            summary = "Get the current date and time",
            description = "Retrieves the current date and time of the simulation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved current date and time"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/CurrentDateTime")
    public ResponseEntity<LocalDateTime> getCurrentDateTime() {
        return ResponseEntity.ok(worldSimulation.getCurrentDateTime());
    }
}
