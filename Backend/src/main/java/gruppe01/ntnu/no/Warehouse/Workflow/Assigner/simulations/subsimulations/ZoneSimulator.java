package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores.WorkerSemaphore;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

  public static String runZoneSimulation(Zone zone, List<ActiveTask> zoneTasks,
                                         Set<PickerTask> pickerTasks,
                                         AtomicDouble totalTaskTime, int simNo) {
    try {

      if (zoneTasks.isEmpty() && pickerTasks.isEmpty()) {
        return "ERROR: Zone " + zone.getId() +
            " No tasks";
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

      new CountDownLatch(1);
      CountDownLatch zoneLatch;

      // Iterate over the tasks in the zone
      if (!zoneTasks.isEmpty()) {
        zoneLatch = new CountDownLatch(zoneTasks.size());
        for (ActiveTask activeTask : zoneTasks) {
          String result = simulateTask(activeTask, zone,
              availableZoneWorkersSemaphore, zoneExecutor, zoneLatch,
              isSimulationSuccessful, errorMessages, zoneTaskTime, simNo);
          if (!result.isEmpty()) {
            return result;
          }
        }
      } else {
        Map<List<Double>, List<List<Double>>> mlData = mlModel.getMcValues(zone.getName());
        zoneLatch = new CountDownLatch(pickerTasks.size());
        for (PickerTask pickerTask : pickerTasks) {
          String result = simulatePickerTask(pickerTask,
              availableZoneWorkersSemaphore, zoneExecutor, zoneLatch,
              isSimulationSuccessful, errorMessages, zoneTaskTime, simNo, mlData);
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return "Zone " + zone.getId() + " simulation failed";
  }

  private static String simulatePickerTask(PickerTask pickerTask,
                                           WorkerSemaphore availableZoneWorkersSemaphore,
                                           ExecutorService zoneExecutor, CountDownLatch zoneLatch,
                                           AtomicBoolean isSimulationSuccessful,
                                           List<String> errorMessages, AtomicDouble zoneTaskTime,
                                           int simNo,
                                           Map<List<Double>, List<List<Double>>> mlData)
      throws IOException, URISyntaxException {
    // Gets a random duration for the task from the ML model

    int taskDuration = getPickerTaskDuration(pickerTask);

    // Start a thread for a single task in a zone
    zoneExecutor.submit(() -> {
      try {
        // Acquire the workers for the task
        while (pickerTask.getWorker() == null) {

          String acquireWorkerError = availableZoneWorkersSemaphore.acquireMultiple(null, pickerTask, simNo);
          if (!acquireWorkerError.isEmpty()) {
            errorMessages.add(acquireWorkerError);
            isSimulationSuccessful.set(false);
            return;
          }
        }
        // Simulate the task duration
        // TODO: Find a quicker way of doing this so that the simulation runs faster
        TimeUnit.MILLISECONDS.sleep(taskDuration);
        // Release the workers when the task is finished
        availableZoneWorkersSemaphore.release(pickerTask.getWorker());
        // Add the task duration to the total task time
        zoneTaskTime.addAndGet(taskDuration);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        zoneLatch.countDown();
      }
    });
    return "";

  }

  /**
   * Gets the duration of a picker task using ranges given by the ML Model
   * Each parameter in a picker task has a set weight attribute which helps calculate
   * the potential time it takes to complete the task
   * Each weight has a range given by the ML that represents the 5th and 95th percentile.
   * @param pickerTask The picker task to get the duration for
   * @return time in minutes (simulated in milliseconds)
   */
  private static int getPickerTaskDuration(PickerTask pickerTask)
      throws IOException, URISyntaxException {
    List<Double> weights = mlModel.getMcWeights(pickerTask.getZone().getName().toUpperCase());

    return (int) ((int) pickerTask.getDistance() * weights.get(0) + pickerTask.getPackAmount() *weights.get(1) +
        pickerTask.getLinesAmount() * weights.get(2) + pickerTask.getWeight() * weights.get(3) +
        pickerTask.getVolume() *weights.get(4) + pickerTask.getAvgHeight() * weights.get(5));

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
                                     int simNo) {
    // Gets a random duration for the task
    // TODO: Replace this with a machine learning model for typical taskduration
    int taskDuration =
        random.nextInt(activeTask.getTask().getMaxTime() - activeTask.getTask().getMinTime()) +
            activeTask.getTask().getMinTime();

    // Set a random EPW for the task (Efficiency gained per worker)
    // Currently at  a range between 0.4 and 0.9
    // TODO: Replace this with a ML learned number
    double epw = random.nextDouble() * 0.5 + 0.4;


    long workersWithRequiredLicenses = zone.getWorkers().stream()
        .filter(worker -> worker.getLicenses()
            .containsAll(activeTask.getTask().getRequiredLicense()))
        .count();

    if (workersWithRequiredLicenses < activeTask.getTask().getMinWorkers()) {
      isSimulationSuccessful.set(false);
      zoneLatch.countDown();
      return "ERROR: ZONE " + zone.getId() +
          " - " + activeTask.getTask().getName() + "!Missing workers with required licenses";
    }

    // Start a thread for a single task in a zone
    zoneExecutor.submit(() -> {
      try {
        // Acquire the workers for the task
        while (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
          String acquireWorkerError =
              availableZoneWorkersSemaphore.acquireMultiple(activeTask,null, simNo);
          if (!acquireWorkerError.isEmpty()) {
            errorMessages.add(acquireWorkerError);
            isSimulationSuccessful.set(false);
            return;
          }
        }
        // Simulate the task duration
        // TODO: Find a quicker way of doing this so that the simulation runs faster
        TimeUnit.MILLISECONDS.sleep(calculateSleepTime(activeTask));
        // Release the workers when the task is finished
        availableZoneWorkersSemaphore.releaseAll(activeTask.getWorkers());
        // Add the task duration to the total task time
        zoneTaskTime.addAndGet(taskDuration);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        zoneLatch.countDown();
      }
    });
    return "";
  }
}

