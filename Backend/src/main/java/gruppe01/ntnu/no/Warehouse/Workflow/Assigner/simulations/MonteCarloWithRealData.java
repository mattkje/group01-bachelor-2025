package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.LicenseService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorkerService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
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
public class MonteCarloWithRealData {
  /**
   * Monte Carlo Simulation of a warehouse time to complete all tasks within a day
   * <p>
   * Currently takes into account the following:
   * - Time to complete each task
   * - Number of workers
   * - Number of workers required to complete a task
   * - Number of zones
   * - Number of workers in a zone
   * - Number of tasks per zone
   */

  // TODO: Integrate real data into this
  // TODO: Add machine learning to predict a day (Separate function)

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

  ZoneSimulator zoneSimulator = new ZoneSimulator();

  private static final Random random = new Random();

  public void monteCarlo() throws InterruptedException, ExecutionException {
    long startTime = System.currentTimeMillis();

    // Retrieve data from database to run simulations with
    List<Worker> workers = workerService.getAllWorkers();
    List<Task> tasks = taskService.getAllTasks();
    List<ActiveTask> activeTasks = activeTaskService.getActiveTasksForToday();
    List<License> licenses = licenseService.getAllLicenses();
    List<Zone> zones = zoneService.getAllZones();

    // Amount of simulations
    // TODO: Set this to a minimum of 5000 simulations or make it a variable
    int simCount = 1;

    // Run the Monte Carlo Simulations
    runSimulation(simCount, workers, zones, activeTasks, licenses);

    long endTime = System.currentTimeMillis();
    System.out.println("Simulation time: " + (endTime - startTime) + " ms");
  }

  /**
   * Runner class for the Monte Carlo Simulation
   *
   * @param simCount    Number of simulations to run (aiming to run at least 5000)
   * @param workers     List of workers
   * @param zones       List of zones
   * @param activeTasks List of active tasks for the given day
   * @param licenses    List of licenses
   * @throws InterruptedException
   * @throws ExecutionException
   */
  private void runSimulation(int simCount, List<Worker> workers, List<Zone> zones,
                             List<ActiveTask> activeTasks, List<License> licenses)
      throws InterruptedException, ExecutionException {

    //TODO: Implement an error checking system to ensure that a simulation can be completed
    List<String> errorMessages = new ArrayList<>();

    ExecutorService simulationExecutor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<Double>> futures = new ArrayList<>();

    System.out.println("Running simulation with " + simCount + " simulations");
    System.out.println("Number of workers: " + workers.size());
    System.out.println("Number of zones: " + zones.size());
    System.out.println("Number of tasks: " + activeTasks.size());
    // For each simulation, run a simulation of each warehouse
    for (int i = 0; i < simCount; i++) {
      int finalI = i;
      futures.add(simulationExecutor.submit(() -> {
        // Create a new executor for each warehouse
        ExecutorService warehouseExecutor = Executors.newFixedThreadPool(zones.size());
        // AtomicDouble to store the total time for all tasks in the warehouse for this simulation
        AtomicDouble totalTaskTime = new AtomicDouble(0);

        // Deep copy of the data to avoid changing the original data
        List<Zone> zonesCopy = zones.stream().map(Zone::new).collect(Collectors.toList());
        List<ActiveTask> activeTasksCopy =
            activeTasks.stream().map(ActiveTask::new).collect(Collectors.toList());

        // Run a simulation for each zone in the warehouse
        for (Zone zone : zonesCopy) {
          // Only get tasks that are in the zone
          List<ActiveTask> zoneTasks = activeTasksCopy.stream()
              .filter(activeTask -> Objects.equals(activeTask.getTask().getZoneId(), zone.getId()))
              .toList();
          // Run the simulation for the zone
          warehouseExecutor.submit(() -> {
            try {
              String result = ZoneSimulator.runZoneSimulation(zone, zoneTasks, totalTaskTime);
              // If an error occurs, add it to the error messages
              if (!result.isEmpty()) {
                synchronized (errorMessages) {
                  errorMessages.add(result);
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
        }

        // Shutdown the warehouse executor and wait for it to finish
        warehouseExecutor.shutdown();
        warehouseExecutor.awaitTermination(1, TimeUnit.DAYS);

        System.out.println("Simulation " + finalI + " finished");

        return totalTaskTime.get() / zonesCopy.size();
      }));
    }

    double totalCompletionTime = 0;
    for (Future<Double> future : futures) {
      totalCompletionTime += future.get();
    }

    if (!errorMessages.isEmpty()) {
      for (String errorMessage : errorMessages) {
        System.out.println(errorMessage);
      }
    }

    simulationExecutor.shutdown();
    simulationExecutor.awaitTermination(1, TimeUnit.DAYS);

    double averageCompletionTime = totalCompletionTime / simCount;
    System.out.println("Average time to complete all tasks: " + averageCompletionTime + " minutes");
  }


}