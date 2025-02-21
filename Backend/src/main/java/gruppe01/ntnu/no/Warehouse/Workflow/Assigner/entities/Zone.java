package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "capacity")
    private int capacity;

    @OneToMany(mappedBy = "zone", fetch = FetchType.EAGER)
    private Set<Task> tasks;

    @OneToMany(mappedBy = "zone", fetch = FetchType.EAGER)
    private Set<Worker> workers;

    public Zone() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
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

    public int getCapacity() {
        return capacity;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public Set<Worker> getWorkers() {
        return workers;
    }
}
