package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;

@Entity
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "licenses")
    @JsonIgnore
    private Set<Worker> workers;

    public License() {
    }

    public License(String forklift) {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWorkers(Set<Worker> workers) {
        this.workers = workers;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Worker> getWorkers() {
        return workers;
    }
}
