package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class WorldSimulation {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private TimetableService timetableService;

    @Autowired
    private ActiveTaskService activeTaskService;

    @Autowired
    private TaskService taskService;

    public void runWorldSimulation(int simulationTime) throws InterruptedException {
        LocalTime currentTime = LocalTime.MIDNIGHT.plusMinutes(1);
        LocalTime endTime = LocalTime.MIDNIGHT;
        // 1440 minutes in a day
        long simulationSleepInMillis = TimeUnit.MINUTES.toMillis(simulationTime) / 1440;
        Random random = new Random();
        List<Worker> availableWorkers = new ArrayList<>();
        List<Worker> busyWorkers = new ArrayList<>();
        List<Worker> workersOnBreak = new ArrayList<>();
        List<Worker> workersDelayedBreak = new ArrayList<>();
        List<ActiveTask> activeTasksInProgress = new ArrayList<>();

        List<Timetable> timetables = timetableService.getAllTimetables();

        LocalDate today = LocalDate.of(2025, 3, 17);
        List<Worker> workersPresentToday = timetables.stream()
                .filter(timetable -> timetable.getStartTime().toLocalDate().equals(today))
                .map(Timetable::getWorker)
                .distinct()
                .toList();

        System.out.println("Workers present today: " + workersPresentToday.size());

        List<ActiveTask> activeTasksToday = activeTaskService.getAllActiveTasks();
        activeTasksToday = activeTasksToday.stream()
                .filter(activeTask -> activeTask.getDate().equals(LocalDate.of(2025, 3, 17)))
                .collect(Collectors.toList());

        System.out.println("Active tasks today: " + activeTasksToday.size());

        for (Timetable timetable : timetables) {
            LocalDateTime timetableStartTime = timetable.getStartTime();
            LocalDateTime timetableEndTime = timetable.getEndTime();
            int randomStartTime = random.nextInt(31) - 15;
            int randomEndTime = random.nextInt(31) - 15;
            timetable.setRealStartTime(timetableStartTime.plusMinutes(randomStartTime));
            timetable.setRealEndTime(timetableEndTime.plusMinutes(randomEndTime));
            System.out.println(timetable.getWorker().getName() + " Starts working at: " + timetable.getRealStartTime().toLocalTime() + " and ends at: " + timetable.getRealEndTime().toLocalTime());
        }


        while (!currentTime.equals(endTime)) {
            for (Timetable timetable : timetables) {
                if (timetable.getRealStartTime().toLocalTime().equals(currentTime)) {
                    System.out.println(timetable.getWorker().getName() + " has started working");
                    availableWorkers.add(timetable.getWorker());
                }
                if ((timetable.getRealEndTime().toLocalTime().isBefore(currentTime) ||
                        timetable.getRealEndTime().toLocalTime().equals(currentTime)) &&
                        availableWorkers.contains(timetable.getWorker())) {
                    System.out.println(timetable.getWorker().getName() + " has stopped working");
                    availableWorkers.remove(timetable.getWorker());
                }
                if ((timetable.getStartTime().toLocalTime().plusHours(timetable.getEndTime().toLocalTime()
                        .minusHours(timetable.getStartTime().getHour()).getHour() / 2).minusMinutes(15L))
                        .equals(currentTime) && !availableWorkers.contains(timetable.getWorker())) {
                    workersDelayedBreak.add(timetable.getWorker());
                } else if ((timetable.getStartTime().toLocalTime().plusHours(timetable.getEndTime().toLocalTime()
                        .minusHours(timetable.getStartTime().getHour()).getHour() / 2).equals(currentTime))
                        && availableWorkers.contains(timetable.getWorker())) {
                    availableWorkers.remove(timetable.getWorker());
                    System.out.println(timetable.getWorker().getName() + " is on break");
                }
                if ((timetable.getStartTime().toLocalTime().plusHours(timetable.getEndTime().toLocalTime()
                        .minusHours(timetable.getStartTime().getHour()).getHour() / 2).plusMinutes(15L))
                        .equals(currentTime) && !workersDelayedBreak.contains(timetable.getWorker())) {
                    System.out.println(timetable.getWorker().getName() + " has ended their lunch break");
                    if (!availableWorkers.contains(timetable.getWorker())) availableWorkers.add(timetable.getWorker());
                }
            }

            //Starts break halfway into the shift
            Iterator<Worker> delayedBreakIterator = workersDelayedBreak.iterator();
            while (delayedBreakIterator.hasNext()) {
                Worker worker = delayedBreakIterator.next();
                if (worker.getCurrentTask() == null) {
                    System.out.println(worker.getName() + " is on break");
                    worker.setBreakStartTime(currentTime);
                    workersOnBreak.add(worker);
                    availableWorkers.remove(worker);
                    delayedBreakIterator.remove();
                }
            }

            //Ends break after 30 minutes
            Iterator<Worker> onBreakIterator = workersOnBreak.iterator();
            while (onBreakIterator.hasNext()) {
                Worker worker = onBreakIterator.next();
                if (worker.getBreakStartTime().plusMinutes(30).equals(currentTime)) {
                    System.out.println(worker.getName() + " has ended their break");
                    if (!availableWorkers.contains(worker)) availableWorkers.add(worker);
                    onBreakIterator.remove();
                }
            }

            // Assign tasks to available workers
            Iterator<ActiveTask> activeTaskIterator = activeTasksToday.iterator();
            while (activeTaskIterator.hasNext()) {
                ActiveTask task = activeTaskIterator.next();

                List<Worker> workers = new ArrayList<>(availableWorkers.stream()
                        .filter(worker -> worker.getZone().equals(task.getTask().getZoneId()))
                        .collect(Collectors.toList()));
                if (workers.size() >= task.getTask().getMaxWorkers()) {
                    int workersAssigned = 0;
                    Iterator<Worker> workerIterator = workers.iterator();

                    while (workerIterator.hasNext() && workersAssigned < task.getTask().getMaxWorkers()) {
                        Worker worker = workerIterator.next();
                        task.addWorker(worker);
                        worker.setCurrentTask(task);
                        busyWorkers.add(worker);
                        availableWorkers.remove(worker);
                        System.out.println(worker.getName() + " has started working on task: " + task.getTask().getName());
                        workersAssigned++;
                    }
                    task.setStartTime(LocalDateTime.of(LocalDate.of(2025, 3, 17), currentTime));
                    activeTasksInProgress.add(task);
                    activeTaskIterator.remove();
                }
            }


            //Check if tasks are completed
            for (ActiveTask task : activeTasksInProgress) {
                int taskDuration = random.nextInt(task.getTask().getMaxTime() - task.getTask().getMinTime() + 1)
                        + task.getTask().getMinTime();
                if (task.getStartTime().plusMinutes(taskDuration).toLocalTime().isBefore(currentTime)) {
                    task.setEndTime(LocalDateTime.of(LocalDate.of(2025, 3, 17), currentTime));
                    Iterator<Worker> busyWorkerIterator = busyWorkers.iterator();
                    while (busyWorkerIterator.hasNext()) {
                        Worker worker = busyWorkerIterator.next();
                        if (worker.getCurrentTask() != null && worker.getCurrentTask().equals(task)) {
                            if (!availableWorkers.contains(worker)) availableWorkers.add(worker);
                            worker.setCurrentTask(null);
                            busyWorkerIterator.remove();
                            System.out.println(worker.getName() + " has completed task: " + task.getTask().getName());
                        }
                    }
                }
            }

            if (currentTime.getMinute() % 10 == 0) {  // Log every 10 minutes
                System.out.println("Current time: " + currentTime);
            }
            currentTime = currentTime.plusMinutes(1);
            TimeUnit.MILLISECONDS.sleep(simulationSleepInMillis);
        }

        System.out.println("Simulation finished");
    }
}
