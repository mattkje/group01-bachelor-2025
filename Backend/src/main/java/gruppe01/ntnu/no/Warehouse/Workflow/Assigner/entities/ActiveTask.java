package gruppe01.ntnu.no.warehouse.workflow.assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an active task in the warehouse workflow system.
 * This class is used to manage tasks that are currently being processed
 * or have finished. An ActiveTask is connected to a specific Task and can
 * have multiple workers assigned to it.
 */
@Schema(description = "Represents an active task in the warehouse workflow system.")
@Entity
@Table(name = "active_task")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ActiveTask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "Unique identifier for the active task.")
  private Long id;

  @Column(name = "due_date")
  @Schema(description = "The due date for the active task.")
  private LocalDateTime dueDate;

  @Column(name = "start_time")
  @Schema(description = "The start time of the active task.")
  private LocalDateTime startTime;

  @Column(name = "end_time")
  @Schema(description = "The end time of the active task.")
  private LocalDateTime endTime;

  @Column(name = "mc_start_time")
  @Schema(description = "The start time of the active task in the Monte Carlo simulation.")
  private LocalDateTime mcStartTime;

  @Column(name = "mc_end_time")
  @Schema(description = "The end time of the active task in the Monte Carlo simulation.")
  private LocalDateTime mcEndTime;

  @Column(name = "eta")
  @Schema(description = "The estimated time of arrival for the active task.")
  private LocalDate eta;

  @Column(name = "date")
  @Schema(description = "The date when the active task was created.")
  private LocalDate date;

  // Recurrence type: 0 = no recurrence, 1 = monthly, 2 = weekly,
  // 3 = every other day, 4 = daily
  @Column(name = "recurrence_type")
  @Schema(description = "The recurrence type of the active task. 1 = monthly, 2 = weekly, 3 = every other day, 4 = daily.")
  private int recurrenceType;

  @ManyToOne
  @JoinColumn(name = "task_id")
  @Schema(description = "The task associated with this active task.")
  private Task task;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "active_task_worker",
      joinColumns = @JoinColumn(name = "active_task_id"),
      inverseJoinColumns = @JoinColumn(name = "worker_id")
  )
  @Schema(description = "The list of workers assigned to this active task.")
  private List<Worker> workers;

  public ActiveTask() {
  }

  public ActiveTask(ActiveTask activeTask) {
    this.id = activeTask.getId();
    this.dueDate = activeTask.getDueDate();
    this.startTime = activeTask.getStartTime();
    this.endTime = activeTask.getEndTime();
    this.eta = activeTask.getEta();
    this.date = activeTask.getDate();
    this.task = activeTask.getTask();
    this.workers = new ArrayList<>(activeTask.getWorkers());
    this.recurrenceType = activeTask.getRecurrenceType();
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public void setWorkers(List<Worker> workers) {
    this.workers = workers;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = dueDate;
  }

  public void setEta(LocalDate eta) {
    this.eta = eta;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public void setRecurrenceType(int recurrenceType) {
    this.recurrenceType = recurrenceType;
  }

  public LocalDateTime getDueDate() {
    return dueDate;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public List<Worker> getWorkers() {
    List<Worker> workerList = new ArrayList<>();
    if (this.workers == null) {
      this.workers = new ArrayList<>();
    }
    for (Worker worker : workers) {
      if (!workerList.contains(worker)) {
        workerList.add(worker);
      }
    }
    return workerList;
  }

  public Task getTask() {
    return task;
  }

  public Long getId() {
    return id;
  }

  public LocalDate getEta() {
    return eta;
  }

  public LocalDate getDate() {
    return date;
  }

  public int getRecurrenceType() {
    return recurrenceType;
  }

  public void addWorker(Worker worker) {
    if (this.workers == null) {
      this.workers = new ArrayList<>();
    }
    if (!this.workers.contains(worker)) {
      this.workers.add(worker);
    }
  }

  public void addMultilpleWorkers(List<Worker> workers) {
    if (this.workers == null) {
      this.workers = new ArrayList<>();
    }
    for (Worker worker : workers) {
      if (!this.workers.contains(worker)) {
        this.workers.add(worker);
      }
    }
  }

  public LocalDateTime getMcStartTime() {
    return mcStartTime;
  }

  public void setMcStartTime(LocalDateTime mcStartTime) {
    this.mcStartTime = mcStartTime;
  }

  public LocalDateTime getMcEndTime() {
    return mcEndTime;
  }

  public void setMcEndTime(LocalDateTime mcEndTime) {
    this.mcEndTime = mcEndTime;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ActiveTask that = (ActiveTask) obj;
    return Objects.equals(this.id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
