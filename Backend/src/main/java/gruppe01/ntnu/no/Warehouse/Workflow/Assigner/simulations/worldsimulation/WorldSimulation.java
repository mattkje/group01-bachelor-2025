package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class WorldSimulation {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private TimetableService timetableService;

    private void runWorldSimulation(int simulationTime) throws InterruptedException {
        int currentTime = 0;
        int maxTime = 1440;
        double simulationSleep = (double) simulationTime / maxTime;
        double simulationSleepInMillis = simulationSleep * 1000;

        List<Worker> workers = workerService.getAllWorkers();
        List<Timetable> timetables = timetableService.getAllTimetables();

        while (currentTime < maxTime) {

            TimeUnit.MILLISECONDS.sleep((long) simulationSleepInMillis);
            currentTime += 1;
        }
    }
}
