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
  void testGetTodaysTimetables() {
    Timetable timetable = new Timetable();
    timetable.setStartTime(LocalDateTime.now());
    when(timetableRepository.findAll()).thenReturn(List.of(timetable));

    List<Timetable> result = timetableService.getTodaysTimetables();

    assertEquals(1, result.size());
    verify(timetableRepository, times(1)).findAll();
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
}