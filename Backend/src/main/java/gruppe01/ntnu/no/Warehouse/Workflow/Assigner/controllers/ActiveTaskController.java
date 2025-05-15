package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
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
 * ActiveTaskController handles HTTP requests related to active tasks.
 * It provides endpoints to create, read, update, and delete active tasks.
 */
@RestController
@RequestMapping("/api/active-tasks")
@Tag(name = "ActiveTaskController", description = "Controller for managing active tasks")
public class ActiveTaskController {

  private final ActiveTaskService activeTaskService;
  private final ZoneService zoneService;
  private final TaskService taskService;
  private final WorkerService workerService;

  /**
   * Constructor for ActiveTaskController.
   *
   * @param activeTaskService The service to handle active task operations.
   */
  public ActiveTaskController(ActiveTaskService activeTaskService, ZoneService zoneService,
                              TaskService taskService, WorkerService workerService) {
    this.activeTaskService = activeTaskService;
    this.zoneService = zoneService;
    this.taskService = taskService;
    this.workerService = workerService;
  }

  @Operation(
      summary = "Get all active tasks",
      description = "Retrieve a list of all active tasks in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved active tasks"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<List<ActiveTask>> getAllActiveTasks() {
    return ResponseEntity.ok(activeTaskService.getAllActiveTasks());
  }

  /**
   * Get ActiveTask by ID.
   *
   * @param id ID of the active task to retrieve
   * @return ResponseEntity containing the active task if found, or a 404 Not Found status
   */
  @Operation(
      summary = "Get active task by ID",
      description = "Retrieve an active task by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved active task"),
      @ApiResponse(responseCode = "404", description = "Active task not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ActiveTask> getActiveTaskById(
      @Parameter(description = "ID of the active task to retrieve")
      @PathVariable Long id) {
    if (activeTaskService.getActiveTaskById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(activeTaskService.getActiveTaskById(id));
    }
  }

  @Operation(
      summary = "Get active tasks for today",
      description = "Retrieve a list of active tasks for today."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved active tasks for today"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/today")
  public ResponseEntity<List<ActiveTask>> getActiveTasksForToday() {
    return ResponseEntity.ok(activeTaskService.getActiveTasksForToday(LocalDateTime.now()));
  }

  /**
   * Get active tasks for today by zone.
   *
   * @param zoneId ID of the zone to retrieve active tasks for today
   * @return ResponseEntity containing the list of active tasks for today by zone
   */
  @Operation(
      summary = "Get active tasks for today by zone",
      description = "Retrieve a list of active tasks for today by zone ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved active tasks for today by zone"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/today/{zoneId}")
  public ResponseEntity<List<ActiveTask>> getActiveTasksForTodayByZone(
      @Parameter(description = "ID of the zone to retrieve active tasks for today")
      @PathVariable Long zoneId) {
    if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(
          activeTaskService.getActiveTasksForTodayByZone(zoneId, LocalDateTime.now()));
    }
  }

  @Operation(
      summary = "Get all completed active tasks",
      description = "Retrieve a list of all completed active tasks."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved completed active tasks"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/completed")
  public ResponseEntity<List<ActiveTask>> getCompletedActiveTasks() {
    return ResponseEntity.ok(activeTaskService.getCompletedActiveTasks());
  }

  @Operation(
      summary = "Get all not started active tasks",
      description = "Retrieve a list of all not started active tasks."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved not started active tasks"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/not-started")
  public ResponseEntity<List<ActiveTask>> getNotStartedActiveTasks() {
    return ResponseEntity.ok(activeTaskService.getNotStartedActiveTasks());
  }

  @Operation(
      summary = "Get all active tasks in progress",
      description = "Retrieve a list of all active tasks that are currently in progress."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved active tasks in progress"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/in-progress")
  public ResponseEntity<List<ActiveTask>> getActiveTasksInProgress() {
    return ResponseEntity.ok(activeTaskService.getActiveTasksInProgress());
  }

  /**
   * Get workers assigned to ActiveTask.
   *
   * @param id ID of the active task to retrieve workers for
   * @return ResponseEntity containing the list of workers assigned to the active task
   */
  @Operation(
      summary = "Get workers assigned to active task",
      description = "Retrieve a list of workers assigned to an active task by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved workers assigned to active task"),
      @ApiResponse(responseCode = "404", description = "Active task not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}/workers")
  public ResponseEntity<List<Worker>> getWorkersAssignedToActiveTask(
      @Parameter(description = "ID of the active task to retrieve workers for")
      @PathVariable Long id) {
    if (activeTaskService.getActiveTaskById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(activeTaskService.getWorkersAssignedToActiveTask(id));
    }
  }

  /**
   * Create a new active task.
   *
   * @param taskId     ID of the task to create an active task for
   * @param activeTask ActiveTask object to create
   * @return ResponseEntity containing the created active task
   */
  @Operation(
      summary = "Create a new active task",
      description = "Create a new active task for a specific task ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully created active task"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Task not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/{taskId}")
  public ResponseEntity<ActiveTask> createActiveTask(
      @Parameter(description = "ID of the task to create an active task for")
      @PathVariable Long taskId,
      @Parameter(description = "ActiveTask object to create")
      @RequestBody ActiveTask activeTask) {
    if (taskService.getTaskById(taskId) == null) {
      return ResponseEntity.notFound().build();
    } else if (activeTask.getDate() == null) {
      return ResponseEntity.badRequest().build();
    } else {
      return new ResponseEntity<>(activeTaskService.createActiveTask(taskId, activeTask),
          HttpStatus.CREATED);
    }
  }

  /**
   * Update an existing active task.
   *
   * @param id         ID of the active task to update
   * @param activeTask ActiveTask object to update
   * @return ResponseEntity containing the updated active task
   */
  @Operation(
      summary = "Update an active task",
      description = "Update an existing active task by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated active task"),
      @ApiResponse(responseCode = "404", description = "Active task not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<ActiveTask> updateActiveTask(
      @Parameter(description = "ID of the active task to update")
      @PathVariable Long id,
      @Parameter(description = "ActiveTask object to update")
      @RequestBody ActiveTask activeTask) {
    if (activeTaskService.getActiveTaskById(id) == null) {
      return ResponseEntity.notFound().build();
    } else if (activeTask.getDate() == null) {
      return ResponseEntity.badRequest().build();
    } else {
      return ResponseEntity.ok(activeTaskService.updateActiveTask(id, activeTask));
    }
  }

  /**
   * Assign a worker to an active task.
   *
   * @param id       ID of the active task to assign a worker to
   * @param workerId ID of the worker to assign to the active task
   * @return ResponseEntity containing the updated active task
   */
  @Operation(
      summary = "Assign a worker to an active task",
      description = "Assign a worker to an active task by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully assigned worker to active task"),
      @ApiResponse(responseCode = "404", description = "Active task or worker not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}/worker/{workerId}")
  public ResponseEntity<ActiveTask> assignWorkerToTask(
      @Parameter(description = "ID of the active task to assign a worker to")
      @PathVariable Long id,
      @Parameter(description = "ID of the worker to assign to the active task")
      @PathVariable Long workerId) {
    if (activeTaskService.getActiveTaskById(id) == null
        || workerService.getWorkerById(workerId).isEmpty()) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(activeTaskService.assignWorkerToTask(id, workerId));
    }
  }

  /**
   * Remove a worker from an active task.
   *
   * @param id       ID of the active task to remove a worker from
   * @param workerId ID of the worker to remove from the active task
   * @return ResponseEntity containing the updated active task
   */
  @Operation(
      summary = "Remove a worker from an active task",
      description = "Remove a worker from an active task by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully removed worker from active task"),
      @ApiResponse(responseCode = "404", description = "Active task or worker not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}/worker/{workerId}/remove")
  public ResponseEntity<ActiveTask> removeWorkerFromTask(
      @Parameter(description = "ID of the active task to remove a worker from")
      @PathVariable Long id,
      @Parameter(description = "ID of the worker to remove from the active task")
      @PathVariable Long workerId) {
    if (activeTaskService.getActiveTaskById(id) == null
        || workerService.getWorkerById(workerId).isEmpty()) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(activeTaskService.removeWorkerFromTask(id, workerId));
    }
  }

