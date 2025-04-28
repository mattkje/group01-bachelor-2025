package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Utils {

  public static Object getLatestEndTime(Map<Long, ZoneSimResult> zoneSimResults) {
    return zoneSimResults.values().stream()
        .map(ZoneSimResult::getLastEndTime)
        .filter(Objects::nonNull) // Exclude null values
        .max(LocalDateTime::compareTo) // Get the latest LocalDateTime
        .orElse(null); // Return null if no valid end times are found
  }

  public static Map<Long, Double> getSimResultAverages(List<SimulationResult> simulationResults) {
      Map<Long, Double> averages = new HashMap<>();
      for (SimulationResult simulationResult : simulationResults) {
          Map<Long, ZoneSimResult> zoneSimResults = simulationResult.getZoneSimResults();
          for (Map.Entry<Long, ZoneSimResult> entry : zoneSimResults.entrySet()) {
              Long zoneId = entry.getKey();
              ZoneSimResult zoneSimResult = entry.getValue();
              // Convert Duration to double (in minutes) and default to 0.0 if null
              double totalDuration = zoneSimResult.getTotalDuration() != null
                  ? zoneSimResult.getTotalDuration().toMinutes()
                  : 0.0;
              averages.put(zoneId, totalDuration);
          }
      }
      return averages;
  }

}
