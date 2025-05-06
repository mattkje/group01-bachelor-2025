package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.PickerTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import smile.regression.RandomForest;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * WorldSimulation class is responsible for simulating a single day of a warehouse.
 * It handles the scheduling of workers, assigning tasks, and managing breaks
 * and generates picker tasks and manages the simulation of active tasks.
 * It uses a machine learning model to estimate the time required for picker tasks.
 * It also provides methods to pause, resume, and change the speed of the simulation.
 */
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

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private MonteCarloDataService monteCarloDataService;

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

    private Map<ActiveTask, LocalDateTime> activeTaskEndTimes;

    private Map<PickerTask, LocalDateTime> pickerTaskEndTimes;

    private List<ActiveTask> activeTasksInProgress;

    private List<PickerTask> pickerTasksInProgress;

    private List<ActiveTask> activeTasksToday;

    private List<PickerTask> pickerTasksToday;

    private MachineLearningModelPicking machineLearningModelPicking;

    private long simulationSleepInMillis;

    private boolean isPaused;

    private boolean isPlaying;

    private HashMap<String, RandomForest> randomForests;

    private Optional<LocalDateTime> firstWorkerTime;

    @Autowired
    private WorkerService workerService;

    @Autowired
    private PickerTaskGenerator pickerTaskGenerator;

    @Autowired
    private PickerTaskService pickerTaskService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private WorldSimDataService worldSimDataService;

    /**
     * Runs the world simulation for a given simulation time and start date.
     * @param simulationTime The simulation time in minutes. If 0, the simulation has no delay.
     * @param startDate The start date for the simulation.
     * @throws Exception If an error occurs during the simulation.
     */
    public void runWorldSimulation(int simulationTime, LocalDate startDate) throws Exception {
        isPlaying = true;
        workday = startDate;
        boolean activeTasksExistForWorkday = false;
        machineLearningModelPicking = new MachineLearningModelPicking();
        randomForests = new HashMap<>();

        flushAllWorkerTasks();
        monteCarloDataService.flushMCData();

        if (workday.getDayOfWeek() == DayOfWeek.SUNDAY) {
            zoneService.updateMachineLearningModel();
        }

        //Initialize the random forests for each zone
        for (Zone zone : zoneService.getAllPickerZones()) {
            String zoneName = zone.getName().toUpperCase();
            randomForests.put(zoneName, machineLearningModelPicking.getModel(zone.getName(), false));
        }

        System.out.println(randomForests.size());

        //Finds the first workday without any active tasks to run the simulation on. Starts on the given date.
        while (!activeTasksExistForWorkday) {
            if (activeTaskService.getActiveTaskByDate(workday).isEmpty()) {
                activeTaskGenerator.generateActiveTasks(workday, 1);
                if (timetableService.getTimetablesByDate(workday).isEmpty()) {
                    timeTableGenerator.generateTimeTable(workday);
                }
                pickerTaskGenerator.generatePickerTasks(workday, 1, 20, machineLearningModelPicking);
                activeTasksExistForWorkday = true;
            } else {
                workday = workday.plusDays(1);
            }
        }

        //Deletes all worldSimData entries from previous simulations
        worldSimDataService.deleteAllData();

        //Decides the speed of the simulation, 0 if no delay.
        currentTime = LocalTime.MIDNIGHT.plusMinutes(1);
        endTime = LocalTime.MIDNIGHT;
        // 1440 minutes in a day
        if (simulationTime == 0) {
            simulationSleepInMillis = 0;
        } else {
            simulationSleepInMillis = TimeUnit.MINUTES.toMillis(simulationTime) / 1440;
        }

        //Initialize variables used in the simulation
        Random random = new Random();
        availableWorkers = new ArrayList<>();
        busyWorkers = new ArrayList<>();
        workersOnBreak = new ArrayList<>();
        workersDelayedBreak = new ArrayList<>();
        activeTasksInProgress = new ArrayList<>();
        workersWaitingForTask = new ArrayList<>();
        activeTaskEndTimes = new HashMap<>();
        pickerTasksInProgress = new ArrayList<>();
        pickerTaskEndTimes = new HashMap<>();

        filterData();

        //Calculates the start time and end time for each worker
        for (Timetable timetable : timetables) {
            LocalDateTime timetableStartTime = timetable.getStartTime();
            LocalDateTime timetableEndTime = timetable.getEndTime();
            int randomStartTime = random.nextInt(16);
            int randomEndTime = random.nextInt(16);
            timetable.setRealStartTime(timetableStartTime.plusMinutes(randomStartTime));
            timetable.setRealEndTime(timetableEndTime.plusMinutes(randomEndTime));
            System.out.println(timetable.getWorker().getName() + " Starts working at: " + timetable.getRealStartTime().toLocalTime() + " and ends at: " + timetable.getRealEndTime().toLocalTime());
        }

        //Starts the simulation
        startSimulating();

        //Last snippet of code that is run. If there are any unfinished tasks, they are set to finished.
        if (isPaused) {
            System.out.println("Simulation paused");
        } else {
            for (ActiveTask activeTask : activeTasksInProgress) {
                activeTask.setEndTime(activeTaskEndTimes.get(activeTask));
                for (Worker worker : activeTask.getWorkers()) {
                    worker.setCurrentTask(null);
                }
                activeTaskService.updateActiveTask(activeTask.getId(), activeTask);
            }

            for (PickerTask pickerTask : pickerTasksInProgress) {
                pickerTask.setEndTime(pickerTaskEndTimes.get(pickerTask));
                pickerTask.getWorker().setCurrentPickerTask(null);
                pickerTaskService.updatePickerTask(pickerTask.getId(), pickerTask.getZone().getId(), pickerTask);
            }
            System.out.println("Simulation finished");
            isPlaying = false;
        }

    }


    /**
     * Starts the simulation loop. Goes through all the workers and tasks and updates their status.
     * It also handles breaks and assigns tasks to available workers. While assigning tasks to workers
     * it also calculates the end time, which is put inside a Map which sets the end time when the task is completed.
     * This code is run for each minute of the simulation.
     *
     * @throws InterruptedException if the simulation thread is interrupted during execution.
     * @throws IOException if an error occurs while estimating the time using the machine learning model.
     */
    private void startSimulating() throws InterruptedException, IOException, ExecutionException {
        //Runs while the current time is not equal to the end time and the simulation is not paused, which is 00:00.
        while (!currentTime.equals(endTime) && !isPaused) {
            for (Timetable timetable : timetables) {
                processTimetable(timetable);
            }

            processWorkersOnBreak(workersDelayedBreak.iterator(), this::startBreak);
            processWorkersOnBreak(workersOnBreak.iterator(), this::endBreak);

            // Assign tasks to available workers
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

                        activeTaskEndTimes.put(task, getEndTime(task));

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

            // Assign picker tasks to available workers
            Iterator<PickerTask> pickerTaskIterator = pickerTasksToday.iterator();
            if (!availableWorkers.isEmpty()) {
                while (pickerTaskIterator.hasNext()) {
                    PickerTask task = pickerTaskIterator.next();

                    List<Worker> workers = new ArrayList<>(availableWorkers.stream()
                            .filter(worker -> worker.getZone().equals(task.getZone().getId()))
                            .collect(Collectors.toList()));
                    Iterator<Worker> workerIterator = workers.iterator();

                    if (workers.isEmpty()) {
                        continue;
                    }

                    Worker worker = workerIterator.next();
                    worker.setCurrentPickerTask(task);
                    task.setStartTime(LocalDateTime.of(workday, currentTime));
                    pickerTaskService.assignWorkerToPickerTask(task.getId(), worker.getId());
                    pickerTaskEndTimes.put(task, getPickerEndTime(task));
                    busyWorkers.add(worker);
                    availableWorkers.remove(worker);
                    workerService.updateWorker(worker.getId(), worker);
                    System.out.println(worker.getName() + " has started working on task: " +
                            task.getId());
                    pickerTasksInProgress.add(task);
                    pickerTaskIterator.remove();
                    pickerTaskService.updatePickerTask(task.getId(), task.getZone().getId(), task);
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
                    activeTaskEndTimes.put(task, getEndTime(task));
                    task.setWorkers(workersAssignedToTask);
                    activeTasksInProgress.add(task);
                    activeTaskIterator.remove();
                    activeTaskService.updateActiveTask(task.getId(), task);
                }
            }

            Iterator<PickerTask> pickerTaskInProgressIterator = pickerTasksInProgress.iterator();
            while (pickerTaskInProgressIterator.hasNext()) {
                PickerTask task = pickerTaskInProgressIterator.next();
                LocalDateTime computedEndTime = pickerTaskEndTimes.get(task);

                if (computedEndTime != null && !computedEndTime.toLocalTime().isAfter(currentTime)) {
                    task.setEndTime(computedEndTime);
                    task.setTime(Duration.between(task.getStartTime(), computedEndTime).toSeconds());
                    pickerTaskService.updatePickerTask(task.getId(), task.getZone().getId(), task);
                    Worker worker = task.getWorker();
                    worker.setCurrentPickerTask(null);
                    workerService.updateWorker(worker.getId(), worker);
                    System.out.println(worker.getName() + " has completed task: " + task.getId());
                    if (!availableWorkers.contains(worker)) availableWorkers.add(worker);
                    busyWorkers.remove(worker);
                    pickerTaskInProgressIterator.remove();
                    pickerTaskEndTimes.remove(task);
                }
            }

            //Check if tasks are completed
            Iterator<ActiveTask> activeTaskInProgressIterator = activeTasksInProgress.iterator();
            while (activeTaskInProgressIterator.hasNext()) {
                ActiveTask task = activeTaskInProgressIterator.next();
                LocalDateTime computedEndTime = activeTaskEndTimes.get(task);

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
                    activeTaskEndTimes.remove(task);
                }
            }

            Set<Worker> uniqueAvailableWorkers = new HashSet<>(availableWorkers);
            availableWorkers = new ArrayList<>(uniqueAvailableWorkers);

            if (currentTime.getMinute() % 10 == 0) {  // Log every 10 minutes
                System.out.println("Current time: " + currentTime);
                worldSimDataService.generateWorldSimData(workday.atTime(currentTime), false);
                // Hinder the simulation from running if there are no workers present
                if (firstWorkerTime.isPresent() && currentTime.isAfter(LocalTime.from(firstWorkerTime.get()))){
                    LocalDateTime daytime = LocalDateTime.of(workday, currentTime);
                    simulationService.runCompleteSimulation(randomForests,daytime);
                }
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
        LocalDate currentDate = LocalDate.now();

        if (workday != null) {
            return workday;
        } else {
            boolean tasks = false;
            while (!tasks) {
                // Check if there are any active tasks for the current date
                if (activeTaskService.getActiveTaskByDate(currentDate).isEmpty()) {
                    currentDate = currentDate.minusDays(1);
                    tasks = true;
                }
                // If no active tasks, move to the next day
                currentDate = currentDate.plusDays(1);
            }
        }
        return currentDate;
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

    public LocalDateTime getPickerEndTime(PickerTask task) throws IOException {
        long estimatedTime = machineLearningModelPicking.estimateTimeUsingModel(randomForests.get(task.getZone().getName().toUpperCase()), task);
        return task.getStartTime().plusSeconds((int) estimatedTime);
    }

    public void pauseSimulation() throws InterruptedException, IOException, ExecutionException {
        isPaused = !isPaused;
        if (!isPaused) {
            startSimulating();
        }
    }

    public void stopSimulation() throws InterruptedException, IOException, ExecutionException {
        isPlaying = false;
        isPaused = false;
        currentTime = LocalTime.MIDNIGHT;
        endTime = LocalTime.MIDNIGHT;
        flushGraphs();
        flushAllWorkerTasks();
    }

    public void changeSimulationSpeed(double speedFactor) {
        if (speedFactor > 0) {
            if (speedFactor == 1) {
                simulationSleepInMillis = TimeUnit.MINUTES.toMillis(60) / 1440;
            } else {
                simulationSleepInMillis = (long) (simulationSleepInMillis / speedFactor);
            }
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

    /**
     * Filters the data for the current workday.
     */
    public void filterData() {
        timetables = timetableService.getTimetablesByDate(workday);

        LocalDate today = workday;
        List<Worker> workersPresentToday = timetables.stream()
                .filter(timetable -> timetable.getStartTime().toLocalDate().equals(today) &&
                        timetable.getWorker().isAvailability())
                .map(Timetable::getWorker)
                .distinct()
                .toList();

        firstWorkerTime = timetables.stream()
                .filter(timetable -> timetable.getStartTime().toLocalDate().equals(today) &&
                        timetable.getWorker().isAvailability())
                .map(Timetable::getStartTime)
                .min(LocalDateTime::compareTo);

        System.out.println("Workers present today: " + workersPresentToday.size());

        activeTasksToday = activeTaskService.getAllActiveTasks();

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

        System.out.println("Active tasks today: " + activeTasksToday.size());

        pickerTasksToday = pickerTaskService.getAllPickerTasks().stream()
                .filter(pickerTask -> pickerTask.getDate().equals(finalWorkday))
                .collect(Collectors.toList());
    }

    /**
     * Processes the timetable for each worker. Calculates that the worker starts their break ca. halfway through
     * their shift (break is 30 minutes) with a plus or minus 15 minute randomization.
     *
     * @param timetable The timetable for the worker.
     */
    private void processTimetable(Timetable timetable) {
        LocalTime realStartTime = timetable.getRealStartTime().toLocalTime();
        LocalTime realEndTime = timetable.getRealEndTime().toLocalTime();
        LocalTime midpoint = timetable.getStartTime().toLocalTime()
                .plusHours(timetable.getEndTime().toLocalTime().minusHours(timetable.getStartTime().getHour()).getHour() / 2);
        LocalTime breakStart = midpoint.minusMinutes(15L);
        LocalTime breakEnd = midpoint.plusMinutes(15L);

        if (realStartTime.equals(currentTime) && timetable.getWorker().isAvailability()) {
            logAndAddWorker(timetable.getWorker(), "has started working");
        }

        if ((realEndTime.isBefore(currentTime) || realEndTime.equals(currentTime)) &&
                (availableWorkers.contains(timetable.getWorker()) || workersWaitingForTask.contains(timetable.getWorker()))) {
            logAndRemoveWorker(timetable.getWorker(), "has stopped working");
        }

        if (breakStart.equals(currentTime) && !availableWorkers.contains(timetable.getWorker()) &&
                !workersDelayedBreak.contains(timetable.getWorker())) {
            workersDelayedBreak.add(timetable.getWorker());
        } else if (midpoint.equals(currentTime) && availableWorkers.contains(timetable.getWorker())) {
            logAndRemoveWorker(timetable.getWorker(), "is on break");
        }

        if (breakEnd.equals(currentTime) && !workersDelayedBreak.contains(timetable.getWorker()) &&
                !workersOnBreak.contains(timetable.getWorker())) {
            logAndAddWorker(timetable.getWorker(), "has ended their lunch break");
        }
    }

    /**
     * Logs the worker's status and adds them to the list of available workers.
     *
     * @param worker The worker to log and add.
     * @param message The message to log.
     */
    private void logAndAddWorker(Worker worker, String message) {
        System.out.println(worker.getName() + " " + message);
        availableWorkers.add(worker);
    }

    /**
     * Logs the worker's status and removes them from the list of available workers.
     *
     * @param worker The worker to log and remove.
     * @param message The message to log.
     */
    private void logAndRemoveWorker(Worker worker, String message) {
        System.out.println(worker.getName() + " " + message);
        availableWorkers.remove(worker);
        workersWaitingForTask.remove(worker);
    }

    /**
     * Processes workers on break by applying the given action to each worker in the iterator.
     *
     * @param iterator The iterator for the workers on break.
     * @param action The action to apply to each worker.
     */
    private void processWorkersOnBreak(Iterator<Worker> iterator, Consumer<Worker> action) {
        while (iterator.hasNext()) {
            Worker worker = iterator.next();
            action.accept(worker);
            iterator.remove();
        }
    }

    /**
     * Starts a break for the worker if they are not currently assigned to a task and are not already on break.
     *
     * @param worker The worker to start a break for.
     */
    private void startBreak(Worker worker) {
        if (worker.getCurrentTaskId() == null && !workersOnBreak.contains(worker)) {
            System.out.println(worker.getName() + " is on break");
            worker.setBreakStartTime(currentTime);
            workersOnBreak.add(worker);
            availableWorkers.remove(worker);
        }
    }

    /**
     * Ends the break for the worker if they have been on break for 30 minutes.
     *
     * @param worker The worker to end the break for.
     */
    private void endBreak(Worker worker) {
        if (worker.getBreakStartTime().plusMinutes(30).equals(currentTime)) {
            System.out.println(worker.getName() + " has ended their break");
            worker.setBreakStartTime(null);
            if (!availableWorkers.contains(worker) && !workersWaitingForTask.contains(worker)) {
                availableWorkers.add(worker);
                workersOnBreak.remove(worker);
            }
        }
    }

    public LocalDateTime getCurrentDateTime() {
        if (workday == null || currentTime == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.of(workday, currentTime);
    }

    public void flushAllWorkerTasks() {
        List<Worker> allWorkers = workerService.getAllWorkers();
        for (Worker worker : allWorkers) {
            worker.setCurrentTask(null);
            worker.setCurrentPickerTask(null);
            workerService.updateWorker(worker.getId(), worker);
        }
    }

    public void flushGraphs() {
        activeTasksInProgress.clear();
        pickerTasksInProgress.clear();
        activeTaskEndTimes.clear();
        pickerTaskEndTimes.clear();
        availableWorkers.clear();
        busyWorkers.clear();
        workersDelayedBreak.clear();
        workersOnBreak.clear();
        workersWaitingForTask.clear();
    }
}