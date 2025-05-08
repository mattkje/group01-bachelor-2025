package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "monte_carlo_data")
@Schema(description = "Represents data from Monte Carlo simulations.")
public class MonteCarloData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the Monte Carlo data.")
    private Long id;

    @Column(name = "sim_no")
    @Schema(description = "The simulation number.")
    private int simNo;

    @Column(name = "time")
    @Schema(description = "The time when the simulation data was recorded.")
    private LocalDateTime time;

    @Column(name = "completed_tasks")
    @Schema(description = "The number of tasks completed during the simulation.")
    private int completedTasks;

    @Column(name = "items_picked")
    @Schema(description = "The number of items picked during the simulation.")
    private int itemsPicked;

    @Column(name = "zone_id")
    @Schema(description = "The ID of the zone where the simulation took place.")
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
