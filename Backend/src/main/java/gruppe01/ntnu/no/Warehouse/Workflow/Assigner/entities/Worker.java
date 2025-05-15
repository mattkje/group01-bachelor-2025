package gruppe01.ntnu.no.warehouse.workflow.assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.*;

/**
 * Represents a Worker entity in the warehouse system.
 * Each worker gets assigned to a specific zone where they
 * will perform several tasks. A worker's efficiency is what
 * determines how fast they can complete a task. The worker's
 * current task is also tracked, and they are also assigned
 * a set of licenses that determine what tasks they can perform.
 */
@Entity
@Table(name = "worker")
@Schema(description = "Represents a Worker entity in the warehouse system.")
public class Worker {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "Unique identifier for the worker.")
  private Long id;

  @Column(name = "name")
  @Schema(description = "The name of the worker.")
  private String name;

  @Column(name = "zone_id", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
  @Schema(description = "The zone where the worker is assigned.")
  private Long zone;

  @Column(name = "work_title")
  @Schema(description = "The work title of the worker.")
  private String workTitle;

  @Column(name = "efficiency")
  @Schema(description = "The efficiency of the worker.")
  private double efficiency;

  @Column(name = "availability")
  @Schema(description = "The availability status of the worker.")
  private boolean availability;

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "current_task_id")
  private ActiveTask currentActiveTask;

  @OneToOne
  @JsonIgnore
  @JoinColumn(name = "current_picker_task_id")
  private PickerTask currentPickerTask;

  @Column(name = "dead")
  @Schema(description = "Indicates if the worker is dead.")
  private boolean dead;

  @ElementCollection
  @CollectionTable(name = "worker_schedule", joinColumns = @JoinColumn(name = "worker_id"))
  @MapKeyColumn(name = "day_of_week")
  @MapKeyEnumerated(EnumType.STRING)
  @Column(name = "work_schedule")
  @JsonIgnore
  @Schema(description = "The work schedule of the worker.")
  private Map<DayOfWeek, WorkerTimeRange> workSchedule = new HashMap<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "worker_license",
      joinColumns = @JoinColumn(name = "worker_id"),
      inverseJoinColumns = @JoinColumn(name = "license_id")
  )
  @Schema(description = "The licenses held by the worker.")
  private Set<License> licenses = new HashSet<>();

  public Worker() {

  }

  public Worker(Worker worker) {
    this.id = worker.id;
    this.name = worker.name;
    this.zone = worker.zone;
    this.workTitle = worker.workTitle;
    this.efficiency = worker.efficiency;
    this.licenses = worker.licenses;
    this.availability = worker.availability;
    this.currentActiveTask = worker.currentActiveTask;
    this.currentPickerTask = worker.currentPickerTask;
    this.dead = worker.dead;
    this.workSchedule = worker.workSchedule;
  }

  public Worker(String name, Long zone, String workTitle, double efficiency,
                ArrayList<License> licenses, boolean availability) {
    this.name = name;
    this.zone = zone;
    this.workTitle = workTitle;
    this.efficiency = efficiency;
    this.licenses = new HashSet<>(licenses);
    this.availability = availability;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setWorkerType(String workerType) {
    this.workTitle = workerType;
  }

  public void setEfficiency(double efficiency) {
    this.efficiency = efficiency;
  }

  public void setLicenses(Set<License> licenses) {
    this.licenses = licenses;
  }

  public void setCurrentTask(ActiveTask currentTask) {
    this.currentActiveTask = currentTask;
  }

  public void setDead(boolean dead) {
    this.dead = dead;
  }

  public void setCurrentPickerTask(PickerTask currentPickerTask) {
    this.currentPickerTask = currentPickerTask;
  }

  public void setWorkSchedule(Map<DayOfWeek, WorkerTimeRange> workSchedule) {
    this.workSchedule = workSchedule;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getWorkerType() {
    return workTitle;
  }

  public double getEfficiency() {
    return efficiency;
  }

  public Set<License> getLicenses() {
    return licenses;
  }

  @JsonProperty("currentTaskId")
  public Long getCurrentTaskId() {
    if (currentActiveTask != null) {
      return currentActiveTask.getId();
    } else if (currentPickerTask != null) {
      return currentPickerTask.getId();
    } else {
      return null;
    }
  }

  public ActiveTask getCurrentActiveTask() {
    return currentActiveTask;
  }

  public PickerTask getCurrentPickerTask() {
    return currentPickerTask;
  }

  public boolean isDead() {
    return dead;
  }

  public Long getZone() {
    return zone;
  }

  public void setZone(Long zone) {
    this.zone = zone;
  }

  public boolean isAvailability() {
    return availability;
  }

  public void setAvailability(boolean availability) {
    this.availability = availability;
  }

  public boolean hasLicense(String requiredLicense) {
    for (License license : licenses) {
      if (license.getName().equals(requiredLicense)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasAllLicenses(Set<License> requiredLicenses) {
    return licenses.containsAll(requiredLicenses);
  }

  public Map<DayOfWeek, WorkerTimeRange> getWorkSchedule() {
    return workSchedule;
  }
}
