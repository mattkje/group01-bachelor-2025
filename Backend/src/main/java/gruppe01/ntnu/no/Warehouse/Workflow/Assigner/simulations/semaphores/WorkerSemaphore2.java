package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A semaphore that controls access to a list of workers.
 * Controls the access to a common resource: Workers
 * Allows for checking for licenses and acquiring workers with the required licenses
 */

public class WorkerSemaphore2 {
  private final Set<Worker> workers;
  private final Semaphore semaphore;
  private final ReentrantLock lock = new ReentrantLock();

  public WorkerSemaphore2(Set<Worker> workers) {
    this.workers = workers;
    this.semaphore = new Semaphore(workers.size());



    // Upon creation of the semaphore, release all busy workers
    //TODO: Once world simulation is up change the localDate based on the simulated time
    for (Worker worker : workers) {
      if (worker.getCurrentTaskId() != null &&
          (worker.getCurrentActiveTask().getEndTime() == null &&
              worker.getCurrentPickerTask().getEndTime() == null) &&
          (worker.getCurrentActiveTask().getDate().isEqual(
              LocalDate.now())) &&
          worker.getCurrentPickerTask().getDate().isEqual(LocalDate.now())) {
        workers.remove(worker);
        semaphore.release();
      }
    }
  }

  /**
   * Acquires workers without checking for licenses.
   * Will be redundant later on, but is used for testing purposes.
   *
   * @param activeTask The task to acquire workers for
   * @return An empty string if successful, an error message if not
   * @throws InterruptedException
   */
  public String acquireMultiple(ActiveTask activeTask, PickerTask pickerTask)
      throws InterruptedException {
    lock.lock();
    try {
      synchronized (workers) {
        int maxWorkers = 1;
        int minWorkers = 1;
        if (activeTask != null) {
          // Check if the number of workers required is less than the number of workers available
          maxWorkers = activeTask.getTask().getMaxWorkers() - activeTask.getWorkers().size();
          minWorkers = activeTask.getTask().getMinWorkers() - activeTask.getWorkers().size();
        }

        // Current permits from the semaphore
        int currentPermits = 0;

        // For each worker the task requires attempt to get a permit from the semaphore
        for (int i = 0; i < maxWorkers; i++) {
          // If a permit is acquired, increment the current permits
          if (semaphore.tryAcquire(1)) {
            currentPermits++;
            // If the current permits is equal to the max workers, break the loop
            if (maxWorkers == currentPermits) {
              break;
            }
          }
        }
        // If the current permits is less than the min workers,
        // release the permits and attempt later
        if (currentPermits < minWorkers) {
          semaphore.release(currentPermits);
          return "";
        }
        // acquired the worker permits
        List<Worker> workersToRemove = new ArrayList<>();

        // Randomness element within the MC simulation
        List<Worker> workerList = new ArrayList<>(workers);
        Collections.shuffle(workerList);// Iterate over the workers to check if they have the required licences for the task
        if (activeTask != null) {
          for (Worker worker : workerList) {
            // If the worker has the required licenses, add them to the workers to remove
            if (worker.getLicenses().containsAll(activeTask.getTask().getRequiredLicense())) {
              workersToRemove.add(worker);
              // If the number of workers acquired is equal to the required number of workers, add them to the task
              if (workersToRemove.size() == activeTask.getTask().getMaxWorkers()) {
                activeTask.addMultilpleWorkers(workersToRemove);
                workersToRemove.forEach(workers::remove);
                return "";
              }
            }
            if (workersToRemove.size() > activeTask.getTask().getMinWorkers()) {
              activeTask.addMultilpleWorkers(workersToRemove);
              workersToRemove.forEach(workers::remove);
              semaphore.release(currentPermits - activeTask.getWorkers().size());
              return "";
            }
          }
        } else {
          for (Worker worker : workerList) {
            workersToRemove.add(worker);
            // If the picker task does not have any worker
            if (pickerTask.getWorker() == null) {
              pickerTask.setWorker(worker);
              workersToRemove.forEach(workers::remove);
              return "";
            }
          }
        }

        // if the number of workers acquired is less than the required number of workers, relase the permits
        semaphore.release(currentPermits);
        return "";
      }
    } finally {
      lock.unlock();
    }
  }


  public void releaseAll(List<Worker> allWorkers) {
    workers.addAll(allWorkers);
    semaphore.release(allWorkers.size());
  }

  public void release(Worker worker) {
    workers.add(worker);
    semaphore.release();
  }

  public int getAvailablePermits() {
    return semaphore.availablePermits();
  }
}