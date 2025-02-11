package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import jakarta.persistence.*;

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

    @ElementCollection
    private List<String> licenses;

    public Worker() {
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

    public void setLicenses(List<String> licenses) {
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

    public List<String> getLicenses() {
        return licenses;
    }
}
