package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.MonteCarloService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

@Component
public class Utils {

  @Autowired
  private MonteCarloService monteCarloService;

  public Object getLatestEndTime(Map<Long, ZoneSimResult> zoneSimResults) {
    return zoneSimResults.values().stream()
        .map(ZoneSimResult::getLastEndTime)
        .filter(Objects::nonNull)
        .max(LocalDateTime::compareTo)
        .orElse(null);
  }

  public Map<LocalDateTime, Integer> getTotalTasksCompleted(List<ZoneSimResult> zoneSimResults) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);
    Map<LocalDateTime, Integer> taskCompletionMap = new HashMap<>();

    for (LocalDateTime time = now; !time.isAfter(endOfDay); time = time.plusMinutes(10)) {
      int totalCompletedTasks = 0;

      for (ZoneSimResult zoneSimResult : zoneSimResults) {
        totalCompletedTasks += zoneSimResult.getCompletedTaskCountAtTime(time);
      }

      taskCompletionMap.put(time, totalCompletedTasks);
    }

    return taskCompletionMap;
  }

  public Map<Long, Double> getSimResultAverages(List<SimulationResult> simulationResults) {
    Map<Long, Double> averages = new HashMap<>();
    for (SimulationResult simulationResult : simulationResults) {
      Map<Long, ZoneSimResult> zoneSimResults = simulationResult.getZoneSimResults();
      for (Map.Entry<Long, ZoneSimResult> entry : zoneSimResults.entrySet()) {
        Long zoneId = entry.getKey();
        ZoneSimResult zoneSimResult = entry.getValue();
        double totalDuration = zoneSimResult.getTotalDuration() != null
            ? zoneSimResult.getTotalDuration().toMinutes()
            : 0.0;
        averages.put(zoneId, totalDuration);
      }
    }
    return averages;
  }

  public void saveSimulationResults(List<SimulationResult> simulationResults,LocalDateTime now) {
    monteCarloService.dropAllData();
    for (int i = 0; i < simulationResults.size(); i++) {
      SimulationResult simulationResult = simulationResults.get(i);
      saveGeneralSimulation(simulationResult,i);
      saveZoneSimulation(simulationResult.getZoneSimResultList(), i,now);
    }
  }
  private Integer findHighestValue(Map<LocalDateTime, Integer> timestamps) {
    return Collections.max(timestamps.values());
  }

  private void saveZoneSimulation(List<ZoneSimResult> zoneSimResultList, int i, LocalDateTime now) {
   for (ZoneSimResult zoneSimResult : zoneSimResultList) {
     Map<LocalDateTime,Integer> timestamps = new HashMap<>();
     LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);

     int testInt = 0;
     for (LocalDateTime time = now; !time.isAfter(endOfDay); time = time.plusMinutes(10)) {
       timestamps.put(time,  zoneSimResult.getCompletedTaskCountAtTime(time));
     }
     timestamps.entrySet().stream()
         .sorted(Map.Entry.comparingByKey())
         .forEachOrdered(entry -> {
           if (!entry.getValue().equals(Collections.max(timestamps.values()))) {
             monteCarloService.generateSimulationDataPoint(i, entry.getKey(),
                 entry.getValue(), zoneSimResult.getZoneId());
           }
         });
   }
  }

  private void saveGeneralSimulation(SimulationResult simulationResult, int i){

    Map<LocalDateTime, Integer> timestamps = getTotalTasksCompleted(simulationResult.getZoneSimResultList());

    int highestValue = findHighestValue(timestamps);
    timestamps.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEachOrdered(entry -> {
          if (entry.getValue() < highestValue) {
            monteCarloService.generateSimulationDataPoint(i, entry.getKey(),
                entry.getValue(),null);
          }
        });
  }

}