package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.LicenseService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WorkerController handles HTTP requests related to workers.
 * It provides endpoints to create, read, update, and delete workers.
 */
@RestController
@RequestMapping("/api/workers")
@Tag(name = "WorkerController", description = "Worker management")
public class WorkerController {

  private final WorkerService workerService;
  private final LicenseService licenseService;

  /**
   * Constructor for WorkerController.
   *
   * @param workerService The service to handle worker operations.
   */
  public WorkerController(WorkerService workerService, LicenseService licenseService) {
    this.workerService = workerService;
    this.licenseService = licenseService;
  }

  @Operation(
      summary = "Get all workers",
      description = "Retrieve a list of all workers in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved all workers"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<List<Worker>> getAllWorkers() {
    return ResponseEntity.ok(workerService.getAllWorkers());
  }

  @Operation(
      summary = "Get available workers",
      description = "Retrieve a list of all available workers."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved available workers"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/available")
  public ResponseEntity<List<Worker>> getAvailableWorkers() {
    return ResponseEntity.ok(workerService.getAvailableWorkers());
  }

  @Operation(
      summary = "Get unavailable workers",
      description = "Retrieve a list of all unavailable workers."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved unavailable workers"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/unavailable")
  public ResponseEntity<List<Worker>> getUnavailableWorkers() {
    return ResponseEntity.ok(workerService.getUnavailableWorkers());
  }

  /**
   * Retrieves a worker by their ID.
   *
   * @param id the ID of the worker
   * @return the worker with the specified ID, or null if not found
   */
  @Operation(
      summary = "Get worker by ID",
      description = "Retrieve a worker by their ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved worker"),
      @ApiResponse(responseCode = "404", description = "Worker not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Optional<Worker>> getWorkerById(@PathVariable Long id) {
    Optional<Worker> worker = workerService.getWorkerById(id);
    if (worker.isPresent()) {
      return ResponseEntity.ok(worker);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty());
    }
  }

  /**
   * Creates a new worker.
   *
   * @param worker the worker to create
   * @return the created worker
   */
  @Operation(
      summary = "Add a new worker",
      description = "Create a new worker in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully created worker"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping
  public ResponseEntity<Worker> addWorker(
      @Parameter(description = "Worker object to be created")
      @RequestBody Worker worker) {
    if (worker.getEfficiency() <= 0 || worker.getName().isBlank() || worker.getName().isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    } else {
      return new ResponseEntity<>(workerService.addWorker(worker), HttpStatus.CREATED);
    }
  }

  /**
   * Updates an existing worker.
   *
   * @param id     the ID of the worker to update
   * @param worker the updated worker object
   * @return the updated worker, or null if not found
   */
  @Operation(
      summary = "Update a worker",
      description = "Update an existing worker's information."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated worker"),
      @ApiResponse(responseCode = "404", description = "Worker not found"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<Worker> updateWorker(
      @Parameter(description = "ID of the worker to update")
      @PathVariable Long id,
      @Parameter(description = "Updated worker object")
      @RequestBody Worker worker) {
    if (worker.getWorkerType().isEmpty()
        || worker.getWorkerType().isBlank()
        || worker.getEfficiency() <= 0
        || worker.getName().isBlank()
        || worker.getName().isEmpty()
        || worker.getWorkSchedule() == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    } else if (workerService.getWorkerById(id).isPresent()) {
      return ResponseEntity.ok(workerService.updateWorker(id, worker));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

  }

  /**
   * Updates the availability status of a worker.
   *
   * @param id the ID of the worker to update
   * @return the updated worker, or null if not found
   */
  @Operation(
      summary = "Update worker availability",
      description = "Update the availability status of a worker."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated worker availability"),
      @ApiResponse(responseCode = "404", description = "Worker not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}/availability")
  public ResponseEntity<Worker> updateWorkerAvailability(
      @Parameter(description = "ID of the worker to update availability")
      @PathVariable Long id) {
    if (workerService.getWorkerById(id).isPresent()) {
      return ResponseEntity.ok(workerService.updateWorkerAvailability(id));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  /**
   * Assigns a worker to a specific zone.
   *
   * @param workerId the ID of the worker to assign
   * @param zoneId   the ID of the zone to assign the worker to
   * @return the updated worker, or null if not found
   */
  @Operation(
      summary = "Add worker to zone",
      description = "Assign a worker to a specific zone."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully added worker to zone"),
      @ApiResponse(responseCode = "404", description = "Worker or zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{workerId}/zone/{zoneId}")
  public ResponseEntity<Worker> addWorkerToZone(
      @Parameter(description = "ID of the worker to assign")
      @PathVariable Long workerId,
      @Parameter(description = "ID of the zone to assign the worker to")
      @PathVariable Long zoneId) {
    if (workerService.getWorkerById(workerId).isPresent()) {
      return ResponseEntity.ok(workerService.addWorkerToZone(workerId, zoneId));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  /**
   * Assigns a license to a specific worker.
   *
   * @param id        the ID of the worker to assign the license to
   * @param licenseId the ID of the license to assign
   * @return the updated worker, or null if not found
   */
  @Operation(
      summary = "Add license to worker",
      description = "Assign a license to a specific worker."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully added license to worker"),
      @ApiResponse(responseCode = "404", description = "Worker or license not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}/license/{licenseId}")
  public ResponseEntity<Worker> addLicenseToWorker(
      @Parameter(description = "ID of the worker to assign license")
      @PathVariable Long id,
      @Parameter(description = "ID of the license to assign")
      @PathVariable Long licenseId) {
    if (workerService.getWorkerById(id).isPresent()
        && licenseService.getLicenseById(licenseId) != null) {
      return ResponseEntity.ok(workerService.addLicenseToWorker(id, licenseId));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  /**
   * Assigns all available licenses to all workers.
   *
   * @return a list of workers with assigned licenses
   */
  @Operation(
      summary = "Add all licenses to workers",
      description = "Assign all available licenses to all workers."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully added all licenses to workers"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/all-licenses")
  public ResponseEntity<List<Worker>> addAllLicensesToWorkers() {
    return ResponseEntity.ok(workerService.addAllLicensesToWorkers());
  }

  /**
   * Deletes a worker by their ID.
   *
   * @param id the ID of the worker to delete
   * @return the deleted worker, or null if not found
   */
  @Operation(
      summary = "Delete a worker",
      description = "Remove a worker from the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted worker"),
      @ApiResponse(responseCode = "404", description = "Worker not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Worker> deleteWorker(
      @Parameter(description = "ID of the worker to delete")
      @PathVariable Long id) {
    if (workerService.getWorkerById(id).isPresent()) {
      return ResponseEntity.ok(workerService.deleteWorker(id));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  /**
   * Scheduled task to create timetables for the next month.
   */
  @Operation(
      summary = "Create timetables for the next month",
      description = "Scheduled task to create timetables for the next month."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully created timetables"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @Scheduled(cron = "0 0 0 15 * ?")
  public void createTimetablesForNewMonth() {
    workerService.createWorkerTimetablesForNextMonth(LocalDate.now());
  }
}
