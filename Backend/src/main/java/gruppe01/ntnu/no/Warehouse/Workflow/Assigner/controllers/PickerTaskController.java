package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.PickerTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/picker-tasks")
public class PickerTaskController {

    @Autowired
    private PickerTaskService pickerTaskService;

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
}
