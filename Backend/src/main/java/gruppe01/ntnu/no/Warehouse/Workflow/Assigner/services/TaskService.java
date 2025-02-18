package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(Task task) {
        if (task != null) {
            return taskRepository.save(task);
        }
        return null;
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task updateTask(Long id, Task task) {
        Task updatedTask = taskRepository.findById(id).get();
        updatedTask.setName(task.getName());
        updatedTask.setDescription(task.getDescription());
        updatedTask.setMaxTime(task.getMaxTime());
        updatedTask.setMinTime(task.getMinTime());
        updatedTask.setMaxWorkers(task.getMaxWorkers());
        updatedTask.setMinWorkers(task.getMinWorkers());
        updatedTask.setRequiredLicense(task.getRequiredLicense());
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
