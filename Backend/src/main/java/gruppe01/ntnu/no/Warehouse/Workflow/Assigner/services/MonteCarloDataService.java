package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.MonteCarloData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.MonteCarloDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonteCarloDataService {

    @Autowired
    private MonteCarloDataRepository monteCarloDataRepository;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private WorldSimDataService worldSimDataService;
    /**
     * Gets world sim data values for a specific zone.
     * Zone 0 is for all zones
     * @param zoneId
     * @return
     */
   public List<List<Integer>> getMCDataValues(long zoneId) {
       Map<Integer, List<Integer>> groupedBySimCount = new HashMap<>();

       List<Integer> worldSimValues = worldSimDataService.getWorldSimValues(zoneId);
       if (worldSimValues == null || worldSimValues.isEmpty()) {
           return new ArrayList<>();
       }
       int lastvalue = worldSimValues.getLast();

       for (MonteCarloData monteCarloData : monteCarloDataRepository.findAll()) {
           if ((zoneService.getZoneById(zoneId) != null && monteCarloData.getZoneId() == zoneId && zoneId != 0) ||
               (zoneService.getZoneById(zoneId) == null && zoneId == 0)) {

               int simCount = monteCarloData.getSimNo();
               groupedBySimCount.putIfAbsent(simCount, new ArrayList<>());
               groupedBySimCount.get(simCount).add(monteCarloData.getCompletedTasks() + lastvalue);
           }
       }

       return new ArrayList<>(groupedBySimCount.values());
   }

   public void flushMCData() {
       monteCarloDataRepository.deleteAll();
   }



}
