package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorldSimData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.MonteCarloService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorldSimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data")
public class MonteCarloController {

    @Autowired
    private WorldSimService worldSimService;

    @Autowired
    private MonteCarloService monteCarloService;

    @GetMapping("/{zoneId}/{date}")
    public List<WorldSimData> getWorldSimData(@PathVariable LocalDate date, @PathVariable long zoneId) {
        return worldSimService.getMostRecentWorldSimDataByZone(date, zoneId);
    }

    @PostMapping("/world-sim")
    public void generateWorldSimData(LocalDateTime now) {
        worldSimService.generateWorldSimData(now, true);
    }

}
