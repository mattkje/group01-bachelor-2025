package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

/**
 * A semaphore that controls access to a list of workers.
 * Controls the access to a common resource: Workers
 * Allows for checking for licenses and acquiring workers with the required licenses
 */

@Component
public class WorkerSemaphore2 {
    private static final Logger logger = Logger.getLogger(WorkerSemaphore2.class.getName());

    @Autowired
    private TimetableService timetableService;

    private Set<Worker> workers;
    private Semaphore semaphore;
    private final ReentrantLock lock = new ReentrantLock();

    static {
        try {
            // Ensure the logs directory exists
            java.nio.file.Path logDir = java.nio.file.Paths.get("logs");
            if (!java.nio.file.Files.exists(logDir)) {
                java.nio.file.Files.createDirectories(logDir);
            }

            // Configure the logger to write to a file
            FileHandler fileHandler = new FileHandler("logs/WorkerSemaphore2.log", false);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Disable console logging
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


  public void initialize(Set<Worker> workersSet, LocalDateTime startTime) {
        this.workers = this.timetableService.getWorkersWorkingByDayAndZone(startTime, workersSet.iterator().next().getZone());
        this.semaphore = new Semaphore(workers.size());
        logger.info("Initializing WorkerSemaphore with " + workers.size() + " workers.");

        for (Worker worker : workers) {
            if (worker.getCurrentTaskId() != null &&
                    (worker.getCurrentActiveTask().getEndTime() == null &&
                            worker.getCurrentPickerTask().getEndTime() == null) &&
                    (worker.getCurrentActiveTask().getDate().isEqual(LocalDate.now())) &&
                    worker.getCurrentPickerTask().getDate().isEqual(startTime.toLocalDate())) {
                workers.remove(worker);
                semaphore.release();
                logger.warning("Worker " + worker.getId() + " removed due to being busy.");
            }
        }
    }

    public String acquireMultiple(ActiveTask activeTask, PickerTask pickerTask, AtomicReference<LocalDateTime> startTime)
            throws InterruptedException {
        lock.lock();
        try {
            logger.info("Attempting to acquire workers for task.  " +
                    "ActiveTask: " + (activeTask != null ? activeTask.getId() : "null") +
                    ", PickerTask: " + (pickerTask != null ? pickerTask.getId() : "null"));
            synchronized (workers) {
                int maxWorkers = 1;
                int minWorkers = 1;
                if (activeTask != null) {
                    maxWorkers = activeTask.getTask().getMaxWorkers() - activeTask.getWorkers().size();
                    minWorkers = activeTask.getTask().getMinWorkers() - activeTask.getWorkers().size();
                }

                int currentPermits = 0;

                for (int i = 0; i < maxWorkers; i++) {
                    if (semaphore.tryAcquire(1)) {
                        currentPermits++;
                        if (maxWorkers == currentPermits) {
                            break;
                        }
                    }
                }

                if (currentPermits < minWorkers) {
                    semaphore.release(currentPermits);
                    logger.warning("Not enough permits available. Required: " + minWorkers + ", Acquired: " + currentPermits + "for task " + "ActiveTask: " + (activeTask != null ? activeTask.getId() : "null") +
                            ", PickerTask: " + (pickerTask != null ? pickerTask.getId() : "null"));
                    return "";
                }

                List<Worker> workersToRemove = new ArrayList<>();
                List<Worker> workerList = new ArrayList<>(workers);
                Collections.shuffle(workerList);

                if (activeTask != null) {
                    for (Worker worker : workerList) {
                        if (worker.getLicenses().containsAll(activeTask.getTask().getRequiredLicense()) && timetableService.workerIsWorking(startTime.get(), worker.getId())) {
                            workersToRemove.add(worker);
                            if (workersToRemove.size() == activeTask.getTask().getMaxWorkers()) {
                                activeTask.addMultilpleWorkers(workersToRemove);
                                workersToRemove.forEach(workers::remove);
                                logger.info("Acquired workers for ActiveTask: " + activeTask.getId());
                                return "";
                            }
                        }
                        if (workersToRemove.size() > activeTask.getTask().getMinWorkers()) {
                            activeTask.addMultilpleWorkers(workersToRemove);
                            workersToRemove.forEach(workers::remove);
                            semaphore.release(currentPermits - activeTask.getWorkers().size());
                            logger.info("Acquired minimum workers for ActiveTask: " + activeTask.getId());
                            return "";
                        }
                    }
                } else {
                    for (Worker worker : workerList) {
                        if (timetableService.workerIsWorking(startTime.get(), worker.getId())) {
                            workersToRemove.add(worker);
                            if (pickerTask.getWorker() == null) {
                                pickerTask.setWorker(worker);
                                workersToRemove.forEach(workers::remove);
                                logger.info("Acquired worker for PickerTask: " + pickerTask.getId());
                                return "";
                            }
                        }
                    }
                }

                semaphore.release(currentPermits);
                logger.warning("Failed to acquire workers for task.");
                return "";
            }
        } finally {
            lock.unlock();
        }
    }

    public void release(Worker worker) {
           synchronized (workers) {
               workers.add(worker);
               semaphore.release();
               logger.info("Released worker: " + worker.getId());
           }
       }

       public void releaseAll(List<Worker> allWorkers) {
           synchronized (workers) {
               workers.addAll(allWorkers);
               semaphore.release(allWorkers.size());
               logger.info("Released all workers: " + allWorkers.size() + " Semaphore size: " + semaphore.availablePermits());
           }
       }

    public int getAvailablePermits() {
        int permits = semaphore.availablePermits();
        logger.info("Available permits: " + permits);
        return permits;
    }
}