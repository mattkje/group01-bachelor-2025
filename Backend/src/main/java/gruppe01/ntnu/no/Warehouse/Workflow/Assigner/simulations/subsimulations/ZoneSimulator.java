package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores.WorkerSemaphore;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
      Set<Worker> zoneWorkers = zone.getWorkers();

      // If there are no workers, return an error message
      if (zoneWorkers.isEmpty()) {
        return "ERROR: Zone " + zone.getId() +
            " has no workers and therefore cannot complete tasks";
      }

      // Error message list for any errors that occur during the completion of tasks
      List<String> errorMessages = new ArrayList<>();
      ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers.size());
      WorkerSemaphore availableZoneWorkersSemaphore = new WorkerSemaphore(zoneWorkers);
      // Latch for the tasks in the zone ensuroing that all tasks are completed before the simulation ends
      CountDownLatch zoneLatch = new CountDownLatch(zoneTasks.size());

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
            CountDownLatch taskLatch = new CountDownLatch(activeTask.getTask().getMinWorkers());

            // Acquire the workers for the task
            String acquireWorkerError =
                availableZoneWorkersSemaphore.acquireMultipleNoLicense(activeTask, taskLatch);
            // If there is an error acquiring the workers, add the error message to the list and return
            if (!acquireWorkerError.isEmpty()) {
              errorMessages.add(acquireWorkerError);
              return;
            }

            // Wait for all workers to be acquired
            taskLatch.await();

            // Simulate the task duration
            // TODO: Find a quicker way of doing this so that the simulation runs faster
            TimeUnit.MILLISECONDS.sleep(taskDuration);
            
            availableZoneWorkersSemaphore.releaseAll(activeTask.getWorkers());
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
      return errorMessages.isEmpty() ? "" : errorMessages.toString();
    } catch (InterruptedException e) {
      System.out.println("Zone simulation interrupted");
      Thread.currentThread().interrupt();
    }
    return "Zone simulation failed";
  }
}
