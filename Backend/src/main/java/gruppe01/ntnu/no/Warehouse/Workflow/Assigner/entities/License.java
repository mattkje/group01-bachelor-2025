package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;

/**
 * Represents a License entity in the system.
 * Both workers and tasks can have licenses.
 * A license is a requirement for a worker to perform a task.
 */
@Entity
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "licenses", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Worker> workers;

    @ManyToMany(mappedBy = "requiredLicense", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Task> tasks;

    public License() {
    }

    public License(String name) {
        this.name = name;
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

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
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

    public Set<Task> getTasks() {
        return tasks;
    }
}
