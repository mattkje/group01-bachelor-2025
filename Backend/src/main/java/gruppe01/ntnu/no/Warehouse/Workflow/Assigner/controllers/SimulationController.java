package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.SimulationService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.MonteCarloWithRealData;
import java.time.LocalDate;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SimulationController {

  @Autowired
  private ActiveTaskGenerator activeTaskGeneratorService;

  @Autowired
  private MonteCarloWithRealData monteCarloWithRealData;
  @Autowired
  private TimeTableGenerator timeTableGenerator;
  @Autowired
  private WorldSimulation worldSimulation;
  @Autowired
  private SimulationService simulationService;

  //  Generate active tasks for any given day
  // Format for day: YYYY-MM-DD
  // Format for numDays: x amount of days going forward, min 1
  @GetMapping("/generate-active-tasks/{date}/{numDays}")
  public void generateActiveTasks(@PathVariable String date, @PathVariable int numDays)
      throws Exception {
    LocalDate startDate = LocalDate.parse(date);
    activeTaskGeneratorService.generateActiveTasks(startDate, numDays);
  }

  @GetMapping("/monte-carlo")
  public List<String> monteCarlo() throws Exception {
    return simulationService.runCompleteSimulation();
  }

  @GetMapping("/monte-carlo/zones/{id}")
  public List<String> monteCarloZone(@PathVariable Long id) throws InterruptedException {
    return simulationService.runZoneSimulation(id);
  }

  @GetMapping("/generate-timetable/{date}/{numDays}")
  public void generateTimeTable(@PathVariable String date, @PathVariable int numDays) {
    LocalDate startDate = LocalDate.parse(date);
    timeTableGenerator.generateTimeTable(startDate, numDays);
  }

  @GetMapping("/run-world-simulation")
  public void runWorldSimulation() throws Exception {
    worldSimulation.runWorldSimulation(2);
  }

}
