package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.MonteCarloData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorldSimData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorldSimDataRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorldSimService {

    @Autowired
    private WorldSimDataRepository worldSimDataRepository;

    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private PickerTaskService pickerTaskService;
    @Autowired
    private ActiveTaskService activeTaskService;

    public void generateWorldSimData(LocalDateTime dateTime, boolean realData) {
        for (Zone zone : zoneRepository.findAll()) {
            WorldSimData worldSimData = new WorldSimData();
            worldSimData.setZone(zone);
            worldSimData.setTime(dateTime);
            worldSimData.setRealData(realData);
            if (zone.getIsPickerZone()) {
                worldSimData.setCompletedTasks(pickerTaskService.getPickerTasksDoneForTodayInZone(dateTime.toLocalDate(), zone.getId()));
                worldSimData.setItemsPicked(pickerTaskService.getItemsPickedByZone(dateTime.toLocalDate(), zone.getId()));
            } else {
                worldSimData.setCompletedTasks(activeTaskService.getActiveTasksDoneForTodayInZone(dateTime.toLocalDate(), zone.getId()));
            }
            worldSimDataRepository.save(worldSimData);
        }
    }

    public List<WorldSimData> getMostRecentWorldSimDataByZone(LocalDate date, long zoneId) {
        List<WorldSimData> worldSimDataList = new ArrayList<>();

        for (WorldSimData worldSimData : worldSimDataRepository.findAll()) {
            if (worldSimData.getTime().toLocalDate().isEqual(date) && worldSimData.getZone().getId().equals(zoneId)) {
                worldSimDataList.add(worldSimData);
            }
        }
        return worldSimDataList;
    }
}
