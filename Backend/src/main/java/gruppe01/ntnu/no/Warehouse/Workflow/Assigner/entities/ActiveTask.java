package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an active task in the warehouse workflow system.
 * This class is used to manage tasks that are currently being processed
 * or have finished. An ActiveTask is connected to a specific Task and can
 * have multiple workers assigned to it.
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ActiveTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "eta")
    private LocalDate eta;

    @Column(name = "strict_start")
    private LocalDateTime strict_start;

    @Column(name="date")
    private LocalDate date;

    // Recurrence type: 0 = no recurrence, 1 = monthly, 2 = weekly,
    // 3 = every other day, 4 = daily
    @Column(name = "recurrence_type")
    private int recurrenceType;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @OneToMany
    @JoinTable(
            name = "active_task_worker",
            joinColumns = @JoinColumn(name = "active_task_id"),
            inverseJoinColumns = @JoinColumn(name = "worker_id")
    )
    private List<Worker> workers;

    public ActiveTask() {
    }

    public ActiveTask(ActiveTask activeTask) {
        this.id = activeTask.getId();
        this.dueDate = activeTask.getDueDate();
        this.startTime = activeTask.getStartTime();
        this.endTime = activeTask.getEndTime();
        this.eta = activeTask.getEta();
        this.strict_start = activeTask.getStrictStart();
        this.date = activeTask.getDate();
        this.task = activeTask.getTask();
        this.workers = new ArrayList<>(activeTask.getWorkers());
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

    public void setStrictStart(LocalDateTime strict_start) {
        this.strict_start = strict_start;
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

    public LocalDateTime getStrictStart() {
        return strict_start;
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
}
