package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.MonteCarloNormalZone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for running simulations in the frontend
 * Contains methods for running simulations on zones
 * Ran from API calls
 */
@Service
public class SimulationService {

  private static final int SIM_COUNT = 2;

  @Autowired
  private ActiveTaskService activeTaskService;

  @Autowired
  private ZoneService zoneService;

  @Autowired
  private MonteCarloNormalZone monteCarloWithRealData;
  @Autowired
  private PickerTaskService pickerTaskService;


  /**
   * Runs a simulation on a zone
   * Returns a list of strings containing the predicted time of completion and any error messages
   *
   * @param zoneId The ID of the zone to run the simulation on
   * @return A list of strings containing the predicted time of completion and any error messages
   */
  public List<String> runZoneSimulation(Long zoneId) {

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
      String result = ZoneSimulator.runZoneSimulation(zoneService.getZoneById(zoneId),
          activeTaskService.getRemainingTasksForTodayByZone(zoneId),pickerTaskService.getPickerTasksForToday(), predictedTime, i);
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
  }

  /**
   * Runs the complete MC simulation
   * The first element of the response contains the time in which the simulation is predicted to be completed
   * The rest of the elements contain any error messages
   * Call it by using the following: /api/monte-carlo
   */
  public Map<Long, List<String>> runCompleteSimulation()
      throws ExecutionException, InterruptedException {
    List<SimulationResult> results = monteCarloWithRealData.monteCarlo(SIM_COUNT);
    HashMap<Long, List<String>> newResult = new HashMap<>();


    if (!results.isEmpty()) {
      // Get the current time
      LocalDateTime currentTime = LocalDateTime.now();
      // Calculate the average zone durations
      Map<Long, Double> averageZoneDurations =
          SimulationResult.calculateAverageZoneDurations(results);

      // Format the predicted completion time for each zone
      averageZoneDurations.forEach((zoneId, averageDuration) -> {
        if (averageDuration > 0) {
          String formattedTime = formatPredictedCompletionTime(currentTime, averageDuration);
          newResult.put(zoneId, List.of(formattedTime));
        } else {
          // Find the error messages for the zone
          for (SimulationResult result : results) {
            if (result.getZoneDurations().containsKey(zoneId)) {
              newResult.put(zoneId,
                  result.getErrorMessages().stream().filter(s -> s.contains("ZONE " + zoneId))
                      .findFirst().map(List::of).orElse(List.of()));
              break;
            }
          }
        }
      });

    } else {
      newResult.put(-1L, List.of("No simulation results available."));
    }

    return newResult;
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
}