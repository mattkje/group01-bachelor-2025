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
    taskTimes.put(taskId, new TaskTime(startTime, endTime));
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

  public LocalDateTime getStartTime(String taskId) {
    TaskTime taskTime = taskTimes.get(taskId);
    return taskTime != null ? taskTime.startTime() : null;
  }

  public LocalDateTime getEndTime(String taskId) {
    TaskTime taskTime = taskTimes.get(taskId);
    return taskTime != null ? taskTime.endTime() : null;
  }

  public int getCompletedTaskCountAtTime(LocalDateTime time) {
    return (int) taskTimes.values().stream()
        .filter(taskTime -> taskTime.startTime() != null && taskTime.endTime() != null)
        .filter(taskTime -> !taskTime.startTime().isAfter(time) && !taskTime.endTime().isAfter(time))
        .count();
  }

  // Inner class to represent the start and end times of a task
  private record TaskTime(LocalDateTime startTime, LocalDateTime endTime) {
  }
}