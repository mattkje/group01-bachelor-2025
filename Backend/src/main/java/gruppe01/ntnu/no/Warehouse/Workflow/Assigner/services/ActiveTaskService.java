package gruppe01.ntnu.no.warehouse.workflow.assigner.services;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Task;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.ActiveTaskRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.TaskRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing active tasks.
 */
@Service
public class ActiveTaskService {

  private final ActiveTaskRepository activeTaskRepository;

  private final WorkerRepository workerRepository;

  private final TaskRepository taskRepository;

  /**
   * Constructor for ActiveTaskService.
   *
   * @param activeTaskRepository The repository for active tasks.
   * @param workerRepository     The repository for workers.
   * @param taskRepository       The repository for tasks.
   */
  public ActiveTaskService(ActiveTaskRepository activeTaskRepository,
                           WorkerRepository workerRepository, TaskRepository taskRepository) {
    this.activeTaskRepository = activeTaskRepository;
    this.workerRepository = workerRepository;
    this.taskRepository = taskRepository;
  }

  /**
   * Retrieves all active tasks from the repository.
   *
   * @return A list of all active tasks.
   */
  public List<ActiveTask> getAllActiveTasks() {
    return activeTaskRepository.findAllWithTasksAndWorkersAndLicenses();
  }

  /**
   * Retrieves an active task by its ID.
   *
   * @param id The ID of the active task to retrieve.
   * @return The active task with the specified ID, or null if not found.
   */
  public ActiveTask getActiveTaskById(Long id) {
    return activeTaskRepository.findById(id).orElse(null);
  }

  /**
   * Retrieves all active tasks for today.
   *
   * @param currentTime The current time to check against. If null, uses the current system time.
   * @return A list of active tasks for today.
   */
  public List<ActiveTask> getActiveTasksForToday(LocalDateTime currentTime) {
    if (currentTime == null) {
      currentTime = LocalDateTime.now();
    }
    return activeTaskRepository.findByDateAndEndTimeIsNull(currentTime.toLocalDate());
  }

  /**
   * Gets all not completed tasks for a date by zone.
   *
   * @param zoneId id of the zone to fetch data from
   * @param currentTime what the tasks are made
   * @return a list of active tasks that have not been completed for a given date
   */
  public List<ActiveTask> getActiveTasksForTodayByZone(Long zoneId, LocalDateTime currentTime) {
    if (currentTime == null) {
      currentTime = LocalDateTime.now();
    }
    return activeTaskRepository.findByDateAndEndTimeIsNullAndTask_Zone_Id(currentTime.toLocalDate(), zoneId);
  }

  /**
   * Retrieves all active tasks for a specific date.
   *
   * @param date The date to filter tasks by.
   * @return A list of active tasks for the specified date.
   */
  public List<ActiveTask> getActiveTaskByDate(LocalDate date) {
    return activeTaskRepository.findByDate(date);
  }

  /**
   * Retrieves all active tasks that have been completed.
   *
   * @return A list of completed active tasks.
   */
  public List<ActiveTask> getCompletedActiveTasks() {
    return activeTaskRepository.findByEndTimeIsNotNull();
  }

  public List<ActiveTask> getNotCompletedActiveTasksByDateAndZone(LocalDate date, Long zoneId) {
    return activeTaskRepository.findNotCompletedTasksByDateAndZone(date, zoneId);
  }

  /**
   * Retrieves all active tasks that have not been started.
   *
   * @return A list of active tasks that have not been started.
   */
  public List<ActiveTask> getNotStartedActiveTasks() {
    return activeTaskRepository.findByStartTimeIsNullAndEndTimeIsNull();
  }

  /**
   * Retrieves all active tasks that are currently in progress.
   *
   * @return A list of active tasks that are currently in progress.
   */
  public List<ActiveTask> getActiveTasksInProgress() {
    return activeTaskRepository.findByStartTimeIsNotNullAndEndTimeIsNull();
  }

  /**
   * Retrieves the number of active tasks completed for today in a specific zone.
   *
   * @param date   The date to filter tasks by.
   * @param zoneId The ID of the zone to filter tasks by.
   * @return The number of active tasks completed for today in the specified zone.
   */
  public int getActiveTasksDoneForTodayInZone(LocalDate date, long zoneId) {
    return activeTaskRepository.countCompletedTasksByDateAndZone(date, zoneId);
  }

  /**
   * Creates a new active task.
   *
   * @param taskId     The ID of the task to associate with the active task.
   * @param activeTask The active task to create.
   * @return The created active task.
   */
  public ActiveTask createActiveTask(Long taskId, ActiveTask activeTask) {
    if (activeTask != null) {
      if (activeTask.getTask() == null && taskRepository.findById(taskId).isPresent()) {
        Task task = taskRepository.findById(taskId).get();
        activeTask.setTask(task);
        createRepeatingActiveTaskUntilNextMonth(activeTask);
      }
      return activeTaskRepository.save(activeTask);
    }
    return null;
  }

