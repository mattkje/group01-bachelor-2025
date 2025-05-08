package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Represents a timetable entity in the system.
 * A timetable is used to schedule the working hours of a worker.
 */
@Entity
@Table(name = "timetable")
@Schema(description = "Represents a timetable entity in the system.")
public class Timetable {
    @GeneratedValue
    @Id
    @Schema(description = "Unique identifier for the timetable.")
    private Long id;

    @Column(name = "start_time")
    @Schema(description = "The start time of the timetable.")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @Schema(description = "The end time of the timetable.")
    private LocalDateTime endTime;

    @Column(name = "real_start_time")
    @Schema(description = "The actual start time of the timetable.")
    private LocalDateTime realStartTime;

    @Column(name = "real_end_time")
    @Schema(description = "The actual end time of the timetable.")
    private LocalDateTime realEndTime;

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false)
    @JsonIgnore
    @Schema(description = "The worker associated with this timetable.")
    private Worker worker;

    public Timetable() {
    }

    public Timetable(LocalDateTime startTime, LocalDateTime endTime, Worker worker) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.worker = worker;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setRealStartTime(LocalDateTime realStartTime) {
        this.realStartTime = realStartTime;
    }

    public void setRealEndTime(LocalDateTime realEndTime) {
        this.realEndTime = realEndTime;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public LocalDateTime getRealStartTime() {
        return realStartTime;
    }

    public LocalDateTime getRealEndTime() {
        return realEndTime;
    }

    public Worker getWorker() {
        return worker;
    }

    @JsonProperty("workerId")
    public Long getWorkerId() {
        return worker != null ? worker.getId() : null;
    }
}
