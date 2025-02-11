package simulations;

   import com.google.common.util.concurrent.AtomicDouble;
   import java.util.ArrayList;
   import java.util.List;
   import java.util.Random;
   import java.util.concurrent.*;

   public class monteCarlo {
     /**
      * Monte Carlo Simulation of a warehouse time to complete all tasks within a day
      *
      * Currently takes into account the following:
      * - Number of tasks
      * - Time to complete each task
      * - Number of workers
      */
     public static void main(String[] args) throws InterruptedException, ExecutionException {

       // Testing purpose: See time used
         long startTime = System.currentTimeMillis();

       // Setting up the simulation
       int simCount = 1000; // Number of simulations
       Random random = new Random();

       // Setting up the simulation worker parameter
       int minWorkers = 8, maxWorkers = 10; // Number of workers active on a given day as a range

       // Setting up the zone parameters
       int minZones = 1, maxZones = 8; // Number of zones in the warehouse as a range
       int minZoneWorkers = 1, maxZoneWorkers = 3; // Number of workers in a zone as a range
       int minZoneTasks = 1, maxZoneTasks = 20; // Number of tasks in a zone as a range


       int minTaskTime = 5, maxTaskTime = 30; // Time to complete each task as a range
       int minTaskWorkers = 1, maxTaskWorkers = 3; // Number of workers required to complete a task as a range

       ExecutorService simulationExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
       List<Future<Double>> futures = new ArrayList<>();

       for (int i = 0; i < simCount; i++) {
         futures.add(simulationExecutor.submit(() -> {
           int zones = random.nextInt(maxZones - minZones + 1) + minZones;
           int availableWorkers = random.nextInt(maxWorkers - minWorkers + 1) + minWorkers;

           ExecutorService executor = Executors.newFixedThreadPool(availableWorkers);
           Semaphore availableWorkersSemaphore = new Semaphore(availableWorkers);

           AtomicDouble totalTaskTime = new AtomicDouble(0);
           for (int k = 0; k < zones; k++) {
             int zoneWorkers = random.nextInt(maxZoneWorkers - minZoneWorkers + 1) + minZoneWorkers;
             int zoneTasks = random.nextInt(maxZoneTasks - minZoneTasks + 1) + minZoneTasks;

               int finalK = k;
               executor.submit(() -> {
               try {
                 availableWorkersSemaphore.acquire(zoneWorkers);

                 // System.out.println("Zone " + finalK + " has " + zoneWorkers + " workers and " + zoneTasks + " tasks");

                 ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers);
                 Semaphore availableZoneWorkersSemaphore = new Semaphore(zoneWorkers);

                 for (int j = 0; j < zoneTasks; j++) {
                   int taskWorkers = random.nextInt(maxTaskWorkers - minTaskWorkers + 1) + minTaskWorkers;
                   // TODO: Fix this temporary fix
                   if (taskWorkers > zoneWorkers) {
                     taskWorkers = zoneWorkers;
                   }
                   int taskTime = random.nextInt(maxTaskTime - minTaskTime + 1) + minTaskTime;

                     int finalJ = j;
                   int finalTaskWorkers = taskWorkers;
                   zoneExecutor.submit(() -> {
                     try {
                       availableZoneWorkersSemaphore.acquire(finalTaskWorkers);
                       TimeUnit.MILLISECONDS.sleep(taskTime);
                       // System.out.println("Task " + finalJ + " in zone " + finalK + " completed in " + taskTime + "ms");
                       availableZoneWorkersSemaphore.release(finalTaskWorkers);
                       // Update task time safely
                       totalTaskTime.addAndGet(taskTime);

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