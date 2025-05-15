package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.PickerTaskGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.SimulationService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.worldsimulation.WorldSimulation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import smile.regression.RandomForest;

/**
 * SimulationController handles HTTP requests related to simulation operations.
 * It provides endpoints to generate active tasks, run Monte Carlo simulations,
 * generate timetables, and manage simulation settings.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "SimulationController", description = "Controller for managing simulations")
public class SimulationController {

  private final ActiveTaskGenerator activeTaskGeneratorService;

  private final TimeTableGenerator timeTableGenerator;

  private final WorldSimulation worldSimulation;

  private final SimulationService simulationService;

  private final PickerTaskGenerator pickerTaskGenerator;

  private final WorldSimulationController worldSimulationController;

  private final MachineLearningModelPicking machineLearningModelPicking;

  /**
   * Constructor for SimulationController.
   *
   * @param activeTaskGeneratorService The service to handle active task generation.
   * @param timeTableGenerator         The service to generate timetables.
   * @param worldSimulation            The service to run world simulations.
   * @param simulationService          The service to handle simulation operations.
   * @param pickerTaskGenerator        The service to generate picker tasks.
   */
  public SimulationController(ActiveTaskGenerator activeTaskGeneratorService,
                              TimeTableGenerator timeTableGenerator,
                              WorldSimulation worldSimulation,
                              SimulationService simulationService,
                              PickerTaskGenerator pickerTaskGenerator,
                              WorldSimulationController worldSimulationController,
                              MachineLearningModelPicking machineLearningModelPicking) {
    this.activeTaskGeneratorService = activeTaskGeneratorService;
    this.timeTableGenerator = timeTableGenerator;
    this.worldSimulation = worldSimulation;
    this.simulationService = simulationService;
    this.pickerTaskGenerator = pickerTaskGenerator;
    this.worldSimulationController = worldSimulationController;
    this.machineLearningModelPicking = machineLearningModelPicking;
  }

  /**
   * Endpoint to generate active tasks for a given date and number of days.
   *
   * @param date    The start date for generating active tasks.
   * @param numDays The number of days to generate active tasks for.
   */
  @Operation(
      summary = "Generate active tasks",
      description = "Generates active tasks for a given date and number of days."
  )
  @GetMapping("/generate-active-tasks/{date}/{numDays}")
  public void generateActiveTasks(
      @Parameter(description = "Start date for generating active tasks")
      @PathVariable String date,
      @Parameter(description = "Number of days to generate active tasks for")
      @PathVariable int numDays) {
    LocalDate startDate = LocalDate.parse(date);
    activeTaskGeneratorService.generateActiveTasks(startDate, numDays);
  }

  /**
   * Endpoint to run Monte Carlo simulation.
   *
   * @return A map containing the results of the Monte Carlo simulation.
   * @throws Exception If an error occurs during the simulation.
   */
  @Operation(
      summary = "Run Monte Carlo simulation",
      description = "Runs a Monte Carlo simulation for the specified date and time."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully ran Monte Carlo simulation"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })


  @GetMapping("/monte-carlo")
  public ResponseEntity<Map<Long, List<String>>> monteCarlo() throws Exception {
    LocalDateTime time = worldSimulationController.getCurrentDateTime().getBody();
    if (time == null) {
      throw new IllegalArgumentException("Current date and time is not available");
    }
    Map<String, RandomForest> models = worldSimulationController.getModels();
    if (models == null || models.isEmpty()) {
      throw new IllegalArgumentException("Models are not available or empty");
    }

    return ResponseEntity.ok(simulationService.runCompleteSimulation(models, time));
  }

  /**
   * Endpoint to run Monte Carlo simulation at the start of the day.
   *
   * @return A map containing the results of the Monte Carlo simulation.
   * @throws Exception If an error occurs during the simulation.
   */
  @Operation(
      summary = "Run Monte Carlo simulation at start of day",
      description = "Runs a Monte Carlo simulation for the start of the current day."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully ran Monte Carlo simulation at start of day"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/monte-carlo-start-of-day")
  public ResponseEntity<Map<Long, List<String>>> monteCarloStartOfDay() throws Exception {
    return ResponseEntity.ok(
        simulationService.runCompleteSimulation(null, LocalDate.now().atStartOfDay()));
  }

  /**
   * Endpoint to run Monte Carlo simulation for a specific zone.
   *
   * @param id The ID of the zone to run simulation for.
   * @return A list of ZoneSimResult containing the simulation results.
   * @throws IOException If an error occurs during the simulation.
   */
  @Operation(
      summary = "Run Monte Carlo simulation for a specific zone",
      description = "Runs a Monte Carlo simulation for the specified zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully ran Monte Carlo simulation for zone"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/monte-carlo/zones/{id}")
  public ResponseEntity<List<ZoneSimResult>> monteCarloZone(
      @Parameter(description = "ID of the zone to run simulation for")
      @PathVariable Long id) throws IOException {
    LocalDateTime time = worldSimulationController.getCurrentDateTime().getBody();
    if (time == null) {
      throw new IllegalArgumentException("Current date and time is not available");
    }
    System.out.println("Current date and time: " + time);
    Map<String, RandomForest> models = worldSimulationController.getModels();
    return ResponseEntity.ok(simulationService.runZoneSimulation(id, time, models));
  }

  /**
   * Endpoint to run Monte Carlo simulation for a specific zone on a specific day.
   *
   * @param id  The ID of the zone to run simulation for.
   * @param day The date in format yyyy-MM-dd.
   * @return A list of ZoneSimResult containing the simulation results.
   * @throws IOException If an error occurs during the simulation.
   */
  @Operation(
      summary = "Run Monte Carlo simulation for a specific zone on a specific day",
      description = "Runs a Monte Carlo simulation for the specified zone on the specified day."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully ran Monte Carlo simulation for zone on day"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/monte-carlo/zones/{id}/day/{day}")
  public ResponseEntity<List<ZoneSimResult>> monteCarloZone(
      @Parameter(description = "ID of the zone to run simulation for")
      @PathVariable Long id,
      @Parameter(description = "Date in format yyyy-MM-dd")
      @PathVariable String day) throws IOException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date = LocalDate.parse(day, formatter);
    LocalDateTime dateTime = date.atStartOfDay();
    return ResponseEntity.ok(simulationService.runZoneSimulation(id, dateTime, null));
  }

  /**
   * Endpoint to generate a timetable for a given date.
   *
   * @param date The date in format yyyy-MM-dd.
   */
  @Operation(
      summary = "Generate timetable",
      description = "Generates a timetable for a given date."
  )
  @GetMapping("/generate-timetable/{date}")
  public void generateTimeTable(
      @Parameter(description = "Start date for generating timetable")
      @PathVariable String date) {
    LocalDate startDate = LocalDate.parse(date);
    timeTableGenerator.generateTimeTable(startDate);
  }

  /**
   * Endpoint to generate picker tasks for a given date and number of days.
   *
   * @param date             The start date for generating picker tasks.
   * @param numDays          The number of days to generate picker tasks for.
   * @param numOfTasksPerDay The number of tasks per day.
   */
  @Operation(
      summary = "Generate picker tasks",
      description = "Generates picker tasks for a given date and number of days."
  )
  @GetMapping("/generate-picker-tasks/{date}/{numDays}/{numOfTasksPerDay}")
  public void generatePickerTasks(
      @Parameter(description = "Start date for generating picker tasks")
      @PathVariable String date,
      @Parameter(description = "Number of days to generate picker tasks for")
      @PathVariable int numDays,
      @Parameter(description = "Number of tasks per day")
      @PathVariable int numOfTasksPerDay) throws Exception {
    LocalDate startDate = LocalDate.parse(date);
    MachineLearningModelPicking machineLearningModelPicking = new MachineLearningModelPicking();
    pickerTaskGenerator.generatePickerTasks(startDate, numDays, numOfTasksPerDay,
        machineLearningModelPicking, false);
  }

  /**
   * Endpoint to run the world simulation.
   *
   * @throws Exception If an error occurs during the simulation.
   */
  @Operation(
      summary = "Run world simulation",
      description = "Runs the world simulation today with a simulation time of 2 minutes."
  )
  @GetMapping("/run-world-simulation")
  public void runWorldSimulation() throws Exception {
    worldSimulation.runWorldSimulation(2, LocalDate.now(), false);
  }

  /**
   * Endpoint to pause the world simulation.
   *
   * @throws Exception If an error occurs during the simulation.
   */
  @Operation(
      summary = "Pause world simulation",
      description = "Pauses the world simulation."
  )
  @GetMapping("/simulate-one-year")
  public void simulateOneYear() throws Exception {
    worldSimulation.simulateOneYear();
  }

  /**
   * Endpoint to set the simulation count.
   *
   * @param simCount The number of simulations to run.
   */
  @Operation(
      summary = "Set simulation count",
      description = "Sets the number of simulations to run."
  )
  @PostMapping("/setSimCount")
  public void setSimCount(
      @Parameter(description = "Number of simulations to run")
      @RequestParam int simCount) {
    if (simCount <= 0) {
      simCount = 1;
    }
    simulationService.setSimCount(simCount);
  }

  @Operation(
      summary = "Get simulation count",
      description = "Retrieves the current number of simulations to run."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved simulation count"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/getSimCount")
  public ResponseEntity<Integer> getSimCount() {
    return ResponseEntity.ok(simulationService.getSimCount());
  }

  /**
   * Endpoint to set the prediction value.
   *
   * @param prediction The prediction value to set.
   */
  @PostMapping("/setPrediction")
  public void setPrediction(
      @Parameter(description = "Prediction value to set")
      @RequestParam boolean prediction) {
    simulationService.setPrediction(prediction);
  }

  /**
   * Endpoint to get the prediction value.
   *
   * @return The current prediction value.
   */
  @GetMapping("/getPrediction")
  public ResponseEntity<Boolean> getPrediction() {
    return ResponseEntity.ok(simulationService.isPrediction());
  }

  /**
   * Endpoint to reset the simulation date.
   */
  @GetMapping("/resetSim")
  public void resetSim() {
    worldSimulation.resetSimulationDate();
  }

  /**
   * Endpoint to get the simulation date.
   *
   * @return The current simulation date.
   */
  @GetMapping("/zone-data/{zoneId}")
  public ResponseEntity<List<Integer>> getZoneData(
      @Parameter(description = "ID of the zone to retrieve data for")
      @PathVariable long zoneId) {
    return ResponseEntity.ok(worldSimulation.getData(zoneId));
  }

  /**
   * Endpoint to create synthetic data for a specific department.
   *
   * @param department The department for which to create synthetic data.
   * @return A response indicating the success of the operation.
   * @throws IOException        If an error occurs during data creation.
   * @throws URISyntaxException If an error occurs with the URI syntax.
   */
  @GetMapping("/create-data/{department}")
  public ResponseEntity<String> createData(@PathVariable String department)
      throws IOException, URISyntaxException {
    machineLearningModelPicking.createSynetheticData(department);
    return ResponseEntity.ok("Data created");
  }
}
