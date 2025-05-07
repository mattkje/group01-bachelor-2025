package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.stream.Collectors;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.*;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service class for managing zones and their associated tasks and workers.
 */
@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;

    private final TaskRepository taskRepository;

    private final ActiveTaskRepository activeTaskRepository;

    private final PickerTaskRepository pickerTaskRepository;

    private final MachineLearningModelPicking machineLearningModelPicking;

    /**
     * Constructor for ZoneService.
     *
     * @param zoneRepository              the repository for Zone entity
     * @param taskRepository              the repository for Task entity
     * @param activeTaskRepository        the repository for ActiveTask entity
     * @param pickerTaskRepository        the repository for PickerTask entity
     * @param machineLearningModelPicking the machine learning model for picking tasks
     */
    public ZoneService(ZoneRepository zoneRepository, TaskRepository taskRepository, ActiveTaskRepository activeTaskRepository, PickerTaskRepository pickerTaskRepository, MachineLearningModelPicking machineLearningModelPicking) {
        this.zoneRepository = zoneRepository;
        this.taskRepository = taskRepository;
        this.activeTaskRepository = activeTaskRepository;
        this.pickerTaskRepository = pickerTaskRepository;
        this.machineLearningModelPicking = machineLearningModelPicking;
    }

    /**
     * Retrieves all zones from the repository.
     *
     * @return a list of all zones
     */
    public List<Zone> getAllZones() {
        return zoneRepository.findAllWithTasksAndWorkersAndLicenses();
    }

    /**
     * Retrieves all task zones from the repository.
     *
     * @return a list of all task zones
     */
    public List<Zone> getAllTaskZones() {
        return zoneRepository.findAll().stream()
                .filter(zone -> !zone.getIsPickerZone())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all picker zones from the repository.
     *
     * @return a list of all picker zones
     */
    public List<Zone> getAllPickerZones() {
        return zoneRepository.findAll().stream()
                .filter(Zone::getIsPickerZone)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a zone by its ID.
     *
     * @param id the ID of the zone
     * @return the zone entity, or null if not found
     */
    public Zone getZoneById(Long id) {
        return zoneRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves all workers associated with a specific zone ID.
     *
     * @param id the ID of the zone
     * @return a set of workers associated with the specified zone
     */
    public Set<Worker> getWorkersByZoneId(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            return zone.getWorkers();
        }
        return null;
    }

    /**
     * Retrieves all tasks associated with a specific zone ID.
     *
     * @param id the ID of the zone
     * @return a set of tasks associated with the specified zone
     */
    public Set<Task> getTasksByZoneId(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            return zone.getTasks();
        }
        return null;
    }

    /**
     * Retrieves all active tasks associated with a specific zone ID.
     *
     * @param id the ID of the zone
     * @return a set of active tasks associated with the specified zone
     */
    public Set<ActiveTask> getActiveTasksByZoneId(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        Set<ActiveTask> activeTasks = new HashSet<>();
        if (zone != null) {
            for (Task task : zone.getTasks()) {
                for (ActiveTask activeTask : activeTaskRepository.findAll()) {
                    if (activeTask.getTask().equals(task)) {
                        activeTasks.add(activeTask);
                    }
                }
            }
        }
        return activeTasks;
    }

    /**
     * Retrieves all picker tasks associated with a specific zone ID.
     *
     * @param id the ID of the zone
     * @return a set of picker tasks associated with the specified zone
     */
    public Set<PickerTask> getPickerTasksByZoneId(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            return zone.getPickerTask();
        }
        return null;
    }

    /**
     * Retrieves all active tasks for today that are unfinished and associated with a specific zone ID.
     *
     * @param id the ID of the zone
     * @return a set of active tasks for today that are unfinished and associated with the specified zone
     */
    public Set<ActiveTask> getTodayUnfinishedTasksByZoneId(Long id) {
        LocalDate today = LocalDate.now();
        return getActiveTasksByZoneId(id).stream()
                .filter(activeTask -> activeTask.getDate().equals(today) && activeTask.getEndTime() == null)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves all picker tasks for today that are unfinished and associated with a specific zone ID.
     *
     * @param id the ID of the zone
     * @return a set of picker tasks for today that are unfinished and associated with the specified zone
     */
    public Set<PickerTask> getTodayUnfinishedPickerTasksByZoneId(Long id) {
        LocalDate today = LocalDate.now();
        return getPickerTasksByZoneId(id).stream()
                .filter(pickerTask -> pickerTask.getDate().equals(today) && pickerTask.getEndTime() == null)
                .collect(Collectors.toSet());
    }

    /**
     * Adds a new zone to the repository.
     *
     * @param zone the zone to be added
     * @return the added zone
     */
    public Zone addZone(Zone zone) {
        if (zone != null && (zone.getPickerTask() == null || zone.getTasks() == null)) {
            return zoneRepository.save(zone);
        }
        return null;
    }

    /**
     * Updates an existing zone in the repository.
     *
     * @param id   the ID of the zone to be updated
     * @param zone the updated zone entity
     * @return the updated zone
     */
    public Zone updateZone(Long id, Zone zone) {
        if (zoneRepository.findById(id).isEmpty()) return null;
        Zone updatedZone = zoneRepository.findById(id).get();

        if ((zone.getPickerTask().isEmpty() && !zone.getIsPickerZone()) || (zone.getTasks().isEmpty() && zone.getIsPickerZone())) {
            updatedZone.setName(zone.getName());
            updatedZone.setWorkers(zone.getWorkers());
            updatedZone.setTasks(zone.getTasks());
            updatedZone.setPickerTask(zone.getPickerTask());
            updatedZone.setCapacity(zone.getCapacity());
            updatedZone.setIsPickerZone(zone.getIsPickerZone());
        }
        return zoneRepository.save(updatedZone);
    }

    /**
     * Adds a task to a specific zone.
     *
     * @param id     the ID of the zone
     * @param taskId the ID of the task to be added
     * @return the updated zone with the added task
     */
    public Zone addTaskToZone(Long id, Long taskId) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task != null) {
                zone.getTasks().add(task);
                return zoneRepository.save(zone);
            }
            return null;
        }
        return null;
    }

    /**
     * Removes a task from a specific zone.
     *
     * @param id     the ID of the zone
     * @param taskId the ID of the task to be removed
     * @return the updated zone with the removed task
     */
    public Zone removeTaskFromZone(Long id, Long taskId) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task != null) {
                zone.getTasks().remove(task);
                return zoneRepository.save(zone);
            }
            return null;
        }
        return null;
    }

    /**
     * Deletes a zone by its ID.
     *
     * @param id the ID of the zone to be deleted
     * @return the deleted zone
     */
    public Zone deleteZone(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            zoneRepository.delete(zone);
        }
        return zone;
    }

    /**
     * Changes the picker zone status of a zone.
     *
     * @param id the ID of the zone
     * @return the updated zone with the changed picker zone status
     */
    public Zone changeIsPickerZone(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            zone.setIsPickerZone(!zone.getIsPickerZone());
            return zoneRepository.save(zone);
        }
        return null;
    }

    /**
     * Retrieves the number of tasks for today by zone ID.
     *
     * @param zoneId the ID of the zone
     * @param date   the date to check
     * @return the number of tasks for today by zone ID
     */
    public Integer getNumberOfTasksForTodayByZone(Long zoneId, LocalDate date) {
        int tasks = 0;

        if (zoneId == 0 || !getZoneById(zoneId).getIsPickerZone()) {
            for (ActiveTask activeTask : activeTaskRepository.findAll()) {
                if (activeTask.getDate().equals(date) && zoneId == 0) {
                    tasks++;
                } else if (activeTask.getDate().equals(date) && activeTask.getTask().getZoneId().equals(zoneId)) {
                    tasks++;
                }
            }
        }

        if (zoneId == 0 || getZoneById(zoneId).getIsPickerZone()) {
            for (PickerTask pickerTask : pickerTaskRepository.findAll()) {
                if (pickerTask.getDate().equals(date) && zoneId == 0) {
                    tasks++;
                } else if (pickerTask.getDate().equals(date) && pickerTask.getZone().getId().equals(zoneId)) {
                    tasks++;
                }
            }
        }
        return tasks;
    }

    /**
     * Retrieves the minimum time for active tasks by zone ID for today.
     *
     * @param zoneId the ID of the zone
     * @return the minimum time for active tasks by zone ID for today
     */
    public Integer getMinTimeForActiveTasksByZoneIdNow(Long zoneId) {
        return getActiveTasksByZoneId(zoneId).stream()
                .filter(activeTask -> activeTask.getEndTime() == null) // Only unfinished tasks
                .mapToInt(activeTask -> activeTask.getTask().getMinTime()) // Get the min time for each task
                .sum(); // Sum up the min times
    }

    /**
     * Updates the machine learning model for picking tasks.
     *
     * @throws IOException if an I/O error occurs
     */
    public void updateMachineLearningModel() throws IOException {
        for (Zone zone : zoneRepository.findAll()) {
            if (zone.getIsPickerZone()) {
                List<PickerTask> pickerTasks = zone.getPickerTask().stream()
                        .filter(pickerTask -> pickerTask.getTime() > 0)
                        .collect(Collectors.toList());
                machineLearningModelPicking.updateMachineLearningModel(pickerTasks, zone.getName());
            }
        }
    }
}
