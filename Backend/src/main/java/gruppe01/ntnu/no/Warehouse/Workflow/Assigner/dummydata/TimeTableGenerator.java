package gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

/**
 * Class for generating a timetable for the simulation generates a timetable x amount of time.
 * Only used inside the world simulation to ensure it runs smoothly and realistically.
 */
@Service
public class TimeTableGenerator {

  private final WorkerService workerService;

  public TimeTableGenerator(WorkerService workerService) {
    this.workerService = workerService;
  }

  public void generateTimeTable(LocalDate date) {
    for (Worker worker : workerService.getAllWorkers()) {
      workerService.createTimetablesTillNextMonth(date, worker);
    }

  }
}
