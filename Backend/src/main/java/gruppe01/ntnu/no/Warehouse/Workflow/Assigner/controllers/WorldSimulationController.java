package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.services.SimulationService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.worldsimulation.WorldSimulation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import smile.regression.RandomForest;

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
  public WorldSimulationController(WorldSimulation worldSimulation,
                                   SimulationService simulationService) {
    this.worldSimulation = worldSimulation;
    this.simulationService = simulationService;
  }

  /**
   * Starts the world simulation with the specified simulation time and count.
   *
   * @param simulationTime the simulation time in minutes
   * @param simCount       the number of Monte-Carlo simulations to run
   * @throws Exception if an error occurs during simulation
   */
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

  /**
   * Pauses the world simulation.
   *
   * @throws InterruptedException if the thread is interrupted
   * @throws IOException          if an I/O error occurs
   * @throws ExecutionException   if an execution error occurs
   */
  @Operation(
      summary = "Pause the world simulation",
      description = "Pauses the world simulation."
  )
  @PostMapping("/pause")
  public void pauseSimulation() throws InterruptedException, IOException, ExecutionException {
    worldSimulation.pauseSimulation();
  }

  /**
   * Stops the world simulation.
   *
   * @throws InterruptedException if the thread is interrupted
   * @throws IOException          if an I/O error occurs
   * @throws ExecutionException   if an execution error occurs
   */
  @Operation(
      summary = "Stop the world simulation",
      description = "Stops the world simulation."
  )
  @PostMapping("/stop")
  public void stopSimulation() throws InterruptedException, IOException, ExecutionException {
    worldSimulation.stopSimulation();
  }

  /**
   * Retrieves the status of the world simulation.
   *
   * @return the status of the simulation
   */
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

  /**
   * Fast forwards the world simulation by changing the speed.
   *
   * @param speedMultiplier the speed multiplier for the simulation
   */
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

  /**
   * Retrieves the current simulation time.
   *
   * @return the current simulation time
   */
  @Operation(
      summary = "Get the current simulation time",
      description = "Retrieves the current simulation time."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved current simulation time"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/currentTime")
  public ResponseEntity<LocalTime> getCurrentSimulationTime() {
    return ResponseEntity.ok(worldSimulation.getCurrentTime());
  }

  /**
   * Retrieves the current date of the simulation.
   *
   * @return the current date of the simulation
   */
  @Operation(
      summary = "Get the current simulation date",
      description = "Retrieves the current simulation date."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved current simulation date"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/currentDate")
  public ResponseEntity<LocalDate> getCurrentDate() {
    return ResponseEntity.ok(worldSimulation.getCurrentDate());
  }

  /**
   * Retrieves the current date and time of the simulation.
   *
   * @return the current date and time of the simulation
   */
  @Operation(
      summary = "Get the current date and time",
      description = "Retrieves the current date and time of the simulation."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved current date and time"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/CurrentDateTime")
  public ResponseEntity<LocalDateTime> getCurrentDateTime() {
    return ResponseEntity.ok(worldSimulation.getCurrentDateTime());
  }

  /**
   * Sets the interval ID for the simulation.
   *
   * @param intervalId the interval ID in milliseconds
   */
  @PostMapping("/setIntervalId")
  public void setIntervalId(
      @Parameter(description = "IntervalId in milliseconds")
      @RequestParam int intervalId) {
    worldSimulation.setIntervalId(intervalId);
  }

  /**
   * Retrieves the interval ID of the simulation.
   *
   * @return the interval ID of the simulation
   */
  @GetMapping("/getIntervalId")
  public ResponseEntity<Integer> getIntervalId() {
    return ResponseEntity.ok(worldSimulation.getIntervalId());
  }

  /**
   * Retrieves the RandomForest models for all zones.
   *
   * @return a map of zone IDs to RandomForest models
   */
  @GetMapping("/getModels")
  public Map<String, RandomForest> getModels() {
    return worldSimulation.getModels();
  }

  /**
   * Gets the RandomForest model for a specific zone.
   *
   * @param zoneId the ID of the zone
   * @return the RandomForest model for the specified zone
   */
  @GetMapping("/getModel/{zoneId}")
  public RandomForest getModel(@PathVariable Long zoneId) {
    Map<String, RandomForest> models = worldSimulation.getModels();
    if (models == null) {
      return null;
    }
    return models.get(zoneId.toString());
  }

  /**
   * Gets the speed of the simulation.
   *
   * @return the speed of the simulation
   */
  @GetMapping("/getSpeed")
  public ResponseEntity<Integer> getSpeed() {
    return ResponseEntity.ok(worldSimulation.getSpeedFactory());
  }
}
