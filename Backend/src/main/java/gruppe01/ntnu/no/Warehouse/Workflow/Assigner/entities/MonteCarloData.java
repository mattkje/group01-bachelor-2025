package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "monte_carlo_data")
public class MonteCarloData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sim_no")
    private int simNo;

    @Column(name = "time")
    private LocalDateTime time;

    @Column(name = "completed_tasks")
    private int completedTasks;

    @Column(name = "items_picked")
    private int itemsPicked;

    @Column(name = "zone_id")
    private long zoneId;

    public MonteCarloData() {
    }

    public MonteCarloData(int simNo, LocalDateTime time, int completedTasks, int itemsPicked, long zoneId) {
        this.simNo = simNo;
        this.time = time;
        this.completedTasks = completedTasks;
        this.itemsPicked = itemsPicked;
        this.zoneId = zoneId;
    }

    public void setSimNo(int simNo) {
        this.simNo = simNo;
    }

    public int getSimNo() {
        return simNo;
    }

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

    public long getZoneId() {
        return zoneId;
    }

    public void setZoneId
            (long zoneId) {
        this.zoneId = zoneId;
    }
}