  /**
   * Updates an existing active task.
   *
   * @param id         The ID of the active task to update.
   * @param updatedData The updated active task data.
   * @return The updated active task.
   */
  @Transactional
  public ActiveTask updateActiveTask(Long id, ActiveTask updatedData) {
    return activeTaskRepository.findById(id)
        .map(existing -> {
          // Manually update fields
          existing.setDate(updatedData.getDate());
          existing.setStartTime(updatedData.getStartTime());
          existing.setEndTime(updatedData.getEndTime());
          existing.setTask(updatedData.getTask());
          existing.setRecurrenceType(updatedData.getRecurrenceType());
          existing.setDueDate(updatedData.getDueDate());
          existing.setWorkers(updatedData.getWorkers());
          existing.setEta(updatedData.getEta());
          existing.setMcEndTime(updatedData.getMcEndTime());
          existing.setMcStartTime(updatedData.getMcStartTime());
          return activeTaskRepository.save(existing);
        })
        .orElse(null);
  }

  /**
   * Assigns a worker to an active task.
   *
   * @param id       The ID of the active task.
   * @param workerId The ID of the worker to assign.
   * @return The updated active task with the assigned worker.
   */
  public ActiveTask assignWorkerToTask(Long id, Long workerId) {
    ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
    if (activeTask != null) {
      Worker worker = workerRepository.findById(workerId).orElse(null);
      if (worker != null) {
        worker.setZone(activeTask.getTask().getZoneId());
        worker.setCurrentTask(activeTask);
        if (activeTask.getWorkers() == null) {
          activeTask.setWorkers(new ArrayList<>());
        }
        if (!activeTask.getWorkers().contains(worker)) {
          activeTask.addWorker(worker);
        }
        return activeTaskRepository.save(activeTask);
      }
      return null;
    }
    return null;
  }

  /**
   * Removes a worker from an active task.
   *
   * @param id       The ID of the active task.
   * @param workerId The ID of the worker to remove.
   * @return The updated active task without the removed worker.
   */
  public ActiveTask removeWorkerFromTask(Long id, Long workerId) {
    ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
    if (activeTask != null) {
      Worker worker = workerRepository.findById(workerId).orElse(null);
      if (worker != null) {
        activeTask.getWorkers().remove(worker);
        return activeTaskRepository.save(activeTask);
      }
      return null;
    }
    return null;
  }

  /**
   * Removes all workers from an active task.
   *
   * @param id The ID of the active task.
   * @return The updated active task without any workers.
   */
  public ActiveTask removeWorkersFromTask(Long id) {
    ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
    if (activeTask != null) {
      activeTask.getWorkers().clear();
      return activeTaskRepository.save(activeTask);
    }
    return null;
  }

  /**
   * Deletes an active task by its ID.
   *
   * @param id The ID of the active task to delete.
   * @return The deleted active task.
   */
  public ActiveTask deleteActiveTask(Long id) {
    ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
    if (activeTask != null) {
      activeTask.getWorkers().clear();
      for (Worker worker : activeTask.getWorkers()) {
        if (worker.getCurrentTaskId() == activeTask.getId()) {
          worker.setCurrentTask(null);
          workerRepository.save(worker);
        }
      }
      activeTaskRepository.delete(activeTask);
    }
    return activeTask;
  }

  /**
   * Deletes all active tasks.
   */
  public void deleteAllActiveTasks() {
    List<ActiveTask> activeTasks = activeTaskRepository.findAll();
    for (ActiveTask activeTask : activeTasks) {
      // Clear relationships to avoid transient data issues
      if (activeTask.getWorkers() != null) {
        activeTask.getWorkers().clear();
      }
      activeTaskRepository.save(activeTask); // Save changes before deletion
    }
    activeTaskRepository.deleteAll();
  }

  /**
   * Retrieves all workers assigned to a specific task.
   *
   * @param taskId The ID of the task to retrieve workers for.
   * @return A list of workers assigned to the specified task.
   */
  public List<Worker> getWorkersAssignedToTask(Long taskId) {
    ActiveTask activeTask = activeTaskRepository.findById(taskId).orElse(null);
    if (activeTask != null) {
      return activeTask.getWorkers();
    }
    return new ArrayList<>();
  }

  /**
   * Creates new active tasks for the next month based on the recurrence type of existing active tasks.
   * RecurrenceTyoe 1 = Monthly, 2 = Weekly, 3 = Every 2 days, 4 = Daily, 0 = No recurrence
   */
  public void createRepeatingActiveTasks() {
    List<ActiveTask> activeTasks = activeTaskRepository.findAll();
    for (ActiveTask activeTask : activeTasks) {
      if (activeTask.getRecurrenceType() == 1) {
        createNewTasks(activeTask, 1, "MONTHS");
      } else if (activeTask.getRecurrenceType() == 2) {
        createNewTasks(activeTask, 1, "WEEKS");
      } else if (activeTask.getRecurrenceType() == 3) {
        createNewTasks(activeTask, 2, "DAYS");
      } else if (activeTask.getRecurrenceType() == 4) {
        createNewTasks(activeTask, 1, "DAYS");
      }
    }
  }

