package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorkerService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Class for generating a timetable for the simulation generates a timetable x amount og time.
 * Only used inside the world simulation to ensure it runs smoothly and realistic
 */
@Service
public class TimeTableGenerator {

  @Autowired
  private WorkerService workerService;

  @Autowired
  private TimetableService timetableService;


  public void generateTimeTable(LocalDate startDate, int numDays) {

    startDate = Objects.requireNonNullElse(startDate, LocalDate.now());
    numDays = Math.max(numDays, 1);

    LocalDate endDate = startDate.plusDays(numDays - 1);

    // range of change that a percentage of workers are available
    double minWorkersPercentage = 0.7;
    double maxWorkersPercentage = 0.9;

    // Times in which works starts and ends
    int[] startHours = {3,4,5,6,7,8,9,15,16,18};
    int[] minutes = {0,30};
    int[] endHours = {3,8,9,10,11,12,13,14,15,16,17,18,19,20,21};

    List<Worker> workers = workerService.getAllWorkers();

    while (!startDate.isAfter(endDate)){
      double workerPercentage = minWorkersPercentage + (Math.random() * (maxWorkersPercentage - minWorkersPercentage));
      for (Worker worker : workers){
        if (Math.random() < workerPercentage){

          int startHour = startHours[(int) (Math.random() * startHours.length)];
          int endHour = startHour + 8;
          int startMinute = minutes[(int) (Math.random() * minutes.length)];
          int endMinute = minutes[(int) (Math.random() * minutes.length)];

          LocalDateTime startTime = startDate.atTime(startHour, startMinute);
          LocalDateTime endTime = startDate.atTime(endHour, endMinute);

          timetableService.addTimetable(new Timetable(startTime, endTime, worker));

        }

      }

    }

  }
}
