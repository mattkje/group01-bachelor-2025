package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ZoneSimResult {

    // Map to store tasks and their start and end times
    private final Map<String, TaskTime> taskTimes = new HashMap<>();
    private String errormessage = "";


    public void setErrorMessage(String errorMessage) {
        this.errormessage = errorMessage;
    }
    // Method to add a task with its start and end times
    public void addTask(String taskId, LocalDateTime startTime, LocalDateTime endTime) {
        TaskTime existingTaskTime = taskTimes.get(taskId);

        if (existingTaskTime != null) {
            // Calculate the new average start and end times
            LocalDateTime newStartTime = averageTime(existingTaskTime.startTime(), startTime, existingTaskTime.count());
            LocalDateTime newEndTime = averageTime(existingTaskTime.endTime(), endTime, existingTaskTime.count());

            // Update the task with the new averaged times and increment the count
            taskTimes.put(taskId, new TaskTime(newStartTime, newEndTime, existingTaskTime.count() + 1));
        } else {
            // Add a new task with a count of 1
            taskTimes.put(taskId, new TaskTime(startTime, endTime, 1));
        }
    }

    public String getErrorMessage() {
        return errormessage;
    }

    // Method to get the duration of a specific task
    public Duration getTaskDuration(String taskId) {
        TaskTime taskTime = taskTimes.get(taskId);
        if (taskTime != null && taskTime.startTime() != null && taskTime.endTime() != null) {
            return Duration.between(taskTime.startTime(), taskTime.endTime());
        }
        return Duration.ZERO;
    }

    public LocalDateTime getLastEndTime() {
        return taskTimes.values().stream()
                .map(TaskTime::endTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    // Method to get the total duration of all tasks
    public Duration getTotalDuration() {
        return taskTimes.values().stream()
                .filter(taskTime -> taskTime.startTime() != null && taskTime.endTime() != null)
                .map(taskTime -> Duration.between(taskTime.startTime(), taskTime.endTime()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    // Helper method to calculate the average of two LocalDateTime values
    private LocalDateTime averageTime(LocalDateTime existingTime, LocalDateTime newTime, int count) {
        long averageSeconds = (existingTime.toEpochSecond(java.time.ZoneOffset.UTC) * count + newTime.toEpochSecond(java.time.ZoneOffset.UTC)) / (count + 1);
        return LocalDateTime.ofEpochSecond(averageSeconds, 0, java.time.ZoneOffset.UTC);
    }

    // Inner class to represent the start and end times of a task, along with a count
    private record TaskTime(LocalDateTime startTime, LocalDateTime endTime, int count) {
    }
}