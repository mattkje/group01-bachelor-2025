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

    runSimulation(simCount, workers, tasks, zones, activeTasks, licenses);

    long endTime = System.currentTimeMillis();
    System.out.println("Time taken: " + (endTime - startTime) + "ms");
  }

  private void runSimulation(int simCount, List<Worker> workers, List<Task> tasks, List<Zone> zones,
                             List<ActiveTask> activeTasks, List<License> licenses)
      throws InterruptedException, ExecutionException {
    ExecutorService simulationExecutor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<Double>> futures = new ArrayList<>();

    System.out.println("Running simulation with " + simCount + " simulations");
    System.out.println("Number of workers: " + workers.size());
    System.out.println("Number of tasks: " + tasks.size());
    System.out.println("Number of zones: " + zones.size());
    System.out.println("Number of active tasks: " + activeTasks.size());
    System.out.println("Number of licenses: " + licenses.size());

    for (int i = 0; i < simCount; i++) {
      System.out.println("Running simulation " + (i + 1) + " of " + simCount);
      futures.add(simulationExecutor.submit(() -> {

        ExecutorService warehouseExecutor = Executors.newFixedThreadPool(workers.size());
        AtomicDouble totalTaskTime = new AtomicDouble(0);

        for (Zone zone : zones) {
          // Only get tasks that are in the zone
          List<ActiveTask> zoneTasks = activeTasks.stream()
              .filter(activeTask -> Objects.equals(activeTask.getTask().getZoneId(), zone.getId()))
              .toList();

          warehouseExecutor.submit(() -> {
            try {
              zoneSimulator.runZoneSimulation(zone,zoneTasks, totalTaskTime);
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
        }

        warehouseExecutor.shutdown();
        warehouseExecutor.awaitTermination(1, TimeUnit.DAYS);

        return totalTaskTime.get() / workers.size();
      }));
    }

    double totalCompletionTime = 0;
    for (Future<Double> future : futures) {
      totalCompletionTime += future.get();
    }

    simulationExecutor.shutdown();
    simulationExecutor.awaitTermination(1, TimeUnit.DAYS);

    double averageCompletionTime = totalCompletionTime / simCount;
    System.out.println("Average time to complete all tasks: " + averageCompletionTime);
  }


}