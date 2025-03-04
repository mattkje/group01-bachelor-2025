package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A semaphore that controls access to a list of workers.
 * Controls the access to a common resource: Workers
 * Allows for checking for licenses and acquiring workers with the required licenses
 */

public class WorkerSemaphore {
  private final Set<Worker> workers;
  private final Semaphore semaphore;
  private final ReentrantLock lock = new ReentrantLock();

  public WorkerSemaphore(Set<Worker> workers) {
    this.workers = workers;
    this.semaphore = new Semaphore(workers.size());
  }

  /**
   * Acquires workers without checking for licenses.
   * Will be redundant later on, but is used for testing purposes.
   *
   * @param activeTask The task to acquire workers for
   * @param latch      The latch to count down when workers are acquired
   * @return An empty string if successful, an error message if not
   * @throws InterruptedException
   */
  public String acquireMultipleNoLicense(ActiveTask activeTask, CountDownLatch latch,int simNo)
      throws InterruptedException {
    lock.lock();
    try {
      synchronized (workers) {
        System.out.println("Task " + activeTask.getId() + " is trying to get: " +
            activeTask.getTask().getMinWorkers() + " from a pool of " + workers.size() +
            " with a semaphore pool of " + semaphore.availablePermits() + " belonging to zone " +
            activeTask.getTask().getZoneId() + " in simulation " + simNo);
        // acquire the required number of workers, if not enough workers are available, wait
        int requiredWorkers = activeTask.getTask().getMinWorkers() - activeTask.getWorkers().size();
        semaphore.acquire(requiredWorkers);
        System.out.println("Task " + activeTask.getId() + " acquired " + requiredWorkers +
            " workers. With a pool of " + workers.size() + " simulation " + simNo);
        // acquired the workers
        // while the number of workers acquired is less than the required number of workers
        List<Worker> workersToRemove = new ArrayList<>();
        // Iterate over the workers
        for (Worker worker : workers) {
          workersToRemove.add(worker);
          if (workersToRemove.size() >= activeTask.getTask().getMinWorkers()) {
            activeTask.getWorkers().addAll(workersToRemove);
            workersToRemove.forEach(workers::remove);
            latch.countDown();
            System.out.println("task: " +activeTask.getId() + " acquired " + activeTask.getWorkers().size() + " simNo: " + simNo);
            return "";
          }
        }
        // if the number of workers acquired is less than the required number of workers
        semaphore.release(requiredWorkers);
      }
    } finally {
      lock.unlock();
    }
    return "";
  }

  public void releaseAll(List<Worker> allWorkers) {
    workers.addAll(allWorkers);
    semaphore.release(allWorkers.size());
  }
}