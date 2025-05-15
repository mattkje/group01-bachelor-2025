package gruppe01.ntnu.no.warehouse.workflow.assigner.services;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.MonteCarloData;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.MonteCarloDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Service class for handling Monte Carlo simulation data.
 * This class is responsible for generating and managing simulation data points.
 */
@Service
public class MonteCarloService {

  private final MonteCarloDataRepository monteCarloDataRepository;

  /**
   * Constructor for MonteCarloService.
   *
   * @param monteCarloDataRepository the repository for MonteCarloData
   */
  public MonteCarloService(MonteCarloDataRepository monteCarloDataRepository) {
    this.monteCarloDataRepository = monteCarloDataRepository;
  }

  /**
   * Generates a simulation data point and saves it to the repository.
   *
   * @param simNo          the simulation number
   * @param now            the current date and time
   * @param tasksCompleted the number of tasks completed
   * @param zoneId         the ID of the zone (can be null)
   */
  public void generateSimulationDataPoint(int simNo, LocalDateTime now, int tasksCompleted,
                                          Long zoneId) {
    MonteCarloData monteCarloData = new MonteCarloData();
    monteCarloData.setSimNo(simNo);
    monteCarloData.setTime(now);
    monteCarloData.setCompletedTasks(tasksCompleted);
    monteCarloData.setZoneId(Objects.requireNonNullElse(zoneId, 0L));
    monteCarloDataRepository.save(monteCarloData);
  }

  /**
   * Deletes all Monte Carlo data from the repository.
   */
  public void dropAllData() {
    monteCarloDataRepository.deleteAll();
  }

}