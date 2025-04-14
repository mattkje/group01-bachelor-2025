package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    @GetMapping
    public List<Zone> getAllZones() {
        return zoneService.getAllZones();
    }

    @GetMapping("/task-zones")
    public List<Zone> getAllTaskZones() {
        return zoneService.getAllTaskZones();
    }

    @GetMapping("/picker-zones")
    public List<Zone> getAllPickerZones() {
        return zoneService.getAllPickerZones();
    }

    @GetMapping("/{id}")
    public Zone getZoneById(@PathVariable Long id) {
        return zoneService.getZoneById(id);
    }

    @GetMapping("/{id}/workers")
    public Set<Worker> getWorkersByZoneId(@PathVariable Long id) {
        return zoneService.getWorkersByZoneId(id);
    }

    @GetMapping("/{id}/tasks")
    public Set<Task> getTasksByZoneId(@PathVariable Long id) {
        return zoneService.getTasksByZoneId(id);
    }

    @GetMapping("/{id}/active-tasks")
    public Set<ActiveTask> getActiveTasksByZoneId(@PathVariable Long id) {
        return zoneService.getActiveTasksByZoneId(id);
    }

    @GetMapping("/{id}/picker-tasks")
    public Set<PickerTask> getPickerTasksByZoneId(@PathVariable Long id) {
        return zoneService.getPickerTasksByZoneId(id);
    }

    @GetMapping("/{id}/active-tasks-now")
    public Set<ActiveTask> getActiveTasksByZoneIdNow(@PathVariable Long id) {
        return zoneService.getTodaysUnfinishedTasksByZoneId(id);
    }

    @PostMapping
    public Zone addZone(@RequestBody Zone zone) {
        return zoneService.addZone(zone);
    }

    @PutMapping("/{id}")
    public Zone updateZone(@PathVariable Long id, @RequestBody Zone zone) {
        return zoneService.updateZone(id, zone);
    }

    @PutMapping("/is-picker-zone/{id}")
    public Zone updatePickerZone(@PathVariable Long id) {
        return zoneService.changeIsPickerZone(id);
    }

    @DeleteMapping("/{id}")
    public Zone deleteZone(@PathVariable Long id) {
        return zoneService.deleteZone(id);
    }
}