  /**
   * Remove all workers from an active task.
   *
   * @param id ID of the active task to remove all workers from
   * @return ResponseEntity containing the updated active task
   */
  @Operation(
      summary = "Remove all workers from an active task",
      description = "Remove all workers from an active task by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully removed all workers from active task"),
      @ApiResponse(responseCode = "404", description = "Active task not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}/workers/remove")
  public ResponseEntity<ActiveTask> removeWorkersFromTask(
      @Parameter(description = "ID of the active task to remove all workers from")
      @PathVariable Long id) {
    if (activeTaskService.getActiveTaskById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(activeTaskService.removeWorkersFromTask(id));
    }
  }

  /**
   * Delete an active task.
   *
   * @param id ID of the active task to delete
   * @return ResponseEntity containing the deleted active task
   */
  @Operation(
      summary = "Delete an active task",
      description = "Delete an active task by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted active task"),
      @ApiResponse(responseCode = "404", description = "Active task not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<ActiveTask> deleteActiveTask(
      @Parameter(description = "ID of the active task to delete")
      @PathVariable Long id) {
    if (activeTaskService.getActiveTaskById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(activeTaskService.deleteActiveTask(id));
    }
  }

  /**
   * Scheduled task to create repeating active tasks at the beginning of each month.
   */
  @Operation(
      summary = "Scheduled task to create repeating active tasks",
      description = "Creates repeating active tasks at the beginning of each month."
  )
  @Scheduled(cron = "0 0 0 1 * ?")
  public void scheduleCreateRepeatingActiveTasks() {
    activeTaskService.createRepeatingActiveTasks();
  }
}
