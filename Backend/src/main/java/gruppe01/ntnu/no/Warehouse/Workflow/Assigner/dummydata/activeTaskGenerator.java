package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TaskService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Class for generating active tasks for the simulation
 * Generates a random numbers of active tasks with random durations and requirements
 */
public class activeTaskGenerator {


  public static void main(String[] args) {

    // Get all tasks to create active tasks from
    TaskService taskService = new TaskService();
    List<Task> tasks = new ArrayList<>(taskService.getAllTasks());

    // Set up generation parameters
    int dueDateChance = 2;
    int[] dueHours = {8, 9, 10, 11, 12, 13, 14, 15, 16};
    int[] dueMinutes = {0, 15, 30, 45};

    Random random = new Random();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2025, Calendar.FEBRUARY, 18);
    Date startDate = calendar.getTime();
    calendar.set(2025, Calendar.MARCH, 18);
    Date endDate = calendar.getTime();

    int minNumTasks = 50;
    int maxNumTasks = 125;

    int strictStartChance = 5;

    // Generate active tasks
    List<Date> dates = new ArrayList<>();
    calendar.setTime(startDate);

    while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {
      int numTasks = random.nextInt(maxNumTasks - minNumTasks) + minNumTasks;

      Date dueDate = calendar.getTime();

      for (int i = 0; i < numTasks; i++) {
        // Generate random due date
        if (random.nextInt(100) == dueDateChance) {
          int dueHour = dueHours[random.nextInt(dueHours.length)];
          int dueMinute = dueMinutes[random.nextInt(dueMinutes.length)];
          calendar.set(Calendar.HOUR_OF_DAY, dueHour);
          calendar.set(Calendar.MINUTE, dueMinute);
          dueDate = calendar.getTime();
        }
        // Generate random task
        Task task = tasks.get(random.nextInt(tasks.size()));
        // Generate random strict start requirement
        boolean strictStart = random.nextInt(100) == strictStartChance;

        // Create active task
        ActiveTask activeTask = new ActiveTask();
        activeTask.setDueDate(dueDate);
        activeTask.setTask(task);
        activeTask.setStrictStart(strictStart);

        // Save active task
        ActiveTaskService activeTaskService = new ActiveTaskService();
        activeTaskService.createActiveTask(task.getId(),activeTask);
      }

      // Iterate through all dates
      dates.add(calendar.getTime());
      calendar.add(Calendar.DATE, 1);
    }


  }
}
