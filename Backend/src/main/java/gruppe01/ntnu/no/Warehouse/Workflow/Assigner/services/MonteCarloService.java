package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.MonteCarloData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.MonteCarloDataRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MonteCarloService {

    @Autowired
    private MonteCarloDataRepository monteCarloDataRepository;

    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private PickerTaskService pickerTaskService;
    @Autowired
    private ActiveTaskService activeTaskService;

    public void generateMonteCarloData(LocalDateTime dateTime, boolean realData) {
        for (Zone zone : zoneRepository.findAll()) {
            MonteCarloData monteCarloData = new MonteCarloData();
            monteCarloData.setZone(zone);
            monteCarloData.setTime(dateTime);
            monteCarloData.setRealData(realData);
            if (zone.getIsPickerZone()) {
                monteCarloData.setCompletedTasks(pickerTaskService.getPickerTasksDoneForTodayInZone(dateTime.toLocalDate(), zone.getId()));
                monteCarloData.setItemsPicked(pickerTaskService.getItemsPickedByZone(dateTime.toLocalDate(), zone.getId()));
            } else {
                monteCarloData.setCompletedTasks(activeTaskService.getActiveTasksDoneForTodayInZone(dateTime.toLocalDate(), zone.getId()));
            }
            monteCarloDataRepository.save(monteCarloData);
        }
    }
}
