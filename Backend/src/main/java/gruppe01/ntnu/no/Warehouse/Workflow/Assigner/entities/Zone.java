package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a Zone entity in the system.
 * A zone is a specific area in the warehouse where tasks are assigned.
 * Zones can have multiple tasks and workers associated with them.
 * A zone is either a picking zone or a non-picking zone.
 * A zone can not have both pickerTasks and Tasks at the same time.
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
public class Zone {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "capacity")
  private int capacity;

  @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
  private Set<Task> tasks;

  @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
  private Set<PickerTask> pickerTask;

  @Column(name = "is_picker_zone")
  private boolean isPickerZone;

  @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
  private Set<Worker> workers;

  public Zone() {
  }

  public Zone(Zone zone) {
    this.id = zone.getId();
    this.name = zone.getName();
    this.capacity = zone.getCapacity();
    this.tasks = zone.getTasks();
    this.workers = zone.getWorkers();
    this.isPickerZone = zone.getIsPickerZone();
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  public void setTasks(Set<Task> tasks) {
    this.tasks = tasks;
  }

  public void setWorkers(Set<Worker> workers) {
    this.workers = workers;
  }

  public void setPickerTask(Set<PickerTask> pickerTask) {
    this.pickerTask = pickerTask;
  }

  public void setIsPickerZone(boolean isPickerZone) {
    this.isPickerZone = isPickerZone;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getCapacity() {
    return capacity;
  }

  public Set<Task> getTasks() {
    return tasks;
  }

  public Set<Worker> getWorkers() {
    return workers;
  }

  public Set<PickerTask> getPickerTask() {
    return pickerTask;
  }

  public boolean getIsPickerZone() {
    return isPickerZone;
  }
}
