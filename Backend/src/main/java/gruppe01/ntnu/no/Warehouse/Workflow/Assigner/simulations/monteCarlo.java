package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
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


  private static final Random random = new Random();
  public static void main(String[] args) throws InterruptedException, ExecutionException {


    // TODO: Testing purpose: See time used, remove later
    long startTime = System.currentTimeMillis();

    // Setting up the simulation worker parameter
    int minWorkers = 4, maxWorkers = 5; // Number of workers active on a given day as a range
    List<Worker> workers = initializeWorkers();

    for (Worker worker : workers) {
      System.out.println(worker.getWorkerType());
    }

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
      int finalI = i; // TODO: Debugging purposes, remove later
      // Add the conlcusion of the simulation to the futures list
      futures.add(simulationExecutor.submit(() -> {
        // Calculate a random amount of zones and workers
        int zones = random.nextInt(maxZones - minZones + 1) + minZones;
        int availableWorkers = random.nextInt(maxWorkers - minWorkers + 1) + minWorkers;

        // Set up the simulation for each warehouse
        ExecutorService warehouseExecutor = Executors.newFixedThreadPool(availableWorkers);
        // A warehouse semaphore to limit the number of workers
        Semaphore availableWorkersSemaphore = new Semaphore(availableWorkers);

        // Total time to complete all tasks in a warehouse
        AtomicDouble totalTaskTime = new AtomicDouble(0);

        //TODO: Debugging purposes, remove later
        System.out.println(
            "Simulation " + finalI + " has " + availableWorkers + " workers and " + zones +
                " zones");

        // Run the simulation for each zone in a warehouse
        for (int k = 0; k < zones; k++) {
          // Calculate a random amount of workers and tasks for each zone
          int zoneWorkers = random.nextInt(maxZoneWorkers - minZoneWorkers + 1) + minZoneWorkers;
          // Limit the number of workers to the available workers (Sim only)
          if (zoneWorkers > availableWorkers) {
            zoneWorkers = availableWorkers;
          }
          // A try,catch requires this step to use the variable
          int finalZoneWorkers = zoneWorkers;
          int zoneTasks = random.nextInt(maxZoneTasks - minZoneTasks + 1) + minZoneTasks;

          // TODO: Debugging purposes, remove later
          int finalK = k;


          // Run zone simulation
          runZoneSimulation(finalK, finalZoneWorkers, zoneTasks, maxZoneWorkers,
              minMaxTaskWorkers, minMinTaskWorkers, maxMaxTaskWorkers, maxMinTaskWorkers,
              maxTaskTime, minTaskTime, totalTaskTime, availableWorkersSemaphore);

        }
        warehouseExecutor.shutdown();
        warehouseExecutor.awaitTermination(1, TimeUnit.DAYS);

        return totalTaskTime.get() / availableWorkers;
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

  private static void runZoneSimulation(int finalK, int finalZoneWorkers, int zoneTasks,
                                        int maxZoneWorkers, int minMaxTaskWorkers,
                                        int minMinTaskWorkers, int maxMaxTaskWorkers,
                                        int maxMinTaskWorkers, int maxTaskTime, int minTaskTime,
                                        AtomicDouble totalTaskTime,
                                        Semaphore availableWorkersSemaphore) {
    try {
      // A zone will attempt to acquire the amount of workers it needs
      availableWorkersSemaphore.acquire(finalZoneWorkers);

      //TODO: Debugging purposes, remove later
      System.out.println(
          "Zone " + finalK + " has " + finalZoneWorkers + " workers and " + zoneTasks +
              " tasks, with a potential of " + maxZoneWorkers + " workers");

      // Set up the zone simulation
      ExecutorService zoneExecutor = Executors.newFixedThreadPool(finalZoneWorkers);
      Semaphore availableZoneWorkersSemaphore = new Semaphore(finalZoneWorkers);

      // Run the simulation for each task in a zone
      for (int j = 0; j < zoneTasks; j++) {
        // Calculate a random amount of workers and time for each task
        int minTaskWorkers =
            random.nextInt(minMaxTaskWorkers - minMinTaskWorkers + 1) + minMinTaskWorkers;
        int maxTaskWorkers =
            random.nextInt(maxMaxTaskWorkers - maxMinTaskWorkers + 1) + maxMinTaskWorkers;

        // TODO: Fix this temporary fix
        // Essentially this will be made redundant by actual values provided by the warehouse later on
        if (minTaskWorkers > finalZoneWorkers) {
          minTaskWorkers = finalZoneWorkers;
        } else if (maxTaskWorkers > finalZoneWorkers) {
          maxTaskWorkers = finalZoneWorkers;
        } else if (maxTaskWorkers < minTaskWorkers) {
          maxTaskWorkers = minTaskWorkers;
        }

        // Calculate the time to complete the task
        final int[] taskTime =
            {random.nextInt(maxTaskTime - minTaskTime + 1) + minTaskTime};
        //TODO: Debugging purposes, remove later
        int finalJ = j;
        int finalMinTaskWorkers = minTaskWorkers;
        int finalMaxTaskWorkers = maxTaskWorkers;

        zoneExecutor.submit(() -> {
          try {
            // Initial amount of workers acquired for a task
            int acquiredWorkers = 0;
            System.out.println("Zone " + finalK + " Task " + finalJ + " started with " +
                finalMinTaskWorkers + " to " + finalMaxTaskWorkers + " workers and " +
                taskTime[0] + "ms");

            // Latch preventing a task from starting until the required workers are acquired
            CountDownLatch latch = new CountDownLatch(1);

            while (acquiredWorkers < finalMinTaskWorkers) {
              // Calculate the amount of workers to acquire
              int toAcquire = finalMaxTaskWorkers - acquiredWorkers;
              //TODO: Debugging purposes, remove later
              System.out.println(
                  "Zone " + finalK + " Task " + finalJ + " trying to acquire " +
                      toAcquire + " workers");
              // Attempt to acquire the workers
              if (availableZoneWorkersSemaphore.tryAcquire(toAcquire, 1,
                  TimeUnit.MILLISECONDS)) {
                // Update the amount of workers acquired
                acquiredWorkers += toAcquire;
                // Release the latch
                latch.countDown();
                System.out.println(
                    "Zone " + finalK + " Task " + finalJ + " Acquired " + toAcquire +
                        " workers" + ", total acquired: " + acquiredWorkers + " here");
              } // If the wanted amount of workers are not available, try to acquire the minimum amount
              else if (availableZoneWorkersSemaphore.tryAcquire(finalMinTaskWorkers, 1,
                  TimeUnit.MILLISECONDS)) {
                // Update the amount of workers acquired
                acquiredWorkers += finalMinTaskWorkers;
                // Release the latch
                latch.countDown();
                System.out.println("Zone " + finalK + " Task " + finalJ +
                    " Acquired 1 worker, total acquired: " + acquiredWorkers);
              } // No workers available, wait
              else {
                System.out.println("Zone " + finalK + " Task " + finalJ + " waiting for " +
                    (finalMinTaskWorkers - acquiredWorkers) + " workers");
              }
            }

            latch.await(); // Wait until the required number of workers is acquired

            long startTaskTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTaskTime < taskTime[0]) {
              int additionalWorkers = finalMaxTaskWorkers - acquiredWorkers;
              if (additionalWorkers > 0) {
                availableZoneWorkersSemaphore.tryAcquire(additionalWorkers, 1,
                    TimeUnit.MILLISECONDS);
              }
              TimeUnit.MILLISECONDS.sleep(100);
            }

            System.out.println(
                "Zone " + finalK + " Task " + finalJ + " completed in " + taskTime[0] +
                    "ms with " + acquiredWorkers + " workers");
            availableZoneWorkersSemaphore.release(acquiredWorkers);
            totalTaskTime.addAndGet(taskTime[0]);

          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
      }

      zoneExecutor.shutdown();
      zoneExecutor.awaitTermination(1, TimeUnit.DAYS);
      System.out.println("Zone " + finalK + " tasks submitted");
      availableWorkersSemaphore.release(finalZoneWorkers);
      System.out.println("Zone " + finalK + " workers released");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}