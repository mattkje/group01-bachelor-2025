package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * A semaphore that controls access to a list of workers.
 * Controls the access to a common resource: Workers
 * Allows for checking for licenses and acquiring workers with the required licenses
 */

public class WorkerSemaphore {
    private final List<Worker> workers;
    private final Semaphore semaphore;

    public WorkerSemaphore(List<Worker> workers) {
        this.workers = workers;
        this.semaphore = new Semaphore(workers.size());
    }

    public Worker acquire(String requiredLicense) throws InterruptedException {
        semaphore.acquire();
        synchronized (workers) {
            // If no license is required, return the first worker
            if (requiredLicense == "" || requiredLicense == null) {
                Worker worker = workers.get(0);
                workers.remove(worker);
                return worker;
            } // If a license is required, return the first worker with the required license
            else {
                for (Worker worker : workers) {
                    if (worker.hasLicense(requiredLicense)) {
                        workers.remove(worker);
                        return worker;
                    }
                }
            }
        }
        semaphore.release();
        return null; // No worker with the required license found
    }

    public void release(Worker worker) {
        synchronized (workers) {
            workers.add(worker);
        }
        semaphore.release();
    }
}