package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.Date;
import java.util.List;

@Entity
public class ActiveTask {

    @Id
    private Long id;

    private Date dueDate;

    private Date startTime;

    private Date endTime;

    @ManyToOne
    private Task task;

    @OneToMany
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
