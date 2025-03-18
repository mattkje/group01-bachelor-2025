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
                                         AtomicDouble totalTaskTime) {
    try {
      // Get the workers in the zone as a set
      Set<Worker> originalZoneWorkers = zone.getWorkers();

      // If there are no workers, return an error message
      if (originalZoneWorkers.isEmpty()) {
        return "ERROR: Zone " + zone.getId() +
            " has no workers and therefore cannot complete tasks";
      }

      // Create a deep copy of the workers for this simulation
      Set<Worker> zoneWorkers = new HashSet<>();
      for (Worker worker : originalZoneWorkers) {
        zoneWorkers.add(new Worker(worker)); // Assuming Worker has a copy constructor
      }

      // Error message list for any errors that occur during the completion of tasks
      List<String> errorMessages = new ArrayList<>();
      errorMessages.add("ERROR: Zone " + zone.getId() + " simulation failed");
      ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers.size());
      // Latch for the tasks in the zone ensuroing that all tasks are completed before the simulation ends
      CountDownLatch zoneLatch = new CountDownLatch(zoneTasks.size());
      WorkerSemaphore availableZoneWorkersSemaphore = new WorkerSemaphore(zoneWorkers);

      AtomicBoolean isSimulationSuccessful = new AtomicBoolean(true);

      // Itearate over the tasks in the zone
      for (ActiveTask activeTask : zoneTasks) {
        // Gets a random duration for the task
        // TODO: Replace this with a machine learning model for typical taskduration
        int taskDuration =
            random.nextInt(activeTask.getTask().getMaxTime() - activeTask.getTask().getMinTime()) +
                activeTask.getTask().getMinTime();
        // Start a thread for a single task in a zone
        zoneExecutor.submit(() -> {
          try {
            // Latch for the workers in the task ensuring that all workers are acquired before the task starts
            CountDownLatch taskLatch = new CountDownLatch(1);
            // Acquire the workers for the task
            String acquireWorkerError =
                availableZoneWorkersSemaphore.acquireMultiple(activeTask, taskLatch);
            // Wait for all workers to be acquired
            taskLatch.await();
            // If there is an error acquiring the workers, add the error message to the list and return
            if (!acquireWorkerError.isEmpty()) {
              errorMessages.add(acquireWorkerError);
              isSimulationSuccessful.set(false);
              return;
            }
            // Simulate the task duration
            // TODO: Find a quicker way of doing this so that the simulation runs faster
            TimeUnit.MILLISECONDS.sleep(taskDuration);
            // Release the workers when the task is finished
            availableZoneWorkersSemaphore.releaseAll(activeTask.getWorkers());
            // Add the task duration to the total task time
            totalTaskTime.addAndGet(taskDuration);
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
        return "Zone " + zone.getId() + " simulation successful";
      }
      return errorMessages.isEmpty() ? "" : errorMessages.toString();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return "Zone " + zone.getId() + " simulation failed";
  }
}
