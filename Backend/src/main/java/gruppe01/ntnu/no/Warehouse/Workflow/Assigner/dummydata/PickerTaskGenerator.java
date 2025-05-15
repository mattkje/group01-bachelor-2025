package gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.PickerTaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This class generates fake PickerTask data for testing purposes.
 * It creates a specified number of PickerTasks for a given zone and date range.
 */
@Component
public class PickerTaskGenerator {

    private final PickerTaskService pickerTaskService;
    private final ZoneService zoneService;

    /**
     * Constructor for PickerTaskGenerator.
     *
     * @param pickerTaskService The service to handle PickerTask operations.
     * @param zoneService       The service to handle Zone operations.
     */
    public PickerTaskGenerator(PickerTaskService pickerTaskService, ZoneService zoneService) {
        this.pickerTaskService = pickerTaskService;
        this.zoneService = zoneService;
    }

    /**
     * Generates PickerTasks for the given date range and zones.
     *
     * @param startDate                   The start date for generating tasks.
     * @param numDays                     The number of days to generate tasks for.
     * @param numTasksPerDay              The number of tasks to generate per day.
     * @param machineLearningModelPicking The machine learning model for picking data.
     * @throws IOException If an error occurs while fetching machine learning data.
     */
    public List<PickerTask> generatePickerTasks(LocalDate startDate, int numDays, int numTasksPerDay,
                                    MachineLearningModelPicking machineLearningModelPicking, boolean testData) throws IOException {

        List<Map<List<Double>, List<List<Double>>>> mcValuesList = List.of(
                machineLearningModelPicking.getMcValues("dry"),
                machineLearningModelPicking.getMcValues("freeze"),
                machineLearningModelPicking.getMcValues("fruit")
        );

        Random random = new Random();
        List<PickerTask> allPickerTasks = new ArrayList<>();
        int[] dueHours = {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
        int[] dueMinutes = {0, 15, 30, 45};

        for (Zone zone : zoneService.getAllPickerZones()) {
            Map<List<Double>, List<List<Double>>> mcValues = getMcValuesForZone(zone, mcValuesList);
            List<List<Double>> valueList = mcValues.values().iterator().next();
            List<PickerTask> pickerTasks = new ArrayList<>();

            for (int i = 0; i < numDays; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                for (int j = 0; j < numTasksPerDay; j++) {
                    PickerTask pickerTask = createPickerTask(zone, currentDate, valueList, random, testData, dueHours, dueMinutes);
                    pickerTasks.add(pickerTask);
                    if (!testData) {
                        pickerTaskService.savePickerTask(pickerTask);
                    } else {
                        allPickerTasks.add(pickerTask);
                    }
                }
            }
        }
        return allPickerTasks;
    }

    private Map<List<Double>, List<List<Double>>> getMcValuesForZone(Zone zone, List<Map<List<Double>, List<List<Double>>>> mcValuesList) {
        return switch (zone.getName().toLowerCase()) {
            case "freeze" -> mcValuesList.get(1);
            case "fruit" -> mcValuesList.get(2);
            default -> mcValuesList.get(0);
        };
    }

    private PickerTask createPickerTask(Zone zone, LocalDate date, List<List<Double>> valueList, Random random,
                                        boolean testData, int[] dueHours, int[] dueMinutes) {
        PickerTask pickerTask = new PickerTask();
        pickerTask.setZone(zone);
        pickerTask.setDate(date);

        pickerTask.setDistance(generateRandomValue(valueList.get(0), random));
        pickerTask.setPackAmount((int) generateRandomValue(valueList.get(1), random));
        pickerTask.setLinesAmount((int) generateRandomValue(valueList.get(2), random));
        pickerTask.setWeight((int) generateRandomValue(valueList.get(3), random));
        pickerTask.setVolume((int) generateRandomValue(valueList.get(4), random));
        pickerTask.setAvgHeight(generateRandomValue(valueList.get(5), random));
        pickerTask.setDueDate(generateDueDate(date, dueHours, dueMinutes, random));
        if (testData) {
            Worker worker = new Worker();
            worker.setId(random.nextLong(1, 51));
            pickerTask.setWorker(worker);
        }

        return pickerTask;
    }

    private double generateRandomValue(List<Double> range, Random random) {
        double min = range.get(0);
        double max = range.get(1);
        double variation = 1 + (random.nextDouble() * 0.1 - 0.05); // ±5% variation
        return roundToTwoDecimals((min + random.nextDouble() * (max - min)) * variation);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private LocalDateTime generateDueDate(LocalDate date, int[] dueHours, int[] dueMinutes, Random random) {
        int hour = dueHours[random.nextInt(dueHours.length)];
        int minute = dueMinutes[random.nextInt(dueMinutes.length)];
        return date.atTime(hour, minute);
    }
}