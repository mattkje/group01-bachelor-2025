package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/active-tasks")
public class ActiveTaskController {

    @Autowired
    private ActiveTaskService activeTaskService;

    @GetMapping
    public List<ActiveTask> getAllActiveTasks() {
        return activeTaskService.getAllActiveTasks();
    }

    @GetMapping("/{id}")
    public ActiveTask getActiveTaskById(@PathVariable Long id) {
        return activeTaskService.getActiveTaskById(id);
    }

    @GetMapping("/today")
    public List<ActiveTask> getActiveTasksForToday() {
        return activeTaskService.getActiveTasksForToday(LocalDateTime.now());
    }

    @GetMapping("/completed")
    public List<ActiveTask> getCompletedActiveTasks() {
        return activeTaskService.getCompletedActiveTasks();
    }

    @GetMapping("/not-started")
    public List<ActiveTask> getNotStartedActiveTasks() {
        return activeTaskService.getNotStartedActiveTasks();
    }

    @GetMapping("/in-progress")
    public List<ActiveTask> getActiveTasksInProgress() {
        return activeTaskService.getActiveTasksInProgress();
    }

    @GetMapping("/{id}/workers")
    public List<Worker> getWorkersAssignedToActiveTask(@PathVariable Long id) {
        return activeTaskService.getWorkersAssignedToActiveTask(id);
    }

    @PostMapping("/{taskId}")
    public ActiveTask createActiveTask(@PathVariable Long taskId, @RequestBody ActiveTask activeTask) {
        return activeTaskService.createActiveTask(taskId, activeTask);
    }

    @PutMapping("/{id}")
    public ActiveTask updateActiveTask(@PathVariable Long id, @RequestBody ActiveTask activeTask) {
        return activeTaskService.updateActiveTask(id, activeTask);
    }

    @PutMapping("/{id}/worker/{workerId}")
    public ActiveTask assignWorkerToTask(@PathVariable Long id, @PathVariable Long workerId) {
        return activeTaskService.assignWorkerToTask(id, workerId);
    }

    @PutMapping("/{id}/worker/{workerId}/remove")
    public ActiveTask removeWorkerFromTask(@PathVariable Long id, @PathVariable Long workerId) {
        return activeTaskService.removeWorkerFromTask(id, workerId);
    }

    @PutMapping("/{id}/workers/remove")
    public ActiveTask removeWorkersFromTask(@PathVariable Long id) {
        return activeTaskService.removeWorkersFromTask(id);
    }

    @DeleteMapping("/{id}")
    public ActiveTask deleteActiveTask(@PathVariable Long id) {
        return activeTaskService.deleteActiveTask(id);
    }

    /**
     * Scheduled task to create repeating active tasks at the beginning of each month.
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void scheduleCreateRepeatingActiveTasks() {
        activeTaskService.CreateRepeatingActiveTasks();
    }
}
