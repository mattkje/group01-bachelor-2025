package gruppe01.ntnu.no.warehouse.workflow.assigner.serviceTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.*;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.*;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZoneServiceTest {

  @Mock
  private ZoneRepository zoneRepository;

  @Mock
  private ActiveTaskRepository activeTaskRepository;

  @InjectMocks
  private ZoneService zoneService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllZones() {
    List<Zone> zones = List.of(new Zone(), new Zone());
    when(zoneRepository.findAllWithTasksAndWorkersAndLicenses()).thenReturn(zones);

    List<Zone> result = zoneService.getAllZones();

    assertEquals(2, result.size());
    verify(zoneRepository, times(1)).findAllWithTasksAndWorkersAndLicenses();
  }

  @Test
  void testGetZoneById() {
    Zone zone = new Zone();
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));

    Zone result = zoneService.getZoneById(1L);

    assertNotNull(result);
    verify(zoneRepository, times(1)).findById(1L);
  }

  @Test
  void testAddZone() {
    Zone zone = new Zone();
    zone.setPickerTask(null);
    zone.setTasks(null);

    when(zoneRepository.save(zone)).thenReturn(zone);

    Zone result = zoneService.addZone(zone);

    assertNotNull(result);
    verify(zoneRepository, times(1)).save(zone);
  }

  @Test
  void testUpdateZone() {
    Zone existingZone = new Zone();
    Zone updatedZone = new Zone();
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(existingZone));
    when(zoneRepository.save(existingZone)).thenReturn(existingZone);

    Zone result = zoneService.updateZone(1L, updatedZone);

    assertNotNull(result);
    verify(zoneRepository, times(1)).save(existingZone);
  }

  @Test
  void testDeleteZone() {
    Zone zone = new Zone();
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));

    Zone result = zoneService.deleteZone(1L);

    assertNotNull(result);
    verify(zoneRepository, times(1)).delete(zone);
  }

  @Test
  void testGetWorkersByZoneId() {
    Zone zone = new Zone();
    Set<Worker> workers = new HashSet<>(List.of(new Worker(), new Worker()));
    zone.setWorkers(workers);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));

    Set<Worker> result = zoneService.getWorkersByZoneId(1L);

    assertEquals(2, result.size());
    verify(zoneRepository, times(1)).findById(1L);
  }

  @Test
  void testGetTasksByZoneId() {
    Zone zone = new Zone();
    Set<Task> tasks = new HashSet<>(List.of(new Task(), new Task()));
    zone.setTasks(tasks);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));

    Set<Task> result = zoneService.getTasksByZoneId(1L);

    assertEquals(2, result.size());
    verify(zoneRepository, times(1)).findById(1L);
  }

  @Test
  void testGetTodaysUnfinishedTasksByZoneId() {
    // Arrange
    ActiveTask task1 = new ActiveTask();
    ActiveTask task2 = new ActiveTask();
    task1.setId(1L); // Ensure unique IDs
    task2.setId(2L);
    task1.setStartTime(LocalDateTime.now());
    task2.setStartTime(LocalDateTime.now());

    Set<ActiveTask> tasks = new HashSet<>();
    tasks.add(task1);
    tasks.add(task2);

    when(activeTaskRepository.findTodayUnfinishedTasksByZoneId(1L, LocalDate.now())).thenReturn(tasks);

    // Act
    Set<ActiveTask> result = zoneService.getTodayUnfinishedTasksByZoneId(1L);

    // Assert
    assertEquals(2, result.size()); // Ensure the size matches the expected value
    verify(activeTaskRepository, times(1)).findTodayUnfinishedTasksByZoneId(1L, LocalDate.now());
  }


}