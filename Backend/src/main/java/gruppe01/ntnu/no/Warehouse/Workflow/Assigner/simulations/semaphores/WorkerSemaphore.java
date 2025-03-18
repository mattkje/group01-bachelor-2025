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
  public String acquireMultiple(ActiveTask activeTask, CountDownLatch latch)
      throws InterruptedException {
    lock.lock();
    try {
      synchronized (workers) {
        // acquire the required number of workers, if not enough workers are available, wait
        int requiredWorkers = activeTask.getTask().getMinWorkers() - activeTask.getWorkers().size();
        semaphore.acquire(requiredWorkers);
        System.out.println("Acquired workers for task " + activeTask.getId());
        // acquired the workers
        // while the number of workers acquired is less than the required number of workers
        List<Worker> workersToRemove = new ArrayList<>();
        // Iterate over the workers
        for (Worker worker : workers) {
          System.out.println("Checking worker " + worker.getName());
          if (worker.getLicenses().containsAll(activeTask.getTask().getRequiredLicense())) {
            workersToRemove.add(worker);
            if (workersToRemove.size() >= activeTask.getTask().getMinWorkers()) {
              System.out.println("Task able to find workers");
              activeTask.getWorkers().addAll(workersToRemove);
              workersToRemove.forEach(workers::remove);
              latch.countDown();
              return "";
            }
          }
        }
        System.out.println("task unable to find workers");
        // if the number of workers acquired is less than the required number of workers
        semaphore.release(requiredWorkers);

        if (workersToRemove.isEmpty()){
          latch.countDown();
          return "No workers with the correct qualifications for task " + activeTask.getTask().getId();
        } else {
          latch.countDown();
          return "Not enough workers with the correct qualifications for task " + activeTask.getTask().getId();
        }
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