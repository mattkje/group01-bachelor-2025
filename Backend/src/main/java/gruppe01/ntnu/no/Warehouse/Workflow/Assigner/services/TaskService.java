package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ZoneRepository zoneRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAllWithLicenses();
    }

    public Task createTask(Task task, Long zoneId) {
        if (task != null) {
            if (task.getMinTime() > task.getMaxTime() || task.getMinTime() < 0 || task.getMaxTime() < 0 || task.getMaxWorkers() < task.getMinWorkers()) {
                throw new IllegalArgumentException("Invalid time range");
            } else {
                Zone zone = zoneRepository.findById(zoneId).get();
                task.setZone(zone);
                return taskRepository.save(task);
            }
        }
        return null;
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task updateTask(Long id, Task task, Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId).get();
        Task updatedTask = taskRepository.findById(id).get();
        updatedTask.setName(task.getName());
        updatedTask.setDescription(task.getDescription());
        updatedTask.setMaxTime(task.getMaxTime());
        updatedTask.setMinTime(task.getMinTime());
        updatedTask.setMaxWorkers(task.getMaxWorkers());
        updatedTask.setMinWorkers(task.getMinWorkers());
        updatedTask.setRequiredLicense(task.getRequiredLicense());
        updatedTask.setZone(zone);
        return taskRepository.save(updatedTask);
    }

    public Task deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            taskRepository.delete(task);
        }
        return task;
    }
}
