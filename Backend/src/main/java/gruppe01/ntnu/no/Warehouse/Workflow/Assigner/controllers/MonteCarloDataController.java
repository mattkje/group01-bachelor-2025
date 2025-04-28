package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.MonteCarloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class MonteCarloDataController {

    @Autowired
    private MonteCarloService monteCarloService;

    @Scheduled(cron = "0 */10 * * * *")
    public void generateMonteCarloData() {
        LocalDateTime now = LocalDateTime.now();
        monteCarloService.generateMonteCarloData(now, true);
    }

    @PostMapping
    public void generateMonteCarloDataNow(LocalDateTime now) {
        monteCarloService.generateMonteCarloData(now, false);
    }
}
