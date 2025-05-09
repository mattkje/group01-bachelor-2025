package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.serviceTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ActiveTaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

class ActiveTaskServiceTest {

    @Mock
    private ActiveTaskRepository activeTaskRepository;

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private TaskRepository taskRepository;

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
        when(activeTaskRepository.findAllWithTasksAndWorkersAndLicenses()).thenReturn(List.of(task1, task2));

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
    void testGetCompletedActiveTasks() {
        // Arrange
        ActiveTask completedTask = new ActiveTask();
        completedTask.setEndTime(java.time.LocalDateTime.now());
        when(activeTaskRepository.findAll()).thenReturn(List.of(completedTask));

        // Act
        List<ActiveTask> result = activeTaskService.getCompletedActiveTasks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(activeTaskRepository, times(1)).findAll();
    }
}