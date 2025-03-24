package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for running simulations in the frontend
 * Contains methods for running simulations on zones
 * Ran from API calls
 */
@Service
public class SimulationService {


  @Autowired
  private ActiveTaskService activeTaskService;

  @Autowired
  private ZoneService zoneService;

  /**
   * Runs a simulation on a zone
   * Returns a list of strings containing the predicted time of completion and any error messages
   *
   * @param zoneId The ID of the zone to run the simulation on
   * @return A list of strings containing the predicted time of completion and any error messages
   */
  public List<String> runZoneSimulation(Long zoneId) throws InterruptedException {

    // Ensure the zoneID exists
    if (zoneId == null || zoneService.getZoneById(zoneId) == null) {
      throw new IllegalArgumentException("Zone ID cannot be null and must be a real zone");
    }

    int simCount = 100;

    // Create a list of strings to store the response
    List<String> response = new ArrayList<>();
    List<String> errorMessages = new ArrayList<>();


    // Create an atomic double to store the predicted time

    double totalPredictedTime = 0.0;

    for (int i = 0; i < simCount; i++) {
        AtomicDouble predictedTime = new AtomicDouble(0.0);
        String result = ZoneSimulator.runZoneSimulation(zoneService.getZoneById(zoneId),
            activeTaskService.getRemainingTasksForTodayByZone(zoneId), predictedTime);
        if (!result.isEmpty()){
          errorMessages.add(result);
        }
        totalPredictedTime += predictedTime.get();
    }

    AtomicDouble predictedTime = new AtomicDouble(0.0);

    // Set the accumulated predicted time
    predictedTime.set(totalPredictedTime / simCount);

    // Get the current time
    LocalDateTime currentTime = LocalDateTime.now();

    // Add the predicted time (in minutes) to the current time
    LocalDateTime predictedCompletionTime = currentTime.plusMinutes((long) predictedTime.get());

    // Format the resulting LocalDateTime to a string in the format HH:mm
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    String formattedTime = predictedCompletionTime.format(formatter);

    response.add(formattedTime);
    response.addAll(errorMessages);
    return response;
  }


}
