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
import java.util.concurrent.atomic.AtomicInteger;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator;
import org.springframework.stereotype.Service;
import smile.regression.RandomForest;

/**
 * Service class for running simulations in the frontend
 * Contains methods for running simulations on zones
 * Ran from API calls
 */
@Service
public class SimulationService {

    private final AtomicInteger simCount = new AtomicInteger(1);

    private final ZoneService zoneService;

    private final MonteCarlo monteCarloWithRealData;

    private final Utils utils;

    private final TimetableService timetableService;

    private static final MachineLearningModelPicking mlModel = new MachineLearningModelPicking();

    /**
     * Constructor for SimulationService.
     *
     * @param zoneService            the service for Zone entity
     * @param monteCarloWithRealData the Monte Carlo simulation service
     * @param utils                  utility class for simulations
     * @param timetableService       the service for Timetable entity
     */
    public SimulationService(ZoneService zoneService, MonteCarlo monteCarloWithRealData, Utils utils, TimetableService timetableService) {
        this.zoneService = zoneService;
        this.monteCarloWithRealData = monteCarloWithRealData;
        this.utils = utils;
        this.timetableService = timetableService;
    }

    /**
     * TODO: Fix this to work with a picker zone
     * TODO: Remake this to work with the new Zone Simulator 2
     * Runs a simulation on a zone
     * Returns a list of strings containing the predicted time of completion and any error messages
     *
     * @param zoneId The ID of the zone to run the simulation on
     * @return A list of strings containing the predicted time of completion and any error messages
     */
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

        if (zone.getIsPickerZone()) {
            pickerTasks = zoneService.getPickerTasksByZoneId(zoneId);
            models = mlModel.getModel(zone.getName(), false);
        } else {
            activeTasks = zoneService.getActiveTasksByZoneId(zoneId).stream().toList();

        }

        if (day == null) {
            day = LocalDate.now().atStartOfDay();
        }

        for (int i = 0; i < getSimCount(); i++) {
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
     *
     * @param models      The models to use for the simulation
     * @param currentTime The current time
     * @return A map containing the predicted completion time and any error messages
     */
    public Map<Long, List<String>> runCompleteSimulation(Map<String, RandomForest> models, LocalDateTime currentTime)
            throws ExecutionException, InterruptedException, IOException {
        List<SimulationResult> results;
        if (models == null) {
            results = monteCarloWithRealData.monteCarlo(getSimCount(), null, currentTime, timetableService);
            if (currentTime == null) {
                currentTime = LocalDateTime.now();
            }
        } else {
            results = monteCarloWithRealData.monteCarlo(getSimCount(), models, currentTime, timetableService);
        }
        HashMap<Long, List<String>> newResult = new HashMap<>();

        if (!results.isEmpty()) {
            // Calculate the average zone durations using the current time as the reference
            Map<Long, LocalDateTime> averageZoneDurations = utils.getSimResultAverages(results);
            // Format the predicted completion time for each zone
            List<SimulationResult> finalResults = results;
            averageZoneDurations.forEach((zoneId, averageZoneDuration) -> {
                List<String> combinedResult = new ArrayList<>();
                if (averageZoneDuration != null) {
                    combinedResult.addAll(formatPredictedCompletionTime(averageZoneDuration));
                }
                // Ensure error messages are retrieved correctly
                List<String> errorMessages = finalResults.getFirst().getErrorMessage(zoneId);
                if (errorMessages != null) {
                    combinedResult.addAll(errorMessages);
                }
                newResult.put(zoneId, combinedResult);
            });
            utils.saveSimulationResults(results, currentTime);
        } else {
            newResult.put(-1L, Collections.singletonList("No simulation results available."));
        }

        return newResult;
    }

    /**
     * Runs the simulation and returns only the results
     * Used for testing purposes
     *
     * @param models      The models to use for the simulation
     * @param currentTime The current time
     * @return A list of simulation results
     */
    public List<SimulationResult> getSimulationResultsOnly(Map<String, RandomForest> models, LocalDateTime currentTime) throws IOException, ExecutionException, InterruptedException {
        if (models == null) {
            return monteCarloWithRealData.monteCarlo(getSimCount(), null, null, timetableService);
        } else {
            return monteCarloWithRealData.monteCarlo(getSimCount(), models, currentTime, timetableService);
        }
    }


    /**
     * Formats the predicted completion time by adding the given minutes to the current time
     * and formatting it to a string in the format HH:mm.
     *
     * @return The formatted predicted completion time
     */
    private List<String> formatPredictedCompletionTime(LocalDateTime time) {
        // Format the time to a string in the format HH:mm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = time.format(formatter);
        // Create a list to hold the formatted time and any error messages
        List<String> result = new ArrayList<>();
        result.add(formattedTime);
        return result;
    }

    /**
     * Gets the logs from a specified file.
     *
     * @param fileName The name of the log file to retrieve
     * @return The content of the log file as a string
     */
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

    /**
     * Gets the current simulation count.
     *
     * @return The current simulation count
     */
    public int getSimCount() {
        return simCount.get();
    }

    /**
     * Sets the simulation count to a new value.
     *
     * @param newSimCount The new simulation count
     */
    public void setSimCount(int newSimCount) {
        simCount.set(newSimCount);
    }

}