package gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.subsimulations;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TimetableService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.results.ZoneSimResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import smile.regression.RandomForest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZoneSimulatorTest {

  private ZoneSimulator zoneSimulator;

  @Mock
  private TimetableService timetableService;

  @Mock
  private RandomForest randomForest;

  private Zone testZone;
  private List<ActiveTask> activeTasks;
  private Set<PickerTask> pickerTasks;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    zoneSimulator = new ZoneSimulator();

    // Initialize test data
    testZone = new Zone();
    testZone.setId(1L);
    testZone.setWorkers(new HashSet<>());

    activeTasks = new ArrayList<>();
    pickerTasks = new HashSet<>();
  }

  @Test
  void testRunZoneSimulation_NoTasks() {
    ZoneSimResult result = zoneSimulator.runZoneSimulation(
        testZone, activeTasks, pickerTasks, randomForest, LocalDateTime.now(), timetableService);

    assertNotNull(result);
    assertEquals("101", result.getErrorMessage().getFirst()); // Error code for no tasks
  }

  @Test
  void testRunZoneSimulation_NoWorkers() {
    PickerTask pickerTask = new PickerTask();
    pickerTask.setZone(testZone);
    pickerTasks.add(pickerTask);

    ZoneSimResult result = zoneSimulator.runZoneSimulation(
        testZone, activeTasks, pickerTasks, randomForest, LocalDateTime.now(), timetableService);

    assertNotNull(result);
    assertEquals("102", result.getErrorMessage().getFirst()); // Error code for no workers
  }

  @Test
  void testRunZoneSimulation_NoWorkersScheduled() {
    Worker worker = new Worker();
    worker.setAvailability(false);
    testZone.getWorkers().add(worker);

    PickerTask pickerTask = new PickerTask();
    pickerTask.setZone(testZone);
    pickerTasks.add(pickerTask);

    when(timetableService.getFirstStartTimeByZoneAndDay(anyLong(), any(LocalDateTime.class)))
        .thenReturn(LocalDateTime.now().toLocalDate().atStartOfDay());

    ZoneSimResult result = zoneSimulator.runZoneSimulation(
        testZone, activeTasks, pickerTasks, randomForest, LocalDateTime.now(), timetableService);

    assertNotNull(result);
    assertEquals("102", result.getErrorMessage().getFirst()); // Error code for no workers scheduled
  }

  @Test
  void testRunZoneSimulation_Success() {
    Worker worker = new Worker();
    worker.setAvailability(true);
    testZone.getWorkers().add(worker);

    PickerTask pickerTask = new PickerTask();
    pickerTask.setZone(testZone);
    pickerTasks.add(pickerTask);

    when(timetableService.getFirstStartTimeByZoneAndDay(anyLong(), any(LocalDateTime.class)))
        .thenReturn(LocalDateTime.now());

    ZoneSimResult result = zoneSimulator.runZoneSimulation(
        testZone, activeTasks, pickerTasks, randomForest, LocalDateTime.now(), timetableService);

    assertNotNull(result);
    assertEquals("104:null:null", result.getErrorMessage().getFirst());
  }
}