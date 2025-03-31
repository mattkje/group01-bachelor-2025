package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TaskService;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class for generating active tasks for the simulation
 * Generates a random numbers of active tasks with random durations and requirements
 */
@Service
public class ActiveTaskGenerator {

  @Autowired
  private TaskService taskService;

  @Autowired
  private ActiveTaskService activeTaskService;

  public void generateActiveTasks(LocalDate startDate, int numDays) throws Exception {
    List<Task> tasks = new ArrayList<>(taskService.getAllTasks());

    // Set up generation parameters
    int dueDateChance = 10;
    int[] dueHours = {8, 9, 10, 11, 12, 13, 14, 15, 16};
    int[] dueMinutes = {0, 15, 30, 45};

    Random random = new Random();

    startDate = Objects.requireNonNullElse(startDate, LocalDate.now());
    numDays = Math.max(numDays, 1);

    LocalDate endDate = startDate.plusDays(numDays - 1);


    int minNumTasks = 10;
    int maxNumTasks = 20;

    int strictStartChance = 5;

    // Generate active tasks
    LocalDate currentDate = startDate;

    while (!currentDate.isAfter(endDate)) {
      int numTasks = random.nextInt(maxNumTasks - minNumTasks) + minNumTasks;
      generateOneDay(currentDate, tasks, numTasks, dueDateChance, dueHours, dueMinutes, random,strictStartChance);
      currentDate = currentDate.plusDays(1);
    }
  }

  private void generateOneDay(LocalDate date, List<Task> tasks, int numTasks, int dueDateChance,
                              int[] dueHours, int[] dueMinutes, Random random, int strictStartChance) throws Exception {
    for (int i = 0; i < numTasks; i++) {
      LocalDateTime dueDate = date.atStartOfDay();
      ActiveTask activeTask = new ActiveTask();

      // Generate random due date
      if (random.nextInt(100) <= dueDateChance) {
        int dueHour = dueHours[random.nextInt(dueHours.length)];
        int dueMinute = dueMinutes[random.nextInt(dueMinutes.length)];
        LocalTime dueTime = LocalTime.of(dueHour, dueMinute);
        dueDate = LocalDateTime.of(date, dueTime);
        activeTask.setDueDate(dueDate);
      } else if (random.nextInt(100) <= strictStartChance) {
        int dueHour = dueHours[random.nextInt(dueHours.length)];
        int dueMinute = dueMinutes[random.nextInt(dueMinutes.length)];
        LocalTime dueTime = LocalTime.of(dueHour, dueMinute);
        dueDate = LocalDateTime.of(date, dueTime);
        if (activeTask.getDueDate() != null && activeTask.getDueDate().isAfter(dueDate)) {
          activeTask.setStrictStart(dueDate);
        }
      }
    // Generate random task
    Task task = tasks.get(random.nextInt(tasks.size()));

    // Create active task
    activeTask.setDueDate(dueDate);
    activeTask.setTask(task);
    activeTask.setDate(date);

    // Save active task
    activeTaskService.createActiveTask(task.getId(), activeTask);
  }
}
    }