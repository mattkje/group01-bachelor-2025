package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Timetable {
    @GeneratedValue
    @Id
    private Long id;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "real_start_time")
    private LocalDateTime realStartTime;

    @Column(name = "real_end_time")
    private LocalDateTime realEndTime;

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    public Timetable() {}

    public Timetable( LocalDateTime startTime, LocalDateTime endTime,  Worker worker) {
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
}
