package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class PickerTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distance")
    private double distance;

    @Column(name = "pack_amount")
    private int packAmount;

    @Column(name = "lines_amount")
    private int linesAmount;

    @Column(name = "weight_g")
    private int weight;

    @Column(name = "volume_ml")
    private int volume;

    @Column(name = "avg_height_m")
    private double avgHeight;

    @Column(name = "time_s")
    private double time;

    @ManyToOne
    @JoinColumn(name = "zoneId")
    @JsonIgnore
    private Zone zone;

    @OneToOne
    @JoinColumn(name = "workerId")
    private Worker worker;

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



}
