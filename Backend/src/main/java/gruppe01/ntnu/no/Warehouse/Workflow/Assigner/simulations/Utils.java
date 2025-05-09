package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ErrorMessage;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ErrorMessageService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.MonteCarloService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import static com.google.common.graph.ElementOrder.sorted;

/**
 * Utility class for simulation-related operations.
 * Contains methods for processing simulation results and saving data.
 */
@Component
public class Utils {

    private final MonteCarloService monteCarloService;

    private final ErrorMessageService errorMessageService;

    /**
     * Constructor for Utils.
     *
     * @param monteCarloService the Monte Carlo simulation service
     */
    public Utils(@Autowired MonteCarloService monteCarloService,
                 @Autowired ErrorMessageService errorMessageService) {
        this.monteCarloService = monteCarloService;
        this.errorMessageService = errorMessageService;
    }

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

    public Map<Long, LocalDateTime> getSimResultAverages(List<SimulationResult> simulationResults) {
        Map<Long, List<LocalDateTime>> zoneEndTimes = new HashMap<>();

        // Collect all LocalDateTime values for each zone
        for (SimulationResult simulationResult : simulationResults) {
            Map<Long, ZoneSimResult> zoneSimResults = simulationResult.getZoneSimResults();
            for (Map.Entry<Long, ZoneSimResult> entry : zoneSimResults.entrySet()) {
                Long zoneId = entry.getKey();
                ZoneSimResult zoneSimResult = entry.getValue();
                LocalDateTime endTime = zoneSimResult.getLastEndTime();
                if (endTime == null) {
                    endTime = LocalDateTime.MIN;
                }
                zoneEndTimes.computeIfAbsent(zoneId, k -> new ArrayList<>()).add(endTime);
            }
        }

        // Calculate the average LocalDateTime for each zone
        Map<Long, LocalDateTime> averages = new HashMap<>();
        for (Map.Entry<Long, List<LocalDateTime>> entry : zoneEndTimes.entrySet()) {
            Long zoneId = entry.getKey();
            List<LocalDateTime> endTimes = entry.getValue();

            // Convert LocalDateTime to epoch seconds, calculate average, and convert back
            long averageEpochSeconds = (long) endTimes.stream()
                    .mapToLong(time -> time.toEpochSecond(ZoneOffset.UTC))
                    .average()
                    .orElse(0);

            averages.put(zoneId, LocalDateTime.ofEpochSecond(averageEpochSeconds, 0, ZoneOffset.UTC));
        }

        return averages;
    }

    public void saveSimulationResults(List<SimulationResult> simulationResults, LocalDateTime now) {
        monteCarloService.dropAllData();
        for (int i = 0; i < simulationResults.size(); i++) {
            SimulationResult simulationResult = simulationResults.get(i);
            saveGeneralSimulation(simulationResult, i);
            saveZoneSimulation(simulationResult.getZoneSimResultList(), i, now);
        }
        this.getBestCaseScenarioForEachZoneSim(simulationResults);
    }

    private Integer findHighestValue(Map<LocalDateTime, Integer> timestamps) {
        return Collections.max(timestamps.values());
    }

    private void saveZoneSimulation(List<ZoneSimResult> zoneSimResultList, int i, LocalDateTime now) {
        for (ZoneSimResult zoneSimResult : zoneSimResultList) {
            if (zoneSimResult.getZone() == null) {
                continue; // Skip if zone is null
            }
            Map<LocalDateTime, Integer> timestamps = new HashMap<>();
            LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);

            for (LocalDateTime time = now; !time.isAfter(endOfDay); time = time.plusMinutes(10)) {
                timestamps.put(time, zoneSimResult.getCompletedTaskCountAtTime(time));
            }
            for (Map.Entry<LocalDateTime, Integer> entry : timestamps.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList()) {
                monteCarloService.generateSimulationDataPoint(i, entry.getKey(),
                        entry.getValue(), zoneSimResult.getZone().getId());
                if (entry.getValue().equals(Collections.max(timestamps.values()))) {
                    break;
                }
            }
        }
    }

    private void saveGeneralSimulation(SimulationResult simulationResult, int i) {

        Map<LocalDateTime, Integer> timestamps = getTotalTasksCompleted(simulationResult.getZoneSimResultList());

        int highestValue = findHighestValue(timestamps);
        for (Map.Entry<LocalDateTime, Integer> entry : timestamps.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            if (entry.getValue() == highestValue) {
                monteCarloService.generateSimulationDataPoint(i, entry.getKey(), entry.getValue(), null);
                break; // Exit the loop after recording the highest value
            }
        }
    }

    public void getBestCaseScenarioForEachZoneSim(List<SimulationResult> simulationResults) {
        Map<Long, ErrorMessage> errorMessages = this.errorMessageService.generateErrorMessageMapFromZones();
        errorMessageService.deleteAll();
        for (SimulationResult simulationResult : simulationResults) {
            Map<Long, ZoneSimResult> zoneSimResults = simulationResult.getZoneSimResults();
            for (Map.Entry<Long, ZoneSimResult> entry : zoneSimResults.entrySet()) {
                LocalDateTime lastEndTime = entry.getValue().getLastEndTime();
                ErrorMessage errorMessage = errorMessages.get(entry.getValue().getZone().getId());
                if (lastEndTime != null && errorMessage != null && errorMessage.getTime() != null &&
                    lastEndTime.isBefore(errorMessage.getTime())) {
                    errorMessage.setTime(lastEndTime);
                    // TODO: Consider doing list to string conversion in the service
                    errorMessage.setMessage(entry.getValue().getErrorMessage().toString());
                } else {
                    assert errorMessage != null;
                    if (errorMessage.getTime() == null && lastEndTime != null){
                        errorMessage.setTime(lastEndTime);
                        errorMessage.setMessage(entry.getValue().getErrorMessage().toString());
                    } else {
                        errorMessage.setMessage(entry.getValue().getErrorMessage().toString());
                    }
                }
            }
        }
        errorMessages.values().stream().toList().forEach(errorMessageService::saveErrorMessage);
    }

}