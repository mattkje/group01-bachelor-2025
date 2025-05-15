package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.PickerTaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/picker-tasks")
@Tag(name = "PickerTaskController", description = "Controller for managing picker tasks")
public class PickerTaskController {

  private final PickerTaskService pickerTaskService;
  private final ZoneService zoneService;
  private final WorkerService workerService;

  /**
   * Constructor for PickerTaskController.
   *
   * @param pickerTaskService The service to handle picker task operations.
   */
  public PickerTaskController(PickerTaskService pickerTaskService, ZoneService zoneService,
                              WorkerService workerService) {
    this.pickerTaskService = pickerTaskService;
    this.zoneService = zoneService;
    this.workerService = workerService;
  }

  @Operation(
      summary = "Get all picker tasks",
      description = "Retrieve a list of all picker tasks in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved picker tasks"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<List<PickerTask>> getAllPickerTasks() {
    return ResponseEntity.ok(pickerTaskService.getAllPickerTasks());
  }

  @Operation(
      summary = "Get picker task by ID",
      description = "Retrieve a picker task by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved picker task"),
      @ApiResponse(responseCode = "404", description = "Picker task not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<PickerTask> getPickerTaskById(
      @Parameter(description = "ID of the picker task to retrieve")
      @PathVariable Long id) {
    if (pickerTaskService.getPickerTaskById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(pickerTaskService.getPickerTaskById(id));
    }
  }

  @Operation(
      summary = "Get picker tasks by zone ID",
      description = "Retrieve a list of picker tasks by zone ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved picker tasks"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/zone/{zoneId}")
  public ResponseEntity<List<PickerTask>> getPickerTasksByZoneId(
      @Parameter(description = "ID of the zone to retrieve picker tasks for")
      @PathVariable Long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(pickerTaskService.getPickerTaskByZoneId(zoneId));
    }
  }

  @Operation(
      summary = "Create a new picker task",
      description = "Create a new picker task in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully created picker task"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Zone not found or PickerTask not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{pickerTaskId}/zone/{zoneId}")
  public ResponseEntity<PickerTask> updatePickerTask(
      @Parameter(description = "ID of the picker task to update")
      @PathVariable Long pickerTaskId,
      @Parameter(description = "ID of the zone to assign the picker task to")
      @PathVariable Long zoneId,
      @Parameter(description = "PickerTask object to update")
      @RequestBody PickerTask pickerTask) {
    if (pickerTask.getZone() == null || pickerTask.getDate() == null) {
      return ResponseEntity.badRequest().build();
    } else if (zoneService.getZoneById(zoneId) == null ||
        pickerTaskService.getPickerTaskById(pickerTaskId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(
          pickerTaskService.updatePickerTask(pickerTaskId, zoneId, pickerTask));
    }
  }

  @Operation(
      summary = "Assign a worker to a picker task",
      description = "Assign a worker to a picker task."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully assigned worker to picker task"),
      @ApiResponse(responseCode = "404", description = "Picker task or worker not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/assign-worker/{pickerTaskId}/{workerId}")
  public ResponseEntity<PickerTask> assignWorkerToPickerTask(
      @Parameter(description = "ID of the picker task to assign a worker to")
      @PathVariable Long pickerTaskId,
      @Parameter(description = "ID of the worker to assign to the picker task")
      @PathVariable Long workerId) {
    if (pickerTaskService.getPickerTaskById(pickerTaskId) == null ||
        workerService.getWorkerById(workerId).isEmpty()) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(pickerTaskService.assignWorkerToPickerTask(pickerTaskId, workerId));
    }
  }

  @Operation(
      summary = "Remove a worker from a picker task",
      description = "Remove a worker from a picker task."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully removed worker from picker task"),
      @ApiResponse(responseCode = "404", description = "Picker task or worker not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/remove-worker/{pickerTaskId}/{workerId}")
  public ResponseEntity<PickerTask> removeWorkerFromPickerTask(
      @Parameter(description = "ID of the picker task to remove a worker from")
      @PathVariable Long pickerTaskId,
      @Parameter(description = "ID of the worker to remove from the picker task")
      @PathVariable Long workerId) {
    if (pickerTaskService.getPickerTaskById(pickerTaskId) == null ||
        workerService.getWorkerById(workerId).isEmpty()) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(
          pickerTaskService.removeWorkerFromPickerTask(pickerTaskId, workerId));
    }
  }

  @Operation(
      summary = "Create a new picker task",
      description = "Create a new picker task in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully created picker task"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/{zoneId}")
  public ResponseEntity<PickerTask> createPickerTask(
      @Parameter(description = "ID of the zone to assign the picker task to")
      @PathVariable Long zoneId,
      @Parameter(description = "PickerTask object to create")
      @RequestBody PickerTask pickerTask) {
    if (pickerTask.getZone() == null || pickerTask.getDate() == null) {
      return ResponseEntity.badRequest().build();
    } else if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(pickerTaskService.createPickerTask(zoneId, pickerTask));
    }
  }

  @Operation(
      summary = "Delete a picker task",
      description = "Delete a picker task by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted picker task"),
      @ApiResponse(responseCode = "404", description = "Picker task not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<PickerTask> deletePickerTask(
      @Parameter(description = "ID of the picker task to delete")
      @PathVariable Long id) {
    if (pickerTaskService.getPickerTaskById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(pickerTaskService.deletePickerTask(id));
    }
  }
}
