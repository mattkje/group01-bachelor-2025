package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores.WorkerSemaphore;
import java.io.IOException;
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
import smile.regression.RandomForest;

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

  private static final MachineLearningModelPicking mlModel = new MachineLearningModelPicking();

  public static String runZoneSimulation(Zone zone, List<ActiveTask> activeTasks,
                                         Set<PickerTask> pickerTasks,
                                         RandomForest randomForest,
                                         AtomicDouble totalTaskTime,
                                         int simNo) {
    try {

      if ((activeTasks == null || activeTasks.isEmpty()) &&
          (pickerTasks == null || pickerTasks.isEmpty())) {
        return "ERROR: Zone " + zone.getId() + " No tasks";
      }
      // Get the workers in the zone as a set
      Set<Worker> originalZoneWorkers = zone.getWorkers();

      // If there are no workers, return an error message
      if (originalZoneWorkers.isEmpty()) {
        return "ERROR: Zone " + zone.getId() +
            " No workers";
      }

      // Create a deep copy of the workers for this simulation
      Set<Worker> zoneWorkers = new HashSet<>();
      for (Worker worker : originalZoneWorkers) {
        if (worker.isAvailability()) {
          zoneWorkers.add(new Worker(worker));
        }
      }


      // Error message list for any errors that occur during the completion of tasks
      List<String> errorMessages = new ArrayList<>();
      ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers.size());
      // Latch for the tasks in the zone ensuroing that all tasks are completed before the simulation ends
      WorkerSemaphore availableZoneWorkersSemaphore = new WorkerSemaphore(zoneWorkers);

      AtomicBoolean isSimulationSuccessful = new AtomicBoolean(true);
      AtomicDouble zoneTaskTime = new AtomicDouble(0.0);
      AtomicDouble totalWaitingTime = new AtomicDouble(0.0);

      new CountDownLatch(1);
      CountDownLatch zoneLatch;

      // Iterate over the tasks in the zone
      if (activeTasks != null && !activeTasks.isEmpty()) {
        zoneLatch = new CountDownLatch(activeTasks.size());
        for (ActiveTask activeTask : activeTasks) {
          String result = simulateTask(activeTask, zone,
              availableZoneWorkersSemaphore, zoneExecutor, zoneLatch,
              isSimulationSuccessful, errorMessages, zoneTaskTime,totalWaitingTime, simNo);
          if (!result.isEmpty()) {
            return result;
          }
        }
      } else {
        zoneLatch = new CountDownLatch(pickerTasks.size());

        for (PickerTask pickerTask : pickerTasks) {
          String result = simulatePickerTask(pickerTask,
              availableZoneWorkersSemaphore, zoneExecutor, zoneLatch,
              isSimulationSuccessful, errorMessages, zoneTaskTime,totalWaitingTime, simNo,
              randomForest);
          if (!result.isEmpty()) {
            return result;
          }
        }
      }

      zoneLatch.await();
      zoneExecutor.shutdown();
      zoneExecutor.awaitTermination(1, TimeUnit.DAYS);
      if (isSimulationSuccessful.get()) {
        totalTaskTime.addAndGet(zoneTaskTime.get());
        return zoneTaskTime.get() + "";
      }
      return errorMessages.isEmpty() ? "" : errorMessages.toString();

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return "Zone " + zone.getId() + " simulation failed";
  }

  private static String simulatePickerTask(PickerTask pickerTask,
                                           WorkerSemaphore availableZoneWorkersSemaphore,
                                           ExecutorService zoneExecutor, CountDownLatch zoneLatch,
                                           AtomicBoolean isSimulationSuccessful,
                                           List<String> errorMessages, AtomicDouble zoneTaskTime,
                                           AtomicDouble totalWaitingTime, // New parameter
                                           int simNo, RandomForest randomForest) {
    zoneExecutor.submit(() -> {
      long waitingStartTime = System.currentTimeMillis(); // Start tracking waiting time
      try {
        // Acquire the workers for the task
        while (pickerTask.getWorker() == null) {
          availableZoneWorkersSemaphore.acquireMultiple(null, pickerTask, simNo);
        }
        long waitingEndTime = System.currentTimeMillis(); // End tracking waiting time
        totalWaitingTime.addAndGet((waitingEndTime - waitingStartTime) / 1000.0); // Add waiting time in seconds
        // Simulate the task duration
        int taskDuration = (int) (mlModel.estimateTimeUsingModel(randomForest, pickerTask)) / 60;
        TimeUnit.MILLISECONDS.sleep(taskDuration);
        availableZoneWorkersSemaphore.release(pickerTask.getWorker());
        zoneTaskTime.addAndGet(taskDuration);
      } catch (InterruptedException | IOException e) {
        Thread.currentThread().interrupt();
      } finally {
        zoneLatch.countDown();
      }
    });
    return "";
  }



  /**
   * Calculates sleep time for the thread for a single task
   * Sleep time is measured using the following formula:
   * T = (Duration (MAX) - ((Workers on task - Min Workers)/(Max Workers - Min Workers)) * (Duration (MAX) - Duration (MIN))) / E
   * E = (Efficiency per worker + EPW N)/N
   * For a better explanation see thesis document
   *
   * @param activeTask The task to calculate sleep time for
   * @return The sleep time for the task
   */
  private static int calculateSleepTime(ActiveTask activeTask) {
    int maxDuration = activeTask.getTask().getMaxTime();
    int minDuration = activeTask.getTask().getMinTime();
    int maxWorkers = activeTask.getTask().getMaxWorkers();
    int minWorkers = activeTask.getTask().getMinWorkers();
    List<Worker> workers = activeTask.getWorkers();

    double totalEfficiency = workers.stream()
        .mapToDouble(Worker::getEfficiency)
        .sum();
    double efficiency = totalEfficiency / workers.size();
    double adjustedDuration = maxDuration -
        ((double) (workers.size() - minWorkers) / (maxWorkers - minWorkers)) *
            (maxDuration - minDuration);

    return (int) (adjustedDuration / efficiency);
  }

  private static String simulateTask(ActiveTask activeTask, Zone zone,
                                     WorkerSemaphore availableZoneWorkersSemaphore,
                                     ExecutorService zoneExecutor,
                                     CountDownLatch zoneLatch,
                                     AtomicBoolean isSimulationSuccessful,
                                     List<String> errorMessages,
                                     AtomicDouble zoneTaskTime,
                                     AtomicDouble totalWaitingTime, // New parameter
                                     int simNo) {
    zoneExecutor.submit(() -> {
      long waitingStartTime = System.currentTimeMillis(); // Start tracking waiting time
      try {
        int check = 0;
        // Acquire the workers for the task
        while (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {

          String acquireWorkerError =
              availableZoneWorkersSemaphore.acquireMultiple(activeTask, null, simNo);
          if (!acquireWorkerError.isEmpty()) {
            System.out.println("Simulation failed");
            errorMessages.add(acquireWorkerError);
            isSimulationSuccessful.set(false);
            return;
          }
          check++;
          if (check == 101) {
            System.out.println("Trying to acquire workers for task " + activeTask.getId() + " Available permits: " +
                availableZoneWorkersSemaphore.getAvailablePermits());
          }
        }
        long waitingEndTime = System.currentTimeMillis(); // End tracking waiting time
        totalWaitingTime.addAndGet((waitingEndTime - waitingStartTime) / 1000.0); // Add waiting time in seconds

        // Simulate the task duration
        TimeUnit.MILLISECONDS.sleep(calculateSleepTime(activeTask));
        availableZoneWorkersSemaphore.releaseAll(activeTask.getWorkers());
        zoneTaskTime.addAndGet(calculateSleepTime(activeTask));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        zoneLatch.countDown();
        System.out.println("Task " + activeTask.getId() + " simulation complete in zone " + zone.getId() + " with " +
            activeTask.getWorkers().size() + " workers");
        System.out.println("Zone " + zone.getId() +  " workers: " + zone.getWorkers().size() + " Semaphore permits: " +
            availableZoneWorkersSemaphore.getAvailablePermits());
      }
    });
    return "";
  }
}

