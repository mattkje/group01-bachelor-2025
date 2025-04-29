package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class WorldSimData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time")
    private LocalDateTime time;

    @Column(name = "completed_tasks")
    private int completedTasks;

    @Column(name = "items_picked")
    private int itemsPicked;

    @Column(name = "real_data")
    private boolean realData;

    @OneToOne
    @JoinColumn(name = "zone_id")
    @JsonIgnore
    private Zone zone;

    public WorldSimData() {}

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getItemsPicked() {
        return itemsPicked;
    }

    public void setItemsPicked(int itemsPicked) {
        this.itemsPicked = itemsPicked;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public boolean isRealData() {
        return realData;
    }

    public void setRealData(boolean realData) {
        this.realData = realData;
    }

    public Zone getZone() {
        return zone;
    }

    @JsonProperty
    public long getZoneId() {
        return zone != null ? zone.getId() : null;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }
}
