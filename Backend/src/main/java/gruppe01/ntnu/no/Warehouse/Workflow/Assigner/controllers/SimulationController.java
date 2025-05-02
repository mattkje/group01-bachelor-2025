package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.PickerTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.SimulationService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.MonteCarlo;

import java.io.IOException;
import java.time.LocalDate;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SimulationController {

    @Autowired
    private ActiveTaskGenerator activeTaskGeneratorService;

    @Autowired
    private MonteCarlo monteCarloWithRealData;
    @Autowired
    private TimeTableGenerator timeTableGenerator;
    @Autowired
    private WorldSimulation worldSimulation;
    @Autowired
    private SimulationService simulationService;
    @Autowired
    private PickerTaskGenerator pickerTaskGenerator;

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
    public Map<Long, String> monteCarlo() throws Exception {
        return simulationService.runCompleteSimulation(null, null);
    }

    @GetMapping("/test/monte-carlo")
    public List<SimulationResult> monteCarloTest() throws Exception {
        return simulationService.getSimulationResultsOnly(null, null);
    }

    @GetMapping("/logs/{fileName}")
    public String getLogs(@PathVariable String fileName) {
        return simulationService.getLogs(fileName);
    }

    @GetMapping("/monte-carlo/zones/{id}")
    public List<ZoneSimResult> monteCarloZone(@PathVariable Long id) throws InterruptedException, IOException {
        return simulationService.runZoneSimulation(id,null);
    }

@GetMapping("/monte-carlo/zones/{id}/day/{day}")
public List<ZoneSimResult> monteCarloZone(@PathVariable Long id, @PathVariable String day) throws InterruptedException, IOException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date = LocalDate.parse(day, formatter);
    LocalDateTime dateTime = date.atStartOfDay();
    return simulationService.runZoneSimulation(id, dateTime);
}


        @GetMapping("/generate-timetable/{date}")
    public void generateTimeTable(@PathVariable String date) {
        LocalDate startDate = LocalDate.parse(date);
        timeTableGenerator.generateTimeTable(startDate);
    }

    @GetMapping("/generate-picker-tasks/{date}/{numDays}/{numOfTasksPerDay}")
    public void generatePickerTasks(@PathVariable String date, @PathVariable int numDays,
                                    @PathVariable int numOfTasksPerDay) throws Exception {
        LocalDate startDate = LocalDate.parse(date);
        MachineLearningModelPicking machineLearningModelPicking = new MachineLearningModelPicking();
        pickerTaskGenerator.generatePickerTasks(startDate, numDays, numOfTasksPerDay, machineLearningModelPicking);
    }

    @GetMapping("/run-world-simulation")
    public void runWorldSimulation() throws Exception {
        worldSimulation.runWorldSimulation(2, LocalDate.now());
    }

    @GetMapping("/simulate-one-year")
    public void simulateOneYear() throws Exception {
        worldSimulation.simulateOneYear();
    }
}
