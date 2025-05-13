package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers.WorldSimulationController;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ErrorMessage;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class for simulation-related operations.
 * Contains methods for processing simulation results and saving data.
 */
@Component
public class Utils {

    private final MonteCarloService monteCarloService;

    private final ErrorMessageService errorMessageService;

    private final ActiveTaskService activeTaskService;

    private final ZoneService zoneService;

    private final PickerTaskService pickerTaskService;


    /**
     * Constructor for Utils.
     *
     * @param monteCarloService the Monte Carlo simulation service
     */
    public Utils(@Autowired MonteCarloService monteCarloService,
                 @Autowired ErrorMessageService errorMessageService,
                 @Autowired ActiveTaskService activeTaskService,
                 @Autowired ZoneService zoneService,
                 @Autowired PickerTaskService pickerTaskService) {
        this.monteCarloService = monteCarloService;
        this.errorMessageService = errorMessageService;
        this.activeTaskService = activeTaskService;
        this.zoneService = zoneService;
        this.pickerTaskService = pickerTaskService;
    }

    public Object getLatestEndTime(Map<Long, ZoneSimResult> zoneSimResults) {
        return zoneSimResults.values().stream()
                .map(ZoneSimResult::getLastEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public Map<LocalDateTime, Integer> getTotalTasksCompleted(List<ZoneSimResult> zoneSimResults, LocalDateTime startTime) {
        LocalDateTime now = startTime;
        if (now == null) {
            now = LocalDateTime.now();
        }
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
        System.out.println("Saving simulation results...");
        monteCarloService.dropAllData();
        for (int i = 0; i < simulationResults.size(); i++) {
            SimulationResult simulationResult = simulationResults.get(i);
            saveGeneralSimulation(simulationResult, i, now);
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

    private void saveGeneralSimulation(SimulationResult simulationResult, int i, LocalDateTime now) {

        Map<LocalDateTime, Integer> timestamps = getTotalTasksCompleted(simulationResult.getZoneSimResultList(), now);

        int highestValue = findHighestValue(timestamps);
        for (Map.Entry<LocalDateTime, Integer> entry : timestamps.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList()) {
            monteCarloService.generateSimulationDataPoint(i, entry.getKey(), entry.getValue(), 0L);
            if (entry.getValue() == highestValue) {
                break; // Exit the loop after recording the highest value
            }
        }
    }

    public void getBestCaseScenarioForEachZoneSim(List<SimulationResult> simulationResults) {
        Map<Long, ErrorMessage> errorMessages = errorMessageService.generateErrorMessageMapFromZones();
        Map<Long, ZoneSimResult> bestCases = new HashMap<>();

        for (SimulationResult simulationResult : simulationResults) {
            simulationResult.getZoneSimResults().forEach((zoneId, zoneSimResult) -> {
                processZoneSimResult(zoneSimResult, errorMessages, bestCases);
            });
        }
        errorMessageService.deleteAll();
        errorMessages.values().forEach(errorMessageService::saveErrorMessage);
        this.saveBestCases(bestCases);
    }

    private void processZoneSimResult(ZoneSimResult zoneSimResult, Map<Long, ErrorMessage> errorMessages, Map<Long, ZoneSimResult> bestCases) {
        LocalDateTime lastEndTime = zoneSimResult.getLastEndTime();
        ErrorMessage errorMessage = errorMessages.get(zoneSimResult.getZone().getId());

        if (errorMessage != null) {
            updateErrorMessage(errorMessage, lastEndTime, zoneSimResult);
            bestCases.put(zoneSimResult.getZone().getId(), zoneSimResult);
        }
    }

    private void updateErrorMessage(ErrorMessage errorMessage, LocalDateTime lastEndTime, ZoneSimResult zoneSimResult) {
        if (lastEndTime != null && (errorMessage.getTime() == null || lastEndTime.isBefore(errorMessage.getTime()))) {
            errorMessage.setTime(lastEndTime);
        }
        errorMessage.setMessage(zoneSimResult.getErrorMessage().toString());
    }

    private void saveBestCases(Map<Long, ZoneSimResult> bestCases) {
        bestCases.values().forEach(zoneSimResult -> {
            if (zoneSimResult.getZone() != null) {
                if (zoneSimResult.getZone().getIsPickerZone()) {
                    savePickerTasks(zoneSimResult);
                } else {
                    saveActiveTasks(zoneSimResult);
                }
            }
        });
    }

    private void savePickerTasks(ZoneSimResult zoneSimResult) {
        Set<PickerTask> pickerTasks = zoneService.getTodayUnfinishedPickerTasksByZoneId(zoneSimResult.getZone().getId());
        zoneSimResult.getPickerTasks().forEach(zonePickerTask -> {
            pickerTasks.stream()
                    .filter(pickerTask -> pickerTask.equals(zonePickerTask)) // Match the same PickerTask
                    .forEach(pickerTask -> {
                        pickerTask.setMcStartTime(zonePickerTask.getStartTime()); // Set startTime
                        pickerTask.setMcEndTime(zonePickerTask.getEndTime());     // Set endTime
                        pickerTaskService.updatePickerTask(pickerTask.getId(), pickerTask.getZoneId(), pickerTask); // Update the task
                    });
        });
    }

    private void saveActiveTasks(ZoneSimResult zoneSimResult) {
        Set<ActiveTask> activeTasks = zoneService.getTodayUnfinishedTasksByZoneId(zoneSimResult.getZone().getId());
        zoneSimResult.getActiveTasks().forEach(zoneActiveTask -> {
            activeTasks.stream()
                    .filter(activeTask -> activeTask.equals(zoneActiveTask)) // Match the same ActiveTask
                    .forEach(activeTask -> {
                        activeTask.setMcStartTime(zoneActiveTask.getStartTime()); // Set mcStartTime
                        activeTask.setMcEndTime(zoneActiveTask.getEndTime());     // Set mcEndTime
                        activeTaskService.updateActiveTask(activeTask.getId(), activeTask); // Update the task
                    });
        });
    }

}