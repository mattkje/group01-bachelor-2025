package simulations;

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

       // Setting up the simulation task parameters
       int minTasks = 10, maxTasks = 100; // Number of tasks on a given day as a range
       int minTaskTime = 5, maxTaskTime = 30; // Time to complete each task as a range
       int minTaskWorkers = 1, maxTaskWorkers = 3; // Number of workers required to complete a task as a range

       ExecutorService simulationExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
       List<Future<Double>> futures = new ArrayList<>();

       for (int i = 0; i < simCount; i++) {
         futures.add(simulationExecutor.submit(() -> {
           int tasks = random.nextInt(maxTasks - minTasks + 1) + minTasks;
           int availableWorkers = random.nextInt(maxWorkers - minWorkers + 1) + minWorkers;

           ExecutorService executor = Executors.newFixedThreadPool(availableWorkers);
           Semaphore semaphore = new Semaphore(availableWorkers);

           double totalTaskTime = 0;

           for (int j = 0; j < tasks; j++) {
             int taskWorkers = random.nextInt(maxTaskWorkers - minTaskWorkers + 1) + minTaskWorkers;
             int taskTime = random.nextInt(maxTaskTime - minTaskTime + 1) + minTaskTime;

             executor.submit(() -> {
               try {
                 semaphore.acquire(taskWorkers);
                 TimeUnit.MILLISECONDS.sleep(taskTime);
                 semaphore.release(taskWorkers);
               } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
               }
             });

             totalTaskTime += taskTime;
           }

           executor.shutdown();
           executor.awaitTermination(1, TimeUnit.DAYS);

           return totalTaskTime / availableWorkers;
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