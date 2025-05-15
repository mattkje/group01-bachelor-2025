package gruppe01.ntnu.no.warehouse.workflow.assigner.services;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.WorldSimData;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.WorldSimDataRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.ZoneRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing WorldSimData entities.
 */
@Service
public class WorldSimDataService {

  private final WorldSimDataRepository worldSimDataRepository;

  private final ZoneRepository zoneRepository;

  private final PickerTaskService pickerTaskService;

  private final ActiveTaskService activeTaskService;

  /**
   * Constructor for WorldSimDataService.
   *
   * @param worldSimDataRepository the repository for WorldSimData entity
   * @param zoneRepository         the repository for Zone entity
   * @param pickerTaskService      the service for PickerTask entity
   * @param activeTaskService      the service for ActiveTask entity
   */
  public WorldSimDataService(WorldSimDataRepository worldSimDataRepository,
                             ZoneRepository zoneRepository, PickerTaskService pickerTaskService,
                             ActiveTaskService activeTaskService) {
    this.worldSimDataRepository = worldSimDataRepository;
    this.zoneRepository = zoneRepository;
    this.pickerTaskService = pickerTaskService;
    this.activeTaskService = activeTaskService;
  }

  /**
   * Generates world simulation data for all zones.
   *
   * @param dateTime the date and time for the simulation data
   * @param realData whether the data is real or simulated
   */
  public void generateWorldSimData(LocalDateTime dateTime, boolean realData) {
    int tasksCompleted = 0;
    int itemsPicked = 0;
    for (Zone zone : zoneRepository.findAll()) {
      WorldSimData worldSimData = new WorldSimData();
      int completedTasks;
      if (zone.getIsPickerZone()) {
        completedTasks = pickerTaskService.getPickerTasksDoneForTodayInZone(dateTime.toLocalDate(),
            zone.getId());
        worldSimData.setCompletedTasks(completedTasks);
        worldSimData.setItemsPicked(
            pickerTaskService.getItemsPickedByZone(dateTime.toLocalDate(), zone.getId()));
        itemsPicked += worldSimData.getItemsPicked();
      } else {
        completedTasks = activeTaskService.getActiveTasksDoneForTodayInZone(dateTime.toLocalDate(),
            zone.getId());
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

  /**
   * Gets the most recent world simulation data for a specific zone.
   *
   * @param zoneId the ID of the zone
   * @return a list of WorldSimData entities for the specified zone
   */
  public List<WorldSimData> getMostRecentWorldSimDataByZone(long zoneId) {
    List<WorldSimData> worldSimDataList = new ArrayList<>();

    for (WorldSimData worldSimData : worldSimDataRepository.findAll()) {
      if (worldSimData.getZone().getId().equals(zoneId)) {
        worldSimDataList.add(worldSimData);
      }
    }
    return worldSimDataList;
  }

  /**
   * Gets the world simulation values for a specific zone.
   *
   * @param zoneId the ID of the zone
   * @return a list of integers representing the completed tasks for the specified zone
   */
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

  /**
   * Deletes all world simulation data from the repository.
   */
  public void deleteAllData() {
    worldSimDataRepository.deleteAll();
  }
}
