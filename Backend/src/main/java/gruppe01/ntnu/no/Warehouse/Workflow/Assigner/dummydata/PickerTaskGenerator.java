package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.PickerTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smile.regression.RandomForest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * This class generates dummy PickerTask data for testing purposes.
 * It creates a specified number of PickerTasks for a given zone and date range.
 */
@Component
public class PickerTaskGenerator {

    @Autowired
    private PickerTaskService pickerTaskService;

    @Autowired
    private ZoneService zoneService;

    public void generatePickerTasks(LocalDate startDate, int numDays, int numTasksPerDay,
                                    MachineLearningModelPicking machineLearningModelPicking) throws IOException {

        Map<List<Double>, List<List<Double>>> mcValues;
        List<List<Double>> valueList;
        List<Map<List<Double>, List<List<Double>>>> mcValuesList = new ArrayList<>();

        mcValuesList.add(machineLearningModelPicking.getMcValues("dry"));
        mcValuesList.add(machineLearningModelPicking.getMcValues("freeze"));
        mcValuesList.add(machineLearningModelPicking.getMcValues("fruit"));

        Random random = new Random();

        //Creates a list of PickerTasks for the given zone and date range
        for (Zone zone : zoneService.getAllPickerZones()) {

            if (zone.getName().equalsIgnoreCase("freeze")) {
                mcValues = mcValuesList.get(1);
            } else if (zone.getName().equalsIgnoreCase("fruit")) {
                mcValues = mcValuesList.get(2);
            } else {
                mcValues = mcValuesList.getFirst();
            }

            valueList = mcValues.values().iterator().next();

            double sDistance = valueList.get(0).get(0);
            double bDistance = valueList.get(0).get(1);
            double sPackAmount = valueList.get(1).get(0);
            double bPackAmount = valueList.get(1).get(1);
            double sLinesAmount = valueList.get(2).get(0);
            double bLinesAmount = valueList.get(2).get(1);
            double sWeight = valueList.get(3).get(0);
            double bWeight = valueList.get(3).get(1);
            double sVolume = valueList.get(4).get(0);
            double bVolume = valueList.get(4).get(1);
            double sAvgHeight = valueList.get(5).get(0);
            double bAvgHeight = valueList.get(5).get(1);

            for (int i = 0; i < numDays; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                for (int j = 0; j < numTasksPerDay; j++) {
                    PickerTask pickerTask = new PickerTask();
                    pickerTask.setZone(zone);
                    pickerTask.setDate(currentDate);
                    int rand = new Random().nextInt(101);

                    // Generate random values for the PickerTask attributes with +- 5% variation.
                    // The values are generated based on the given zone's average values.
                    double distance = roundToTwoDecimals((sDistance + (rand / 100.0) * (bDistance - sDistance)) * (1 + (random.nextDouble() * 0.1 - 0.05)));
                    int packAmount = (int) ((sPackAmount + (rand / 100.0) * (bPackAmount - sPackAmount)) * (1 + (random.nextDouble() * 0.1 - 0.05)));
                    int linesAmount = (int) ((sLinesAmount + (rand / 100.0) * (bLinesAmount - sLinesAmount)) * (1 + (random.nextDouble() * 0.1 - 0.05)));
                    int weight = (int) ((sWeight + (rand / 100.0) * (bWeight - sWeight)) * (1 + (random.nextDouble() * 0.1 - 0.05)));
                    int volume = (int) ((sVolume + (rand / 100.0) * (bVolume - sVolume)) * (1 + (random.nextDouble() * 0.1 - 0.05)));
                    double avgHeight = roundToTwoDecimals((sAvgHeight + (rand / 100.0) * (bAvgHeight - sAvgHeight)) * (1 + (random.nextDouble() * 0.1 - 0.05)));

                    pickerTask.setDistance(distance);
                    pickerTask.setPackAmount(packAmount);
                    pickerTask.setLinesAmount(linesAmount);
                    pickerTask.setWeight(weight);
                    pickerTask.setVolume(volume);
                    pickerTask.setAvgHeight(avgHeight);

                    pickerTaskService.savePickerTask(pickerTask);
                }
            }
        }

    }
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
