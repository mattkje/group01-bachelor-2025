package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.semaphores;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.springframework.stereotype.Component;

/**
 * A semaphore that controls access to a list of workers.
 * Controls the access to a common resource: Workers
 * Allows for checking for licenses and acquiring workers with the required licenses
 */

@Component
public class WorkerSemaphore2 {
    private static final Logger logger = Logger.getLogger(WorkerSemaphore2.class.getName());

    private final TimetableService timetableService;

    // Set of workers that are available
    private Set<Worker> workers;
    private final ReentrantLock lock = new ReentrantLock();

    // Static block to configure the logger
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

    /**
     * WorkerSemaphore constructor keeping accounts of the workers
     *
     * @param timetableService service for retrieving workers timetables
     */
    public WorkerSemaphore2(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    /**
     * Initialize the WorkerSemaphore with a set of workers and a start time.
     *
     * @param workersSet set of workers to be initialized (Zone workers)
     * @param startTime  Current Time of the simulation / real-time-
     */
    public void initialize(Set<Worker> workersSet, LocalDateTime startTime) {
        // Get workers that are working that day
        this.workers = this.timetableService.getWorkersWorkingByDayAndZone(startTime, workersSet.iterator().next().getZone());
        logger.info("Initializing WorkerSemaphore with " + workers.size() + " workers.");

        // Remove workers that are busy at the start time
        List<Worker> workersToRemove = new ArrayList<>();
        // Iterate over the workers and check if they are busy
        for (Worker worker : workers) {
            // Check if the worker is busy with an active task or picker task
            if (worker.getCurrentPickerTask() != null) {
                // if the worker is busy with a picker task that has started that day and has no end time
                if ((worker.getCurrentPickerTask().getStartTime().toLocalDate().equals(startTime.toLocalDate()) && worker.getCurrentPickerTask().getEndTime() == null) || !worker.isAvailability()) {
                    workersToRemove.add(worker);
                    logger.warning("Worker " + worker.getId() + " removed due to being busy. at Zone: " + worker.getZone());
                }
            } else if (worker.getCurrentActiveTask() != null) {
                // if the worker is busy with an active task that has started that day and has no end time
                if ((worker.getCurrentActiveTask().getStartTime().toLocalDate().equals(startTime.toLocalDate()) && worker.getCurrentActiveTask().getEndTime() == null) || !worker.isAvailability()) {
                    workersToRemove.add(worker);
                    logger.warning("Worker " + worker.getId() + " removed due to being busy. at Zone: " + worker.getZone());
                }
            }
        }

        // Remove workers after iteration
        workersToRemove.forEach(workers::remove);
    }

    public String acquireMultiple(ActiveTask activeTask, PickerTask pickerTask, AtomicReference<LocalDateTime> startTime, Long zoneId)
            throws InterruptedException {
        // Acquire the lock to ensure thread safety
        lock.lock();
        try {
            logger.info("Attempting to acquire workers for task.  " +
                    "ActiveTask: " + (activeTask != null ? activeTask.getId() : "null") +
                    ", PickerTask: " + (pickerTask != null ? pickerTask.getId() : "null") + " at Zone: " + zoneId);
            if (timetableService.isEveryoneFinishedWorking(zoneId, startTime.get())) {
                logger.warning("All workers are finished working at " + startTime.get() + ". Unable to complete ActiveTask: " + (activeTask != null ? activeTask.getId() : "null") +
                        ", PickerTask: " + (pickerTask != null ? pickerTask.getId() : "null") + " at Zone: " + zoneId);
                // ERROR: WORKERS HAVE GONE HOME, TASK CAN NOT COMPLETE
                return zoneId +  ",104," + (activeTask != null ? activeTask.getId() : "null") +
                        "," + (pickerTask != null ? pickerTask.getId() : "null");
            }

            synchronized (workers) {
                // Check if the workers are available
                List<Worker> workersToRemove = new ArrayList<>();
                List<Worker> workerList = new ArrayList<>(workers);
                Collections.shuffle(workerList);

                if (activeTask != null) {
                    if (!timetableService.isAnyoneQualifiedWorkingToday(zoneId, startTime.get(), activeTask)) {
                        // ERROR: NO QUALIFIED WORKERS AT ZONE FOR TASK
                        return  zoneId + ",105," + activeTask.getId();
                    }
                    workerList.removeIf(worker -> !worker.getLicenses().containsAll(activeTask.getTask().getRequiredLicense()));
                    // No qualified workers available currently
                    if (workerList.isEmpty()) {
                        return "";
                    }
                    for (Worker worker : workerList) {
                        if (timetableService.workerIsWorking(startTime.get(), worker.getId())) {
                            workersToRemove.add(worker);
                            if (workersToRemove.size() == activeTask.getTask().getMaxWorkers()) {
                                activeTask.addMultilpleWorkers(workersToRemove);
                                workersToRemove.forEach(workers::remove);
                                logger.info("Acquired workers for ActiveTask: " + activeTask.getId() + "at Zone: " + zoneId);
                                return "";
                            }
                        }
                        logger.info("Starting time: " + startTime.get() + " Worker: " + worker.getName() + " Worker ID: " + worker.getId());
                        logger.info("Worker " + worker.getName() + "is busy? " + timetableService.workerIsWorking(startTime.get(), worker.getId()));
                        if (workersToRemove.size() >= activeTask.getTask().getMinWorkers()) {
                            activeTask.addMultilpleWorkers(workersToRemove);
                            workersToRemove.forEach(workers::remove);
                            logger.info("Acquired minimum workers for ActiveTask: " + activeTask.getId() + "at Zone: " + zoneId);
                            return "";
                        }

                    }
                } else {
                    for (Worker worker : workerList) {
                        if (timetableService.workerIsWorking(startTime.get(), worker.getId())) {
                            workersToRemove.add(worker);
                            assert pickerTask != null;
                            if (pickerTask.getWorker() == null) {
                                pickerTask.setWorker(worker);
                                workersToRemove.forEach(workers::remove);
                                logger.info("Acquired worker for PickerTask: " + pickerTask.getId() + "at Zone: " + zoneId);
                                return "";
                            }
                        } else {
                            if (timetableService.workerHasFinishedShift(worker.getId(), startTime.get())) {
                                workers.remove(worker);
                            }
                        }
                    }
                }
                logger.warning("Failed to acquire workers for task. Workers at work right now: " + workerList.size() + " at zone: " + zoneId);
                return "";
            }
        } finally {
            lock.unlock();
        }
    }

    public void release(Worker worker) {
        synchronized (workers) {
            workers.add(worker);
            logger.info("Released worker: " + worker.getId() + " at Zone: " + worker.getZone());
        }
    }

    public void releaseAll(List<Worker> allWorkers) {
        synchronized (workers) {
            workers.addAll(allWorkers);
            logger.info("Released all workers: " + allWorkers.size() + " Worker size: " + workers.size() + " at Zone: " + allWorkers.get(0).getZone());
        }
    }
}