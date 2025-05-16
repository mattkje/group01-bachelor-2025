package gruppe01.ntnu.no.warehouse.workflow.assigner.serviceTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Timetable;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.TimetableRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TimetableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimetableServiceTest {

  @Mock
  private TimetableRepository timetableRepository;

  @Mock
  private WorkerRepository workerRepository;

  @InjectMocks
  private TimetableService timetableService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllTimetables() {
    List<Timetable> timetables = List.of(new Timetable(), new Timetable());
    when(timetableRepository.findAll()).thenReturn(timetables);

    List<Timetable> result = timetableService.getAllTimetables();

    assertEquals(2, result.size());
    verify(timetableRepository, times(1)).findAll();
  }

  @Test
  void testAddTimetable() {
    Timetable timetable = new Timetable();
    Worker worker = new Worker();
    when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
    when(timetableRepository.save(timetable)).thenReturn(timetable);

    Timetable result = timetableService.addTimetable(timetable, 1L);

    assertNotNull(result);
    assertEquals(worker, timetable.getWorker());
    verify(timetableRepository, times(1)).save(timetable);
  }

  @Test
  void testSetStartTime() {
    Timetable timetable = new Timetable();
    when(timetableRepository.findById(1L)).thenReturn(Optional.of(timetable));
    when(timetableRepository.save(timetable)).thenReturn(timetable);

    Timetable result = timetableService.setStartTime(1L);

    assertNotNull(result);
    assertNotNull(result.getStartTime());
    verify(timetableRepository, times(1)).save(timetable);
  }

  @Test
  void testDeleteAllTimetables() {
    timetableService.deleteAllTimetables();

    verify(timetableRepository, times(1)).deleteAll();
  }

  @Test
  void testDeleteTimetable() {
    timetableService.deleteTimetable(1L);

    verify(timetableRepository, times(1)).deleteById(1L);
  }

  @Test
  void testGetTimetableById() {
      // Arrange
      Timetable timetable = new Timetable();
      when(timetableRepository.findById(1L)).thenReturn(Optional.of(timetable));

      // Act
      Timetable result = timetableService.getTimetableById(1L);

      // Assert
      assertNotNull(result);
      assertEquals(timetable, result);
      verify(timetableRepository, times(1)).findById(1L);
  }

  @Test
  void testGetTodaysTimetables() {
      // Arrange
      List<Timetable> timetables = List.of(new Timetable());
      when(timetableRepository.findByRealStartDate(LocalDate.now())).thenReturn(timetables);

      // Act
      List<Timetable> result = timetableService.getTodaysTimetables();

      // Assert
      assertEquals(1, result.size());
      verify(timetableRepository, times(1)).findByRealStartDate(LocalDate.now());
  }

  @Test
  void testGetTodayTimetablesByZone() {
      // Arrange
      List<Timetable> timetables = List.of(new Timetable());
      when(timetableRepository.findTodayTimetablesByZone(1L, LocalDate.now())).thenReturn(timetables);

      // Act
      List<Timetable> result = timetableService.getTodayTimetablesByZone(1L);

      // Assert
      assertEquals(1, result.size());
      verify(timetableRepository, times(1)).findTodayTimetablesByZone(1L, LocalDate.now());
  }

  @Test
  void testGetAllTimetablesByZone() {
      // Arrange
      List<Timetable> timetables = List.of(new Timetable());
      when(timetableRepository.findAllByZoneId(1L)).thenReturn(timetables);

      // Act
      List<Timetable> result = timetableService.getAllTimetablesByZone(1L);

      // Assert
      assertEquals(1, result.size());
      verify(timetableRepository, times(1)).findAllByZoneId(1L);
  }

  @Test
  void testGetTimetablesByDate() {
      // Arrange
      List<Timetable> timetables = List.of(new Timetable());
      when(timetableRepository.findByStartDate(LocalDate.now())).thenReturn(timetables);

      // Act
      List<Timetable> result = timetableService.getTimetablesByDate(LocalDate.now());

      // Assert
      assertEquals(1, result.size());
      verify(timetableRepository, times(1)).findByStartDate(LocalDate.now());
  }

  @Test
  void testUpdateTimetable() {
      // Arrange
      Timetable existingTimetable = new Timetable();
      Timetable updatedTimetable = new Timetable();
      updatedTimetable.setStartTime(LocalDateTime.now());
      updatedTimetable.setEndTime(LocalDateTime.now().plusHours(1));
      when(timetableRepository.findById(1L)).thenReturn(Optional.of(existingTimetable));
      when(timetableRepository.save(existingTimetable)).thenReturn(existingTimetable);

      // Act
      Timetable result = timetableService.updateTimetable(1L, updatedTimetable);

      // Assert
      assertNotNull(result);
      assertEquals(updatedTimetable.getStartTime(), existingTimetable.getStartTime());
      assertEquals(updatedTimetable.getEndTime(), existingTimetable.getEndTime());
      verify(timetableRepository, times(1)).findById(1L);
      verify(timetableRepository, times(1)).save(existingTimetable);
  }

  @Test
  void testGetTimetablesByDayAndZone() {
      // Arrange
      List<Timetable> timetables = List.of(new Timetable());
      when(timetableRepository.findByDayAndZone(LocalDate.now(), 1L)).thenReturn(timetables);

      // Act
      List<Timetable> result = timetableService.getTimetablesByDayAndZone(LocalDateTime.now(), 1L);

      // Assert
      assertEquals(1, result.size());
      verify(timetableRepository, times(1)).findByDayAndZone(LocalDate.now(), 1L);
  }
@Test
void testWorkerIsWorking() {
    Timetable timetable = new Timetable();
    LocalDateTime now = LocalDateTime.now();
    timetable.setRealStartTime(now.minusHours(1));
    timetable.setRealEndTime(now.plusHours(1));
    when(timetableRepository.findWorkerTimetableForDay(1L, now))
        .thenReturn(List.of(timetable));

    boolean result = timetableService.workerIsWorking(now, 1L);

    assertTrue(result);
    verify(timetableRepository, times(1)).findWorkerTimetableForDay(1L, now);
}

  @Test
  void testWorkerHasFinishedShift() {
    Timetable timetable = new Timetable();
    LocalDateTime now = LocalDateTime.now();
    timetable.setRealEndTime(now.minusHours(1));
    when(timetableRepository.findWorkerTimetableForDay(1L, now))
          .thenReturn(List.of(timetable));

      // Act
      boolean result = timetableService.workerHasFinishedShift(1L, now);

      // Assert
      assertTrue(result);
      verify(timetableRepository, times(1)).findWorkerTimetableForDay(1L, now);
  }

  @Test
  void testCountWorkersNotFinished() {
      // Arrange
      Timetable timetable = new Timetable();
      Worker worker = new Worker();
      worker.setAvailability(true);
      worker.setZone(1L);
      timetable.setWorker(worker);
      timetable.setRealEndTime(LocalDateTime.now().plusHours(1));
      when(timetableRepository.findByStartDate(LocalDate.now())).thenReturn(List.of(timetable));

      // Act
      int result = timetableService.countWorkersNotFinished(1L, LocalDateTime.now());

      // Assert
      assertEquals(1, result);
      verify(timetableRepository, times(1)).findByStartDate(LocalDate.now());
  }
}