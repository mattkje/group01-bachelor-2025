package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.PickerTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.SimulationService;

import java.io.IOException;
import java.time.LocalDate;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SimulationController {

    private final ActiveTaskGenerator activeTaskGeneratorService;

    private final TimeTableGenerator timeTableGenerator;

    private final WorldSimulation worldSimulation;

    private final SimulationService simulationService;

    private final PickerTaskGenerator pickerTaskGenerator;

    /**
     * Constructor for SimulationController.
     *
     * @param activeTaskGeneratorService The service to handle active task generation.
     * @param timeTableGenerator         The service to generate timetables.
     * @param worldSimulation            The service to run world simulations.
     * @param simulationService          The service to handle simulation operations.
     * @param pickerTaskGenerator        The service to generate picker tasks.
     */
    public SimulationController(ActiveTaskGenerator activeTaskGeneratorService,
                                TimeTableGenerator timeTableGenerator,
                                WorldSimulation worldSimulation,
                                SimulationService simulationService,
                                PickerTaskGenerator pickerTaskGenerator) {
        this.activeTaskGeneratorService = activeTaskGeneratorService;
        this.timeTableGenerator = timeTableGenerator;
        this.worldSimulation = worldSimulation;
        this.simulationService = simulationService;
        this.pickerTaskGenerator = pickerTaskGenerator;
    }

    /**
     * Endpoint to generate active tasks for a given date and number of days.
     *
     * @param date    The start date for generating active tasks.
     * @param numDays The number of days to generate active tasks for.
     */
    @GetMapping("/generate-active-tasks/{date}/{numDays}")
    public void generateActiveTasks(@PathVariable String date, @PathVariable int numDays) {
        LocalDate startDate = LocalDate.parse(date);
        activeTaskGeneratorService.generateActiveTasks(startDate, numDays);
    }

    @GetMapping("/monte-carlo")
    public Map<Long, List<String>> monteCarlo() throws Exception {
        return simulationService.runCompleteSimulation(null, null);
    }

    @GetMapping("/monte-carlo-start-of-day")
    public Map<Long, List<String>> monteCarloStartOfDay() throws Exception {
        return simulationService.runCompleteSimulation(null, LocalDate.now().atStartOfDay());
    }

    @GetMapping("/monte-carlo/zones/{id}")
    public List<ZoneSimResult> monteCarloZone(@PathVariable Long id) throws InterruptedException, IOException {
        return simulationService.runZoneSimulation(id, null);
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
