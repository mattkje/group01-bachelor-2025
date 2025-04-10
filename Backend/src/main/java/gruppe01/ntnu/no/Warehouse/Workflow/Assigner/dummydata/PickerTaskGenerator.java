package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.PickerTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.Random;

@Component
public class PickerTaskGenerator {

    @Autowired
    private PickerTaskService pickerTaskService;

    @Autowired
    private ZoneService zoneService;

    public void generatePickerTasks(LocalDate startDate, int numDays, int numTasksPerDay, Long zoneId) throws FileNotFoundException {
        double bDistance = 0;
        double sDistance = 0;
        int bPackAmount = 0;
        int sPackAmount = 0;
        int bLinesAmount = 0;
        int sLinesAmount = 0;
        int bWeight = 0;
        int sWeight = 0;
        int bVolume = 0;
        int sVolume = 0;
        double bAvgHeight = 0;
        double sAvgHeight = 0;

        Zone zone = zoneService.getZoneById(zoneId);
        if (zone.getName().equalsIgnoreCase("Dry")) {
            bDistance = 1241.5403;
            sDistance = 74.1977;
            bPackAmount = 248;
            sPackAmount = 1;
            bLinesAmount = 150;
            sLinesAmount = 1;
            bWeight = 782253;
            sWeight = 84;
            bVolume = 1690022;
            sVolume = 485;
            bAvgHeight = 1.7078884;
            sAvgHeight = 0.9563604;
        } else if (zone.getName().equalsIgnoreCase("Freeze")) {
            sDistance = 4.478804;
            bDistance = 428.2755;
            sPackAmount = 2;
            bPackAmount = 147;
            sLinesAmount = 3;
            bLinesAmount = 92;
            sWeight = 2083;
            bWeight = 670606;
            sVolume = 895;
            bVolume = 1756839;
            sAvgHeight = 0.8935458;
            bAvgHeight = 1.8443329;
        } else if (zone.getName().equalsIgnoreCase("Fruit")) {
            sDistance = 69.59557;
            bDistance = 604.0576;
            sPackAmount = 1;
            bPackAmount = 105;
            sLinesAmount = 1;
            bLinesAmount = 85;
            sWeight = 2293;
            bWeight = 628776;
            sVolume = 49218;
            bVolume = 1751236;
            sAvgHeight = 0.95309156;
            bAvgHeight = 2.335161;
        } else {
            throw new IllegalArgumentException("Invalid zone name: " + zone.getName());
        }

        Random random = new Random();

        for (int i = 0; i < numDays; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            for (int j = 0; j < numTasksPerDay; j++) {
                PickerTask pickerTask = new PickerTask();
                pickerTask.setZone(zone);
                pickerTask.setDate(currentDate);
                int rand = new Random().nextInt(101);

                double distance = (sDistance + (rand / 100.0) * (bDistance - sDistance)) * (1 + (random.nextDouble() * 0.1 - 0.05));
                int packAmount = (int) ((sPackAmount + (rand / 100.0) * (bPackAmount - sPackAmount)) * (1 + (random.nextDouble() * 0.1 - 0.05)));
                int linesAmount = (int) ((sLinesAmount + (rand / 100.0) * (bLinesAmount - sLinesAmount)) * (1 + (random.nextDouble() * 0.1 - 0.05)));
                int weight = (int) ((sWeight + (rand / 100.0) * (bWeight - sWeight)) * (1 + (random.nextDouble() * 0.1 - 0.05)));
                int volume = (int) ((sVolume + (rand / 100.0) * (bVolume - sVolume)) * (1 + (random.nextDouble() * 0.1 - 0.05)));
                double avgHeight = (sAvgHeight + (rand / 100.0) * (bAvgHeight - sAvgHeight)) * (1 + (random.nextDouble() * 0.1 - 0.05));

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
