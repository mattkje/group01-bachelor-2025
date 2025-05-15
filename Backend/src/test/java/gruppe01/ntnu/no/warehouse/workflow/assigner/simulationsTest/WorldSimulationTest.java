package gruppe01.ntnu.no.Warehouse.workflow.assigner.simulationsTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.PickerTaskGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.*;
import gruppe01.ntnu.no.warehouse.workflow.assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.*;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.worldsimulation.WorldSimulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import smile.regression.RandomForest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorldSimulationTest {

  private TimetableService timetableService;
  private ActiveTaskService activeTaskService;
  private ActiveTaskGenerator activeTaskGenerator;
  private TimeTableGenerator timeTableGenerator;
  private SimulationService simulationService;
  private MonteCarloDataService monteCarloDataService;
  private WorkerService workerService;
  private PickerTaskGenerator pickerTaskGenerator;
  private PickerTaskService pickerTaskService;
  private ZoneService zoneService;
  private WorldSimDataService worldSimDataService;
  private NotificationService notificationService;

  private WorldSimulation worldSimulation;

  @BeforeEach
  void setUp() {
    timetableService = mock(TimetableService.class);
    activeTaskService = mock(ActiveTaskService.class);
    activeTaskGenerator = mock(ActiveTaskGenerator.class);
    timeTableGenerator = mock(TimeTableGenerator.class);
    simulationService = mock(SimulationService.class);
    monteCarloDataService = mock(MonteCarloDataService.class);
    workerService = mock(WorkerService.class);
    pickerTaskGenerator = mock(PickerTaskGenerator.class);
    pickerTaskService = mock(PickerTaskService.class);
    zoneService = mock(ZoneService.class);
    worldSimDataService = mock(WorldSimDataService.class);
    notificationService = mock(NotificationService.class);

    worldSimulation = new WorldSimulation(
        timetableService,
        activeTaskService,
        activeTaskGenerator,
        timeTableGenerator,
        simulationService,
        monteCarloDataService,
        workerService,
        pickerTaskGenerator,
        pickerTaskService,
        zoneService,
        worldSimDataService,
        notificationService
    );
  }

  @Test
  void testRunWorldSimulation() throws Exception {
    when(zoneService.getAllPickerZones()).thenReturn(Collections.emptyList());
    when(activeTaskService.getActiveTaskByDate(any())).thenReturn(Collections.emptyList());
    when(timetableService.getTimetablesByDate(any())).thenReturn(Collections.emptyList());
    worldSimulation.runWorldSimulation(0, LocalDate.of(2025, 1, 1), true);
    verify(notificationService).deleteAll();
    verify(monteCarloDataService).flushMCData();
  }

  @Test
  void testGetCurrentTime() {
    assertNotNull(worldSimulation.getCurrentTime());
  }

  @Test
  void testGetCurrentDate() {
    assertNotNull(worldSimulation.getCurrentDate());
  }

  @Test
  void testGetEndTime() {
    ActiveTask task = mock(ActiveTask.class);
    Task t = mock(Task.class);
    when(task.getTask()).thenReturn(t);
    when(t.getMinWorkers()).thenReturn(1);
    when(t.getMaxWorkers()).thenReturn(2);
    when(t.getMaxTime()).thenReturn(60);
    when(t.getMinTime()).thenReturn(30);
    when(task.getId()).thenReturn(1L);
    when(task.getStartTime()).thenReturn(LocalDateTime.now());
    when(activeTaskService.getWorkersAssignedToTask(anyLong())).thenReturn(List.of(mock(Worker.class)));
    LocalDateTime endTime = worldSimulation.getEndTime(task);
    assertNotNull(endTime);
  }

  @Test
  void testGetPickerEndTime() throws IOException {
    PickerTask task = mock(PickerTask.class);
    Zone zone = mock(Zone.class);
    when(zone.getName()).thenReturn("A");
    when(task.getZone()).thenReturn(zone);
    when(task.getStartTime()).thenReturn(LocalDateTime.now());
    // Set up model
    worldSimulation.getModels().put("A", mock(RandomForest.class));
    // Set up ML model
    var mlModel = mock(MachineLearningModelPicking.class);
    // Use reflection to set private field
    try {
      var field = WorldSimulation.class.getDeclaredField("machineLearningModelPicking");
      field.setAccessible(true);
      field.set(worldSimulation, mlModel);
    } catch (Exception e) {
      fail(e);
    }
    when(mlModel.estimateTimeUsingModel(any(), any(), anyLong())).thenReturn(1000L);
    LocalDateTime endTime = worldSimulation.getPickerEndTime(task, 1L);
    assertNotNull(endTime);
  }

  @Test
  void testPauseSimulation() throws Exception {
    // Should not throw
    worldSimulation.pauseSimulation();
  }

  @Test
  void testStopSimulation() throws Exception {
    // Should not throw
    worldSimulation.stopSimulation();
  }

  @Test
  void testChangeSimulationSpeed() {
    worldSimulation.changeSimulationSpeed(1.0);
    assertTrue(worldSimulation.getSpeedFactory() >= 0);
  }

  @Test
  void testGetPauseStatus() {
    int status = worldSimulation.getPauseStatus();
    assertTrue(status >= -1 && status <= 2);
  }

  @Test
  void testSimulateOneYear() throws Exception {
    when(zoneService.getAllPickerZones()).thenReturn(Collections.emptyList());
    when(activeTaskService.getActiveTaskByDate(any())).thenReturn(Collections.emptyList());
    when(timetableService.getTimetablesByDate(any())).thenReturn(Collections.emptyList());
    // Should not throw
    worldSimulation.simulateOneYear();
  }

  @Test
  void testFilterData() {
    when(timetableService.getTimetablesByDate(any())).thenReturn(Collections.emptyList());
    when(activeTaskService.getActiveTasksForToday(any())).thenReturn(Collections.emptyList());
    when(pickerTaskService.getPickerTasksByDate(any())).thenReturn(Collections.emptyList());
    worldSimulation.filterData();
  }

  @Test
  void testGetCurrentDateTime() {
    assertNotNull(worldSimulation.getCurrentDateTime());
  }

  @Test
  void testFlushAllWorkerTasks() {
    when(workerService.getAllWorkers()).thenReturn(Collections.emptyList());
    when(activeTaskService.getAllActiveTasks()).thenReturn(Collections.emptyList());
    worldSimulation.flushAllWorkerTasks();
  }

  @Test
  void testFlushGraphs() {
    worldSimulation.flushGraphs();
  }

  @Test
  void testResetSimulationDate() {
    worldSimulation.resetSimulationDate();
  }

  @Test
  void testSetAndGetIntervalId() {
    worldSimulation.setIntervalId(2);
    assertEquals(2, worldSimulation.getIntervalId());
  }

  @Test
  void testGetDataForZoneZero() {
    when(timetableService.getTimetablesByDayAndZone(any(), eq(0L))).thenReturn(List.of(mock(Timetable.class)));
    when(activeTaskService.getNotCompletedActiveTasksByDateAndZone(any(), eq(0L))).thenReturn(List.of());
    when(pickerTaskService.getPickerTasksNotDoneForTodayInZone(any(), eq(0L))).thenReturn(List.of());
    when(timetableService.getTimetabelsOfWorkersWorkingByZone(any(), eq(0L))).thenReturn(List.of());
    List<Integer> data = worldSimulation.getData(0L);
    assertNotNull(data);
  }

  @Test
  void testGetDataForNonExistentZone() {
    when(zoneService.getZoneById(99L)).thenReturn(null);
    List<Integer> data = worldSimulation.getData(99L);
    assertTrue(data.isEmpty());
  }

  @Test
  void testResetTaskData() {
    doNothing().when(workerService).removeTasks();
    doNothing().when(activeTaskService).deleteAllActiveTasks();
    doNothing().when(pickerTaskService).deleteAllPickerTasks();
    doNothing().when(timetableService).deleteAllTimetables();
    worldSimulation.resetTaskData();
  }

  @Test
  void testGetSpeedFactory() {
    assertEquals(0, worldSimulation.getSpeedFactory());
  }

  @Test
  void testGetModels() {
    assertNotNull(worldSimulation.getModels());
  }

  @Test
  void testGetEndTimeWithNoWorkersAssigned() {
    ActiveTask task = mock(ActiveTask.class);
    Task t = mock(Task.class);
    when(task.getTask()).thenReturn(t);
    when(t.getMinWorkers()).thenReturn(1);
    when(t.getMaxWorkers()).thenReturn(2);
    when(t.getMaxTime()).thenReturn(60);
    when(t.getMinTime()).thenReturn(30);
    when(task.getId()).thenReturn(1L);
    when(task.getStartTime()).thenReturn(LocalDateTime.now());
    when(activeTaskService.getWorkersAssignedToTask(anyLong())).thenReturn(Collections.emptyList());
    LocalDateTime endTime = worldSimulation.getEndTime(task);
    assertNotNull(endTime);
  }

  @Test
  void testChangeSimulationSpeedNegative() {
    worldSimulation.changeSimulationSpeed(-1.0);
    assertTrue(worldSimulation.getSpeedFactory() <= 0);
  }

  @Test
  void testSetAndGetIntervalIdNegative() {
    worldSimulation.setIntervalId(-1);
    assertEquals(-1, worldSimulation.getIntervalId());
  }
}