package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a PickerTask entity in the warehouse system.
 * This class is different from an ActiveTask in that it is more detailed
 * and meant for picker tasks specifically.
 * A PickerTask is associated with a specific zone and can be assigned to a single worker.
 */
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "picker_task")
@Schema(description = "Represents a PickerTask entity in the warehouse system.")
public class PickerTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the PickerTask.")
    private Long id;

    @Column(name = "distance")
    @Schema(description = "The distance to be covered for this task.")
    private double distance;

    @Column(name = "pack_amount")
    @Schema(description = "The number of packs to be picked.")
    private int packAmount;

    @Column(name = "lines_amount")
    @Schema(description = "The number of lines to be picked.")
    private int linesAmount;

    @Column(name = "weight_g")
    @Schema(description = "The weight of the items to be picked in grams.")
    private int weight;

    @Column(name = "volume_ml")
    @Schema(description = "The volume of the items to be picked in milliliters.")
    private int volume;

    @Column(name = "avg_height_m")
    @Schema(description = "The average height of the items to be picked in meters.")
    private double avgHeight;

    @Column(name = "time_s")
    @Schema(description = "The time required to complete the task in seconds.")
    private double time;

    @Column(name = "start_time")
    @Schema(description = "The start time of the task.")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @Schema(description = "The end time of the task.")
    private LocalDateTime endTime;

    @Column(name = "mc_start_time")
    @Schema(description = "The start time of the task in the Monte Carlo simulation.")
    private LocalDateTime mcStartTime;

    @Column(name = "mc_end_time")
    @Schema(description = "The end time of the task in the Monte Carlo simulation.")
    private LocalDateTime mcEndTime;

    @ManyToOne
    @JoinColumn(name = "zone_Id")
    @JsonIgnore
    @Schema(description = "The zone where the task is to be performed.")
    private Zone zone;

    @OneToOne
    @JoinColumn(name = "worker_Id")
    @Schema(description = "The worker assigned to this task.")
    private Worker worker;

    @Column(name = "date")
    @Schema(description = "The date when the task is to be performed.")
    private LocalDate date;

    @Column(name = "due_date")
    @Schema(description = "The due date for the task.")
    private LocalDateTime dueDate;

    public PickerTask() {
    }

    public PickerTask(double distance, int packAmount, int linesAmount, int weight, int volume, double avgHeight, double time, Zone zone) {
        this.distance = distance;
        this.packAmount = packAmount;
        this.linesAmount = linesAmount;
        this.weight = weight;
        this.volume = volume;
        this.avgHeight = avgHeight;
        this.time = time;
        this.zone = zone;
    }

    public PickerTask(PickerTask pickerTask) {
        this.id = pickerTask.getId();
        this.distance = pickerTask.getDistance();
        this.packAmount = pickerTask.getPackAmount();
        this.linesAmount = pickerTask.getLinesAmount();
        this.weight = pickerTask.getWeight();
        this.volume = pickerTask.getVolume();
        this.avgHeight = pickerTask.getAvgHeight();
        this.time = pickerTask.getTime();
        this.zone = new Zone(pickerTask.getZone());
        this.worker = pickerTask.getWorker();
        this.date = pickerTask.getDate();
        this.startTime = pickerTask.getStartTime();
        this.endTime = pickerTask.getEndTime();
    }

    public Worker getWorker() {
        return worker;
    }

    public Long getId() {
        return id;
    }

    public double getDistance() {
        return distance;
    }

    public int getPackAmount() {
        return packAmount;
    }

    public int getLinesAmount() {
        return linesAmount;
    }

    public int getWeight() {
        return weight;
    }

    public int getVolume() {
        return volume;
    }

    public double getAvgHeight() {
        return avgHeight;
    }

    public double getTime() {
        return time;
    }

    public Zone getZone() {
        return zone;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    @JsonProperty("zoneId")
    public Long getZoneId() {
        return zone != null ? zone.getId() : null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setPackAmount(int packAmount) {
        this.packAmount = packAmount;
    }

    public void setLinesAmount(int linesAmount) {
        this.linesAmount = linesAmount;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setAvgHeight(double avgHeight) {
        this.avgHeight = avgHeight;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getMcStartTime() {
        return mcStartTime;
    }

    public void setMcStartTime(LocalDateTime mcStartTime) {
        this.mcStartTime = mcStartTime;
    }

    public LocalDateTime getMcEndTime() {
        return mcEndTime;
    }

    public void setMcEndTime(LocalDateTime mcEndTime) {
        this.mcEndTime = mcEndTime;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PickerTask that = (PickerTask) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
