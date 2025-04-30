package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ActiveTaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActiveTaskService {

    @Autowired
    private ActiveTaskRepository activeTaskRepository;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private TaskRepository taskRepository;

    public List<ActiveTask> getAllActiveTasks() {
        return activeTaskRepository.findAllWithTasksAndWorkersAndLicenses();
    }

    public ActiveTask getActiveTaskById(Long id) {
        return activeTaskRepository.findById(id).orElse(null);
    }

    public List<ActiveTask> getActiveTasksForToday(LocalDateTime currentTime) {
        if (currentTime == null) {
            currentTime = LocalDateTime.now();
        }
        List<ActiveTask> activeTasks = new ArrayList<>();
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getDate().equals(currentTime.toLocalDate()) && activeTask.getEndTime() == null) {
                activeTasks.add(activeTask);
            }
        }
        return activeTasks;
    }

    public List<ActiveTask>getRemainingTasksForToday(){
        LocalDate currentDate = LocalDate.now();
        List<ActiveTask> activeTasks = new ArrayList<>();
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getDate().equals(currentDate) && activeTask.getEndTime() == null) {
                activeTasks.add(activeTask);
            }
        }
        return activeTasks;
    }

    public List<ActiveTask> getRemainingTasksForTodayByZone(Long zoneId){
        LocalDate currentDate = LocalDate.now();
        List<ActiveTask> activeTasks = new ArrayList<>();
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getDate().equals(currentDate) && activeTask.getEndTime() == null && activeTask.getTask().getZoneId().equals(zoneId)) {
                activeTasks.add(activeTask);
            }
        }
        return activeTasks;
    }

    public List<ActiveTask> getActiveTaskByDate(LocalDate date) {
        List<ActiveTask> activeTasks = new ArrayList<>();
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getDate().equals(date)) {
                activeTasks.add(activeTask);
            }
        }
        return activeTasks;
    }

    public List<ActiveTask> getCompletedActiveTasks() {
        List<ActiveTask> completedActiveTasks = new ArrayList<>();
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getEndTime() != null) {
                completedActiveTasks.add(activeTask);
            }
        }
        return completedActiveTasks;
    }

    public List<ActiveTask> getNotStartedActiveTasks() {
        List<ActiveTask> incompleteActiveTasks = new ArrayList<>();
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getEndTime() == null && activeTask.getStartTime() == null) {
                incompleteActiveTasks.add(activeTask);
            }
        }
        return incompleteActiveTasks;
    }

    public List<ActiveTask> getActiveTasksInProgress() {
        List<ActiveTask> activeTasksInProgress = new ArrayList<>();
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getStartTime() != null && activeTask.getEndTime() == null) {
                activeTasksInProgress.add(activeTask);
            }
        }
        return activeTasksInProgress;
    }

    public int getActiveTasksDoneForTodayInZone(LocalDate date, long zoneId) {
        int count = 0;
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getDate().isEqual(date) && activeTask.getEndTime() != null && activeTask.getTask().getZoneId().equals(zoneId)) {
                count++;
            }
        }
        return count;
    }

    public ActiveTask createActiveTask(Long taskId, ActiveTask activeTask) {
        if (activeTask != null) {
            if (activeTask.getTask() == null){
                Task task = taskRepository.findById(taskId).get();
                activeTask.setTask(task);
                createRepeatingActiveTaskUntilNextMonth(activeTask);
            }
            return activeTaskRepository.save(activeTask);
        }
        return null;
    }

    public ActiveTask updateActiveTask(Long id, ActiveTask activeTask) {
        ActiveTask updatedActiveTask = activeTaskRepository.findById(id).get();
        updatedActiveTask.setTask(activeTask.getTask());
        updatedActiveTask.setWorkers(activeTask.getWorkers());
        updatedActiveTask.setStartTime(activeTask.getStartTime());
        updatedActiveTask.setEndTime(activeTask.getEndTime());
        updatedActiveTask.setDueDate(activeTask.getDueDate());
        if (updatedActiveTask.getRecurrenceType() != activeTask.getRecurrenceType()) {
            for (Task task : taskRepository.findAll()) {
                if (task.getZoneId().equals(activeTask.getTask().getZoneId()) && task.getId().equals(id)) {
                    taskRepository.delete(task);
                }
            }
            createRepeatingActiveTaskUntilNextMonth(activeTask);
        }
        updatedActiveTask.setRecurrenceType(activeTask.getRecurrenceType());
        return activeTaskRepository.save(updatedActiveTask);
    }

    public ActiveTask assignWorkerToTask(Long id, Long workerId) {
        ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
        if (activeTask != null) {
            Worker worker = workerRepository.findById(workerId).orElse(null);
            if (worker != null) {
                worker.setZone(activeTask.getTask().getZoneId());
                worker.setCurrentTask(activeTask);
                if (activeTask.getWorkers() == null) activeTask.setWorkers(new ArrayList<>());
                if (!activeTask.getWorkers().contains(worker)) activeTask.addWorker(worker);
                return activeTaskRepository.save(activeTask);
            }
            return null;
        }
        return null;
    }

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

    public ActiveTask removeWorkersFromTask(Long id) {
        ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
        if (activeTask != null) {
            activeTask.getWorkers().clear();
            return activeTaskRepository.save(activeTask);
        }
        return null;
    }

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

    public void deleteAllActiveTasks() {
        activeTaskRepository.deleteAll();
    }

    public void deleteAllActiveTasksForToday() {
        LocalDate currentDate = LocalDate.now();
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getDate().equals(currentDate)) {
                activeTaskRepository.delete(activeTask);
            }
        }
    }

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
    public void CreateRepeatingActiveTasks() {
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
     * @param unit      The unit of time to increment (e.g., "MONTHS", "WEEKS", "DAYS").
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
     * @param date    The date to increment.
     * @param increment The amount to increment.
     * @param unit    The unit of time to increment (e.g., "MONTHS", "WEEKS", "DAYS").
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

    private void createNewTasksUntilNextMonth(ActiveTask activeTask, int increment, String unit, LocalDate startOfNextMonth) {
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

    public List<Worker> getWorkersAssignedToActiveTask(Long id) {
        ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
        if (activeTask != null) {
            return activeTask.getWorkers();
        }
        return new ArrayList<>();
    }
}