  /**
   * Creates new tasks based on the given active task's date and recurrence type.
   *
   * @param activeTask The active task to base the new tasks on.
   * @param increment  The amount to increment the date by.
   * @param unit       The unit of time to increment (e.g., "MONTHS", "WEEKS", "DAYS").
   */
  private void createNewTasks(ActiveTask activeTask, int increment, String unit) {
    LocalDate tempDate = incrementDate(activeTask.getDate(), increment, unit);
    activeTask.setRecurrenceType(0);

    while (tempDate.isBefore(LocalDate.now().plusMonths(1))) {
      ActiveTask newTask = new ActiveTask(activeTask);
      newTask.setDate(tempDate);
      newTask.setRecurrenceType(0);
      newTask.setStartTime(null);
      newTask.setEndTime(null);
      newTask.setWorkers(new ArrayList<>());

      if (tempDate.isAfter(LocalDate.now().plusMonths(1))) {
        newTask.setRecurrenceType(activeTask.getRecurrenceType());
      }

      activeTaskRepository.save(newTask);
      tempDate = incrementDate(tempDate, increment, unit);
    }
  }

  /**
   * Increments the given date by the specified amount and unit.
   *
   * @param date      The date to increment.
   * @param increment The amount to increment.
   * @param unit      The unit of time to increment (e.g., "MONTHS", "WEEKS", "DAYS").
   * @return The incremented date.
   */
  private LocalDate incrementDate(LocalDate date, int increment, String unit) {
    return switch (unit) {
      case "MONTHS" -> date.plusMonths(increment);
      case "WEEKS" -> date.plusWeeks(increment);
      case "DAYS" -> date.plusDays(increment);
      default -> throw new IllegalArgumentException("Invalid time unit: " + unit);
    };
  }

  /**
   * Creates new active tasks until the start of the next month based on the given active task's date and recurrence type.
   *
   * @param activeTask The active task to base the new tasks on.
   */
  public void createRepeatingActiveTaskUntilNextMonth(ActiveTask activeTask) {
    LocalDate startOfNextMonth = LocalDate.now().withDayOfMonth(1).plusMonths(1);

    if (activeTask.getRecurrenceType() == 1) {
      createNewTasksUntilNextMonth(activeTask, 1, "MONTHS", startOfNextMonth);
    } else if (activeTask.getRecurrenceType() == 2) {
      createNewTasksUntilNextMonth(activeTask, 1, "WEEKS", startOfNextMonth);
    } else if (activeTask.getRecurrenceType() == 3) {
      createNewTasksUntilNextMonth(activeTask, 2, "DAYS", startOfNextMonth);
    } else if (activeTask.getRecurrenceType() == 4) {
      createNewTasksUntilNextMonth(activeTask, 1, "DAYS", startOfNextMonth);
    }
  }

  /**
   * Creates new tasks until the start of the next month based on the given active task's date and recurrence type.
   *
   * @param activeTask       The active task to base the new tasks on.
   * @param increment        The amount to increment the date by.
   * @param unit             The unit of time to increment (e.g., "MONTHS", "WEEKS", "DAYS").
   * @param startOfNextMonth The start date of the next month.
   */
  private void createNewTasksUntilNextMonth(ActiveTask activeTask, int increment, String unit,
                                            LocalDate startOfNextMonth) {
    LocalDate tempDate = incrementDate(activeTask.getDate(), increment, unit);
    activeTask.setRecurrenceType(0);

    while (tempDate.isBefore(startOfNextMonth)) {
      ActiveTask newTask = new ActiveTask(activeTask);
      newTask.setDate(tempDate);
      newTask.setRecurrenceType(0);
      newTask.setStartTime(null);
      newTask.setEndTime(null);
      newTask.setWorkers(new ArrayList<>());

      activeTaskRepository.save(newTask);
      tempDate = incrementDate(tempDate, increment, unit);
    }
  }

  /**
   * Retrieves all workers assigned to an active task.
   *
   * @param id The ID of the active task to retrieve workers for.
   * @return A list of workers assigned to the specified active task.
   */
  public List<Worker> getWorkersAssignedToActiveTask(Long id) {
    ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
    if (activeTask != null) {
      return activeTask.getWorkers();
    }
    return new ArrayList<>();
  }

  /**
   * Get all active tasks by date that are not finished.
   *
   * @param currentTime the time to fetch the data from
   * @return a list of active tasks by given date that are unfinished
   */
  public List<ActiveTask> getUnfinishedActiveTasksForToday(LocalDateTime currentTime) {
    if (currentTime == null) {
      currentTime = LocalDateTime.now();
    }
    return activeTaskRepository.findByDateAndEndTimeIsNull(currentTime.toLocalDate());
  }
}
