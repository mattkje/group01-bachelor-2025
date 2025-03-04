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
      Set<Worker> zoneWorkers = zone.getWorkers();
 /*     System.out.println("Zone " + zone.getId() + " acquired " + zoneWorkers.size() +
          " workers. With a capacity of " + zone.getCapacity());*/

      if (zoneWorkers.isEmpty()) {
        return "ERROR: Zone " + zone.getId() + " has no workers and therefore cannot complete tasks";
      }

      ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers.size());
      WorkerSemaphore availableZoneWorkersSemaphore = new WorkerSemaphore(zoneWorkers);
      CountDownLatch zoneLatch = new CountDownLatch(zoneTasks.size());

      for (ActiveTask activeTask : zoneTasks) {
        // TODO: This currently only gets the average time. Make it more realistic, perhaphs a random
        int taskDuration = activeTask.getTask().getMaxTime() - activeTask.getTask().getMinTime();

        zoneExecutor.submit(() -> {
          try {
            CountDownLatch taskLatch = new CountDownLatch(1);
          /*  System.out.println("Task " + activeTask.getId() + " waiting for " +
                activeTask.getTask().getMinWorkers() + " workers in zone " + zone.getId());*/
            availableZoneWorkersSemaphore.acquireMultipleNoLicense(activeTask, taskLatch);

            taskLatch.await();
          /*  System.out.println("Task " + activeTask.getId() + " starting in zone " + zone.getId() +
                "with duration " + taskDuration);*/

            long startTaskTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTaskTime < taskDuration) {
              long elapsedTime = System.currentTimeMillis() - startTaskTime;

            }
           //  System.out.println( "Task duration: " + taskDuration + "minutes");
            TimeUnit.MILLISECONDS.sleep(1);

            availableZoneWorkersSemaphore.releaseAll(activeTask.getWorkers());
            totalTaskTime.addAndGet(taskDuration);
            //System.out.println("task " + activeTask.getId() + " completed in zone " + zone.getId());

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
      return "";
    } catch (InterruptedException e) {
      System.out.println("Zone simulation interrupted");
      Thread.currentThread().interrupt();
    }
    return "Zone simulation failed";
  }
}
