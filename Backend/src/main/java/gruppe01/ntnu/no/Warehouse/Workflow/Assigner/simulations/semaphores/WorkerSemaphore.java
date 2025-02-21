package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;

/**
 * A semaphore that controls access to a list of workers.
 * Controls the access to a common resource: Workers
 * Allows for checking for licenses and acquiring workers with the required licenses
 */

public class WorkerSemaphore {
    private final Set<Worker> workers;
    private final Semaphore semaphore;

    public WorkerSemaphore(Set<Worker> workers) {
        this.workers = workers;
        this.semaphore = new Semaphore(workers.size());
    }

    public Worker acquire(ActiveTask activeTask) throws InterruptedException {
        semaphore.acquire();
        synchronized (workers) {
            // System.out.println("Acquiring worker with license: " + requiredLicense);
            // If no license is required, return the first worker
            if (activeTask.getTask().getRequiredLicense().isEmpty()) {
                for (Worker worker : workers) {
                    if (worker.getLicenses().isEmpty()) {
                        workers.remove(worker);
                        // System.out.println("Acquired worker with no license");
                        return worker;
                    }
                }
                // If no worker with no license is found, return the first worker
                Worker worker = workers.iterator().next();
                workers.remove(worker);
                // System.out.println("Acquired worker with license: " + requiredLicense);
                return worker;
            }// If a license is required, return the first worker with the required license
            else {
                for (Worker worker : workers) {
                    if (worker.hasAllLicenses(activeTask.getTask().getRequiredLicense())) {
                        workers.remove(worker);
                        return worker;
                    }
                }
            }
        }
        semaphore.release();
        return null; // No worker with the required license found
    }

public void acquireNoLicense(ActiveTask activeTask) throws InterruptedException {
    semaphore.acquire();
    synchronized (workers) {
        if (!workers.isEmpty()) {
            Worker worker = workers.iterator().next();
            workers.remove(worker);
            activeTask.getWorkers().add(worker);
        }
    }
    semaphore.release();
}

public void acquireMultiple(ActiveTask activeTask, CountDownLatch latch) throws InterruptedException {
    semaphore.acquire(activeTask.getTask().getMinWorkers() - activeTask.getWorkers().size());
    synchronized (workers) {
        while (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
            for (Worker worker : workers) {
                if (worker.hasAllLicenses(activeTask.getTask().getRequiredLicense())) {
                    activeTask.getWorkers().add(worker);
                    workers.remove(worker);
                    if (activeTask.getWorkers().size() == activeTask.getTask().getMinWorkers()) {
                        latch.countDown(); // Count down the latch
                        break;
                    }
                }
            }
            if (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
                workers.wait();
            }
        }
    }
    semaphore.release(activeTask.getTask().getMinWorkers() - activeTask.getWorkers().size());
}

public void acquireMultipleNoLicense(ActiveTask activeTask, CountDownLatch latch) throws InterruptedException {
    semaphore.acquire(activeTask.getTask().getMinWorkers() - activeTask.getWorkers().size());
    synchronized (workers) {
        while (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
            List<Worker> acquiredWorkers = new ArrayList<>();
            for (Worker worker : workers) {
                if (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
                    acquiredWorkers.add(worker);
                    activeTask.getWorkers().add(worker);
                } else {
                    break;
                }
            }
            workers.removeAll(acquiredWorkers);

            if (activeTask.getWorkers().size() < activeTask.getTask().getMinWorkers()) {
                workers.wait();
            }
        }
        latch.countDown();
    }
    semaphore.release(activeTask.getTask().getMinWorkers() - activeTask.getWorkers().size());
}

public void release(Worker worker) {
    synchronized (workers) {
        workers.add(worker);
        workers.notifyAll();
    }
    semaphore.release();
}

public void releaseAll(List<Worker> allWorkers) {
    synchronized (workers) {
        workers.addAll(allWorkers);
        workers.notifyAll();
    }
    semaphore.release(allWorkers.size());
}
}