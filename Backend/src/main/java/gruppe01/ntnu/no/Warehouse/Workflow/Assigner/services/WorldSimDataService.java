package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorldSimData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorldSimDataRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorldSimDataService {

    @Autowired
    private WorldSimDataRepository worldSimDataRepository;

    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private PickerTaskService pickerTaskService;
    @Autowired
    private ActiveTaskService activeTaskService;

    public void generateWorldSimData(LocalDateTime dateTime, boolean realData) {
        int tasksCompleted = 0;
        int itemsPicked = 0;
        for (Zone zone : zoneRepository.findAll()) {
            WorldSimData worldSimData = new WorldSimData();
            int completedTasks = 0;
            if (zone.getIsPickerZone()) {
                completedTasks = pickerTaskService.getPickerTasksDoneForTodayInZone(dateTime.toLocalDate(), zone.getId());
                worldSimData.setCompletedTasks(completedTasks);
                worldSimData.setItemsPicked(pickerTaskService.getItemsPickedByZone(dateTime.toLocalDate(), zone.getId()));
                itemsPicked += worldSimData.getItemsPicked();
            } else {
                completedTasks = activeTaskService.getActiveTasksDoneForTodayInZone(dateTime.toLocalDate(), zone.getId());
                worldSimData.setCompletedTasks(completedTasks);
            }
            tasksCompleted += completedTasks;
            worldSimData.setZone(zone);
            worldSimData.setTime(dateTime);
            worldSimData.setRealData(realData);

            worldSimDataRepository.save(worldSimData);
        }
        WorldSimData worldSimData = new WorldSimData();
        worldSimData.setCompletedTasks(tasksCompleted);
        worldSimData.setItemsPicked(itemsPicked);
        worldSimData.setTime(dateTime);
        worldSimData.setZone(null);
        worldSimDataRepository.save(worldSimData);
    }

    public List<WorldSimData> getMostRecentWorldSimDataByZone(long zoneId) {
        List<WorldSimData> worldSimDataList = new ArrayList<>();

        for (WorldSimData worldSimData : worldSimDataRepository.findAll()) {
            if (worldSimData.getZone().getId().equals(zoneId)) {
                worldSimDataList.add(worldSimData);
            }
        }
        return worldSimDataList;
    }

    public List<Integer> getWorldSimValues(long zoneId) {
        List<Integer> worldSimValues = new ArrayList<>();

        for (WorldSimData worldSimData : worldSimDataRepository.findAll()) {
            if (worldSimData.getZone() != null && worldSimData.getZone().getId().equals(zoneId)) {
                worldSimValues.add(worldSimData.getCompletedTasks());
            } else if (worldSimData.getZone() == null && zoneId == 0) {
                worldSimValues.add(worldSimData.getCompletedTasks());
            }
        }
        return worldSimValues;
    }

    public void deleteAllData() {
        worldSimDataRepository.deleteAll();
    }
}
