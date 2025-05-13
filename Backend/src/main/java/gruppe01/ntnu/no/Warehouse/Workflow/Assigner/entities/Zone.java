package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Zone entity in the system.
 * A zone is a specific area in the warehouse where tasks are assigned.
 * Zones can have multiple tasks and workers associated with them.
 * A zone is either a picking zone or a non-picking zone.
 * A zone cannot have both pickerTasks and Tasks at the same time.
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "zone")
@Schema(description = "Represents a Zone entity in the system.")
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the zone.")
    private Long id;

    @Column(name = "name")
    @Schema(description = "The name of the zone.")
    private String name;

    @Column(name = "capacity")
    @Schema(description = "The capacity of the zone.")
    private int capacity;

    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
    @Schema(description = "The tasks associated with this zone.")
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
    @Schema(description = "The picker tasks associated with this zone.")
    @JsonIgnore
    private Set<PickerTask> pickerTask = new HashSet<>();

    @Column(name = "is_picker_zone")
    @Schema(description = "Indicates whether this zone is a picking zone.")
    private boolean isPickerZone;

    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
    @Schema(description = "The workers associated with this zone.")
    private Set<Worker> workers = new HashSet<>();

    public Zone() {
    }

    public Zone(Zone zone) {
        this.id = zone.getId();
        this.name = zone.getName();
        this.capacity = zone.getCapacity();
        this.tasks = zone.getTasks();
        this.workers = zone.getWorkers();
        this.isPickerZone = zone.getIsPickerZone();
        this.pickerTask = zone.getPickerTask();
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

    public void setPickerTask(Set<PickerTask> pickerTask) {
        this.pickerTask = pickerTask;
    }

    public void setIsPickerZone(boolean isPickerZone) {
        this.isPickerZone = isPickerZone;
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

    public Set<PickerTask> getPickerTask() {
        return pickerTask;
    }

    public boolean getIsPickerZone() {
        return isPickerZone;
    }
}