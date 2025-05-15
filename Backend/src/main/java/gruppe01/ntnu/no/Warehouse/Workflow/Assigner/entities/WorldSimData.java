package gruppe01.ntnu.no.warehouse.workflow.assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents a WorldSimData entity in the system.
 * This class is used to store simulation data for a specific zone.
 */
@Entity
@Table(name = "world_sim_data")
@Schema(description = "Represents a WorldSimData entity in the system.")
public class WorldSimData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "Unique identifier for the WorldSimData.")
  private Long id;

  @Column(name = "time")
  @Schema(description = "The time when the simulation data was recorded.")
  private LocalDateTime time;

  @Column(name = "completed_tasks")
  @Schema(description = "The number of tasks completed during the simulation.")
  private int completedTasks;

  @Column(name = "items_picked")
  @Schema(description = "The number of items picked during the simulation.")
  private int itemsPicked;

  @Column(name = "real_data")
  @Schema(description = "Indicates whether the data is real or simulated.")
  private boolean realData;

  @OneToOne
  @JoinColumn(name = "zone_id")
  @JsonIgnore
  @Schema(description = "The zone associated with this simulation data.")
  private Zone zone;

  public WorldSimData() {
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
  public Long getZoneId() {
    return zone != null ? zone.getId() : null;
  }

  public void setZone(Zone zone) {
    this.zone = zone;
  }
}
