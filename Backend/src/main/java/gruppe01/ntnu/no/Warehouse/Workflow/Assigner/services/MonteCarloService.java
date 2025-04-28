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

    public void generateSimulationDataPoint(int simNo, LocalDateTime now, int tasksCompleted) {


        MonteCarloData monteCarloData = new MonteCarloData();
        monteCarloData.setSimNo(simNo);
        monteCarloData.setTime(now);
        monteCarloData.setCompletedTasks(tasksCompleted);
        monteCarloData.setZone(0L);

        monteCarloDataRepository.save(monteCarloData);

    }

    public void dropAllData() {
        monteCarloDataRepository.deleteAll();
    }

}
