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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
  public List<SimulationResult> monteCarlo(int simCount)
      throws InterruptedException, ExecutionException {

    ZoneSimulator zoneSimulator = new ZoneSimulator();
    // List of potential error messages
    List<String> errorMessages = new ArrayList<>();
    // Create a thread pool for the simulations
    ExecutorService simulationExecutor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    // List of futures to store the results of the simulations
    List<Future<SimulationResult>> futures = new ArrayList<>();
    // List of zones to run the simulation on
    List<Zone> zones = zoneService.getAllZones();
    List<ActiveTask> activeTasks = activeTaskService.getActiveTasksForToday();
    // this for loop represents one warehouse simulation
    for (int i = 0; i < simCount; i++) {
      System.out.println("Running simulation " + (i + 1) + " of " + simCount);
      int finalI = i;
      futures.add(simulationExecutor.submit(() -> {
        // Create a thread pool for the warehouse simulation that is the size of the amount of zones in the warehouse
        ExecutorService warehouseExecutor = Executors.newFixedThreadPool(zones.size());
        // Create an atomic double to store the total task time for the simulation
        AtomicDouble totalTaskTime = new AtomicDouble(0);
        // Creating hard copies of each object to avoid concurrency issues
        List<Zone> zonesCopy = zones.stream().map(Zone::new).toList();
        List<ActiveTask> activeTasksCopy =
            activeTasks.stream().map(ActiveTask::new).toList();
        Set<PickerTask> pickerTasksCopy =
            pickerTaskService.getPickerTasksForToday().stream().map(PickerTask::new)
                .collect(Collectors.toSet());

        // Individual zone simulation results
        Map<Long, Double> zoneDurations = new HashMap<>();

        // Run the simulation for each zone in parallel
        for (Zone zone : zonesCopy) {
          warehouseExecutor.submit(() -> {
            try {
              String result = "";
              // If zone is a normal zone
              if (!zone.getIsPickerZone()) {
                // Filter the active tasks for the zone
                List<ActiveTask> zoneTasks = activeTasksCopy.stream()
                    .filter(activeTask -> Objects.equals(activeTask.getTask().getZoneId(),
                        zone.getId()))
                    .toList();
                // run zone simulation
                result =
                    zoneSimulator.runZoneSimulation(zone, zoneTasks, null, totalTaskTime, finalI);
              } else { // If zone is a picker zone
                // Filter the picker tasks for the zone
                Set<PickerTask> zoneTasks = pickerTasksCopy.stream()
                    .filter(pickerTask -> Objects.equals(pickerTask.getZoneId(), zone.getId()))
                    .collect(Collectors.toSet());
                // run zone simulation
                result = zoneSimulator.runZoneSimulation(zone, null, zoneTasks, totalTaskTime, finalI);
              }
              try {
                // Ensure the result is a number
                double parsedResult = Double.parseDouble(result);
                synchronized (zoneDurations) {
                  zoneDurations.put(zone.getId(), parsedResult);
                }
              } catch (NumberFormatException e) {
                // If the result is not a number, add it to the error messages (Error happened during simulation)
                synchronized (errorMessages) {
                  // Default 0.0 for the zone duration
                  zoneDurations.put(zone.getId(), 0.0);
                  errorMessages.add(result);
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
        }

        warehouseExecutor.shutdown();
        warehouseExecutor.awaitTermination(1, TimeUnit.DAYS);
        // Calculate the average completion time for the simulation
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