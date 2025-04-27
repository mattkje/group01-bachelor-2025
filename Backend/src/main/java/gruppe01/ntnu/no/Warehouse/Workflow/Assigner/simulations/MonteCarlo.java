package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.LicenseService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.PickerTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorkerService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator;
import jakarta.transaction.Transactional;
import java.io.IOException;
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
import java.util.Random;
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
  ZoneSimulator zoneSimulator = new ZoneSimulator();
  List<String> errorMessages = new ArrayList<>();
  ExecutorService simulationExecutor =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  List<Future<SimulationResult>> futures = new ArrayList<>();
  List<Zone> zones = zoneService.getAllZones();
  List<ActiveTask> activeTasks = activeTaskService.getActiveTasksForToday();
  Map<String,RandomForest> models = mlModel.getAllModels();
  // Initialize lazy-loaded collections
  activeTasks.forEach(task -> Hibernate.initialize(task.getWorkers()));

  for (int i = 0; i < simCount; i++) {
    System.out.println("Running simulation " + (i + 1) + " of " + simCount);
    int finalI = i;
    futures.add(simulationExecutor.submit(() -> {
      ExecutorService warehouseExecutor = Executors.newFixedThreadPool(zones.size());
      AtomicDouble totalTaskTime = new AtomicDouble(0);
      List<Zone> zonesCopy = zones.stream().map(Zone::new).toList();
      List<ActiveTask> activeTasksCopy = activeTasks.stream()
          .map(ActiveTask::new)
          .toList();
      Set<PickerTask> pickerTasksCopy =
          pickerTaskService.getPickerTasksForToday().stream().map(PickerTask::new)
              .collect(Collectors.toSet());
      Map<Long, Double> zoneDurations = new HashMap<>();

      for (Zone zone : zonesCopy) {
        warehouseExecutor.submit(() -> {
          try {
            String result = "";
            if (!zone.getIsPickerZone()) {
              List<ActiveTask> zoneTasks = activeTasksCopy.stream()
                  .filter(activeTask -> Objects.equals(activeTask.getTask().getZoneId(),
                      zone.getId()))
                  .toList();
              result = zoneSimulator.runZoneSimulation(zone, zoneTasks, null, null,totalTaskTime, finalI);
            } else {
              Set<PickerTask> zoneTasks = pickerTasksCopy.stream()
                  .filter(pickerTask -> Objects.equals(pickerTask.getZoneId(), zone.getId()))
                  .collect(Collectors.toSet());
              result = zoneSimulator.runZoneSimulation(zone, null, zoneTasks,models.get(zone.getName().toUpperCase()), totalTaskTime, finalI);
            }
            try {
              double parsedResult = Double.parseDouble(result);
              synchronized (zoneDurations) {
                zoneDurations.put(zone.getId(), parsedResult);
              }
            } catch (NumberFormatException e) {
              synchronized (errorMessages) {
                zoneDurations.put(zone.getId(), 0.0);
                errorMessages.add(result);
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
           // System.out.println("Zone " + zone.getId() + " simulation complete ");
          }
        });
      }

      warehouseExecutor.shutdown();
      warehouseExecutor.awaitTermination(1, TimeUnit.DAYS);
      double averageCompletionTime = totalTaskTime.get() / zonesCopy.size();
      return new SimulationResult(averageCompletionTime, zoneDurations, errorMessages);
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