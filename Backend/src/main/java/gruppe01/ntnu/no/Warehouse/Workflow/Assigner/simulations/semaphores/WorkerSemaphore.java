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
   * @return An empty string if successful, an error message if not
   * @throws InterruptedException
   */
  public String acquireMultiple(ActiveTask activeTask)
      throws InterruptedException {
    lock.lock();
    try {
      synchronized (workers) {
        // acquire the required number of workers, if not enough workers are available, wait
        int requiredWorkers = activeTask.getTask().getMinWorkers() - activeTask.getWorkers().size();
        semaphore.acquire(requiredWorkers);
        // acquired the workers
        // while the number of workers acquired is less than the required number of workers
        List<Worker> workersToRemove = new ArrayList<>();
        // Iterate over the workers
        for (Worker worker : workers) {
          if (worker.getLicenses().containsAll(activeTask.getTask().getRequiredLicense())) {
            workersToRemove.add(worker);
            if (workersToRemove.size() >= activeTask.getTask().getMinWorkers()) {
              activeTask.getWorkers().addAll(workersToRemove);
              workersToRemove.forEach(workers::remove);
              return "";
            }
          }
        }
        // if the number of workers acquired is less than the required number of workers
        semaphore.release(requiredWorkers);

        // TODO: ADD error checking in case the zone does nt have the qualified workers to avoid infinite loop of searching for workers
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
}