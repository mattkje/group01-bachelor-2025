package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.PickerTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.PickerTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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

    private LocalDate workday;

    private LocalTime currentTime;

    private LocalTime endTime;

    private List<Timetable> timetables;

    private List<Worker> availableWorkers;

    private List<Worker> workersDelayedBreak;

    private List<Worker> workersOnBreak;

    private List<ActiveTask> activeTasksWithDueDates;

    private List<Worker> workersWaitingForTask;

    private List<Worker> busyWorkers;

    private Map<ActiveTask, LocalDateTime> taskEndTimes;

    private List<ActiveTask> activeTasksInProgress;

    private List<ActiveTask> activeTasksToday;

    private List<PickerTask> pickerTasksToday;

    private long simulationSleepInMillis;

    private boolean isPaused;

    private boolean isPlaying;

    @Autowired
    private WorkerService workerService;

    @Autowired
    private PickerTaskGenerator pickerTaskGenerator;

    @Autowired
    private PickerTaskService pickerTaskService;

    public void runWorldSimulation(int simulationTime, LocalDate startDate) throws Exception {
        isPlaying = true;
        workday = startDate;
        boolean activeTasksExistForWorkday = false;

        while (!activeTasksExistForWorkday) {
            if (activeTaskService.getActiveTaskByDate(workday).isEmpty()) {
                activeTaskGenerator.generateActiveTasks(workday, 1);
                timeTableGenerator.generateTimeTable(workday, 1);
                pickerTaskGenerator.generatePickerTasks(workday, 1, 20);
                activeTasksExistForWorkday = true;
            } else {
                workday = workday.plusDays(1);
            }
        }


        currentTime = LocalTime.MIDNIGHT.plusMinutes(1);
        endTime = LocalTime.MIDNIGHT;
        // 1440 minutes in a day
        if (simulationTime == 0) {
            simulationSleepInMillis = 0;
        } else {
            simulationSleepInMillis = TimeUnit.MINUTES.toMillis(simulationTime) / 1440;
        }
        Random random = new Random();
        availableWorkers = new ArrayList<>();
        busyWorkers = new ArrayList<>();
        workersOnBreak = new ArrayList<>();
        workersDelayedBreak = new ArrayList<>();
        activeTasksInProgress = new ArrayList<>();
        workersWaitingForTask = new ArrayList<>();
        taskEndTimes = new HashMap<>();

        timetables = timetableService.getTimetablesByDate(workday);

        LocalDate today = workday;
        List<Worker> workersPresentToday = timetables.stream()
                .filter(timetable -> timetable.getStartTime().toLocalDate().equals(today) &&
                        timetable.getWorker().isAvailability())
                .map(Timetable::getWorker)
                .distinct()
                .toList();

        System.out.println("Workers present today: " + workersPresentToday.size());

        activeTasksToday = activeTaskService.getAllActiveTasks();
        System.out.println("Active tasks today: " + activeTasksToday.size());

        LocalDate finalWorkday = workday;

        activeTasksWithDueDates = activeTasksToday.stream()
                .filter(activeTask -> (activeTask.getDate().equals(finalWorkday) &&
                        activeTask.getDueDate().toLocalTime() != LocalTime.of(0, 0)))
                .collect(Collectors.toList());

        activeTasksToday = activeTasksToday.stream()
                .filter(activeTask -> activeTask.getDate().equals(finalWorkday) &&
                        activeTask.getDueDate().toLocalTime() == LocalTime.of(0, 0))
                //.sorted(Comparator.comparing(ActiveTask::getStrictStart))
                .collect(Collectors.toList());

        pickerTasksToday = pickerTaskService.getAllPickerTasks().stream()
                .filter(pickerTask -> pickerTask.getDate().equals(finalWorkday))
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

        startSimulating();
        if (isPaused) {
            System.out.println("Simulation paused");
        } else {
            System.out.println("Simulation finished");
            isPlaying = false;
        }

    }

    private void startSimulating() throws InterruptedException {
        while (!currentTime.equals(endTime) && !isPaused) {
            for (Timetable timetable : timetables) {
                if (timetable.getRealStartTime().toLocalTime().equals(currentTime) &&
                        timetable.getWorker().isAvailability()) {
                    System.out.println(timetable.getWorker().getName() + " has started working");
                    availableWorkers.add(timetable.getWorker());
                }
                if ((timetable.getRealEndTime().toLocalTime().isBefore(currentTime) ||
                        timetable.getRealEndTime().toLocalTime().equals(currentTime)) &&
                        (availableWorkers.contains(timetable.getWorker()) ||
                        workersWaitingForTask.contains(timetable.getWorker()))) {
                    System.out.println(timetable.getWorker().getName() + " has stopped working");
                    availableWorkers.remove(timetable.getWorker());
                    workersWaitingForTask.remove(timetable.getWorker());
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
                    worker.setBreakStartTime(null);
                    if (!availableWorkers.contains(worker) && !workersWaitingForTask.contains(worker)) availableWorkers.add(worker);
                    onBreakIterator.remove();
                }
            }

            Iterator<ActiveTask> activeTaskWithDueDatesIterator = activeTasksWithDueDates.iterator();
            while (activeTaskWithDueDatesIterator.hasNext()) {
                ActiveTask task = activeTaskWithDueDatesIterator.next();
                if (!task.getDueDate().minusMinutes(task.getTask().getMaxTime()).toLocalTime().isAfter(currentTime)) {
                    List<Worker> workers = Stream.concat(
                                    availableWorkers.stream(),
                                    workersWaitingForTask.stream())
                            .filter(worker -> worker.getZone().equals(task.getTask().getZoneId()) &&
                                    (task.getTask().getRequiredLicense().isEmpty() ||
                                            worker.getLicenses().containsAll(task.getTask().getRequiredLicense())))
                            .collect(Collectors.toList());
                    int workersSlots;
                    if (workers.size() >= task.getTask().getMinWorkers()) {
                        workersSlots = Math.min(workers.size(), task.getTask().getMaxWorkers());
                        int workersAssigned = 0;
                        Iterator<Worker> workerIterator = workers.iterator();
                        List<Worker> workersAssignedToTask = new ArrayList<>();
                        List<Worker> toRemove = new ArrayList<>();

                        while (workerIterator.hasNext() && workersAssigned < workersSlots) {
                            Worker worker = workerIterator.next();
                            workersAssignedToTask.add(worker);
                            worker.setCurrentTask(task);
                            busyWorkers.add(worker);
                            toRemove.add(worker);
                            activeTaskService.assignWorkerToTask(task.getId(), worker.getId());
                            workerService.updateWorker(worker.getId(), worker);
                            System.out.println(worker.getName() + " has started working on task: " +
                                    task.getTask().getName());
                            workersAssigned++;
                        }
                        availableWorkers.removeAll(toRemove);
                        task.setWorkers(new ArrayList<>(workersAssignedToTask));
                        task.setStartTime(LocalDateTime.of(workday, currentTime));

                        taskEndTimes.put(task, getEndTime(task));

                        activeTasksInProgress.add(task);
                        activeTaskWithDueDatesIterator.remove();
                        activeTaskService.updateActiveTask(task.getId(), task);
                    } else {
                        for (Worker worker : workers) {
                            if (!workersWaitingForTask.contains(worker)) {
                                workersWaitingForTask.add(worker);
                                availableWorkers.remove(worker);
                                System.out.println(worker.getName() + " is waiting for a task");
                            }
                        }
                    }
                }
            }

            Iterator<PickerTask> pickerTaskIterator = pickerTasksToday.iterator();
            while (pickerTaskIterator.hasNext()) {
                PickerTask task = pickerTaskIterator.next();

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
                    taskEndTimes.put(task, getEndTime(task));
                    task.setWorkers(workersAssignedToTask);
                    activeTasksInProgress.add(task);
                    activeTaskIterator.remove();
                    activeTaskService.updateActiveTask(task.getId(), task);
                }
            }


            //Check if tasks are completed
            Iterator<ActiveTask> activeTaskInProgressIterator = activeTasksInProgress.iterator();
            while (activeTaskInProgressIterator.hasNext()) {
                ActiveTask task = activeTaskInProgressIterator.next();
                LocalDateTime computedEndTime = taskEndTimes.get(task);

                if (computedEndTime != null && !computedEndTime.toLocalTime().isAfter(currentTime)) {
                    // Now set endTime since we are processing completion
                    task.setEndTime(computedEndTime);
                    activeTaskService.updateActiveTask(task.getId(), task);

                    for (Worker worker : activeTaskService.getWorkersAssignedToTask(task.getId())) {
                        if (!availableWorkers.contains(worker)) availableWorkers.add(worker);
                        worker.setCurrentTask(null);
                        workerService.updateWorker(worker.getId(), worker);
                        System.out.println(worker.getName() + " has completed task: " + task.getTask().getName());
                    }

                    // Remove task from progress and map
                    activeTaskInProgressIterator.remove();
                    taskEndTimes.remove(task);
                }
            }

            Set<Worker> uniqueAvailableWorkers = new HashSet<>(availableWorkers);
            availableWorkers = new ArrayList<>(uniqueAvailableWorkers);

            if (currentTime.getMinute() % 10 == 0) {  // Log every 10 minutes
                System.out.println("Current time: " + currentTime);
            }
            currentSimulationTime = currentTime;
            currentTime = currentTime.plusMinutes(1);
            TimeUnit.MILLISECONDS.sleep(simulationSleepInMillis);
        }
    }

    public LocalTime getCurrentTime() {
        return currentSimulationTime;
    }

    public LocalDate getCurrentDate() {
        return this.workday;
    }

    public LocalDateTime getEndTime(ActiveTask task) {
        Task task1 = task.getTask();
        int assignedWorkers = activeTaskService.getWorkersAssignedToTask(task.getId()).size();
        int minWorkers = task1.getMinWorkers();
        int maxWorkers = task1.getMaxWorkers();

        double interpolationFactor = 0.0;
        if (maxWorkers > minWorkers) {
            interpolationFactor = Math.min(1.0, Math.max(0.0, (double)(assignedWorkers - minWorkers) / (maxWorkers - minWorkers)));
        }

        double taskDuration = task1.getMaxTime() - interpolationFactor * (task1.getMaxTime() - task1.getMinTime());

        double averageEfficiency = activeTaskService.getWorkersAssignedToTask(task.getId()).stream()
                .mapToDouble(Worker::getEfficiency)
                .average().orElse(1.0);

        double actualDuration = taskDuration / averageEfficiency;

        // Add random offset between -5 and +5 minutes
        int randomOffset = ThreadLocalRandom.current().nextInt(-5, 6); // Inclusive of -5 and +5

        return task.getStartTime().plusMinutes((int) actualDuration + randomOffset);
    }

    public void pauseSimulation() throws InterruptedException {
        isPaused = !isPaused;
        if (!isPaused) {
            startSimulating();
        }
    }

    public void changeSimulationSpeed(double speedFactor) {
        if (speedFactor > 0) {
            simulationSleepInMillis = (long) (TimeUnit.MINUTES.toMillis(1) / (1440 * speedFactor));
        }
    }

    public int getPauseStatus() {
        if (isPaused) {
            return 2;
        }
        else if (!isPaused && isPlaying) {
            return 1;
        }
        else if (!isPlaying && !isPaused) {
            return 0;
        } else  {
            return -1;
        }
    }

    public void simulateOneYear() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        int numDays = 365;
        for (int i = 0; i < numDays; i++) {
            runWorldSimulation(0, startDate.plusDays(i));
        }

    }
}
