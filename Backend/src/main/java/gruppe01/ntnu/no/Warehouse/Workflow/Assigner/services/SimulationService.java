package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.MonteCarlo;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.Utils;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smile.regression.RandomForest;

/**
 * Service class for running simulations in the frontend
 * Contains methods for running simulations on zones
 * Ran from API calls
 */
@Service
public class SimulationService {

    private static final int SIM_COUNT = 10;

    @Autowired
    private ActiveTaskService activeTaskService;
    @Autowired
    private ZoneService zoneService;
    @Autowired
    private MonteCarlo monteCarloWithRealData;
    @Autowired
    private PickerTaskService pickerTaskService;
    @Autowired
    private Utils utils;
    @Autowired
    private TimetableService timetableService;

    private static final MachineLearningModelPicking mlModel = new MachineLearningModelPicking();


//TODO: Fix this to work with a picker zone

    /**
     * Runs a simulation on a zone
     * Returns a list of strings containing the predicted time of completion and any error messages
     *
     * @param zoneId The ID of the zone to run the simulation on
     * @return A list of strings containing the predicted time of completion and any error messages
     */
    //TODO: Remake this to work with the new Zone Simulator 2
    public List<ZoneSimResult> runZoneSimulation(Long zoneId, LocalDateTime day) throws IOException {
        Zone zone = zoneService.getZoneById(zoneId);
        // Ensure the zoneID exists
        if (zone == null) {
            throw new IllegalArgumentException("Zone ID cannot be null and must be a real zone");
        }
        ZoneSimulator zoneSimulator = new ZoneSimulator();
        // Create a list of zoneSimResult
        List<ZoneSimResult> zoneSimResults = new ArrayList<>();
        List<ActiveTask> activeTasks = new ArrayList<>();
        Set<PickerTask> pickerTasks = new HashSet<>();
        RandomForest models = null;

        if (zone.getIsPickerZone()){
            pickerTasks = zoneService.getPickerTasksByZoneId(zoneId);
            models = mlModel.getModel(zone.getName(),false);
        } else {
            activeTasks = zoneService.getActiveTasksByZoneId(zoneId).stream().toList();

        }

        if (day == null) {
            day = LocalDateTime.now();
        }

        for (int i = 0; i < SIM_COUNT; i++) {
            ZoneSimResult zoneSimResult = zoneSimulator.runZoneSimulation(
                    zone,
                    activeTasks,
                    pickerTasks,
                    models,
                    day,
                    timetableService
            );
            zoneSimResults.add(zoneSimResult);
        }
        return zoneSimResults;
    }

    /**
     * Runs the complete MC simulation
     * The first element of the response contains the time in which the simulation is predicted to be completed
     * The rest of the elements contain any error messages
     * Call it by using the following: /api/monte-carlo
     */
    public Map<Long, String> runCompleteSimulation(Map<String, RandomForest> models, LocalDateTime currentTime)
            throws ExecutionException, InterruptedException, IOException {
        List<SimulationResult> results = new ArrayList<>();
        if (models == null) {
            results = monteCarloWithRealData.monteCarlo(SIM_COUNT, null, null, timetableService);
            currentTime = LocalDateTime.now();
        } else {
            results = monteCarloWithRealData.monteCarlo(SIM_COUNT, models,currentTime, timetableService);
        }
        HashMap<Long, String> newResult = new HashMap<>();

        if (!results.isEmpty()) {
            // Get the current time
            // Calculate the average zone durations using the current time as the reference
            Map<Long, Double> averageZoneDurations = utils.getSimResultAverages(results);

            // Format the predicted completion time for each zone
            List<SimulationResult> finalResults = results;
            LocalDateTime finalCurrentTime = currentTime;
            averageZoneDurations.forEach((zoneId, averageDuration) -> {
                if (averageDuration > 0) {
                    newResult.put(zoneId, formatPredictedCompletionTime(finalCurrentTime, averageDuration));
                } else {
                    newResult.put(zoneId, finalResults.getFirst().getErrorMessage(zoneId));
                }
            });
            utils.saveSimulationResults(results,currentTime);
        } else {
            newResult.put(-1L, "No simulation results available.");
        }

        return newResult;
    }

    public List<SimulationResult> getSimulationResultsOnly(Map<String, RandomForest> models, LocalDateTime currentTime) throws IOException, ExecutionException, InterruptedException {
        List<SimulationResult> results = new ArrayList<>();
        if (models == null) {
            return monteCarloWithRealData.monteCarlo(SIM_COUNT, null, null, timetableService);
        } else {
            return monteCarloWithRealData.monteCarlo(SIM_COUNT, models,currentTime, timetableService);
        }
    }


    /**
     * Formats the predicted completion time by adding the given minutes to the current time
     * and formatting it to a string in the format HH:mm.
     *
     * @param currentTime The current time
     * @param minutes     The number of minutes to add to the current time
     * @return The formatted predicted completion time
     */
    private String formatPredictedCompletionTime(LocalDateTime currentTime, double minutes) {
        LocalDateTime predictedCompletionTime = currentTime.plusMinutes((long) minutes);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return predictedCompletionTime.format(formatter);
    }

    public String getLogs(String fileName) {
        try {
            // Define the path to the log file
            Path logFilePath = Paths.get("logs", fileName);

            // Read the file content and return it as a string
            return Files.readString(logFilePath);
        } catch (IOException e) {
            // Handle the case where the file is not found or cannot be read
            return "Error reading log file: " + e.getMessage();
        }
    }
}