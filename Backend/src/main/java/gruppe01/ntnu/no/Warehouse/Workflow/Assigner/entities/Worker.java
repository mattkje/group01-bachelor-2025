package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import jakarta.persistence.*;

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

    @Column(name = "zone_id")
    private int zone;

    @Column(name = "work_title")
    private String workTitle;

    @Column(name = "effectiveness")
    private double effectiveness;

    @Column(name = "availability")
    private boolean availability;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "worker_license",
            joinColumns = @JoinColumn(name = "worker_id"),
            inverseJoinColumns = @JoinColumn(name = "license_id")
    )
    private Set<License> licenses;

    public Worker() {
    }

    public Worker(String name, int zone, String workTitle, double effectiveness, ArrayList<License> licenses, boolean availability) {
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

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }
}
