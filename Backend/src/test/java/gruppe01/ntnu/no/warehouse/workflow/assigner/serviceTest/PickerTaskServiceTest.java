package gruppe01.ntnu.no.warehouse.workflow.assigner.serviceTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;

import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.PickerTaskRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.PickerTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PickerTaskServiceTest {

  @Mock
  private PickerTaskRepository pickerTaskRepository;

  @Mock
  private WorkerRepository workerRepository;

  @InjectMocks
  private PickerTaskService pickerTaskService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllPickerTasks() {
    List<PickerTask> pickerTasks = List.of(new PickerTask(), new PickerTask());
    when(pickerTaskRepository.findAll()).thenReturn(pickerTasks);

    List<PickerTask> result = pickerTaskService.getAllPickerTasks();

    assertEquals(2, result.size());
    verify(pickerTaskRepository, times(1)).findAll();
  }

  @Test
  void testGetPickerTaskById() {
    PickerTask pickerTask = new PickerTask();
    when(pickerTaskRepository.findById(1L)).thenReturn(Optional.of(pickerTask));

    PickerTask result = pickerTaskService.getPickerTaskById(1L);

    assertNotNull(result);
    verify(pickerTaskRepository, times(1)).findById(1L);
  }

  @Test
  void testSavePickerTask() {
    PickerTask pickerTask = new PickerTask();
    when(pickerTaskRepository.save(pickerTask)).thenReturn(pickerTask);

    PickerTask result = pickerTaskService.savePickerTask(pickerTask);

    assertNotNull(result);
    verify(pickerTaskRepository, times(1)).save(pickerTask);
  }

  @Test
  void testAssignWorkerToPickerTask() {
    PickerTask pickerTask = new PickerTask();
    Worker worker = new Worker();
    when(pickerTaskRepository.findById(1L)).thenReturn(Optional.of(pickerTask));
    when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
    when(pickerTaskRepository.save(pickerTask)).thenReturn(pickerTask);

    PickerTask result = pickerTaskService.assignWorkerToPickerTask(1L, 1L);

    assertNotNull(result);
    assertEquals(worker, pickerTask.getWorker());
    verify(workerRepository, times(1)).save(worker);
    verify(pickerTaskRepository, times(1)).save(pickerTask);
  }

  @Test
  void testRemoveWorkerFromPickerTask() {
    PickerTask pickerTask = new PickerTask();
    Worker worker = new Worker();
    worker.setId(1L);
    pickerTask.setWorker(worker);
    when(pickerTaskRepository.findById(1L)).thenReturn(Optional.of(pickerTask));
    when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
    when(pickerTaskRepository.save(pickerTask)).thenReturn(pickerTask);

    PickerTask result = pickerTaskService.removeWorkerFromPickerTask(1L, 1L);

    assertNotNull(result);
    assertNull(pickerTask.getWorker());
    verify(workerRepository, times(1)).save(worker);
    verify(pickerTaskRepository, times(1)).save(pickerTask);
  }

  @Test
  void testDeletePickerTask() {
    PickerTask pickerTask = new PickerTask();
    when(pickerTaskRepository.findById(1L)).thenReturn(Optional.of(pickerTask));

    pickerTaskService.deletePickerTask(1L);

    verify(pickerTaskRepository, times(1)).delete(pickerTask);
  }
}