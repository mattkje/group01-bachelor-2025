package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ActiveTaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ZoneService {

    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ActiveTaskRepository activeTaskRepository;

    public List<Zone> getAllZones() {
        return zoneRepository.findAllWithTasksAndWorkersAndLicenses();
    }

    public Zone getZoneById(Long id) {
        return zoneRepository.findById(id).orElse(null);
    }

    public Set<Worker> getWorkersByZoneId(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            return zone.getWorkers();
        }
        return null;
    }

    public Set<Task> getTasksByZoneId(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            return zone.getTasks();
        }
        return null;
    }

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

    public Zone addZone(Zone zone) {
        if (zone != null) {
            return zoneRepository.save(zone);
        }
        return null;
    }

    public Zone updateZone(Long id, Zone zone) {
        Zone updatedZone = zoneRepository.findById(id).get();
        updatedZone.setName(zone.getName());
        updatedZone.setWorkers(zone.getWorkers());
        updatedZone.setTasks(zone.getTasks());
        updatedZone.setCapacity(zone.getCapacity());
        return zoneRepository.save(updatedZone);
    }

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

    public Zone deleteZone(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            zoneRepository.delete(zone);
        }
        return zone;
    }
}
