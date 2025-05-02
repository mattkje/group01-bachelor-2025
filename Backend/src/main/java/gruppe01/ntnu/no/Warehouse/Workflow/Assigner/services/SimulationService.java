package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.MonteCarlo;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.Utils;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

    private static final int SIM_COUNT = 5;

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

//TODO: Fix this to work with a picker zone

    /**
     * Runs a simulation on a zone
     * Returns a list of strings containing the predicted time of completion and any error messages
     *
     * @param zoneId The ID of the zone to run the simulation on
     * @return A list of strings containing the predicted time of completion and any error messages
     */
    //TODO: Remake this to work with the new Zone Simulator 2
   /* public List<String> runZoneSimulation(Long zoneId) {

        ZoneSimulator zoneSimulator = new ZoneSimulator();
        // Ensure the zoneID exists
        if (zoneId == null || zoneService.getZoneById(zoneId) == null) {
            throw new IllegalArgumentException("Zone ID cannot be null and must be a real zone");
        }

        // Create a list of strings to store the response
        List<String> response = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        // Create an atomic double to store the predicted time
        double totalPredictedTime = 0.0;

        for (int i = 0; i < SIM_COUNT; i++) {
            AtomicDouble predictedTime = new AtomicDouble(0.0);
            String result = zoneSimulator.runZoneSimulation(zoneService.getZoneById(zoneId),
                    activeTaskService.getRemainingTasksForTodayByZone(zoneId),
                    pickerTaskService.getPickerTasksForToday(), null, predictedTime, i);
            if (!result.isEmpty() && i == 0) {
                errorMessages.add(result);
            }
            totalPredictedTime += predictedTime.get();
        }

        double predictedTime = totalPredictedTime / SIM_COUNT;

        // Get the current time
        LocalDateTime currentTime = LocalDateTime.now();

        // Format the predicted completion time
        String formattedTime = formatPredictedCompletionTime(currentTime, predictedTime);

        response.add(formattedTime);
        response.addAll(errorMessages);
        return response;
    }*/

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