package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import com.google.common.util.concurrent.AtomicDouble;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.subsimulations.ZoneSimulator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {


  @Autowired
  private ActiveTaskService activeTaskService;

  @Autowired
  private ZoneService zoneService;


  public List<String> runZoneSimulation(Long zoneId) {

      List<String> response = new ArrayList<>();
      AtomicDouble predictedTime = new AtomicDouble(0.0);

      String errorMessages = ZoneSimulator.runZoneSimulation(zoneService.getZoneById(zoneId),
          activeTaskService.getRemainingTasksForTodayByZone(zoneId), predictedTime);

      // Get the current time
      LocalDateTime currentTime = LocalDateTime.now();

      // Add the predicted time (in minutes) to the current time
      LocalDateTime predictedCompletionTime = currentTime.plusMinutes((long) predictedTime.get());

      // Format the resulting LocalDateTime to a string in the format HH:mm
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
      String formattedTime = predictedCompletionTime.format(formatter);

      response.add(formattedTime);
      response.add(errorMessages);

      return response;
  }

}
