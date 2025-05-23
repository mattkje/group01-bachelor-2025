package gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Task;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TaskService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Service for generating active tasks for simulation purposes.
 * This class generates a random number of active tasks with random durations and requirements
 * for a specified date range.
 */
@Component
public class ActiveTaskGenerator {

  private final TaskService taskService;

  private final ActiveTaskService activeTaskService;


  public ActiveTaskGenerator(TaskService taskService, ActiveTaskService activeTaskService) {
    this.taskService = taskService;
    this.activeTaskService = activeTaskService;
  }

  /**
   * Generates active tasks for a given date range.
   *
   * @param startDate The start date for generating active tasks. If null, the current date is used.
   * @param numDays   The number of days to generate active tasks for. Must be greater than 0.
   * @throws IllegalArgumentException if numDays is less than 1 or if no tasks are available.
   */
  public void generateActiveTasks(LocalDate startDate, int numDays) {
    if (numDays < 1) {
      throw new IllegalArgumentException("Number of days must be at least 1.");
    }

    List<Task> tasks = new ArrayList<>(taskService.getAllTasks());
    if (tasks.isEmpty()) {
      throw new IllegalArgumentException("No tasks available for generating active tasks.");
    }

    startDate = Objects.requireNonNullElse(startDate, LocalDate.now());
    LocalDate endDate = startDate.plusDays(numDays - 1);

    Random random = new Random();
    int minNumTasks = 50;
    int maxNumTasks = 90;
    int[] dueHours = {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
    int[] dueMinutes = {0, 15, 30, 45};

    for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate);
         currentDate = currentDate.plusDays(1)) {
      int numTasks = random.nextInt(maxNumTasks - minNumTasks + 1) + minNumTasks;
      generateTasksForDay(currentDate, tasks, numTasks, dueHours, dueMinutes, random);
    }
  }

  /**
   * Generates active tasks for a specific day.
   *
   * @param date       The date for which active tasks are generated.
   * @param tasks      The list of available tasks to assign.
   * @param numTasks   The number of active tasks to generate.
   * @param dueHours   The possible hours for the due date.
   * @param dueMinutes The possible minutes for the due date.
   * @param random     The random number generator.
   */
  private void generateTasksForDay(LocalDate date, List<Task> tasks, int numTasks, int[] dueHours,
                                   int[] dueMinutes,
                                   Random random) {
    for (int i = 0; i < numTasks; i++) {
      Task task = tasks.get(random.nextInt(tasks.size()));
      LocalDateTime dueDate = generateDueDate(date, dueHours, dueMinutes, random);

      ActiveTask activeTask = new ActiveTask();
      activeTask.setTask(task);
      activeTask.setDate(date);
      activeTask.setDueDate(dueDate);

      activeTaskService.createActiveTask(task.getId(), activeTask);
    }
  }

  /**
   * Generates a random due date for a task based on the given parameters.
   *
   * @param date       The base date for the due date.
   * @param dueHours   The possible hours for the due date.
   * @param dueMinutes The possible minutes for the due date.
   * @param random     The random number generator.
   * @return The generated due date, or null if no due date is assigned.
   */
  private LocalDateTime generateDueDate(LocalDate date, int[] dueHours, int[] dueMinutes,
                                        Random random) {
    int hour = dueHours[random.nextInt(dueHours.length)];
    int minute = dueMinutes[random.nextInt(dueMinutes.length)];
    return LocalDateTime.of(date, LocalTime.of(hour, minute));
  }
}