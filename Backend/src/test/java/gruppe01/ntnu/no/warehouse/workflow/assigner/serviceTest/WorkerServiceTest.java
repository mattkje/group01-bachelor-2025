package gruppe01.ntnu.no.warehouse.workflow.assigner.serviceTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.License;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Task;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Timetable;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.WorkerTimeRange;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.LicenseRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.TimetableRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.LicenseService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkerServiceTest {

  @Mock
  private WorkerRepository workerRepository;

  @Mock
  private LicenseRepository licenseRepository;

  @Mock
  private TimetableRepository timetableRepository;

  @InjectMocks
  private WorkerService workerService;


  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllWorkers() {
    List<Worker> workers = List.of(new Worker(), new Worker());
    when(workerRepository.findAllWithLicenses()).thenReturn(workers);

    List<Worker> result = workerService.getAllWorkers();

    assertEquals(2, result.size());
    verify(workerRepository, times(1)).findAllWithLicenses();
  }

  @Test
  void testAddWorker() {
    Worker worker = new Worker();
    worker.setWorkSchedule(
        Map.of(DayOfWeek.MONDAY, new WorkerTimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))));
    when(workerRepository.save(worker)).thenReturn(worker);

    Worker result = workerService.addWorker(worker);

    assertNotNull(result);
    verify(timetableRepository, times(1)).saveAll(anyList());
    verify(workerRepository, times(1)).save(worker);
  }


  @Test
  void testAddLicenseToWorker() {
    Worker worker = new Worker();
    License license = new License();
    when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
    when(licenseRepository.findById(1L)).thenReturn(Optional.of(license));
    when(workerRepository.save(worker)).thenReturn(worker);

    Worker result = workerService.addLicenseToWorker(1L, 1L);

    assertNotNull(result);
    verify(workerRepository, times(1)).save(worker);
  }

  @Test
  void testDeleteWorker() {
    Worker worker = new Worker();
    when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
    when(workerRepository.save(worker)).thenReturn(worker);

    Worker result = workerService.deleteWorker(1L);

    assertNotNull(result);
    assertTrue(result.isDead());
    verify(workerRepository, times(1)).save(worker);
  }

  @Test
  void testGetAvailableWorkers() {
      List<Worker> workers = List.of(new Worker(), new Worker());
      when(workerRepository.findAvailableWorkers()).thenReturn(workers);

      List<Worker> result = workerService.getAvailableWorkers();

      assertEquals(2, result.size());
      verify(workerRepository, times(1)).findAvailableWorkers();
  }

  @Test
  void testGetUnavailableWorkers() {
      List<Worker> workers = List.of(new Worker());
      when(workerRepository.findUnavailableWorkers()).thenReturn(workers);

      List<Worker> result = workerService.getUnavailableWorkers();

      assertEquals(1, result.size());
      verify(workerRepository, times(1)).findUnavailableWorkers();
  }

  @Test
  void testUpdateWorkerAvailability() {
      Worker worker = new Worker();
      worker.setAvailability(true);
      when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
      when(workerRepository.save(worker)).thenReturn(worker);

      Worker result = workerService.updateWorkerAvailability(1L);

      assertNotNull(result);
      assertFalse(result.isAvailability());
      verify(workerRepository, times(1)).save(worker);
  }

  @Test
  void testAddWorkerToZone() {
      Worker worker = new Worker();
      when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
      when(workerRepository.save(worker)).thenReturn(worker);

      Worker result = workerService.addWorkerToZone(1L, 2L);

      assertNotNull(result);
      assertEquals(2L, result.getZone());
      verify(workerRepository, times(1)).save(worker);
  }

  @Test
  void testCreateWorkerTimetablesForNextMonth() {
      Worker worker = new Worker();
      worker.setWorkSchedule(Map.of(DayOfWeek.MONDAY, new WorkerTimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))));
      when(workerRepository.findAll()).thenReturn(List.of(worker));

      workerService.createWorkerTimetablesForNextMonth(LocalDate.now());

      verify(timetableRepository, times(1)).saveAll(anyList());
  }

  @Test
  void testAddAllLicensesToWorkers() {
      Worker worker = new Worker();
      License license = new License();
      when(workerRepository.findAll()).thenReturn(List.of(worker));
      when(licenseRepository.findAll()).thenReturn(List.of(license));
      when(workerRepository.save(worker)).thenReturn(worker);

      List<Worker> result = workerService.addAllLicensesToWorkers();

      assertEquals(1, result.size());
      assertTrue(result.get(0).getLicenses().contains(license));
      verify(workerRepository, times(1)).save(worker);
  }

  @Test
  void testRemoveTasks() {
      Worker worker = new Worker();
      worker.setCurrentTask(new ActiveTask());
      worker.setCurrentPickerTask(new PickerTask());
      when(workerRepository.findAll()).thenReturn(List.of(worker));
      when(workerRepository.save(worker)).thenReturn(worker);

      workerService.removeTasks();

      assertNull(worker.getCurrentActiveTask());
      assertNull(worker.getCurrentPickerTask());
      verify(workerRepository, times(1)).save(worker);
  }
}