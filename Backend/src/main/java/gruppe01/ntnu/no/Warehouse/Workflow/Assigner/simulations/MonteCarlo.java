package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.SimulationResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import smile.regression.RandomForest;

/**
 * Monte Carlo Simulation of a warehouse time to complete all tasks within a day.
 * Handles a workday with both picker and non-picker zones
 * Simulates the current worker layout from the time it is run, not the start of the day.
 */
@Service
public class MonteCarlo {

    private final ZoneService zoneService;

    private final ActiveTaskService activeTaskService;

    private final PickerTaskService pickerTaskService;

    private final Utils utils;

    /**
     * Constructor for MonteCarlo.
     *
     * @param zoneService         the service for Zone entity
     * @param activeTaskService   the service for ActiveTask entity
     * @param pickerTaskService   the service for PickerTask entity
     * @param utils               utility class for simulations
     */
    public MonteCarlo(@Autowired ZoneService zoneService, @Autowired ActiveTaskService activeTaskService, @Autowired PickerTaskService pickerTaskService, @Autowired Utils utils) {
        this.zoneService = zoneService;
        this.activeTaskService = activeTaskService;
        this.pickerTaskService = pickerTaskService;
        this.utils = utils;
    }

    private static final MachineLearningModelPicking mlModel = new MachineLearningModelPicking();

    /**
     * Runs several Monte Carlo simulation on the warehouse.
     * Each zone is threaded and run in parallel.
     * Each task is run in parallel.
     * No database entries are modified during the simulation, only deep copies.
     *
     * @param simCount         The number of simulations to run
     * @param models           The models to use for calculating the time to complete a picker task
     * @param currentTime      The current time to use for the simulation (if null, the current time is used)
     * @param timetableService The timetable service to use for getting the current time
     * @return A list of simulation results, one for each simulation
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Transactional
    public List<SimulationResult> monteCarlo(int simCount, Map<String, RandomForest> models, LocalDateTime currentTime, TimetableService timetableService)
            throws InterruptedException, ExecutionException, IOException {
        // Zone simulator object to run the simulation
        ExecutorService simulationExecutor =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // List of futures to hold the results of the simulation
        List<Future<SimulationResult>> futures = new ArrayList<>();
        // Get all zones and active tasks for today
        List<Zone> zones = zoneService.getAllZones();
        // if no time is given, use the current time
        if (currentTime == null) {
            currentTime = LocalDateTime.now();
        }
        List<ActiveTask> activeTasks = activeTaskService.getActiveTasksForToday(currentTime);
        // if no models are given, get them from the database
        if (models == null) {
            models = mlModel.getAllModels();
        }
        // Initialize lazy-loaded collections
        activeTasks.forEach(task -> Hibernate.initialize(task.getWorkers()));

        // for loop iterating over the number of simulations
        for (int i = 0; i < simCount; i++) {
            ;
            // Effectively final variables for the lambda expression
            Map<String, RandomForest> finalModels = models;
            LocalDateTime finalCurrentTime = currentTime;
            // Submit the simulation to the executor service
            futures.add(simulationExecutor.submit(() -> {
                ExecutorService warehouseExecutor = Executors.newFixedThreadPool(zones.size());
                // Create a deep copy of the zones and active tasks
                List<Zone> zonesCopy = zones.stream().map(Zone::new).toList();
                List<ActiveTask> activeTasksCopy = activeTasks.stream()
                        .map(ActiveTask::new)
                        .toList();
                Set<PickerTask> pickerTasksCopy =
                        pickerTaskService.getPickerTasksForToday().stream().map(PickerTask::new)
                                .collect(Collectors.toSet());
                // Create a map to hold the results of the simulation
                Map<Long, ZoneSimResult> zoneSimResults = new HashMap<>();
                // Iterate over the zones and run the simulation for each zone
                for (Zone zone : zonesCopy) {
                    ZoneSimulator zoneSimulator = new ZoneSimulator();
                    warehouseExecutor.submit(() -> {
                        try {
                            ZoneSimResult zoneSimResult = new ZoneSimResult();
                            // run the simulation for a non picker zone
                            if (!zone.getIsPickerZone()) {
                                List<ActiveTask> zoneTasks = activeTasksCopy.stream()
                                        .filter(activeTask -> Objects.equals(activeTask.getTask().getZoneId(),
                                                zone.getId()))
                                        .toList();
                                zoneSimResult = zoneSimulator.runZoneSimulation(zone, zoneTasks, null, null,
                                        finalCurrentTime, timetableService);
                            } // run the simulation for a picker zone
                            else {
                                Set<PickerTask> zoneTasks = pickerTasksCopy.stream()
                                        .filter(pickerTask -> Objects.equals(pickerTask.getZoneId(), zone.getId()))
                                        .collect(Collectors.toSet());
                                zoneSimResult = zoneSimulator.runZoneSimulation(zone, null, zoneTasks,
                                        finalModels.get(zone.getName().toUpperCase()), finalCurrentTime, timetableService);
                            }
                            synchronized (zoneSimResults) {
                                // Add the result to the map
                                zoneSimResults.put(zone.getId(), zoneSimResult);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                warehouseExecutor.shutdown();
                if (!warehouseExecutor.awaitTermination(5, TimeUnit.MINUTES)) {
                    System.err.println("WarehouseExecutor did not terminate in the specified time.");
                    warehouseExecutor.shutdownNow(); // Force shutdown
                }
                return new SimulationResult(utils.getLatestEndTime(zoneSimResults), zoneSimResults);
            }));
        }

        List<SimulationResult> results = new ArrayList<>();
        for (Future<SimulationResult> future : futures) {
            results.add(future.get());
        }
        simulationExecutor.shutdown();
        return results;
    }


}