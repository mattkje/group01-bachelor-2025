package gruppe01.ntnu.no.warehouse.workflow.assigner.serviceTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Task;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.TaskRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.ZoneRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private ZoneRepository zoneRepository;

  @InjectMocks
  private TaskService taskService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllTasks() {
    List<Task> tasks = List.of(new Task(), new Task());
    when(taskRepository.findAllWithLicenses()).thenReturn(tasks);

    List<Task> result = taskService.getAllTasks();

    assertEquals(2, result.size());
    verify(taskRepository, times(1)).findAllWithLicenses();
  }

  @Test
  void testCreateTask() {
    Task task = new Task();
    task.setMinTime(1);
    task.setMaxTime(5);
    task.setMinWorkers(1);
    task.setMaxWorkers(3);
    Zone zone = new Zone();
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(taskRepository.save(task)).thenReturn(task);

    Task result = taskService.createTask(task, 1L);

    assertNotNull(result);
    verify(taskRepository, times(1)).save(task);
  }

  @Test
  void testCreateTask_InvalidTimeRange() {
    Task task = new Task();
    task.setMinTime(10);
    task.setMaxTime(5);

    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(task, 1L));

    assertEquals("Invalid time range", exception.getMessage());
    verify(taskRepository, never()).save(any());
  }

  @Test
  void testGetTaskById() {
    Task task = new Task();
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

    Task result = taskService.getTaskById(1L);

    assertNotNull(result);
    verify(taskRepository, times(1)).findById(1L);
  }

  @Test
  void testUpdateTask() {
    Task task = new Task();
    task.setName("Updated Task");
    Zone zone = new Zone();
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(taskRepository.findById(1L)).thenReturn(Optional.of(new Task()));
    when(taskRepository.save(any(Task.class))).thenReturn(task);

    Task result = taskService.updateTask(1L, task, 1L);

    assertNotNull(result);
    assertEquals("Updated Task", result.getName());
    verify(taskRepository, times(1)).save(any(Task.class));
  }

  @Test
  void testDeleteTask() {
    Task task = new Task();
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

    Task result = taskService.deleteTask(1L);

    assertNotNull(result);
    verify(taskRepository, times(1)).delete(task);
  }
}