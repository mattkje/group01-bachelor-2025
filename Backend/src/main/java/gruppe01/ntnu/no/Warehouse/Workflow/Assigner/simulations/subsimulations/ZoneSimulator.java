package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores.WorkerSemaphore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulates a zone in a warehouse
 * Takes into account the following:
 * - Number of workers
 * - Number of workers possible in a zone
 * - Number of tasks
 * - Number of workers required to complete a task
 */

public class ZoneSimulator {

  static Random random = new Random();

  public static String runZoneSimulation(Zone zone, List<ActiveTask> zoneTasks,
                                  AtomicDouble totalTaskTime, int simNo) {
    try {
      // Get the workers in the zone as a set
      Set<Worker> originalZoneWorkers = zone.getWorkers();

      // If there are no workers, return an error message
      if (originalZoneWorkers.isEmpty()) {
        return "ERROR: Zone " + zone.getId() +
            " No workers";
      }

      // Create a deep copy of the workers for this simulation
      Set<Worker> zoneWorkers = new HashSet<>();
      for (Worker worker : originalZoneWorkers) {
        zoneWorkers.add(new Worker(worker)); // Assuming Worker has a copy constructor
      }

      // Error message list for any errors that occur during the completion of tasks
      List<String> errorMessages = new ArrayList<>();
      ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers.size());
      // Latch for the tasks in the zone ensuroing that all tasks are completed before the simulation ends
      CountDownLatch zoneLatch = new CountDownLatch(zoneTasks.size());
      WorkerSemaphore availableZoneWorkersSemaphore = new WorkerSemaphore(zoneWorkers);

      AtomicBoolean isSimulationSuccessful = new AtomicBoolean(true);
      AtomicDouble zoneTaskTime = new AtomicDouble(0.0);

      // Iterate over the tasks in the zone
      for (ActiveTask activeTask : zoneTasks) {
        // Gets a random duration for the task
        // TODO: Replace this with a machine learning model for typical taskduration
        int taskDuration =
            random.nextInt(activeTask.getTask().getMaxTime() - activeTask.getTask().getMinTime()) +
                activeTask.getTask().getMinTime();

        // Set a random EPW for the task (Efficiency gained per worker)
        // Currently at  a range between 0.4 and 0.9
        // TODO: Replace this with a ML learned number
        double epw = random.nextDouble() * 0.5 + 0.4;

        if (zone.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
          isSimulationSuccessful.set(false);
          zoneLatch.countDown();
          return "ERROR: ZONE " + zone.getId() + " - MISSING WORKERS FOR TASK " +
              activeTask.getId();
        }

        long workersWithRequiredLicenses = zone.getWorkers().stream()
            .filter(worker -> worker.getLicenses()
                .containsAll(activeTask.getTask().getRequiredLicense()))
            .count();

        if (workersWithRequiredLicenses < activeTask.getTask().getMinWorkers()) {
          isSimulationSuccessful.set(false);
          zoneLatch.countDown();
          return "ERROR: ZONE " + zone.getId() +
              " - " + activeTask.getTask().getName() + "!Missing workers with required licenses";
        }

        // Start a thread for a single task in a zone
        zoneExecutor.submit(() -> {
          try {
            // Acquire the workers for the task
            while (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
              String acquireWorkerError =
                  availableZoneWorkersSemaphore.acquireMultiple(activeTask,simNo);
              if (!acquireWorkerError.isEmpty()) {
                errorMessages.add(acquireWorkerError);
                isSimulationSuccessful.set(false);
                return;
              }
            }
            // Simulate the task duration
            // TODO: Find a quicker way of doing this so that the simulation runs faster
            TimeUnit.MILLISECONDS.sleep(taskDuration);
            // Release the workers when the task is finished
            availableZoneWorkersSemaphore.releaseAll(activeTask.getWorkers());
            // Add the task duration to the total task time
            zoneTaskTime.addAndGet(taskDuration);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            zoneLatch.countDown();
          }
        });
      }


      zoneLatch.await();
      zoneExecutor.shutdown();
      zoneExecutor.awaitTermination(1, TimeUnit.DAYS);
      if (isSimulationSuccessful.get()) {
        totalTaskTime.addAndGet(zoneTaskTime.get());
        return "";
      }
      return errorMessages.isEmpty() ? "" : errorMessages.toString();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return "Zone " + zone.getId() + " simulation failed";
  }
}
