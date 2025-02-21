package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores.WorkerSemaphore;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

  public static void runZoneSimulation(Zone zone, List<ActiveTask> zoneTasks,
                                       AtomicDouble totalTaskTime) {
    try {
      // Get the available workers in the zone
      List<Worker> zoneWorkers = (List<Worker>) zone.getWorkers();
      System.out.println("Zone " + zone.getId() + " acquired " + zoneWorkers.size() +
          " workers. With a capacity of " + zone.getCapacity());

      // Check if there are any acquired workers
      if (zoneWorkers.isEmpty()) {
        return;
      }

      // Set up the zone simulation
      ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers.size());
      WorkerSemaphore availableZoneWorkersSemaphore = new WorkerSemaphore(zoneWorkers);

      // Run the simulation for each task in a zone
      for (ActiveTask activeTask : zoneTasks) {
        // TODO: Make this a more permanent and better number using statistics to calculate
        int taskDuration = activeTask.getTask().getMaxTime() - activeTask.getTask().getMinTime();
        // Run a task in a separate thread
        zoneExecutor.submit(() -> {
          try {

            // TODO; TEST, this while loop might be redundant
            while (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
              // Acquire the minimum amount of workers with the required licenses to start the task
              availableZoneWorkersSemaphore.acquireMultiple(activeTask);
            }

            long startTaskTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTaskTime < taskDuration) {
              if (activeTask.getTask().getMaxWorkers() - activeTask.getWorkers().size() > 0) {
                availableZoneWorkersSemaphore.acquire(activeTask);
              }
              TimeUnit.MILLISECONDS.sleep(1);
            }
            availableZoneWorkersSemaphore.releaseAll(activeTask.getWorkers());
            totalTaskTime.addAndGet(taskDuration);

          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
      }

      zoneExecutor.shutdown();
      zoneExecutor.awaitTermination(1, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
