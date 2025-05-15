package gruppe01.ntnu.no.warehouse.workflow.assigner.services;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Task;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.TaskRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing Task entities.
 */
@Service
public class TaskService {

  private final TaskRepository taskRepository;

  private final ZoneRepository zoneRepository;

  /**
   * Constructor for TaskService.
   *
   * @param taskRepository the repository for Task entity
   * @param zoneRepository the repository for Zone entity
   */
  public TaskService(TaskRepository taskRepository, ZoneRepository zoneRepository) {
    this.taskRepository = taskRepository;
    this.zoneRepository = zoneRepository;
  }

  /**
   * Gets all tasks from the repository.
   *
   * @return a list of all tasks
   */
  public List<Task> getAllTasks() {
    return taskRepository.findAllWithLicenses();
  }

  /**
   * Creates a new task for a specific zone.
   *
   * @param task   the task to be created
   * @param zoneId the ID of the zone
   * @return the created task
   */
  public Task createTask(Task task, Long zoneId) {
    if (task != null) {
      if (task.getMinTime() > task.getMaxTime() || task.getMinTime() < 0 || task.getMaxTime() < 0 ||
          task.getMaxWorkers() < task.getMinWorkers()) {
        throw new IllegalArgumentException("Invalid time range");
      } else {
        if (zoneRepository.findById(zoneId).isEmpty()) {
          return null;
        }
        Zone zone = zoneRepository.findById(zoneId).get();
        task.setZone(zone);
        return taskRepository.save(task);
      }
    }
    return null;
  }

  /**
   * Gets all tasks for a specific zone.
   *
   * @param id the ID of the zone
   * @return a list of tasks for the specified zone
   */
  public Task getTaskById(Long id) {
    return taskRepository.findById(id).orElse(null);
  }

  /**
   * Updates a task for a specific zone.
   *
   * @param id     the ID of the zone
   * @param task   the task to be updated
   * @param zoneId the ID of the zone
   * @return a list of tasks for the specified zone
   */
  public Task updateTask(Long id, Task task, Long zoneId) {
    if (zoneRepository.findById(zoneId).isEmpty() || taskRepository.findById(id).isEmpty()) {
      return null;
    }
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

  /**
   * Deletes a task by its ID.
   *
   * @param id the ID of the task to be deleted
   * @return the deleted task
   */
  public Task deleteTask(Long id) {
    Task task = taskRepository.findById(id).orElse(null);
    if (task != null) {
      taskRepository.delete(task);
    }
    return task;
  }
}
