package gruppe01.ntnu.no.Warehouse.Workflow.Assigner;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Task {

    @Id
    private Long id;

    private String name;

    private String description;

    private int maxTime;

    private int minTime;

    private int minWorkers;

    private int maxWorkers;

    @ElementCollection
    private List<String> requiredLicense;

    @OneToMany(mappedBy = "task")
    private List<ActiveTask> activeTasks;

    public Task() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }

    public void setMinWorkers(int minWorkers) {
        this.minWorkers = minWorkers;
    }

    public void setMaxWorkers(int maxWorkers) {
        this.maxWorkers = maxWorkers;
    }

    public void setRequiredLicense(List<String> requiredLicense) {
        this.requiredLicense = requiredLicense;
    }

    public void setActiveTasks(List<ActiveTask> activeTasks) {
        this.activeTasks = activeTasks;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getMinTime() {
        return minTime;
    }

    public int getMinWorkers() {
        return minWorkers;
    }

    public int getMaxWorkers() {
        return maxWorkers;
    }

    public List<String> getRequiredLicense() {
        return requiredLicense;
    }

    public List<ActiveTask> getActiveTasks() {
        return activeTasks;
    }

    public Long getId() {
        return id;
    }
}
