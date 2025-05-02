package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorldSimData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
public class MonteCarloController {

    @Autowired
    private WorldSimDataService worldSimDataService;

    @Autowired
    private MonteCarloService monteCarloService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private MonteCarloDataService monteCarloDataService;

    @Autowired
    private WorldSimulation worldSimulation;

    @GetMapping("/{zoneId}")
    public List<WorldSimData> getWorldSimData(@PathVariable long zoneId) {
        return worldSimDataService.getMostRecentWorldSimDataByZone(zoneId);
    }

    @GetMapping("/{zoneId}/values")
    public List<Integer> getWorldSimValues(@PathVariable long zoneId) {
        return worldSimDataService.getWorldSimValues(zoneId);
    }

    @PostMapping("/world-sim")
    public void generateWorldSimData(LocalDateTime now) {
        worldSimDataService.generateWorldSimData(now, true);
    }

    @GetMapping("/{zoneId}/mcvalues")
    public List<List<Integer>> getMCSimValues(@PathVariable long zoneId) {
        return monteCarloDataService.getMCDataValues(zoneId);
    }

    @GetMapping("/{zoneId}/graph-data")
    public Map<String, Object> getAllData(@PathVariable long zoneId) {
        Map<String, Object> response = new HashMap<>();
        response.put("realData", worldSimDataService.getWorldSimValues(zoneId));
        response.put("simulationData", monteCarloDataService.getMCDataValues(zoneId));
        response.put("currentDate", worldSimulation.getCurrentDate());
        response.put("activeTasks", zoneService.getNumberOfTasksForTodayByZone(zoneId, worldSimulation.getCurrentDate()));
        return response;
    }
}
