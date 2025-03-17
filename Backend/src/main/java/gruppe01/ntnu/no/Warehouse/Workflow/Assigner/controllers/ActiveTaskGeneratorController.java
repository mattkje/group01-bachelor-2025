package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.MonteCarloWithRealData;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    //  Generate active tasks for any given day
    // Format for day: YYYY-MM-DD
   // Format for numDays: x amount of days going forward, min 1
    @GetMapping("/generate-active-tasks/{date}/{numDays}")
    public void generateActiveTasks(@PathVariable String date, @PathVariable int numDays) throws Exception {
        LocalDate startDate = LocalDate.parse(date);
        activeTaskGeneratorService.generateActiveTasks(startDate, numDays);
    }

    @GetMapping("/monte-carlo")
    public void monteCarlo() throws Exception {
        monteCarloWithRealData.monteCarlo();
    }
}
