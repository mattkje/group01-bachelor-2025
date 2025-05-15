package gruppe01.ntnu.no.warehouse.workflow.assigner.serviceTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.ActiveTaskRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.TaskRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ActiveTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ActiveTaskServiceTest {

  @Mock
  private ActiveTaskRepository activeTaskRepository;

  @InjectMocks
  private ActiveTaskService activeTaskService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllActiveTasks() {
    // Arrange
    ActiveTask task1 = new ActiveTask();
    ActiveTask task2 = new ActiveTask();
    when(activeTaskRepository.findAllWithTasksAndWorkersAndLicenses()).thenReturn(
        List.of(task1, task2));

    // Act
    List<ActiveTask> result = activeTaskService.getAllActiveTasks();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(activeTaskRepository, times(1)).findAllWithTasksAndWorkersAndLicenses();
  }

  @Test
  void testGetActiveTaskById() {
    // Arrange
    ActiveTask task = new ActiveTask();
    when(activeTaskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));

    // Act
    ActiveTask result = activeTaskService.getActiveTaskById(1L);

    // Assert
    assertNotNull(result);
    verify(activeTaskRepository, times(1)).findById(1L);
  }

  @Test
  void testGetActiveTasksForToday() {
      // Arrange
      LocalDateTime currentTime = LocalDateTime.now();
      ActiveTask task1 = new ActiveTask();
      ActiveTask task2 = new ActiveTask();
      when(activeTaskRepository.findByDateAndEndTimeIsNull(currentTime.toLocalDate()))
          .thenReturn(List.of(task1, task2));

      // Act
      List<ActiveTask> result = activeTaskService.getActiveTasksForToday(currentTime);

      // Assert
      assertNotNull(result);
      assertEquals(2, result.size());
      verify(activeTaskRepository, times(1)).findByDateAndEndTimeIsNull(currentTime.toLocalDate());
  }

  @Test
  void testGetNotStartedActiveTasks() {
      // Arrange
      ActiveTask task1 = new ActiveTask();
      ActiveTask task2 = new ActiveTask();
      when(activeTaskRepository.findByStartTimeIsNullAndEndTimeIsNull())
          .thenReturn(List.of(task1, task2));

      // Act
      List<ActiveTask> result = activeTaskService.getNotStartedActiveTasks();

      // Assert
      assertNotNull(result);
      assertEquals(2, result.size());
      verify(activeTaskRepository, times(1)).findByStartTimeIsNullAndEndTimeIsNull();
  }

  @Test
  void testGetActiveTasksInProgress() {
      // Arrange
      ActiveTask task1 = new ActiveTask();
      ActiveTask task2 = new ActiveTask();
      when(activeTaskRepository.findByStartTimeIsNotNullAndEndTimeIsNull())
          .thenReturn(List.of(task1, task2));

      // Act
      List<ActiveTask> result = activeTaskService.getActiveTasksInProgress();

      // Assert
      assertNotNull(result);
      assertEquals(2, result.size());
      verify(activeTaskRepository, times(1)).findByStartTimeIsNotNullAndEndTimeIsNull();
  }

  @Test
  void testDeleteActiveTask() {
      // Arrange
      Long taskId = 1L;
      ActiveTask task = new ActiveTask();
      task.setWorkers(new ArrayList<>());
      when(activeTaskRepository.findById(taskId)).thenReturn(java.util.Optional.of(task));

      // Act
      ActiveTask result = activeTaskService.deleteActiveTask(taskId);

      // Assert
      assertNotNull(result);
      verify(activeTaskRepository, times(1)).delete(task);
  }

  @Test
  void testDeleteAllActiveTasks() {
      // Arrange
      ActiveTask task1 = new ActiveTask();
      ActiveTask task2 = new ActiveTask();
      when(activeTaskRepository.findAll()).thenReturn(List.of(task1, task2));

      // Act
      activeTaskService.deleteAllActiveTasks();

      // Assert
      verify(activeTaskRepository, times(1)).deleteAll();
  }
}