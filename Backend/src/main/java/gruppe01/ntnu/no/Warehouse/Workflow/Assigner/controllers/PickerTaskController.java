package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.PickerTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/picker-tasks")
public class PickerTaskController {

    private final PickerTaskService pickerTaskService;

    /**
     * Constructor for PickerTaskController.
     *
     * @param pickerTaskService The service to handle picker task operations.
     */
    public PickerTaskController(PickerTaskService pickerTaskService) {
        this.pickerTaskService = pickerTaskService;
    }

    @GetMapping
    public List<PickerTask> getAllPickerTasks() {
        return pickerTaskService.getAllPickerTasks();
    }

    @GetMapping("/{id}")
    public PickerTask getPickerTaskById(@PathVariable Long id) {
        return pickerTaskService.getPickerTaskById(id);
    }

    @GetMapping("/zone/{zoneId}")
    public List<PickerTask> getPickerTasksByZoneId(@PathVariable Long zoneId) {
        return pickerTaskService.getPickerTaskByZoneId(zoneId);
    }

    @PutMapping("/{pickerTaskId}/zone/{zoneId}")
    public PickerTask updatePickerTask(@PathVariable Long pickerTaskId, @PathVariable Long zoneId, @RequestBody PickerTask pickerTask) {
        return pickerTaskService.updatePickerTask(pickerTaskId, zoneId, pickerTask);
    }

    @PutMapping("/assign-worker/{pickerTaskId}/{workerId}")
    public PickerTask assignWorkerToPickerTask(@PathVariable Long pickerTaskId, @PathVariable Long workerId) {
        return pickerTaskService.assignWorkerToPickerTask(pickerTaskId, workerId);
    }

    @PutMapping("/remove-worker/{pickerTaskId}/{workerId}")
    public PickerTask removeWorkerFromPickerTask(@PathVariable Long pickerTaskId, @PathVariable Long workerId) {
        return pickerTaskService.removeWorkerFromPickerTask(pickerTaskId, workerId);
    }

    @DeleteMapping("/{id}")
    public void deletePickerTask(@PathVariable Long id) {
        pickerTaskService.deletePickerTask(id);
    }
}
