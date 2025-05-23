package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Timetable;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TimetableService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TimetableController handles HTTP requests related to timetables.
 * It provides endpoints to create, read, update, and delete timetables.
 */
@RestController
@RequestMapping("/api/timetables")
@Tag(name = "TimetableController", description = "Controller for managing timetables")
public class TimetableController {

  private final TimetableService timetableService;
  private final ZoneService zoneService;
  private final WorkerService workerService;

  /**
   * Constructor for TimetableController.
   *
   * @param timetableService The service to handle timetable operations.
   */
  public TimetableController(TimetableService timetableService, ZoneService zoneService,
                             WorkerService workerService) {
    this.timetableService = timetableService;
    this.zoneService = zoneService;
    this.workerService = workerService;
  }

  @Operation(
      summary = "Get all timetables",
      description = "Retrieve a list of all timetables"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved all timetables"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<List<Timetable>> getTimetables() {
    return ResponseEntity.ok(timetableService.getAllTimetables());
  }

  /**
   * Retrieves a timetable by zoneId.
   *
   * @param zoneId the ID of the zone
   * @return the timetable for the specified zoneId, or null if not found
   */
  @Operation(
      summary = "Get timetable by zone ID",
      description = "Retrieve a timetable by zone ID"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved timetable"),
      @ApiResponse(responseCode = "404", description = "Timetable not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/today/zone/{zoneId}")
  public ResponseEntity<List<Timetable>> getTimetableForToday(
      @Parameter(description = "ID of the zone to retrieve timetables for")
      @PathVariable Long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(timetableService.getTodayTimetablesByZone(zoneId));
    }
  }

  /**
   * Retrieves all timetables by zoneId.
   *
   * @param zoneId the ID of the zone
   * @return a list of all timetables for the specified zoneId
   */
  @Operation(
      summary = "Get all timetables by zone ID",
      description = "Retrieve all timetables by zone ID"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved all timetables for the zone"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("zone/{zoneId}")
  public ResponseEntity<List<Timetable>> getAllTimetablesByZone(
      @Parameter(description = "ID of the zone to retrieve timetables for")
      @PathVariable Long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(timetableService.getAllTimetablesByZone(zoneId));
    }
  }

  /**
   * Retrieves all timetables by date and zoneId.
   *
   * @param day    the date to retrieve timetables for
   * @param zoneId the ID of the zone
   * @return a list of all timetables for the specified date and zoneId
   */
  @Operation(
      summary = "Get timetables by date and zone ID",
      description = "Retrieve all timetables by date and zone ID"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved all timetables for the zone on the specified day"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("{day}/zone/{zoneId}")
  public ResponseEntity<List<Timetable>> getTimetablesByDayAndZone(
      @Parameter(description = "Date to retrieve timetables for")
      @PathVariable LocalDateTime day,
      @Parameter(description = "ID of the zone to retrieve timetables for")
      @PathVariable Long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(timetableService.getTimetablesByDayAndZone(day, zoneId));
    }
  }

  /**
   * Retrieves all timetables for one week by date and zoneId.
   *
   * @param date   the starting date to retrieve timetables for
   * @param zoneId the ID of the zone
   * @return a list of all timetables for the specified week and zoneId
   */
  @Operation(
      summary = "Get timetables for one week",
      description = "Retrieve all timetables for one week by date and zone ID"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved all timetables for the zone on the specified week"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/one-week/{date}/{zoneId}")
  public ResponseEntity<List<Timetable>> getTimetablesForOneWeek(
      @Parameter(description = "The starting date to retrieve timetables for")
      @PathVariable LocalDate date,
      @Parameter(description = "ID of the zone to retrieve timetables for")
      @PathVariable Long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(timetableService.getTimetablesForOneWeek(date, zoneId));
    }
  }

  /**
   * Sets the start time for a timetable to the current time.
   *
   * @param id the ID of the timetable to set the start time for
   * @return the updated timetable
   */
  @Operation(
      summary = "Set start time for a timetable to current time",
      description = "Set the start time for a timetable to current time"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully set start time for the timetable"),
      @ApiResponse(responseCode = "404", description = "Timetable not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}/set-start-time")
  public ResponseEntity<Timetable> setStartTime(
      @Parameter(description = "ID of the timetable to set start time for")
      @PathVariable Long id) {
    if (timetableService.getTimetableById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(timetableService.setStartTime(id));
    }
  }

  /**
   * Updates a timetable by ID.
   *
   * @param id        the ID of the timetable to update
   * @param timetable the updated timetable object
   * @return the updated timetable
   */
  @Operation(
      summary = "Update timetable by ID",
      description = "Update a timetable by ID"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated the timetable"),
      @ApiResponse(responseCode = "404", description = "Timetable not found"),
      @ApiResponse(responseCode = "400", description = "Invalid timetable data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<Timetable> updateTimetable(
      @Parameter(description = "ID of the timetable to update")
      @PathVariable Long id,
      @Parameter(description = "Timetable object to update")
      @RequestBody Timetable timetable) {
    if (timetableService.getTimetableById(id) == null) {
      return ResponseEntity.notFound().build();
    } else if (timetable.getEndTime().isBefore(timetable.getStartTime())) {
      return ResponseEntity.badRequest().build();
    } else {
      return ResponseEntity.ok(timetableService.updateTimetable(id, timetable));
    }
  }

  /**
   * Adds a new timetable for a worker.
   *
   * @param timetable the timetable to add
   * @param workerId  the ID of the worker to add the timetable for
   * @return the added timetable
   */
  @Operation(
      summary = "Add a new timetable",
      description = "Add a new timetable for a worker"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully added the timetable"),
      @ApiResponse(responseCode = "404", description = "Worker not found"),
      @ApiResponse(responseCode = "400", description = "Invalid timetable data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/{workerId}")
  public ResponseEntity<Timetable> addTimetable(
      @Parameter(description = "Timetable object to add")
      @RequestBody Timetable timetable,
      @Parameter(description = "ID of the worker to add the timetable for")
      @PathVariable Long workerId) {
    if (workerService.getWorkerById(workerId).isEmpty()) {
      return ResponseEntity.notFound().build();
    } else if (timetable.getEndTime().isBefore(timetable.getStartTime())) {
      return ResponseEntity.badRequest().build();
    } else {
      return new ResponseEntity<>(timetableService.addTimetable(timetable, workerId),
          HttpStatus.CREATED);
    }
  }

  /**
   * Deletes a timetable by ID.
   *
   * @param id the ID of the timetable to delete
   * @return the deleted timetable
   */
  @Operation(
      summary = "Delete a timetable by ID",
      description = "Delete a timetable by ID"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted the timetable"),
      @ApiResponse(responseCode = "404", description = "Timetable not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Timetable> deleteTimetable(@PathVariable Long id) {
    if (timetableService.getTimetableById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(timetableService.deleteTimetable(id));
    }
  }
}
