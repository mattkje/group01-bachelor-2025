package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorldSimData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MonteCarloController handles HTTP requests related to Monte Carlo simulations.
 * It provides endpoints to retrieve world simulation data, generate new data,
 * and get simulation values.
 */
@RestController
@RequestMapping("/api/data")
public class MonteCarloController {

    private final WorldSimDataService worldSimDataService;

    private final ZoneService zoneService;

    private final MonteCarloDataService monteCarloDataService;

    private final WorldSimulation worldSimulation;

    // Constructor for MonteCarloController
    public MonteCarloController(WorldSimDataService worldSimDataService,
                                ZoneService zoneService, MonteCarloDataService monteCarloDataService,
                                WorldSimulation worldSimulation) {
        this.worldSimDataService = worldSimDataService;
        this.zoneService = zoneService;
        this.monteCarloDataService = monteCarloDataService;
        this.worldSimulation = worldSimulation;
    }

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
        LocalDate currentDate = worldSimulation.getCurrentDate(); // Store the result in a variable
        response.put("realData", worldSimDataService.getWorldSimValues(zoneId));
        response.put("simulationData", monteCarloDataService.getMCDataValues(zoneId));
        response.put("currentDate", currentDate);
        response.put("activeTasks", zoneService.getNumberOfTasksForTodayByZone(zoneId, currentDate));
        return response;
    }
}
