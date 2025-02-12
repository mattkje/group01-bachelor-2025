package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "work_title")
    private String workTitle;

    @Column(name = "effectiveness")
    private double effectiveness;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "worker_license",
            joinColumns = @JoinColumn(name = "worker_id"),
            inverseJoinColumns = @JoinColumn(name = "license_id")
    )
    private Set<License> licenses;

    @ManyToOne
    @JsonIgnore
    private Zone zone;

    public Worker() {
    }

    public Worker(String name, String workTitle, double effectiveness, ArrayList<License> licenses) {
        this.name = name;
        this.workTitle = workTitle;
        this.effectiveness = effectiveness;
        this.licenses = new HashSet<>(licenses);
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

    public void setZone(Zone zone) {
        this.zone = zone;
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

    public Zone getZone() {
        return zone;
    }
}
