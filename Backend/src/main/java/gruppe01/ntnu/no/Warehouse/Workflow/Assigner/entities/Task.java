package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import jakarta.persistence.*;

import java.util.Set;

/**
 * Represents a Task in the warehouse system. A Task can represent
 * multiple ActiveTask entities and is associated with a specific Zone.
 * Task objects are reusable in the sense that the same task can have
 * multiple ActiveTask instances associated with it. A Task is
 * associated with a specific Zone and can have multiple licenses
 * required for its execution.
 */
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

    public Task() {
    }

    public Task(String packaging, String packagingItems, int i, int i1, int i2, int i3, Object o) {
        this.name = packaging;
        this.description = packagingItems;
        this.minDuration = i;
        this.maxDuration = i1;
        this.minWorkers = i2;
        this.maxWorkers = i3;
        this.zone = (Zone) o;
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
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

    public Long getId() {
        return id;
    }

    public Long getZoneId() {
        return zone != null ? zone.getId() : null;
    }
}
