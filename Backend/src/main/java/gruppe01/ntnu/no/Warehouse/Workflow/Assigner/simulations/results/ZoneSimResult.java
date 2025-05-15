package gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.results;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ZoneSimResult {

  private Zone zone;
  private List<ActiveTask> activeTasks;
  private List<PickerTask> pickerTasks;
  private List<String> errorMessages = new ArrayList<>();

  public ZoneSimResult() {
    this.activeTasks = new ArrayList<>();
    this.pickerTasks = new ArrayList<>();
  }

  public void setZone(Zone zone) {
    this.zone = zone;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessages.add(errorMessage);
  }

  public void addTask(ActiveTask task, PickerTask pickerTask) {
    if (task == null && pickerTask == null) {
      throw new IllegalArgumentException("Both task and pickerTask cannot be null");
    }
    if (task == null) {
      this.pickerTasks.add(pickerTask);

    } else {
      this.activeTasks.add(task);
    }
  }

  public List<String> getErrorMessage() {
    return errorMessages;
  }

  public LocalDateTime getLastEndTime() {
    if (zone != null && zone.getIsPickerZone()) {
      return pickerTasks.stream()
          .map(PickerTask::getEndTime)
          .filter(Objects::nonNull)
          .max(LocalDateTime::compareTo)
          .orElse(null);
    } else {
      return activeTasks.stream()
          .map(ActiveTask::getEndTime)
          .filter(Objects::nonNull)
          .max(LocalDateTime::compareTo)
          .orElse(null);
    }
  }

  public int getCompletedTaskCountAtTime(LocalDateTime time) {
    if (zone != null && zone.getIsPickerZone()) {
      return (int) pickerTasks.stream()
          .filter(task -> task.getEndTime() != null && !task.getEndTime().isAfter(time))
          .count();
    } else {
      return (int) activeTasks.stream()
          .filter(task -> task.getEndTime() != null && !task.getEndTime().isAfter(time))
          .count();
    }
  }

  public Zone getZone() {
    return zone;
  }

  public List<ActiveTask> getActiveTasks() {
    return activeTasks;
  }

  public List<PickerTask> getPickerTasks() {
    return pickerTasks;
  }
}