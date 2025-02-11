package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import com.google.common.util.concurrent.AtomicDouble;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class monteCarlo {
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
  public static void main(String[] args) throws InterruptedException, ExecutionException {

    // Testing purpose: See time used
    long startTime = System.currentTimeMillis();

    // Setting up the simulation
    int simCount = 1; // Number of simulations
    Random random = new Random();

    // Setting up the simulation worker parameter
    int minWorkers = 4, maxWorkers = 7; // Number of workers active on a given day as a range

    // Setting up the zone parameters
    int minZones = 1, maxZones = 1; // Number of zones in the warehouse as a range
    int minZoneWorkers = 1, maxZoneWorkers = 6; // Number of workers in a zone as a range
    int minZoneTasks = 1, maxZoneTasks = 20; // Number of tasks in a zone as a range


    int minTaskTime = 5, maxTaskTime = 30; // Time to complete each task as a range
    int minMinTaskWorkers = 1,
        minMaxTaskWorkers = 3; // Minimum number of workers required to complete a task as a range

    int maxMinTaskWorkers = 1,
        maxMaxTaskWorkers = 5; // Maximum number of workers able to work on a given task as a range


    ExecutorService simulationExecutor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<Double>> futures = new ArrayList<>();

    for (int i = 0; i < simCount; i++) {
      int finalI = i;
      futures.add(simulationExecutor.submit(() -> {
        int zones = random.nextInt(maxZones - minZones + 1) + minZones;
        int availableWorkers = random.nextInt(maxWorkers - minWorkers + 1) + minWorkers;

        ExecutorService executor = Executors.newFixedThreadPool(availableWorkers);
        Semaphore availableWorkersSemaphore = new Semaphore(availableWorkers);

        AtomicDouble totalTaskTime = new AtomicDouble(0);

        System.out.println("Simulation " + finalI + " has " + availableWorkers + " workers and " + zones + " zones");
        for (int k = 0; k < zones; k++) {
          int zoneWorkers = random.nextInt(maxZoneWorkers - minZoneWorkers + 1) + minZoneWorkers;
          int zoneTasks = random.nextInt(maxZoneTasks - minZoneTasks + 1) + minZoneTasks;

          int finalK = k;
          executor.submit(() -> {
            try {
              availableWorkersSemaphore.acquire(zoneWorkers);

              System.out.println("Zone " + finalK + " has " + zoneWorkers + " workers and " + zoneTasks + " tasks");

              ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers);
              Semaphore availableZoneWorkersSemaphore = new Semaphore(zoneWorkers);

              for (int j = 0; j < zoneTasks; j++) {
                int minTaskWorkers =
                    random.nextInt(minMaxTaskWorkers - minMinTaskWorkers + 1) + minMinTaskWorkers;
                int maxTaskWorkers = random.nextInt(maxMaxTaskWorkers - maxMinTaskWorkers + 1) + maxMinTaskWorkers;
                // TODO: Fix this temporary fix
                // Essentially this will be made reduntant by actual values provided by the warehouse later on
                if (minTaskWorkers > zoneWorkers) {
                  minTaskWorkers = zoneWorkers;
                } else if (maxTaskWorkers > zoneWorkers) {
                  maxTaskWorkers = zoneWorkers;
                } else if (maxTaskWorkers < minTaskWorkers) {
                  maxTaskWorkers = minTaskWorkers;
                }


                final int[] taskTime =
                    {random.nextInt(maxTaskTime - minTaskTime + 1) + minTaskTime};
                int finalJ = j;
                int finalMinTaskWorkers = minTaskWorkers;
                int finalMaxTaskWorkers = maxTaskWorkers;
                zoneExecutor.submit(() -> {
                  try {
                    int acquiredWorkers = 0;
                    System.out.println("Zone " + finalK + " Task " + finalJ + " started with " + finalMinTaskWorkers + " to " + finalMaxTaskWorkers + " workers and " + taskTime[0] + "ms");
                    while (acquiredWorkers < finalMaxTaskWorkers) {
                      int toAcquire = finalMaxTaskWorkers - acquiredWorkers;
                      if (availableZoneWorkersSemaphore.tryAcquire(toAcquire, 1, TimeUnit.MILLISECONDS)) {
                        acquiredWorkers += toAcquire;
                        System.out.println("Zone " + finalK + " Task " + finalJ + " Acquired " + toAcquire + " workers" + ", total acquired: " + acquiredWorkers + " here");
                      } else if (acquiredWorkers < finalMinTaskWorkers) {
                        if (availableZoneWorkersSemaphore.tryAcquire(1, 1, TimeUnit.MILLISECONDS)) {
                          acquiredWorkers++;
                          System.out.println("Zone " + finalK + " Task " + finalJ + " Acquired 1 worker, total acquired: " + acquiredWorkers);
                        }
                      } else {
                        break;
                      }
                    }

                    long startTaskTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - startTaskTime < taskTime[0]) {
                      int additionalWorkers = finalMaxTaskWorkers - acquiredWorkers;
                      if (additionalWorkers > 0 && availableZoneWorkersSemaphore.tryAcquire(additionalWorkers, 100, TimeUnit.MILLISECONDS)) {
                        acquiredWorkers += additionalWorkers;
                        taskTime[0] -= (taskTime[0] * additionalWorkers) / finalMaxTaskWorkers;
                      }
                      TimeUnit.MILLISECONDS.sleep(100);
                    }

                    System.out.println("Zone " + finalK + " Task " + finalJ + " completed in " + taskTime[0] + "ms with " + acquiredWorkers + " workers");
                    availableZoneWorkersSemaphore.release(acquiredWorkers);
                    totalTaskTime.addAndGet(taskTime[0]);

                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                  }
                });
              }
              availableWorkersSemaphore.release(zoneWorkers);
              zoneExecutor.shutdown();
              zoneExecutor.awaitTermination(1, TimeUnit.DAYS);

            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          });

        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        return totalTaskTime.get() / availableWorkers;
      }));
    }

    double totalCompletionTime = 0;
    for (Future<Double> future : futures) {
      totalCompletionTime += future.get();
    }

    long endTime = System.currentTimeMillis();
    simulationExecutor.shutdown();
    simulationExecutor.awaitTermination(1, TimeUnit.DAYS);

    double averageCompletionTime = totalCompletionTime / simCount;
    System.out.println("Average time to complete all tasks: " + averageCompletionTime);
    System.out.println("Time taken: " + (endTime - startTime) + "ms");
  }
}