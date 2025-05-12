package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.PickerTaskRepository;

import java.time.LocalDate;
import java.util.Set;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing PickerTask entities.
 */
@Service
public class PickerTaskService {

    private final PickerTaskRepository pickerTaskRepository;

    private final WorkerRepository workerRepository;

    private final ZoneRepository zoneRepository;

    /**
     * Constructor for PickerTaskService.
     *
     * @param pickerTaskRepository the repository for PickerTask
     * @param workerRepository     the repository for Worker
     * @param zoneRepository       the repository for Zone
     */
    public PickerTaskService(PickerTaskRepository pickerTaskRepository, WorkerRepository workerRepository, ZoneRepository zoneRepository) {
        this.pickerTaskRepository = pickerTaskRepository;
        this.workerRepository = workerRepository;
        this.zoneRepository = zoneRepository;
    }

    /**
     * Retrieves all PickerTask entities.
     *
     * @return a list of all PickerTask entities
     */
    public List<PickerTask> getAllPickerTasks() {
        return pickerTaskRepository.findAll();
    }

    /**
     * Retrieves a PickerTask entity by its ID.
     *
     * @param id the ID of the PickerTask
     * @return the PickerTask entity, or null if not found
     */
    public PickerTask getPickerTaskById(Long id) {
        return pickerTaskRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves all PickerTask entities for a specific zone ID.
     *
     * @param id the ID of the zone
     * @return a list of PickerTask entities for the specified zone
     */
    public List<PickerTask> getPickerTaskByZoneId(Long id) {
        List<PickerTask> pickerTasks = new ArrayList<>();
        for (PickerTask pickerTask : pickerTaskRepository.findAll()) {
            if (pickerTask.getZone().getId().equals(id)) {
                pickerTasks.add(pickerTask);
            }
        }
        return pickerTasks;
    }

    /**
     * Retrieves all PickerTask entities for today.
     *
     * @return a set of PickerTask entities for today
     */
    public Set<PickerTask> getPickerTasksForToday() {
        List<PickerTask> pickerTasks = new ArrayList<>();
        for (PickerTask pickerTask : pickerTaskRepository.findAll()) {
            if (pickerTask.getDate().isEqual(LocalDate.now())) {
                pickerTasks.add(pickerTask);
            }
        }
        return Set.copyOf(pickerTasks);
    }

    /**
     * Retrieves all PickerTask entities for a specific date.
     *
     * @param date the date to filter by
     * @return an int of PickerTask entities for the specified date
     */
    public int getPickerTasksDoneForTodayInZone(LocalDate date, long zoneId) {
        int count = 0;
        for (PickerTask pickerTask : pickerTaskRepository.findAll()) {
            if (pickerTask.getDate().isEqual(date) && pickerTask.getEndTime() != null && pickerTask.getZone().getId().equals(zoneId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Retrieves all PickerTask entities for a specific date.
     *
     * @param date the date to filter by
     * @return an int of PickerTask entities for the specified date
     */
    public int getItemsPickedByZone(LocalDate date, long zoneId) {
        int count = 0;
        for (PickerTask pickerTask : pickerTaskRepository.findAll()) {
            if (pickerTask.getDate().isEqual(date) && pickerTask.getEndTime() != null && pickerTask.getZone().getId().equals(zoneId)) {
                count += pickerTask.getPackAmount();
            }
        }
        return count;
    }

    /**
     * Saves a PickerTask entity.
     *
     * @param pickerTask the PickerTask entity to save
     */
    public PickerTask savePickerTask(PickerTask pickerTask) {
        if (pickerTask != null) {
            return pickerTaskRepository.save(pickerTask);
        }
        return null;
    }

    /**
     * Assigns a worker to a PickerTask.
     *
     * @param pickerTaskId the ID of the PickerTask
     * @param workerId     the ID of the Worker
     * @return the updated PickerTask entity
     */
    public PickerTask assignWorkerToPickerTask(Long pickerTaskId, Long workerId) {
        PickerTask pickerTask = getPickerTaskById(pickerTaskId);
        Worker worker = workerRepository.findById(workerId).orElse(null);
        if (pickerTask != null && worker != null) {
            pickerTask.setWorker(worker);
            worker.setCurrentPickerTask(pickerTask);
            workerRepository.save(worker);
            return savePickerTask(pickerTask);
        }
        return null;
    }

    /**
     * Removes a worker from a PickerTask.
     *
     * @param pickerTaskId the ID of the PickerTask
     * @param workerId     the ID of the Worker
     * @return the updated PickerTask entity
     */
    public PickerTask removeWorkerFromPickerTask(Long pickerTaskId, Long workerId) {
        PickerTask pickerTask = getPickerTaskById(pickerTaskId);
        Worker worker = workerRepository.findById(workerId).orElse(null);
        if (pickerTask.getWorker().getId().equals(workerId) && worker != null) {
            worker.setCurrentPickerTask(null);
            pickerTask.setWorker(null);
            workerRepository.save(worker);
            return savePickerTask(pickerTask);
        }
        return null;
    }

    /**
     * Updates a PickerTask entity.
     *
     * @param pickerTaskId the ID of the PickerTask to update
     * @param zoneId       the ID of the Zone
     * @param pickerTask   the updated PickerTask entity
     * @return the updated PickerTask entity
     */
    public PickerTask updatePickerTask(Long pickerTaskId, Long zoneId, PickerTask pickerTask) {
        return pickerTaskRepository.findById(pickerTaskId).map(existingTask -> {
            // Update fields
            existingTask.setDistance(pickerTask.getDistance());
            existingTask.setPackAmount(pickerTask.getPackAmount());
            existingTask.setLinesAmount(pickerTask.getLinesAmount());
            existingTask.setWeight(pickerTask.getWeight());
            existingTask.setVolume(pickerTask.getVolume());
            existingTask.setAvgHeight(pickerTask.getAvgHeight());
            existingTask.setTime(pickerTask.getTime());
            existingTask.setStartTime(pickerTask.getStartTime());
            existingTask.setEndTime(pickerTask.getEndTime());
            existingTask.setDate(pickerTask.getDate());
            existingTask.setWorker(pickerTask.getWorker());

            // Handle zone update
            if (!existingTask.getZone().getId().equals(zoneId)) {
                Zone zone = zoneRepository.findById(zoneId)
                        .orElseThrow(() -> new IllegalArgumentException("Zone not found with id: " + pickerTask.getZoneId()));
                if (zone.getIsPickerZone()) {
                    existingTask.setZone(zone);
                }
            }

            // Save and return the updated task
            return pickerTaskRepository.save(existingTask);
        }).orElseThrow(() -> new IllegalArgumentException("PickerTask not found with id: " + pickerTaskId));
    }

    public PickerTask createPickerTask(long zoneId, PickerTask pickerTask) {
        Zone zone = zoneRepository.findById(zoneId).orElse(null);
        if (zone == null) {
            throw new IllegalArgumentException("Zone not found with id: " + zoneId);
        } else {
            return pickerTaskRepository.save(pickerTask);
        }
    }

    /**
     * Deletes a PickerTask entity.
     *
     * @param id the ID of the PickerTask to delete
     */
    public PickerTask deletePickerTask(Long id) {
        PickerTask pickerTask = pickerTaskRepository.findById(id).orElse(null);
        if (pickerTask != null) {
            if (pickerTask.getWorker() != null) {
                pickerTask.getWorker().setCurrentPickerTask(null);
                workerRepository.save(pickerTask.getWorker());
            }
            pickerTaskRepository.delete(pickerTask);
            return pickerTask;
        }
        return null;
    }

    public void deleteAllPickerTasks() {
        for (PickerTask pickerTask : pickerTaskRepository.findAll()) {
            pickerTask.setWorker(null);
            pickerTaskRepository.save(pickerTask);
        }
        pickerTaskRepository.deleteAll(pickerTaskRepository.findAll());
    }
}
