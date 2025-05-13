package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores.WorkerSemaphore2;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.cglib.core.Local;
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

  private final AtomicReference<LocalDateTime> lastTime =
      new AtomicReference<>(LocalDateTime.now());

  private static final MachineLearningModelPicking mlModel = new MachineLearningModelPicking();

  private TimetableService timetableService;

  private List<ActiveTask> activeTasks = new ArrayList<>();


  public ZoneSimResult runZoneSimulation(Zone zone, List<ActiveTask> activeTasksList,
                                         Set<PickerTask> pickerTasks,
                                         RandomForest randomForest,
                                         LocalDateTime startTime,
                                         TimetableService timetableService) {

    // Initialize ZoneSimResult
    ZoneSimResult zoneSimResult = new ZoneSimResult();
    this.timetableService = timetableService;
    zoneSimResult.setZone(zone);
    this.activeTasks = activeTasksList;
    try {
      // Check if the zone has any tasks
      if ((activeTasks == null || activeTasks.isEmpty()) &&
          (pickerTasks == null || pickerTasks.isEmpty())) {
        // ERROR: NO TASKS
        zoneSimResult.setErrorMessage("101");
        return zoneSimResult;
      }
      // Get the workers in the zone as a set
      Set<Worker> originalZoneWorkers = zone.getWorkers();

      // Create a deep copy of the workers for this simulation
      Set<Worker> zoneWorkers = new HashSet<>();
      for (Worker worker : originalZoneWorkers) {
        // remove unavailable workers from the set
        if (worker.isAvailability()) {
          zoneWorkers.add(new Worker(worker));
        }
      }
      // Check if the zone has any workers
      if (zoneWorkers.isEmpty()) {
        // ERROR: NO WORKERS
        zoneSimResult.setErrorMessage(
            "102");
        return zoneSimResult;
      }

      //System.out.println("Zone " + zone.getId() + " has " + zoneWorkers.size() + " workers");

      // Get the first start time for the zone for a worker that is available


      LocalDateTime newTime = timetableService.getFirstStartTimeByZoneAndDay(
          zone.getId(), startTime);

      if (activeTasks != null && !activeTasks.isEmpty()) {
        // Get the earliest opportunity for the active tasks
        LocalDateTime earliestOpportunity = getEarliestOpportunity(activeTasks);
        if (earliestOpportunity != null) {
          newTime = earliestOpportunity;
        }
      }
      // If the new time is start of day, it means no workers are scheduled to work that day
      if (Objects.equals(newTime, startTime.toLocalDate().atStartOfDay())) {
        // ERROR: NO WORKERS COMING TO WORK TODAY
        zoneSimResult.setErrorMessage(
            "103");
        return zoneSimResult;
      }
      // If the new time is before the last time, set the last time to the new time
      LocalDateTime finalNewTime = newTime;
      this.lastTime.updateAndGet(current -> finalNewTime.isAfter(startTime) ? finalNewTime : startTime);
      ExecutorService zoneExecutor = Executors.newFixedThreadPool(zoneWorkers.size());
      // Latch for the tasks in the zone ensuring that all tasks are completed before the simulation ends
      WorkerSemaphore2 availableZoneWorkersSemaphore = new WorkerSemaphore2(timetableService);
      availableZoneWorkersSemaphore.initialize(zoneWorkers, this.lastTime.get());
      //Countdown latch based on zone tasks
      CountDownLatch zoneLatch;
      // Iterate over the tasks in the zone
      if (activeTasks != null && !activeTasks.isEmpty()) {
        zoneLatch = new CountDownLatch(activeTasks.size());
        // Filter and sort the active tasks based on the number of workers and due date
        activeTasks = filterAndSortActiveTasks(activeTasks);
        for (ActiveTask activeTask : activeTasks) {
          // Simulate the task
          String result = simulateTask(activeTask,
              availableZoneWorkersSemaphore, zoneExecutor, zoneLatch,
              zoneSimResult);
          if (!result.isEmpty()) {
            zoneSimResult.setErrorMessage(result);
            return zoneSimResult;
          }
        }
      } else {
        zoneLatch = new CountDownLatch(pickerTasks.size());
        pickerTasks = filterAndSortPickerTasks(pickerTasks);
        for (PickerTask pickerTask : pickerTasks) {
          // Simulate the picker task
          String result = simulatePickerTask(pickerTask,
              availableZoneWorkersSemaphore, zoneExecutor, zoneLatch,
              randomForest, zoneSimResult);
          if (!result.isEmpty()) {
            zoneSimResult.setErrorMessage(result);
            return zoneSimResult;
          }

        }
      }
      // Wait for all tasks to complete
      zoneLatch.await();
      System.out.println("Zone " + zone.getId() + " simulation completed at " + this.lastTime.get());
      zoneExecutor.shutdown();
      if (!zoneExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
        zoneSimResult.setErrorMessage("100");
        zoneExecutor.shutdownNow();
        return zoneSimResult;
      }
      return zoneSimResult;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return zoneSimResult;
  }

  private Set<PickerTask> filterAndSortPickerTasks(Set<PickerTask> pickerTasks) {
    return pickerTasks.stream()
        // Filter out tasks that have an endTime
        .filter(task -> task.getEndTime() == null)
        // Sort tasks by whether they have workers
        .sorted((task1, task2) -> Boolean.compare(
            task2.getWorker() != null,
            task1.getWorker() != null
        ))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * Simulates a picker task in the zone on its own thread
   *
   * @param pickerTask                    The picker task to simulate
   * @param availableZoneWorkersSemaphore The common resource for the workers
   * @param zoneExecutor                  The executor service for the zone
   * @param zoneLatch                     The countdown latch for the zone (ensures all tasks are completed)
   * @param randomForest                  The random forest model to use for task duration calculation
   * @param zoneSimResult                 The zone simulation result object
   * @return Any error messages that may occur
   */
  private String simulatePickerTask(PickerTask pickerTask,
                                    WorkerSemaphore2 availableZoneWorkersSemaphore,
                                    ExecutorService zoneExecutor, CountDownLatch zoneLatch,
                                    RandomForest randomForest, ZoneSimResult zoneSimResult) {
    zoneExecutor.submit(() -> {
      try {
        // Acquire the workers for the task from the semaphore
        while (pickerTask.getWorker() == null) {
          String result = availableZoneWorkersSemaphore.acquireMultiple(null, pickerTask, lastTime,
              pickerTask.getZone().getId());
          // if this, then task will not complete
          if (!result.isEmpty()) {
            zoneSimResult.setErrorMessage(result);
            return;
          }
        }
        // Set start time of task to the last time
        LocalDateTime startTime = this.lastTime.get();
        // Simulate the task duration using the model (divided by 60 to get minutes)
        int taskDuration = (int) (mlModel.estimateTimeUsingModel(randomForest, pickerTask, pickerTask.getWorker().getId())) / 60;
        // Sleep for the task duration
        TimeUnit.MILLISECONDS.sleep(taskDuration);
        // Set picker task attributes
        pickerTask.setStartTime(startTime);
        pickerTask.setEndTime(startTime.plusMinutes(taskDuration));
        // Set the last time to the end time of the task
        // Set the last time to the end time of the task
        synchronized (this.lastTime) {
          this.lastTime.updateAndGet(current ->
              startTime.plusMinutes(taskDuration).isAfter(current) ?
                  startTime.plusMinutes(taskDuration) : current
          );
        }
        // Add the task to the simulation result
        zoneSimResult.addTask(null, pickerTask);
        // Release the workers back to the semaphore
        availableZoneWorkersSemaphore.release(pickerTask.getWorker());
      } catch (InterruptedException | IOException e) {
        Thread.currentThread().interrupt();
      } finally {
        // Decrement the countdown latch for the zone
        zoneLatch.countDown();
      }
    });
    return "";
  }

  /**
   * Simulates an active task in the zone on its own thread.
   *
   * @param activeTask                    The active task to simulate
   * @param availableZoneWorkersSemaphore The common resource for the workers
   * @param zoneExecutor                  The executor service for the zone
   * @param zoneLatch                     The countdown latch for the zone (ensures all tasks are completed)
   * @param zoneSimResult                 The zone simulation result object
   * @return Any error messages that may occur
   */
  private String simulateTask(ActiveTask activeTask,
                              WorkerSemaphore2 availableZoneWorkersSemaphore,
                              ExecutorService zoneExecutor,
                              CountDownLatch zoneLatch,
                              ZoneSimResult zoneSimResult
  ) {
    zoneExecutor.submit(() -> {
      try {
        // Acquire the workers for the task
        while (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
          String acquireWorkerError =
              availableZoneWorkersSemaphore.acquireMultiple(activeTask, null, lastTime,
                  activeTask.getTask().getZoneId());
          // if this, then task will not complete
          if (!acquireWorkerError.isEmpty()) {
            zoneSimResult.setErrorMessage(acquireWorkerError);
            return;
          }
          this.getEarliestOpportunity(activeTasks);
         // System.out.println("trying to acquire " + activeTask.getTask().getMinWorkers() + " workers for task " + activeTask.getId() + "semaphore " + availableZoneWorkersSemaphore.getWorkers() + "at time " + this.lastTime.get());
        }
        // Set start time of task to the last time

        LocalDateTime startTime = this.lastTime.get();
        int sleepTime = calculateSleepTime(activeTask);
        // Simulate the task duration
        TimeUnit.MILLISECONDS.sleep(sleepTime);
        // Set the task attributes
        activeTask.setStartTime(startTime);
        activeTask.setEndTime(activeTask.getStartTime().plusMinutes(sleepTime));
        //System.out.println( "Task " + activeTask.getId() + " Started at " + activeTask.getStartTime()+" completed at time " + activeTask.getEndTime());
        // Set the last time to the end time of the task
        synchronized (this.lastTime) {
          this.lastTime.updateAndGet(current ->
              startTime.plusMinutes(sleepTime).isAfter(current) ? startTime.plusMinutes(sleepTime) :
                  current
          );
        }
        // Add the task to the simulation result
        zoneSimResult.addTask(activeTask, null);
        // Release the workers back to the semaphore
        availableZoneWorkersSemaphore.releaseAll(activeTask.getWorkers());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        // Decrement the countdown latch for the zone
        zoneLatch.countDown();
      }
    });
    return "";
  }

  /**
   * Filters and sorts the active tasks based on the number of workers and due date.
   * Ensures that tasks with workers already on it are prioritized so that the workers are released sooner.
   * This is done due to thread amount restrictions.
   * Also sorts the tasks by due date ensuring tasks that are prioritized are completed first.
   *
   * @param activeTasks The list of active tasks to filter and sort
   * @return The filtered and sorted list of active tasks
   */
  private static List<ActiveTask> filterAndSortActiveTasks(List<ActiveTask> activeTasks) {
    return activeTasks.stream()
        // Filter out tasks that have an endTime
        .filter(task -> task.getEndTime() == null)
        // Sort tasks by workers first, then by nearest dueDate
        .sorted((task1, task2) -> {
          // Prioritize tasks with workers
          int workersComparison = Boolean.compare(
              task2.getWorkers().size() > 0,
              task1.getWorkers().size() > 0
          );
          if (workersComparison != 0) {
            return workersComparison;
          }
          // If both tasks have the same worker condition, sort by dueDate
          LocalDateTime dueDate1 = task1.getDueDate();
          LocalDateTime dueDate2 = task2.getDueDate();
          if (dueDate1 != null && dueDate2 != null) {
            return dueDate1.compareTo(dueDate2);
          }
          return 0;
        })
        .collect(Collectors.toList());
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

    if (maxWorkers <= minWorkers) {
      maxWorkers = minWorkers + 1;
    }
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


  private LocalDateTime getEarliestOpportunity(List<ActiveTask> activeTasks) {
      int fewest = activeTasks.stream()
          .filter(task -> task.getEndTime() == null) // Exclude finished tasks
          .min(Comparator.comparingInt(
              task -> task.getTask().getMinWorkers())) // Find the task with the fewest minWorkers
          .map(task -> task.getTask().getMinWorkers()) // Extract the minWorkers value
          .orElse(0);

      if (fewest == 0) {
          return null;
      }

      // Filter tasks to include only those with exactly `fewest` minWorkers
      List<ActiveTask> filteredTasks = activeTasks.stream()
          .filter(task -> task.getTask().getMinWorkers() == fewest)
          .toList();

      LocalDateTime earliestTime = this.timetableService.getEarliestTimeWithMinWorkers(
          fewest, activeTasks.getFirst().getTask().getZoneId(), this.lastTime.get(),filteredTasks);

      if (earliestTime != null && !this.lastTime.get().isAfter(earliestTime)) {
          this.lastTime.updateAndGet(current ->
              current.isBefore(earliestTime) ? earliestTime : current
          );
          return earliestTime;
      }
      return null;
  }
}

