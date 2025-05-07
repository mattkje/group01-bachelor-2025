package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class SimulationResultEntitiy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "latest_end_time")
    private LocalDateTime LatestEndTime;

    @Column(name = "error_message")
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