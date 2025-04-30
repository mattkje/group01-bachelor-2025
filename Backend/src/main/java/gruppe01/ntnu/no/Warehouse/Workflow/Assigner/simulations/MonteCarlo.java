package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers.WorldSimulationController;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.LicenseService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.PickerTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorkerService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator2;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import smile.regression.RandomForest;

@Service
public class MonteCarlo {
  /**
   * Monte Carlo Simulation of a warehouse time to complete all tasks within a day.
   * Handles a workday with both picker and non-picker zones
   * Simulates the current worker layout from the time it is run, not the start of the day.
   */
  @Autowired
  private WorkerService workerService;

  @Autowired
  private ZoneService zoneService;

  @Autowired
  private TaskService taskService;

  @Autowired
  private ActiveTaskService activeTaskService;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private PickerTaskService pickerTaskService;

  @Autowired
  private Utils utils;

  @Autowired
  private WorldSimulation worldSimulation;

  private static final MachineLearningModelPicking mlModel = new MachineLearningModelPicking();

  //TODO: Save the result of the simulation to a file for quicker access between pages

  /**
   * Runner class for the Monte Carlo Simulation.
   * Starts a new thread for each simulation, and runs the simulation in parallel.
   *
   * @param simCount Number of simulations to run (Production runs 5000 simulations), but possible to change for easier testing
   * @return A list of SimulationResult objects containing the results of the simulation to parse to the frontend
   * @throws InterruptedException - if the thread is interrupted
   * @throws ExecutionException   - if the simulation fails
   */
  @Transactional
  public List<SimulationResult> monteCarlo(int simCount)
      throws InterruptedException, ExecutionException, IOException {
    System.out.println("Starting simulations");
    ZoneSimulator2 zoneSimulator = new ZoneSimulator2();
    List<String> errorMessages = new ArrayList<>();
    ExecutorService simulationExecutor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<SimulationResult>> futures = new ArrayList<>();
    List<Zone> zones = zoneService.getAllZones();
    List<ActiveTask> activeTasks = activeTaskService.getActiveTasksForToday();
    Map<String, RandomForest> models = mlModel.getAllModels();
    // Initialize lazy-loaded collections
    activeTasks.forEach(task -> Hibernate.initialize(task.getWorkers()));

    for (int i = 0; i < simCount; i++) {
      System.out.println("Running simulation " + (i + 1) + " of " + simCount);
      futures.add(simulationExecutor.submit(() -> {
        ExecutorService warehouseExecutor = Executors.newFixedThreadPool(zones.size());
        List<Zone> zonesCopy = zones.stream().map(Zone::new).toList();
        List<ActiveTask> activeTasksCopy = activeTasks.stream()
            .map(ActiveTask::new)
            .toList();
        Set<PickerTask> pickerTasksCopy =
            pickerTaskService.getPickerTasksForToday().stream().map(PickerTask::new)
                .collect(Collectors.toSet());
        Map<Long,ZoneSimResult> zoneSimResults = new HashMap<>();
        for (Zone zone : zonesCopy) {
          warehouseExecutor.submit(() -> {
            try {
              ZoneSimResult zoneSimResult = new ZoneSimResult();
              if (!zone.getIsPickerZone()) {
                List<ActiveTask> zoneTasks = activeTasksCopy.stream()
                    .filter(activeTask -> Objects.equals(activeTask.getTask().getZoneId(),
                        zone.getId()))
                    .toList();
                zoneSimResult = zoneSimulator.runZoneSimulation(zone, zoneTasks, null, null,
                    worldSimulation.getCurrentDateTime());
              } else {
                Set<PickerTask> zoneTasks = pickerTasksCopy.stream()
                    .filter(pickerTask -> Objects.equals(pickerTask.getZoneId(), zone.getId()))
                    .collect(Collectors.toSet());
                zoneSimResult = zoneSimulator.runZoneSimulation(zone, null, zoneTasks,
                    models.get(zone.getName().toUpperCase()), worldSimulation.getCurrentDateTime());
              }
              synchronized (zoneSimResults) {
                zoneSimResults.put(zone.getId(), zoneSimResult);
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
        }

        warehouseExecutor.shutdown();
        warehouseExecutor.awaitTermination(1, TimeUnit.DAYS);
        return new SimulationResult(utils.getLatestEndTime(zoneSimResults),zoneSimResults);
      }));
    }

    List<SimulationResult> results = new ArrayList<>();
    for (Future<SimulationResult> future : futures) {
      results.add(future.get());
    }

    simulationExecutor.shutdown();
    simulationExecutor.awaitTermination(1, TimeUnit.DAYS);

    System.out.println("Simulation complete");
    return results;
  }


}