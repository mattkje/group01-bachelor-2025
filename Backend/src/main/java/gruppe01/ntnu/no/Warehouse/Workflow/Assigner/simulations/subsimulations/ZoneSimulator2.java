package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores.WorkerSemaphore2;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import smile.regression.RandomForest;

/**
 * Simulates a zone in a warehouse
 * Takes into account the following:
 * - Number of workers
 * - Number of workers possible in a zone
 * - Number of tasks
 * - Number of workers required to complete a task
 */

public class ZoneSimulator2 {

  static Random random = new Random();

  LocalDateTime lastTime = LocalDateTime.now();

  private static final MachineLearningModelPicking mlModel = new MachineLearningModelPicking();

  public ZoneSimResult runZoneSimulation(Zone zone, List<ActiveTask> activeTasks,
                                         Set<PickerTask> pickerTasks,
                                         RandomForest randomForest,
                                         LocalDateTime startTime) {
    ZoneSimResult zoneSimResult = new ZoneSimResult();
    zoneSimResult.setZoneId(zone.getId());
    try {
      if ((activeTasks == null || activeTasks.isEmpty()) &&
          (pickerTasks == null || pickerTasks.isEmpty())) {
        zoneSimResult.setErrorMessage("ERROR: Zone " + zone.getId() + " No tasks");
        return zoneSimResult;
      }
      // Get the workers in the zone as a set
      Set<Worker> originalZoneWorkers = zone.getWorkers();

      // If there are no workers, return an error message
      if (originalZoneWorkers.isEmpty()) {
        zoneSimResult.setErrorMessage("ERROR: Zone " + zone.getId() +
            " No workers");
        return zoneSimResult;
      }

      // Create a deep copy of the workers for this simulation
      Set<Worker> zoneWorkers = new HashSet<>();
      for (Worker worker : originalZoneWorkers) {
        if (worker.isAvailability()) {
          zoneWorkers.add(new Worker(worker));
        }
      }

      ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers.size());
      // Latch for the tasks in the zone ensuroing that all tasks are completed before the simulation ends
      WorkerSemaphore2 availableZoneWorkersSemaphore = new WorkerSemaphore2(zoneWorkers);

      this.lastTime = startTime;

      new CountDownLatch(1);
      CountDownLatch zoneLatch;

      // Iterate over the tasks in the zone
      if (activeTasks != null && !activeTasks.isEmpty()) {
        zoneLatch = new CountDownLatch(activeTasks.size());

        activeTasks = filterAndSortActiveTasks(activeTasks);

        for (ActiveTask activeTask : activeTasks) {
          String result = simulateTask(activeTask,
              availableZoneWorkersSemaphore, zoneExecutor, zoneLatch,
               lastTime,zoneSimResult);
          if (!result.isEmpty()) {
            zoneSimResult.setErrorMessage(result);
            return zoneSimResult;
          }
        }


      } else {
        zoneLatch = new CountDownLatch(pickerTasks.size());
        for (PickerTask pickerTask : pickerTasks) {
          String result = simulatePickerTask(pickerTask,
              availableZoneWorkersSemaphore, zoneExecutor, zoneLatch,
              randomForest, startTime,zoneSimResult);
          if (!result.isEmpty()) {
            zoneSimResult.setErrorMessage(result);
            return zoneSimResult;
          }
        }
      }

      zoneLatch.await();
      zoneExecutor.shutdown();
      zoneExecutor.awaitTermination(1, TimeUnit.DAYS);
      return zoneSimResult;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return zoneSimResult;
  }

  private String simulatePickerTask(PickerTask pickerTask,
                                    WorkerSemaphore2 availableZoneWorkersSemaphore,
                                    ExecutorService zoneExecutor, CountDownLatch zoneLatch,
                                    RandomForest randomForest, LocalDateTime startTime,
                                    ZoneSimResult zoneSimResult) {
    zoneExecutor.submit(() -> {
      try {
        // Acquire the workers for the task
        while (pickerTask.getWorker() == null) {
          availableZoneWorkersSemaphore.acquireMultiple(null, pickerTask);
        }
        // Simulate the task duration
        int taskDuration = (int) (mlModel.estimateTimeUsingModel(randomForest, pickerTask)) / 60;
        TimeUnit.MILLISECONDS.sleep(taskDuration);

        pickerTask.setStartTime(startTime);
        pickerTask.setEndTime(startTime.plusMinutes(taskDuration));
        this.lastTime = startTime.plusMinutes(taskDuration);
        zoneSimResult.addTask(pickerTask.getId().toString(),
            pickerTask.getStartTime(), pickerTask.getEndTime());
        availableZoneWorkersSemaphore.release(pickerTask.getWorker());
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

  private String simulateTask(ActiveTask activeTask,
                              WorkerSemaphore2 availableZoneWorkersSemaphore,
                              ExecutorService zoneExecutor,
                              CountDownLatch zoneLatch,
                              LocalDateTime startTime,
                              ZoneSimResult zoneSimResult
  ) {
    zoneExecutor.submit(() -> {
      try {
        // Acquire the workers for the task
        while (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
          String acquireWorkerError =
              availableZoneWorkersSemaphore.acquireMultiple(activeTask, null);
          if (!acquireWorkerError.isEmpty()) {
            zoneSimResult.setErrorMessage(acquireWorkerError);
            return;
          }
        }

        int sleepTime = calculateSleepTime(activeTask);
        TimeUnit.MILLISECONDS.sleep(sleepTime);
        this.lastTime = startTime.plusMinutes(sleepTime);


        availableZoneWorkersSemaphore.releaseAll(activeTask.getWorkers());
        activeTask.setStartTime(startTime);
        activeTask.setEndTime(startTime.plusMinutes(sleepTime));
        zoneSimResult.addTask(activeTask.getId().toString(),
            activeTask.getStartTime(), activeTask.getEndTime());

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        zoneLatch.countDown();
      }
    });
    return "";
  }

  private static List<ActiveTask> filterAndSortActiveTasks(List<ActiveTask> activeTasks) {
    return activeTasks.stream()
        // Filter out tasks that have an endTime
        .filter(task -> task.getEndTime() == null)
        // Sort tasks by nearest dueDate
        .sorted((task1, task2) -> {
          LocalDateTime dueDate1 = task1.getDueDate();
          LocalDateTime dueDate2 = task2.getDueDate();

          if (dueDate1 != null && dueDate2 != null) {
            return dueDate1.compareTo(dueDate2);
          }
          return 0;
        })
        .collect(Collectors.toList());
  }
}

