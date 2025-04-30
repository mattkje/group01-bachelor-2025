package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorldSimData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.MonteCarloDataService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.MonteCarloService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorldSimDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data")
public class MonteCarloController {

    @Autowired
    private WorldSimDataService worldSimDataService;

    @Autowired
    private MonteCarloService monteCarloService;

    @Autowired
    private MonteCarloDataService monteCarloDataService;

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
}
