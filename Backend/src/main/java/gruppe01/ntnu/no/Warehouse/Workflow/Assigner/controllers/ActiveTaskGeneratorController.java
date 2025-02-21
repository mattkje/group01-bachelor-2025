package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.MonteCarloWithRealData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ActiveTaskGeneratorController {

    @Autowired
    private ActiveTaskGenerator activeTaskGeneratorService;

    @Autowired
    private MonteCarloWithRealData monteCarloWithRealDataService;
  @Autowired
  private MonteCarloWithRealData monteCarloWithRealData;

    @GetMapping("/generate-active-tasks")
    public void generateActiveTasks() throws Exception {
        activeTaskGeneratorService.generateActiveTasks();
    }

    @GetMapping("/monte-carlo")
    public void monteCarlo() throws Exception {
        monteCarloWithRealData.monteCarlo();
    }
}
