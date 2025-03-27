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
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator;
import java.util.HashMap;
import java.util.Map;
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

  /**
   * Runner class for the Monte Carlo Simulation
   *
   * @param simCount    Number of simulations to run (aiming to run at least 5000) List of licenses
   * @throws InterruptedException
   * @throws ExecutionException
   */
public List<SimulationResult> monteCarlo(int simCount)
    throws InterruptedException, ExecutionException {

    List<String> errorMessages = new ArrayList<>();
    ExecutorService simulationExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<SimulationResult>> futures = new ArrayList<>();

    List<ActiveTask> activeTasks = activeTaskService.getActiveTasksForToday();
    List<Zone> zones = zoneService.getAllZones();

    for (int i = 0; i < simCount; i++) {
        int finalI = i;
        futures.add(simulationExecutor.submit(() -> {
            ExecutorService warehouseExecutor = Executors.newFixedThreadPool(zones.size());
            AtomicDouble totalTaskTime = new AtomicDouble(0);

            List<Zone> zonesCopy = zones.stream().map(Zone::new).collect(Collectors.toList());
            List<ActiveTask> activeTasksCopy = activeTasks.stream().map(ActiveTask::new).collect(Collectors.toList());

            Map<Long, Double> zoneDurations = new HashMap<>();

            for (Zone zone : zonesCopy) {
                List<ActiveTask> zoneTasks = activeTasksCopy.stream()
                    .filter(activeTask -> Objects.equals(activeTask.getTask().getZoneId(), zone.getId()))
                    .toList();

                warehouseExecutor.submit(() -> {
                    try {
                        String result = zoneSimulator.runZoneSimulation(zone, zoneTasks, totalTaskTime, finalI);
                        try {
                            double parsedResult = Double.parseDouble(result);
                            synchronized (zoneDurations) {
                                zoneDurations.put(zone.getId(), parsedResult);
                            }
                        } catch (NumberFormatException e) {
                            synchronized (errorMessages) {
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
            double averageCompletionTime = totalTaskTime.get() / zonesCopy.size();
            return new SimulationResult(averageCompletionTime, zoneDurations);
        }));
    }

    List<SimulationResult> results = new ArrayList<>();
    for (Future<SimulationResult> future : futures) {
        results.add(future.get());
    }

    simulationExecutor.shutdown();
    simulationExecutor.awaitTermination(1, TimeUnit.DAYS);

    return results;
}


}