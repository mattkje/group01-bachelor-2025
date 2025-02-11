package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class ActiveTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "due_date")
    private Date dueDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;

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

    public void setId(Long id) {
        this.id = id;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public List<Worker> getWorkers() {
        return workers;
    }

    public Task getTask() {
        return task;
    }

    public Long getId() {
        return id;
    }
}
