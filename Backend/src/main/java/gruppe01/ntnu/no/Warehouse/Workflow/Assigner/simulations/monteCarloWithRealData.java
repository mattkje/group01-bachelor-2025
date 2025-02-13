package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores.WorkerSemaphore;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class monteCarloWithRealData {
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


  private static final Random random = new Random();
  public static void main(String[] args) throws InterruptedException, ExecutionException {


    // TODO: Testing purpose: See time used, remove later
    long startTime = System.currentTimeMillis();

    // Setting up the simulation worker parameter
    int minWorkers = 4, maxWorkers = 5; // Number of workers active on a given day as a range
    List<Worker> workers = initializeWorkers();


    // Setting up the zone parameters
    int minZones = 6, maxZones = 8; // Number of zones in the warehouse as a range
    int minZoneWorkers = 1, maxZoneWorkers = 6; // Number of workers in a zone as a range
    int minZoneTasks = 1, maxZoneTasks = 20; // Number of tasks in a zone as a range

    // Setting up the task parameters
    int minTaskTime = 5, maxTaskTime = 30; // Time to complete each task as a range
    int minMinTaskWorkers = 5,
        minMaxTaskWorkers = 5; // Minimum number of workers required to complete a task as a range

    int maxMinTaskWorkers = 5,
        maxMaxTaskWorkers = 5; // Maximum number of workers able to work on a given task as a range


    // Setting up the simulation
    int simCount = 1; // Number of simulations
    ExecutorService simulationExecutor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<Double>> futures = new ArrayList<>();


    // Run each simulation
    for (int i = 0; i < simCount; i++) {
      // Add the conlcusion of the simulation to the futures list
      futures.add(simulationExecutor.submit(() -> {
        // Calculate a random amount of zones and workers
        int zones = random.nextInt(maxZones - minZones + 1) + minZones;

        // Set up the simulation for each warehouse based on the size of the worker pool
        ExecutorService warehouseExecutor = Executors.newFixedThreadPool(workers.size());
        // A warehouse semaphore to limit the number of workers
        WorkerSemaphore availableWorkersSemaphore = new WorkerSemaphore(workers);

        // Total time to complete all tasks in a warehouse
        AtomicDouble totalTaskTime = new AtomicDouble(0);

        // Run the simulation for each zone in a warehouse
        for (int k = 0; k < zones; k++) {
          // Calculate a random amount of workers and tasks for each zone
          int zoneWorkers = random.nextInt(maxZoneWorkers - minZoneWorkers + 1) + minZoneWorkers;
          // Limit the number of workers to the available workers (Sim only)
          if (zoneWorkers > workers.size()) {
            zoneWorkers = workers.size();
          }
          // A try,catch requires this step to use the variable
          int finalZoneWorkers = zoneWorkers;
          int zoneTasks = random.nextInt(maxZoneTasks - minZoneTasks + 1) + minZoneTasks;

          // Run zone simulation
          runZoneSimulation(finalZoneWorkers, zoneTasks, maxZoneWorkers,
              minMaxTaskWorkers, minMinTaskWorkers, maxMaxTaskWorkers, maxMinTaskWorkers,
              maxTaskTime, minTaskTime, totalTaskTime, availableWorkersSemaphore);

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

    long endTime = System.currentTimeMillis();

    double averageCompletionTime = totalCompletionTime / simCount;
    System.out.println("Average time to complete all tasks: " + averageCompletionTime);
    System.out.println("Time taken: " + (endTime - startTime) + "ms");
  }



 private static void runZoneSimulation( int finalZoneWorkers, int zoneTasks,
                                       int maxZoneWorkers, int minMaxTaskWorkers,
                                       int minMinTaskWorkers, int maxMaxTaskWorkers,
                                       int maxMinTaskWorkers, int maxTaskTime, int minTaskTime,
                                       AtomicDouble totalTaskTime,
                                       WorkerSemaphore availableWorkersSemaphore) {
     try {
         // A zone will attempt to acquire the amount of workers it needs
         List<Worker> acquiredWorkers = new ArrayList<>();
         for (int i = 0; i < finalZoneWorkers; i++) {
             Worker worker = availableWorkersSemaphore.acquire("");
             if (worker != null) {
                 acquiredWorkers.add(worker);
             }
         }

         // Check if there are any acquired workers
         if (acquiredWorkers.isEmpty()) {
             return;
         }
         // Set up the zone simulation
         ExecutorService zoneExecutor = Executors.newFixedThreadPool(acquiredWorkers.size());
         Semaphore availableZoneWorkersSemaphore = new Semaphore(acquiredWorkers.size());

         // Run the simulation for each task in a zone
         for (int j = 0; j < zoneTasks; j++) {
             // Calculate a random amount of workers and time for each task
             int minTaskWorkers =
                 random.nextInt(minMaxTaskWorkers - minMinTaskWorkers + 1) + minMinTaskWorkers;
             int maxTaskWorkers =
                 random.nextInt(maxMaxTaskWorkers - maxMinTaskWorkers + 1) + maxMinTaskWorkers;

             // TODO: Fix this temporary fix
             // Essentially this will be made redundant by actual values provided by the warehouse later on
             if (minTaskWorkers > acquiredWorkers.size()) {
                 minTaskWorkers = acquiredWorkers.size();
             } else if (maxTaskWorkers > acquiredWorkers.size()) {
                 maxTaskWorkers = acquiredWorkers.size();
             } else if (maxTaskWorkers < minTaskWorkers) {
                 maxTaskWorkers = minTaskWorkers;
             }

             // Calculate the time to complete the task
             final int[] taskTime =
                 {random.nextInt(maxTaskTime - minTaskTime + 1) + minTaskTime};
             int finalMinTaskWorkers = minTaskWorkers;
             int finalMaxTaskWorkers = maxTaskWorkers;

             zoneExecutor.submit(() -> {
                 try {
                     // Initial amount of workers acquired for a task
                     int acquiredWorkersCount = 0;
                     // Latch preventing a task from starting until the required workers are acquired
                     CountDownLatch latch = new CountDownLatch(1);

                     while (acquiredWorkersCount < finalMinTaskWorkers) {
                         // Calculate the amount of workers to acquire
                         int toAcquire = finalMaxTaskWorkers - acquiredWorkersCount;
                         // Attempt to acquire the workers
                         if (availableZoneWorkersSemaphore.tryAcquire(toAcquire, 1,
                             TimeUnit.MILLISECONDS)) {
                             // Update the amount of workers acquired
                             acquiredWorkersCount += toAcquire;
                             // Release the latch
                             latch.countDown();
                         } // If the wanted amount of workers are not available, try to acquire the minimum amount
                         else if (availableZoneWorkersSemaphore.tryAcquire(finalMinTaskWorkers, 1,
                             TimeUnit.MILLISECONDS)) {
                             // Update the amount of workers acquired
                             acquiredWorkersCount += finalMinTaskWorkers;
                             // Release the latch
                             latch.countDown();
                         } // No workers available, wait

                     }

                     latch.await(); // Wait until the required number of workers is acquired

                     long startTaskTime = System.currentTimeMillis();
                     while (System.currentTimeMillis() - startTaskTime < taskTime[0]) {
                         int additionalWorkers = finalMaxTaskWorkers - acquiredWorkersCount;
                         if (additionalWorkers > 0) {
                             availableZoneWorkersSemaphore.tryAcquire(additionalWorkers, 1,
                                 TimeUnit.MILLISECONDS);
                         }
                         TimeUnit.MILLISECONDS.sleep(100);
                     }
                     availableZoneWorkersSemaphore.release(acquiredWorkersCount);
                     totalTaskTime.addAndGet(taskTime[0]);

                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                 }
             });
         }

         zoneExecutor.shutdown();
         zoneExecutor.awaitTermination(1, TimeUnit.DAYS);
         // Release the workers back to the semaphore
         availableWorkersSemaphore.releaseAll(acquiredWorkers);
     } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
     }
 }

  /**
   * Initialize the workers for the simulation
   * Later on replaced by real worker data
   * @return A list of workers active that day
   */
  private static List<Worker> initializeWorkers() {
    List<Worker> workers = new ArrayList<>();
    workers.add(new Worker("Anna Jane", 1L, "Packaging Specialist", 1.0, new ArrayList<License>() {{
      add(new License("Forklift"));
    }}, true));
    workers.add(new Worker("John Doe",1L, "Crane Operator", 1.0, new ArrayList<License>() {{
      add(new License("Crane Operator"));
    }}, true));
    workers.add(new Worker("Jane Doe", 1L,"Warehouse", 1.0, new ArrayList<License>() {{
    }}, true));
    workers.add(new Worker("John Smith", 1L,"Warehouse", 1.0, new ArrayList<License>() {{
    }},true));
    workers.add(new Worker("Jane Smith", 1L,"Warehouse", 1.0, new ArrayList<License>() {{
    }},true));
    workers.add(new Worker("John Johnson",2L, "Warehouse", 1.0, new ArrayList<License>() {{
    }},true));
    workers.add(new Worker("Jane Johnson",2L, "Warehouse", 1.0, new ArrayList<License>() {{
    }},true));
    workers.add(new Worker("John Brown", 2L,"Warehouse", 1.0, new ArrayList<License>() {{
    }},true));

    return workers;
  }
}