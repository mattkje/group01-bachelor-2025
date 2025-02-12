package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "max_duration")
    private int maxDuration;

    @Column(name = "min_duration")
    private int minDuration;

    @Column(name = "min_workers")
    private int minWorkers;

    @Column(name = "max_workers")
    private int maxWorkers;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "task_license",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "license_id")
    )
    private Set<License> requiredLicense;

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
        this.maxDuration = maxTime;
    }

    public void setMinTime(int minTime) {
        this.minDuration = minTime;
    }

    public void setMinWorkers(int minWorkers) {
        this.minWorkers = minWorkers;
    }

    public void setMaxWorkers(int maxWorkers) {
        this.maxWorkers = maxWorkers;
    }

    public void setRequiredLicense(Set<License> requiredLicense) {
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
        return maxDuration;
    }

    public int getMinTime() {
        return minDuration;
    }

    public int getMinWorkers() {
        return minWorkers;
    }

    public int getMaxWorkers() {
        return maxWorkers;
    }

    public Set<License> getRequiredLicense() {
        return requiredLicense;
    }

    public List<ActiveTask> getActiveTasks() {
        return activeTasks;
    }

    public Long getId() {
        return id;
    }
}
