package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorkerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * WorkerController handles HTTP requests related to workers.
 * It provides endpoints to create, read, update, and delete workers.
 */
@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService workerService;

    /**
     * Constructor for WorkerController.
     *
     * @param workerService The service to handle worker operations.
     */
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping
    public List<Worker> getAllWorkers() {
        return workerService.getAllWorkers();
    }

    @GetMapping("/available")
    public List<Worker> getAvailableWorkers() {
        return workerService.getAvailableWorkers();
    }

    @GetMapping("/unavailable")
    public List<Worker> getUnavailableWorkers() {
        return workerService.getUnavailableWorkers();
    }

    @GetMapping("/{id}")
    public Optional<Worker> getWorkerById(@PathVariable Long id) {
        return workerService.getWorkerById(id);
    }

    @PostMapping
    public Worker addWorker(@RequestBody Worker worker) {
        return workerService.addWorker(worker);
    }

    @PutMapping("/{id}")
    public Worker updateWorker(@PathVariable Long id, @RequestBody Worker worker) {
        return workerService.updateWorker(id, worker);
    }

    @PutMapping("/{id}/availability")
    public Worker updateWorkerAvailability(@PathVariable Long id) {
        return workerService.updateWorkerAvailability(id);
    }

    @PutMapping("/{workerId}/zone/{zoneId}")
    public Worker addWorkerToZone(@PathVariable Long workerId, @PathVariable Long zoneId) {
        return workerService.addWorkerToZone(workerId, zoneId);
    }

    @PutMapping("/{id}/license/{licenseId}")
    public Worker addLicenseToWorker(@PathVariable Long id, @PathVariable Long licenseId) {
        return workerService.addLicenseToWorker(id, licenseId);
    }

    @DeleteMapping("/{id}")
    public Worker deleteWorker(@PathVariable Long id) {
        return workerService.deleteWorker(id);
    }

    /**
     * Scheduled task to create timetables for the next month.
     */
    @Scheduled(cron = "0 0 0 15 * ?")
    public void createTimetablesForNewMonth() {
        workerService.createWorkerTimetablesForNextMonth(LocalDate.now());
    }
}
