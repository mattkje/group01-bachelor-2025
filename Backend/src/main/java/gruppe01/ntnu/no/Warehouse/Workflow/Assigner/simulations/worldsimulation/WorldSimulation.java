package gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.worldsimulation;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.PickerTaskGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.*;
import gruppe01.ntnu.no.warehouse.workflow.assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.*;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;
import smile.regression.RandomForest;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

  private final AtomicInteger intervalId = new AtomicInteger(1);

  private final List<Integer> intervals = List.of(10, 30, 60);
  private final NotificationService notificationService;
  private LocalTime currentSimulationTime;

  private LocalDate workday = LocalDate.now();

  private LocalTime currentTime = LocalTime.MIDNIGHT;

  private LocalTime endTime;

  private List<Timetable> timetables;

  private List<Worker> availableWorkers;

  private List<ActiveTask> activeTasksWithDueDates;

  private List<Worker> workersWaitingForTask;

  private List<Worker> busyWorkers;

  private Map<ActiveTask, LocalDateTime> activeTaskEndTimes;

  private Map<PickerTask, LocalDateTime> pickerTaskEndTimes;

  private Map<Long, Integer> completedTaskMap;

  private List<ActiveTask> activeTasksInProgress;

  private List<PickerTask> pickerTasksInProgress;

  private List<PickerTask> pickerTasksToday;

  private MachineLearningModelPicking machineLearningModelPicking;

  private long simulationSleepInMillis;

  private boolean isPaused;

  private boolean isPlaying;

  private boolean resetData;

  private volatile boolean isSimulationRunning = false;

  private HashMap<String, RandomForest> randomForests;

  private Optional<LocalDateTime> firstWorkerTime;

  private final TimetableService timetableService;

  private final ActiveTaskService activeTaskService;

  private final ActiveTaskGenerator activeTaskGenerator;

  private final TimeTableGenerator timeTableGenerator;

  private final SimulationService simulationService;

  private final MonteCarloDataService monteCarloDataService;

  private final WorkerService workerService;

  private final PickerTaskGenerator pickerTaskGenerator;

  private final PickerTaskService pickerTaskService;

  private final ZoneService zoneService;

  private final WorldSimDataService worldSimDataService;

  private int speedFactory = 0;

  /**
   * Constructor for WorldSimulation.
   *
   * @param activeTaskService     the service for ActiveTask entity
   * @param activeTaskGenerator   the generator for ActiveTask entity
   * @param timeTableGenerator    the generator for Timetable entity
   * @param simulationService     the service for Simulation entity
   * @param monteCarloDataService the service for MonteCarloData entity
   * @param workerService         the service for Worker entity
   * @param pickerTaskGenerator   the generator for PickerTask entity
   * @param pickerTaskService     the service for PickerTask entity
   * @param zoneService           the service for Zone entity
   * @param worldSimDataService   the service for WorldSimData entity
   */
  public WorldSimulation(
      TimetableService timetableService,
      ActiveTaskService activeTaskService,
      ActiveTaskGenerator activeTaskGenerator,
      TimeTableGenerator timeTableGenerator,
      SimulationService simulationService,
      MonteCarloDataService monteCarloDataService,
      WorkerService workerService,
      PickerTaskGenerator pickerTaskGenerator,
      PickerTaskService pickerTaskService,
      ZoneService zoneService,
      WorldSimDataService worldSimDataService, NotificationService notificationService) {
    this.timetableService = timetableService;
    this.activeTaskService = activeTaskService;
    this.activeTaskGenerator = activeTaskGenerator;
    this.timeTableGenerator = timeTableGenerator;
    this.simulationService = simulationService;
    this.monteCarloDataService = monteCarloDataService;
    this.workerService = workerService;
    this.pickerTaskGenerator = pickerTaskGenerator;
    this.pickerTaskService = pickerTaskService;
    this.zoneService = zoneService;
    this.worldSimDataService = worldSimDataService;
    this.notificationService = notificationService;
  }

  /**
   * Runs the world simulation for a given simulation time and start date.
   *
   * @param simulationTime The simulation time in minutes. If 0, the simulation has no delay.
   * @param startDate      The start date for the simulation.
   * @throws IOException, InterruptedException, ExecutionException
   */
  public void runWorldSimulation(int simulationTime, LocalDate startDate)
      throws IOException, InterruptedException, ExecutionException {
    isPlaying = true;
    isPaused = false;
    resetData = false;
    workday = startDate;
    boolean activeTasksExistForWorkday = false;
    machineLearningModelPicking = new MachineLearningModelPicking();
    randomForests = new HashMap<>();
    this.speedFactory = 0;

    flushAllWorkerTasks();
    monteCarloDataService.flushMCData();
    notificationService.deleteAll();

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
        pickerTaskGenerator.generatePickerTasks(workday, 1, 50, machineLearningModelPicking, false);
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
    activeTasksInProgress = new ArrayList<>();
    workersWaitingForTask = new ArrayList<>();
    activeTaskEndTimes = new HashMap<>();
    pickerTasksInProgress = new ArrayList<>();
    pickerTaskEndTimes = new HashMap<>();
    completedTaskMap = new HashMap<>();

    filterData();

    //Calculates the start time and end time for each worker
    for (Timetable timetable : timetables) {
      LocalDateTime timetableStartTime = timetable.getStartTime();
      LocalDateTime timetableEndTime = timetable.getEndTime();
      int randomStartTime = random.nextInt(16);
      int randomEndTime = random.nextInt(16);
      timetable.setRealStartTime(timetableStartTime.plusMinutes(randomStartTime));
      timetable.setRealEndTime(timetableEndTime.plusMinutes(randomEndTime));
      timetableService.updateTimetable(timetable.getId(), timetable);
      if (timetable.getWorker().isAvailability()) {
        System.out.println(timetable.getWorker().getName() + " Starts working at: " +
            timetable.getRealStartTime().toLocalTime() + " and ends at: " +
            timetable.getRealEndTime().toLocalTime());
      } else {
        System.out.println(timetable.getWorker().getName() + " Is scheduled to work at: " +
            timetable.getRealStartTime().toLocalTime() + " and ends at: " +
            timetable.getRealEndTime().toLocalTime() + " but is not available");
      }
    }

    //Starts the simulation
    startSimulating();
  }

  /**
   * Starts the simulation loop. Goes through all the workers and tasks and updates their status.
   * It also handles breaks and assigns tasks to available workers. While assigning tasks to workers
   * it also calculates the end time, which is put inside a Map which sets the end time when the task is completed.
   * This code is run for each minute of the simulation.
   *
   * @throws InterruptedException if the simulation thread is interrupted during execution.
   * @throws IOException          if an error occurs while estimating the time using the machine learning model.
   */
  private void startSimulating() throws InterruptedException, IOException, ExecutionException {
    //Runs while the current time is not equal to the end time and the simulation is not paused, which is 00:00.
    while (!currentTime.equals(endTime) && !isPaused && isPlaying && !resetData) {
      for (Timetable timetable : timetables) {
        processTimetable(timetable);
      }

      // Assign tasks to available workers
      Iterator<ActiveTask> activeTaskIterator = activeTasksWithDueDates.iterator();
      while (activeTaskIterator.hasNext()) {
        ActiveTask task = activeTaskIterator.next();

        List<Worker> workers = new ArrayList<>(availableWorkers.stream()
            .filter(worker -> worker.getZone().equals(task.getTask().getZoneId()) &&
                worker.getLicenses().containsAll(task.getTask().getRequiredLicense()) &&
                worker.isAvailability())
            .collect(Collectors.toList()));
        int workersSlots = 0;
        if (workers.size() >= task.getTask().getMinWorkers()) {
          workersSlots = task.getTask().getMinWorkers();
          if (workers.size() < task.getTask().getMaxWorkers() &&
              workers.size() > task.getTask().getMinWorkers()) {
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

      // Assign tasks to available workers
      Iterator<ActiveTask> activeTaskWithDueDatesIterator = activeTasksWithDueDates.iterator();
      while (activeTaskWithDueDatesIterator.hasNext()) {
        ActiveTask task = activeTaskWithDueDatesIterator.next();
        if (!task.getDueDate().minusMinutes(task.getTask().getMaxTime()).toLocalTime()
            .isAfter(currentTime)) {
          List<Worker> workers = Stream.concat(
                  availableWorkers.stream(),
                  workersWaitingForTask.stream())
              .filter(worker -> worker.getZone().equals(task.getTask().getZoneId()) &&
                  worker.getLicenses().containsAll(task.getTask().getRequiredLicense()) &&
                  worker.isAvailability())
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
              .filter(worker -> worker.getZone().equals(task.getZone().getId()) &&
                  worker.isAvailability())
              .collect(Collectors.toList()));
          Iterator<Worker> workerIterator = workers.iterator();

          if (workers.isEmpty()) {
            continue;
          }

          Worker worker = workerIterator.next();
          task.setStartTime(LocalDateTime.of(workday, currentTime));
          pickerTaskService.assignWorkerToPickerTask(task.getId(), worker.getId());
          pickerTaskEndTimes.put(task, getPickerEndTime(task, worker.getId()));
          task.setWorker(worker);
          worker.setCurrentPickerTask(task);
          workerService.updateWorker(worker.getId(), worker);
          busyWorkers.add(worker);
          availableWorkers.remove(worker);
          System.out.println(worker.getName() + " has started working on task: " +
              task.getId());
          pickerTasksInProgress.add(task);
          pickerTaskIterator.remove();
          pickerTaskService.updatePickerTask(task.getId(), task.getZone().getId(), task);
        }
      }

      //Checks if picker tasks are completed
      Iterator<PickerTask> pickerTaskInProgressIterator = pickerTasksInProgress.iterator();
      while (pickerTaskInProgressIterator.hasNext()) {
        PickerTask task = pickerTaskInProgressIterator.next();
        LocalDateTime computedEndTime = pickerTaskEndTimes.get(task);

        if (computedEndTime != null && !computedEndTime.toLocalTime().isAfter(currentTime)) {
          task.setEndTime(computedEndTime);
          task.setTime(Duration.between(task.getStartTime(), computedEndTime).toSeconds());
          completedTaskMap.put(task.getZoneId(),
              completedTaskMap.getOrDefault(task.getZoneId(), 0) + 1);
          pickerTaskService.updatePickerTask(task.getId(), task.getZone().getId(), task);
          Worker worker = task.getWorker();
          worker.setCurrentPickerTask(null);
          workerService.updateWorker(worker.getId(), worker);
          System.out.println(worker.getName() + " has completed task: " + task.getId());
            if (!availableWorkers.contains(worker)) {
                availableWorkers.add(worker);
            }
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
          completedTaskMap.put(task.getTask().getZoneId(),
              completedTaskMap.getOrDefault(task.getTask().getZoneId(), 0) + 1);

          for (Worker worker : activeTaskService.getWorkersAssignedToTask(task.getId())) {
              if (!availableWorkers.contains(worker)) {
                  availableWorkers.add(worker);
              }
            workersWaitingForTask.remove(worker);
            worker.setCurrentTask(null);
            workerService.updateWorker(worker.getId(), worker);
            System.out.println(
                worker.getName() + " has completed task: " + task.getTask().getName());
          }

          // Remove task from progress and map
          activeTaskInProgressIterator.remove();
          activeTaskEndTimes.remove(task);
        }
      }

      Set<Worker> uniqueAvailableWorkers = new HashSet<>(availableWorkers);
      availableWorkers = new ArrayList<>(uniqueAvailableWorkers);

      if (currentTime.getMinute() % intervals.get(intervalId.get()) == 0) {
        System.out.println("Current time: " + currentTime);
        // Hinder the simulation from running if there are no workers present
        if (firstWorkerTime.isPresent() && currentTime.isAfter(
            LocalTime.from(firstWorkerTime.get().minus(Duration.ofMinutes(60))))) {
          if (isSimulationRunning) {
            System.out.println("A simulation is already running. Skipping this iteration.");
          } else {
            LocalDateTime daytime = LocalDateTime.of(workday, currentTime);
            isSimulationRunning = true; // Mark simulation as running

            CompletableFuture.runAsync(() -> {
              try {
                simulationService.runCompleteSimulation(randomForests, daytime);
              } catch (ExecutionException | InterruptedException | IOException e) {
                throw new RuntimeException(e);
              } finally {
                isSimulationRunning = false; // Reset the flag when simulation completes
              }
            });
          }
        }
      }

      if (currentTime.getMinute() % 10 == 0) {
        worldSimDataService.generateWorldSimData(workday.atTime(currentTime), false);
      }
      currentSimulationTime = currentTime;
      currentTime = currentTime.plusMinutes(1);
      TimeUnit.MILLISECONDS.sleep(simulationSleepInMillis);
    }

    if (resetData) {
      isPlaying = false;
      resetTaskData();
    }

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
        pickerTaskService.updatePickerTask(pickerTask.getId(), pickerTask.getZone().getId(),
            pickerTask);
      }

      System.out.println("Simulation finished");
      isPlaying = false;
    }
  }

  public LocalTime getCurrentTime() {
    if (currentSimulationTime == null) {
      currentSimulationTime = LocalTime.MIDNIGHT;
    }
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
    int assignedWorkersSize = activeTaskService.getWorkersAssignedToTask(task.getId()).size();
    int minWorkers = task1.getMinWorkers();
    int maxWorkers = task1.getMaxWorkers();

    double interpolationFactor = 0.0;
    if (maxWorkers > minWorkers) {
      // Calculate the interpolation factor based on the number of assigned workers
      interpolationFactor = Math.min(1.0,
          Math.max(0.0, (double) (assignedWorkersSize - minWorkers) / (maxWorkers - minWorkers)));
    }

    double taskDuration =
        task1.getMaxTime() - interpolationFactor * (task1.getMaxTime() - task1.getMinTime());

    // Calculate the average efficiency of the workers assigned to the task
    double averageEfficiency = activeTaskService.getWorkersAssignedToTask(task.getId()).stream()
        .mapToDouble(Worker::getEfficiency)
        .average().orElse(1.0);

    double actualDuration = taskDuration / averageEfficiency;

    // Add random offset between -5 and +5 minutes
    int randomOffset = ThreadLocalRandom.current().nextInt(-5, 6);

    return task.getStartTime().plusMinutes((int) actualDuration + randomOffset);
  }

  public LocalDateTime getPickerEndTime(PickerTask task, long workerId) throws IOException {
    long estimatedTime = machineLearningModelPicking.estimateTimeUsingModel(
        randomForests.get(task.getZone().getName().toUpperCase()), task, workerId);

    int randomOffset = ThreadLocalRandom.current().nextInt(-5, 6) * 60;

    return task.getStartTime().plusSeconds((int) estimatedTime + randomOffset);
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
    this.speedFactory = (int) speedFactor;
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
    } else if (!isPaused && isPlaying) {
      return 1;
    } else if (!isPlaying && !isPaused) {
      return 0;
    } else {
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

    activeTasksWithDueDates = activeTaskService.getActiveTasksForToday(workday.atTime(currentTime));

    LocalDate finalWorkday = workday;

    activeTasksWithDueDates = activeTasksWithDueDates.stream()
        .sorted(Comparator.comparing(ActiveTask::getDueDate))
        .collect(Collectors.toList());

    System.out.println("Active tasks today: " + activeTasksWithDueDates.size());

    pickerTasksToday = pickerTaskService.getPickerTasksByDate(finalWorkday).stream()
        .sorted(Comparator.comparing(PickerTask::getDueDate))
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

    if (realStartTime.equals(currentTime) && timetable.getWorker().isAvailability()) {
      logAndAddWorker(timetable.getWorker(), "has started working");
    }

    if ((realEndTime.isBefore(currentTime) || realEndTime.equals(currentTime)) &&
        (availableWorkers.contains(timetable.getWorker()) ||
            workersWaitingForTask.contains(timetable.getWorker()))) {
      logAndRemoveWorker(timetable.getWorker(), "has stopped working");
    }
  }

  /**
   * Logs the worker's status and adds them to the list of available workers.
   *
   * @param worker  The worker to log and add.
   * @param message The message to log.
   */
  private void logAndAddWorker(Worker worker, String message) {
    System.out.println(worker.getName() + " " + message);
    availableWorkers.add(worker);
  }

  /**
   * Logs the worker's status and removes them from the list of available workers.
   *
   * @param worker  The worker to log and remove.
   * @param message The message to log.
   */
  private void logAndRemoveWorker(Worker worker, String message) {
    System.out.println(worker.getName() + " " + message);
    availableWorkers.remove(worker);
    workersWaitingForTask.remove(worker);
  }

  public LocalDateTime getCurrentDateTime() {
    if (workday == null || currentTime == null) {
      return LocalDateTime.now();
    } else if (currentTime == LocalTime.of(23, 59)) {
      return LocalDateTime.of(workday, LocalTime.MIDNIGHT);
    }
    return LocalDateTime.of(workday, currentTime);
  }

  public void flushAllWorkerTasks() {
    List<Worker> allWorkers = workerService.getAllWorkers();
    List<ActiveTask> allActiveTasks = activeTaskService.getAllActiveTasks();
    for (Worker worker : allWorkers) {
      worker.setCurrentTask(null);
      worker.setCurrentPickerTask(null);
      workerService.updateWorker(worker.getId(), worker);
    }
    for (ActiveTask activeTask : allActiveTasks) {
      if (activeTask.getStartTime() != null && activeTask.getEndTime() == null) {
        activeTask.setStartTime(null);
        activeTask.setWorkers(null);
        activeTaskService.updateActiveTask(activeTask.getId(), activeTask);
      }
    }
  }

  private void clearCollection(Collection<?> collection) {
    if (collection != null) {
      collection.clear();
    }
  }

  public void flushGraphs() {
    clearCollection(activeTasksInProgress);
    clearCollection(pickerTasksInProgress);
    if (activeTaskEndTimes != null) {
      activeTaskEndTimes.clear();
    }
    if (pickerTaskEndTimes != null) {
      pickerTaskEndTimes.clear();
    }
    clearCollection(availableWorkers);
    clearCollection(busyWorkers);
    clearCollection(workersWaitingForTask);
  }

  public void resetSimulationDate() {
    resetData = true;
    if (!isPlaying) {
      resetTaskData();
    }
  }

  public void setIntervalId(int intervalId) {
    this.intervalId.set(intervalId);
  }

  public int getIntervalId() {
    return intervalId.get();
  }

  public List<Integer> getData(long zoneId) {
    List<Integer> data = new ArrayList<>();

    if (zoneId == 0) {
      data.add(
          timetableService.getTimetablesByDayAndZone(workday.atTime(currentTime), zoneId).size());
      data.add(activeTaskService.getNotCompletedActiveTasksByDateAndZone(workday, zoneId).size()
          + pickerTaskService.getPickerTasksNotDoneForTodayInZone(workday, zoneId).size());
      data.add(completedTaskMap != null
          ? completedTaskMap.values().stream().mapToInt(Integer::intValue).sum()
          : 0);
      data.add(
          timetableService.getTimetabelsOfWorkersWorkingByZone(workday.atTime(currentTime), zoneId)
              .size());
      return data;
    } else if (zoneService.getZoneById(zoneId) == null) {
      return data;
    } else {
      //workers showing up today
      data.add(
          timetableService.getTimetablesByDayAndZone(workday.atTime(currentTime), zoneId).size());
      //not started
      data.add(activeTaskService.getNotCompletedActiveTasksByDateAndZone(workday, zoneId).size()
          + pickerTaskService.getPickerTasksNotDoneForTodayInZone(workday, zoneId).size());
      //completed tasks
      data.add(completedTaskMap != null ?
          completedTaskMap.getOrDefault(zoneId, 0)
          : 0);
      //workers at job
      data.add(
          timetableService.getTimetabelsOfWorkersWorkingByZone(workday.atTime(currentTime), zoneId)
              .size());
      return data;
    }
  }

  public void resetTaskData() {
    isPaused = false;
    flushGraphs();
    workday = LocalDate.now();
    currentTime = LocalTime.MIDNIGHT;
    workerService.removeTasks();
    activeTaskService.deleteAllActiveTasks();
    pickerTaskService.deleteAllPickerTasks();
    timetableService.deleteAllTimetables();

  }

  public int getSpeedFactory() {
    return speedFactory;
  }

  public Map<String, RandomForest> getModels() {
    return randomForests;
  }
}