package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * TaskController handles HTTP requests related to tasks.
 * It provides endpoints to create, read, update, and delete tasks.
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "TaskController", description = "Controller for managing tasks")
public class TaskController {

    private final TaskService taskService;
    private final ZoneService zoneService;

    /**
     * Constructor for TaskController.
     *
     * @param taskService The service to handle task operations.
     */
    public TaskController(TaskService taskService, ZoneService zoneService) {
        this.taskService = taskService;
        this.zoneService = zoneService;
    }

    @Operation(
            summary = "Get all tasks",
            description = "Retrieve a list of all tasks in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @Operation(
            summary = "Get task by ID",
            description = "Retrieve a task by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved task"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @Parameter(description = "ID of the task to retrieve")
            @PathVariable Long id) {
        if (taskService.getTaskById(id) == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(taskService.getTaskById(id));
        }
    }

    @Operation(
            summary = "Create a new task",
            description = "Create a new task in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created task"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{zoneId}")
    public ResponseEntity<Task> createTask(
            @Parameter(description = "Task object to create")
            @RequestBody Task task,
            @Parameter(description = "ID of the zone to assign the task to")
            @PathVariable Long zoneId) {
        if (task.getMinWorkers() > task.getMaxWorkers() || task.getMinWorkers() <= 0 || task.getName().isEmpty() ||
                task.getZoneId() == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(taskService.createTask(task, zoneId));
        }
    }

    @Operation(
            summary = "Update an existing task",
            description = "Update an existing task in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated task"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/{zoneId}")
    public ResponseEntity<Task> updateTask(
            @Parameter(description = "ID of the task to update")
            @PathVariable Long id,
            @Parameter(description = "Task object with updated data")
            @RequestBody Task task,
            @Parameter(description = "ID of the zone to assign the task to")
            @PathVariable Long zoneId) {
        if (task.getMinWorkers() > task.getMaxWorkers() || task.getMinWorkers() <= 0 || task.getName().isEmpty() ||
                task.getZoneId() == null) {
            return ResponseEntity.badRequest().build();
        } else if (taskService.getTaskById(id) == null || zoneService.getZoneById(id) == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(taskService.updateTask(id, task, zoneId));
        }
    }

    @Operation(
            summary = "Delete a task",
            description = "Delete a task by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted task"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Task> deleteTask(
            @Parameter(description = "ID of the task to delete")
            @PathVariable Long id) {
        if (taskService.getTaskById(id) == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(taskService.deleteTask(id));
        }
    }
}
