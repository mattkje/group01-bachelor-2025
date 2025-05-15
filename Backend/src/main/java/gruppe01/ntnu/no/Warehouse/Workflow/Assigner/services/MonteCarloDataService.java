package gruppe01.ntnu.no.warehouse.workflow.assigner.services;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.MonteCarloData;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.MonteCarloDataRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling Monte Carlo data operations.
 */
@Service
public class MonteCarloDataService {

  private final MonteCarloDataRepository monteCarloDataRepository;

  private final ZoneService zoneService;

  private final WorldSimDataService worldSimDataService;

  /**
   * Constructor for MonteCarloDataService.
   *
   * @param monteCarloDataRepository the repository for MonteCarloData entity
   * @param zoneService              the service for Zone entity
   * @param worldSimDataService      the service for WorldSimData entity
   */
  public MonteCarloDataService(MonteCarloDataRepository monteCarloDataRepository,
                               ZoneService zoneService,
                               WorldSimDataService worldSimDataService) {
    this.monteCarloDataRepository = monteCarloDataRepository;
    this.zoneService = zoneService;
    this.worldSimDataService = worldSimDataService;
  }

  /**
   * Retrieves Monte Carlo data values for a given zone ID.
   *
   * @param zoneId the ID of the zone
   * @return a list of lists containing Monte Carlo data values
   */
  public List<List<Integer>> getMCDataValues(long zoneId) {
    Map<Integer, List<Integer>> groupedBySimCount = new HashMap<>();

    List<Integer> worldSimValues = worldSimDataService.getWorldSimValues(zoneId);
    if (worldSimValues == null || worldSimValues.isEmpty()) {
      return new ArrayList<>();
    }
    int lastValue = worldSimValues.getLast();

    for (MonteCarloData monteCarloData : monteCarloDataRepository.findAll()) {
      if ((zoneService.getZoneById(zoneId) != null && monteCarloData.getZoneId() == zoneId &&
          zoneId != 0) ||
          (zoneService.getZoneById(zoneId) == null && zoneId == 0)) {

        int simCount = monteCarloData.getSimNo();
        groupedBySimCount.putIfAbsent(simCount, new ArrayList<>());
        groupedBySimCount.get(simCount).add(monteCarloData.getCompletedTasks() + lastValue);
      }
    }

    return new ArrayList<>(groupedBySimCount.values());
  }

  public String getZoneEndTime(long zoneId) {
    //MonteCarloData monteCarloData = monteCarloDataRepository.findLastByZoneId(zoneId).getTime();
    return "";
  }

  public void flushMCData() {
    monteCarloDataRepository.deleteAll();
  }


}
