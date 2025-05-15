package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.WorldSimData;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.MonteCarloDataService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorldSimDataService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.worldsimulation.WorldSimulation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * MonteCarloController handles HTTP requests related to Monte Carlo simulations.
 * It provides endpoints to retrieve world simulation data, generate new data,
 * and get simulation values.
 */
@RestController
@RequestMapping("/api/data")
@Tag(name = "MonteCarloController", description = "Controller for managing Monte Carlo simulations")
public class MonteCarloController {

  private final WorldSimDataService worldSimDataService;

  private final ZoneService zoneService;

  private final MonteCarloDataService monteCarloDataService;

  private final WorldSimulation worldSimulation;

  /**
   * Constructor for MonteCarloController.
   *
   * @param worldSimDataService   the service to handle world simulation data operations
   * @param zoneService           the service to handle zone operations
   * @param monteCarloDataService the service to handle Monte Carlo simulation data operations
   * @param worldSimulation       the world simulation instance
   */
  public MonteCarloController(WorldSimDataService worldSimDataService,
                              ZoneService zoneService, MonteCarloDataService monteCarloDataService,
                              WorldSimulation worldSimulation) {
    this.worldSimDataService = worldSimDataService;
    this.zoneService = zoneService;
    this.monteCarloDataService = monteCarloDataService;
    this.worldSimulation = worldSimulation;
  }

  /**
   * Retrieves the most recent world simulation data for a specific zone.
   *
   * @param zoneId the ID of the zone to retrieve data for
   * @return a ResponseEntity containing the world simulation data or a 404 status if not found
   */
  @Operation(
      summary = "Get world simulation data by zone ID",
      description = "Retrieve the most recent world simulation data for a specific zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved world simulation data"),
      @ApiResponse(responseCode = "404", description = "World simulation data not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{zoneId}")
  public ResponseEntity<List<WorldSimData>> getWorldSimData(
      @Parameter(description = "ID of the zone to retrieve data for")
      @PathVariable long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(worldSimDataService.getMostRecentWorldSimDataByZone(zoneId));
    }
  }

  /**
   * Retrieves the most recent world simulation values for a specific zone.
   *
   * @param zoneId the ID of the zone to retrieve values for
   * @return a ResponseEntity containing the world simulation values or a 404 status if not found
   */
  @Operation(
      summary = "Get world simulation values by zone ID",
      description = "Retrieve the most recent world simulation values for a specific zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved world simulation values"),
      @ApiResponse(responseCode = "404", description = "World simulation values not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{zoneId}/values")
  public ResponseEntity<List<Integer>> getWorldSimValues(
      @Parameter(description = "ID of the zone to retrieve values for")
      @PathVariable long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(worldSimDataService.getWorldSimValues(zoneId));
    }
  }

  /**
   * Generates new world simulation data for the specified date and time.
   *
   * @param now the date and time to generate data for
   */
  @Operation(
      summary = "Generate world simulation data",
      description = "Generates new world simulation data for the specified date and time."
  )
  @PostMapping("/world-sim")
  public void generateWorldSimData(LocalDateTime now) {
    worldSimDataService.generateWorldSimData(now, true);
  }

  /**
   * Retrieves Monte Carlo simulation values for a specific zone.
   *
   * @param zoneId the ID of the zone to retrieve simulation values for
   * @return a ResponseEntity containing the Monte Carlo simulation values or a 404
  status if not found
   */
  @Operation(
      summary = "Generate Monte Carlo simulation data",
      description = "Generates new Monte Carlo simulation data for the specified zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully generated Monte Carlo simulation data"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{zoneId}/mcvalues")
  public ResponseEntity<List<List<Integer>>> getMonteCarloSimulationValues(
      @Parameter(description = "ID of the zone to retrieve Monte Carlo simulation values for")
      @PathVariable long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(monteCarloDataService.getMCDataValues(zoneId));
    }
  }

  /**
   * Retrieves all data for a specific zone, including real data,
   * simulation data, and active tasks.
   *
   * @param zoneId the ID of the zone to retrieve data for
   * @return a ResponseEntity containing all data for the zone or a 404 status if not found
   */
  @Operation(
      summary = "Get all data for a specific zone",
      description = "Retrieve all data for a specific zone, "
          + "including real data, simulation data, and active tasks."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved all data for the zone"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{zoneId}/graph-data")
  public ResponseEntity<Map<String, Object>> getAllData(
      @Parameter(description = "ID of the zone to retrieve data for")
      @PathVariable long zoneId) {
    if (zoneService.getZoneById(zoneId) == null && zoneId != 0) {
      return ResponseEntity.notFound().build();
    } else {
      Map<String, Object> response = new HashMap<>();
      LocalDate currentDate = worldSimulation.getCurrentDate(); // Store the result in a variable
      response.put("realData", worldSimDataService.getWorldSimValues(zoneId));
      response.put("simulationData", monteCarloDataService.getMCDataValues(zoneId));
      response.put("currentDate", currentDate);
      response.put("activeTasks", zoneService.getNumberOfTasksForTodayByZone(zoneId, currentDate));
      return ResponseEntity.ok(response);
    }
  }
}
