package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
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
import java.util.stream.Stream;

@Component
public class WorldSimulation {

    @Autowired
    private TimetableService timetableService;

    @Autowired
    private ActiveTaskService activeTaskService;

    @Autowired
    private ActiveTaskGenerator activeTaskGenerator;

    @Autowired
    private TimeTableGenerator timeTableGenerator;

    private LocalTime currentSimulationTime;

    @Autowired
    private WorkerService workerService;

    public void runWorldSimulation(int simulationTime) throws Exception {

        LocalDate workday = LocalDate.now();
        boolean activeTasksExistForWorkday = false;

        while (!activeTasksExistForWorkday) {
            if (activeTaskService.getActiveTaskByDate(workday).isEmpty()) {
                activeTaskGenerator.generateActiveTasks(workday, 1);
                timeTableGenerator.generateTimeTable(workday, 1);
                activeTasksExistForWorkday = true;
            } else {
                workday = workday.plusDays(1);
            }
        }


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
        List<Worker> workersWaitingForTask = new ArrayList<>();

        List<Timetable> timetables = timetableService.getTimetablesByDate(workday);

        LocalDate today = workday;
        List<Worker> workersPresentToday = timetables.stream()
                .filter(timetable -> timetable.getStartTime().toLocalDate().equals(today) &&
                        timetable.getWorker().isAvailability())
                .map(Timetable::getWorker)
                .distinct()
                .toList();

        System.out.println("Workers present today: " + workersPresentToday.size());

        List<ActiveTask> activeTasksToday = activeTaskService.getAllActiveTasks();
        System.out.println("Active tasks today: " + activeTasksToday.size());

        LocalDate finalWorkday = workday;
        activeTasksToday = activeTasksToday.stream()
                .filter(activeTask -> activeTask.getDate().equals(finalWorkday) &&
                        activeTask.getDueDate().toLocalTime() == LocalTime.of(0, 0))
                .sorted(Comparator.comparing(ActiveTask::getStrictStart))
                .collect(Collectors.toList());

        List<ActiveTask> activeTasksWithDueDates = activeTasksToday.stream()
                .filter(activeTask -> activeTask.getDueDate().toLocalTime() != LocalTime.of(0, 0))
                .collect(Collectors.toList());

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
                        .equals(currentTime) && !availableWorkers.contains(timetable.getWorker()) &&
                        !workersDelayedBreak.contains(timetable.getWorker())) {
                    workersDelayedBreak.add(timetable.getWorker());
                } else if ((timetable.getStartTime().toLocalTime().plusHours(timetable.getEndTime().toLocalTime()
                        .minusHours(timetable.getStartTime().getHour()).getHour() / 2).equals(currentTime))
                        && availableWorkers.contains(timetable.getWorker())) {
                    availableWorkers.remove(timetable.getWorker());
                    System.out.println(timetable.getWorker().getName() + " is on break");
                }
                if ((timetable.getStartTime().toLocalTime().plusHours(timetable.getEndTime().toLocalTime()
                        .minusHours(timetable.getStartTime().getHour()).getHour() / 2).plusMinutes(15L))
                        .equals(currentTime) && !workersDelayedBreak.contains(timetable.getWorker()) &&
                        !workersOnBreak.contains(timetable.getWorker())) {
                    System.out.println(timetable.getWorker().getName() + " has ended their lunch break");
                    if (!availableWorkers.contains(timetable.getWorker())) availableWorkers.add(timetable.getWorker());
                }
            }

            //Starts break halfway into the shift
            Iterator<Worker> delayedBreakIterator = workersDelayedBreak.iterator();
            while (delayedBreakIterator.hasNext()) {
                Worker worker = delayedBreakIterator.next();
                if (worker.getCurrentTask() == null && !workersOnBreak.contains(worker)) {
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

            Iterator<ActiveTask> activeTaskWithDueDatesIterator = activeTasksWithDueDates.iterator();
            while (activeTaskWithDueDatesIterator.hasNext()) {
                ActiveTask task = activeTaskWithDueDatesIterator.next();
                if (task.getDueDate().minusMinutes(task.getTask().getMaxTime()).toLocalTime().equals(currentTime)) {
                    List<Worker> workers = new ArrayList<>(Stream.concat(
                                    availableWorkers.stream(),
                                    workersWaitingForTask.stream()
                                            .filter(worker -> worker.getZone().equals(task.getTask().getZoneId()) &&
                                                    task.getTask().getRequiredLicense().isEmpty() ||
                                                    worker.getLicenses().containsAll(task.getTask().getRequiredLicense())))
                            .filter(worker -> worker.getZone().equals(task.getTask().getZoneId()) &&
                                    (task.getTask().getRequiredLicense().isEmpty() ||
                                            worker.getLicenses().containsAll(task.getTask().getRequiredLicense())))
                            .collect(Collectors.toList()));
                    int workersSlots = 0;
                    if (workers.size() >= task.getTask().getMinWorkers()) {
                        workersSlots = task.getTask().getMinWorkers();
                        if (workers.size() < task.getTask().getMaxWorkers() && workers.size() > task.getTask().getMinWorkers()) {
                            workersSlots = workers.size();
                            if (workers.size() > task.getTask().getMaxWorkers()) {
                                workersSlots = task.getTask().getMaxWorkers();
                            }
                        }
                        int workersAssigned = 0;
                        Iterator<Worker> workerIterator = workers.iterator();
                        List<Worker> workersAssignedToTask = new ArrayList<>();

                        while (workerIterator.hasNext() && workersAssigned < workersSlots) {
                            Worker worker = workerIterator.next();
                            workersAssignedToTask.add(worker);
                            worker.setCurrentTask(task);
                            busyWorkers.add(worker);
                            availableWorkers.remove(worker);
                            workersWaitingForTask.remove(worker);
                            activeTaskService.assignWorkerToTask(task.getId(), worker.getId());
                            workerService.updateWorker(worker.getId(), worker);
                            System.out.println(worker.getName() + " has started working on task: " +
                                    task.getTask().getName());
                            workersAssigned++;
                        }
                        task.setStartTime(LocalDateTime.of(workday, currentTime));
                        task.setWorkers(workersAssignedToTask);
                        activeTasksInProgress.add(task);
                        activeTaskWithDueDatesIterator.remove();
                    } else {
                        for (Worker worker : workers) {
                            if (!workersWaitingForTask.contains(worker)) {
                                workersWaitingForTask.add(worker);
                                availableWorkers.remove(worker);
                            }
                        }
                    }
                }
            }

            // Assign tasks to available workers
            Iterator<ActiveTask> activeTaskIterator = activeTasksToday.iterator();
            while (activeTaskIterator.hasNext()) {
                ActiveTask task = activeTaskIterator.next();

                List<Worker> workers = new ArrayList<>(availableWorkers.stream()
                        .filter(worker -> worker.getZone().equals(task.getTask().getZoneId()) &&
                                (task.getTask().getRequiredLicense().isEmpty() ||
                                        worker.getLicenses().containsAll(task.getTask().getRequiredLicense())))
                        .collect(Collectors.toList()));
                int workersSlots = 0;
                if (workers.size() >= task.getTask().getMinWorkers()) {
                    workersSlots = task.getTask().getMinWorkers();
                    if (workers.size() < task.getTask().getMaxWorkers() && workers.size() > task.getTask().getMinWorkers()) {
                        workersSlots = workers.size();
                        if (workers.size() > task.getTask().getMaxWorkers()) {
                            workersSlots = task.getTask().getMaxWorkers();
                        }
                    }
                    int workersAssigned = 0;
                    Iterator<Worker> workerIterator = workers.iterator();
                    List<Worker> workersAssignedToTask = new ArrayList<>();

                    while (workerIterator.hasNext() && workersAssigned < workersSlots) {
                        Worker worker = workerIterator.next();
                        workersAssignedToTask.add(worker);
                        worker.setCurrentTask(task);
                        busyWorkers.add(worker);
                        availableWorkers.remove(worker);
                        activeTaskService.assignWorkerToTask(task.getId(), worker.getId());
                        workerService.updateWorker(worker.getId(), worker);
                        System.out.println(worker.getName() + " has started working on task: " +
                                task.getTask().getName());
                        workersAssigned++;
                    }
                    task.setStartTime(LocalDateTime.of(workday, currentTime));
                    task.setWorkers(workersAssignedToTask);
                    activeTasksInProgress.add(task);
                    activeTaskIterator.remove();
                }
            }


            //Check if tasks are completed
            Iterator<ActiveTask> activeTaskInProgressIterator = activeTasksInProgress.iterator();
            while (activeTaskInProgressIterator.hasNext()) {
                ActiveTask task = activeTaskInProgressIterator.next();
                if (task.getEndTime() == null) {
                    Task task1 = task.getTask();
                    double workerFactor = (double) (task.getWorkers().size() - task1.getMinWorkers()) /
                            Math.max(1, (task1.getMaxWorkers() - task1.getMinWorkers()));
                    double taskDuration = task1.getMinTime() + workerFactor * (task1.getMaxTime() - task1.getMinTime());
                    double averageEffectiveness = task.getWorkers().stream()
                            .mapToDouble(Worker::getEffectiveness)
                            .average().orElse(1);
                    double actualDuration = taskDuration / averageEffectiveness;
                    task.setEndTime(task.getStartTime().plusMinutes((int) actualDuration));
                }
                if (task.getEndTime().toLocalTime().isBefore(currentTime) && activeTasksInProgress.contains(task)) {
                    activeTaskInProgressIterator.remove();
                    activeTaskService.updateActiveTask(task.getId(), task);
                    task.setEndTime(LocalDateTime.of(workday, currentTime));
                    Iterator<Worker> busyWorkerIterator = busyWorkers.iterator();
                    while (busyWorkerIterator.hasNext()) {
                        Worker worker = busyWorkerIterator.next();
                        if (worker.getCurrentTask() != null && worker.getCurrentTask().equals(task)) {
                            if (!availableWorkers.contains(worker)) availableWorkers.add(worker);
                            worker.setCurrentTask(null);
                            busyWorkerIterator.remove();
                            workerService.updateWorker(worker.getId(), worker);
                            System.out.println(worker.getName() + " has completed task: " + task.getTask().getName());
                        }
                    }
                }
            }

            if (currentTime.getMinute() % 10 == 0) {  // Log every 10 minutes
                System.out.println("Current time: " + currentTime);
            }
            currentSimulationTime = currentTime;
            currentTime = currentTime.plusMinutes(1);
            TimeUnit.MILLISECONDS.sleep(simulationSleepInMillis);
        }
        System.out.println("Simulation finished");
    }

    public LocalTime getCurrentTime() {
        return currentSimulationTime;
    }
}
