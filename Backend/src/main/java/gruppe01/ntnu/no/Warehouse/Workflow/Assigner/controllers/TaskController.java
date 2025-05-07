package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TaskController handles HTTP requests related to tasks.
 * It provides endpoints to create, read, update, and delete tasks.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    /**
     * Constructor for TaskController.
     *
     * @param taskService The service to handle task operations.
     */
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PostMapping("/{zoneId}")
    public Task createTask(@RequestBody Task task, @PathVariable Long zoneId) {
        return taskService.createTask(task, zoneId);
    }

    @PutMapping("/{id}/{zoneId}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task, @PathVariable Long zoneId) {
        return taskService.updateTask(id, task, zoneId);
    }

    @DeleteMapping("/{id}")
    public Task deleteTask(@PathVariable Long id) {
        return taskService.deleteTask(id);
    }
}
