package simulations;

import java.util.Random;

public class monteCarlo {
  /**
   * Monte Carlo Simulation of a warehouse time to complete all tasks within a day
   *
   * Currently takes into account the following:
   * - Number of tasks
   * - Time to complete each task
   * - Number of workers
   */
  public static void main(String[] args) {

    int simCount = 10000; // Number of simulations
    int minWorkers = 8, maxWorkers = 10; // Number of workers active on a given day as a range

    int minTasks = 10, maxTasks = 100; // Number of tasks on a given day as a range
    int minTaskTime = 5, maxTaskTime = 30; // Time to complete each task as a range


    Random random = new Random();
    double totalCompletionTime = 0;

    for (int i = 0; i < simCount; i++) {
      int tasks = random.nextInt(maxTasks - minTasks + 1) + minTasks;
      int workers = random.nextInt(maxWorkers - minWorkers + 1) + minWorkers;

      double totalTaskTime = 0;
        for (int j = 0; j < tasks; j++) {
            totalTaskTime += random.nextInt(maxTaskTime - minTaskTime + 1) + minTaskTime;
        }

        double completionTime = totalTaskTime / workers; // For each worker the time to complete a task is reduced
        totalCompletionTime += completionTime;
      // System.out.println("Simulation " + (i + 1) + ": " + completionTime);
    }

    double averageCompletionTime = totalCompletionTime / simCount;
    System.out.println("Average time to complete all tasks: " + averageCompletionTime);
  }

}
