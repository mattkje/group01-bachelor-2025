package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

@Entity
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "zone_id", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long zone;

    @Column(name = "work_title")
    private String workTitle;

    @Column(name = "effectiveness")
    private double effectiveness;

    @Column(name = "availability")
    private boolean availability;

    @ManyToOne
    @JsonIgnore
    private ActiveTask currentTask;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "dead")
    private boolean dead;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "worker_license",
            joinColumns = @JoinColumn(name = "worker_id"),
            inverseJoinColumns = @JoinColumn(name = "license_id")
    )
    private Set<License> licenses;

    public Worker(){

    }

    public Worker(Worker worker) {
        this.id = worker.id;
        this.name = worker.name;
        this.zone = worker.zone;
        this.workTitle = worker.workTitle;
        this.effectiveness = worker.effectiveness;
        this.licenses = new HashSet<>(worker.licenses);
        this.availability = worker.availability;
    }

    public Worker(String name, Long zone, String workTitle, double effectiveness, ArrayList<License> licenses, boolean availability) {
        this.name = name;
        this.zone = zone;
        this.workTitle = workTitle;
        this.effectiveness = effectiveness;
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

    public void setEffectiveness(double effectiveness) {
        this.effectiveness = effectiveness;
    }

    public void setLicenses(Set<License> licenses) {
        this.licenses = licenses;
    }

    public void setCurrentTask(ActiveTask currentTask) {
        this.currentTask = currentTask;
    }

    public void setBreakStartTime(LocalTime breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
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

    public double getEffectiveness() {
        return effectiveness;
    }

    public Set<License> getLicenses() {
        return licenses;
    }

    public ActiveTask getCurrentTask() {
        return currentTask;
    }

    public boolean isDead() {
        return dead;
    }

    public LocalTime getBreakStartTime() {
        return breakStartTime;
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
}
