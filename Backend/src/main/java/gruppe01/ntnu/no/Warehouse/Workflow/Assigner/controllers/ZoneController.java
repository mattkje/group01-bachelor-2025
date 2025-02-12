package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    @GetMapping
    public List<Zone> getAllZones() {
        return zoneService.getAllZones();
    }

    @GetMapping("/{id}")
    public Zone getZoneById(@PathVariable Long id) {
        return zoneService.getZoneById(id);
    }

    @GetMapping("/{id}/workers")
    public List<Worker> getWorkersByZoneId(@PathVariable Long id) {
        return zoneService.getWorkersByZoneId(id);
    }

    @GetMapping("/{id}/tasks")
    public List<Task> getTasksByZoneId(@PathVariable Long id) {
        return zoneService.getTasksByZoneId(id);
    }

    @PostMapping
    public Zone addZone(@RequestBody Zone zone) {
        return zoneService.addZone(zone);
    }

    @PutMapping("/{id}")
    public Zone updateZone(@PathVariable Long id, @RequestBody Zone zone) {
        return zoneService.updateZone(id, zone);
    }

    @PutMapping("/{id}/workers")
    public Zone addWorkerToZone(@PathVariable Long id, @RequestBody int workerId) {
        return zoneService.addWorkerToZone(id, workerId);
    }
}
