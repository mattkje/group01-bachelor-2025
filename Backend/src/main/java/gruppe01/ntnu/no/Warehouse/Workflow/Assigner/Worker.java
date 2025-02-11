package gruppe01.ntnu.no.Warehouse.Workflow.Assigner;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class Worker {

    @Id
    private Long id;

    private String name;

    private String workerType;

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
        this.workerType = workerType;
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
        return workerType;
    }

    public double getEffectiveness() {
        return effectiveness;
    }

    public List<String> getLicenses() {
        return licenses;
    }
}
