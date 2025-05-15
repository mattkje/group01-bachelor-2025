package gruppe01.ntnu.no.warehouse.workflow.assigner.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Schema(description = "Represents the result of a simulation.")
public class SimulationResultEntitiy {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "Unique identifier for the simulation result.")
  private Long id;

  @Column(name = "zone_id")
  @Schema(description = "The ID of the zone where the simulation took place.")
  private long zoneId;

  @Column(name = "latest_end_time")
  @Schema(description = "The latest end time of the simulation.")
  private LocalDateTime LatestEndTime;

  @Column(name = "error_message")
  @Schema(description = "Error message if the simulation failed.")
  private String errorMessage;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public long getZoneId() {
    return zoneId;
  }

  public void setZoneId(long zoneId) {
    this.zoneId = zoneId;
  }

  public LocalDateTime getLatestEndTime() {
    return LatestEndTime;
  }

  public void setLatestEndTime(LocalDateTime latestEndTime) {
    this.LatestEndTime = latestEndTime;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}