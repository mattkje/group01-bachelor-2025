package gruppe01.ntnu.no.warehouse.workflow.assigner.entities;

import jakarta.persistence.Embeddable;

import java.time.LocalTime;

/**
 * Represents a time range for a worker's working hours.
 * This class is used to define the start and end times of a worker's shift.
 */
@Embeddable
public class WorkerTimeRange {
  private LocalTime startTime;
  private LocalTime endTime;

  public WorkerTimeRange() {
  }

  public WorkerTimeRange(LocalTime startTime, LocalTime endTime) {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }
}