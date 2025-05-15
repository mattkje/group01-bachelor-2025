package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.PickerTaskController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.PickerTaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

@WebMvcTest(PickerTaskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PickerTaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PickerTaskService pickerTaskService;

  @MockitoBean
  private ZoneService zoneService;

  @MockitoBean
  private WorkerService workerService;

  @Test
  void testGetAllPickerTasks() throws Exception {
    PickerTask task1 = new PickerTask();
    PickerTask task2 = new PickerTask();
    when(pickerTaskService.getAllPickerTasks()).thenReturn(List.of(task1, task2));

    mockMvc.perform(get("/api/picker-tasks"))
        .andExpect(status().isOk());
    verify(pickerTaskService, times(1)).getAllPickerTasks();
  }

  @Test
  void testGetPickerTaskById() throws Exception {
      long taskId = 1L;
      PickerTask task = new PickerTask();
      when(pickerTaskService.getPickerTaskById(taskId)).thenReturn(task);

      mockMvc.perform(get("/api/picker-tasks/{id}", taskId))
          .andExpect(status().isOk());
      verify(pickerTaskService, times(2)).getPickerTaskById(taskId);
  }

  @Test
  void testGetPickerTasksByZoneId() throws Exception {
      long zoneId = 1L;
      List<PickerTask> tasks = List.of(new PickerTask(), new PickerTask());
      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
      when(pickerTaskService.getPickerTaskByZoneId(zoneId)).thenReturn(tasks);

      mockMvc.perform(get("/api/picker-tasks/zone/{zoneId}", zoneId))
          .andExpect(status().isOk());
      verify(pickerTaskService, times(1)).getPickerTaskByZoneId(zoneId);
  }

  @Test
  void testAssignWorkerToPickerTask() throws Exception {
      long taskId = 1L;
      long workerId = 2L;
      PickerTask task = new PickerTask();
      when(pickerTaskService.getPickerTaskById(taskId)).thenReturn(task);
      when(workerService.getWorkerById(workerId)).thenReturn(java.util.Optional.of(new Worker()));
      when(pickerTaskService.assignWorkerToPickerTask(taskId, workerId)).thenReturn(task);

      mockMvc.perform(put("/api/picker-tasks/assign-worker/{pickerTaskId}/{workerId}", taskId, workerId))
          .andExpect(status().isOk());
      verify(pickerTaskService, times(1)).assignWorkerToPickerTask(taskId, workerId);
  }

  @Test
  void testRemoveWorkerFromPickerTask() throws Exception {
      long taskId = 1L;
      long workerId = 2L;
      PickerTask task = new PickerTask();
      when(pickerTaskService.getPickerTaskById(taskId)).thenReturn(task);
      when(workerService.getWorkerById(workerId)).thenReturn(java.util.Optional.of(new Worker()));
      when(pickerTaskService.removeWorkerFromPickerTask(taskId, workerId)).thenReturn(task);

      mockMvc.perform(put("/api/picker-tasks/remove-worker/{pickerTaskId}/{workerId}", taskId, workerId))
          .andExpect(status().isOk());
      verify(pickerTaskService, times(1)).removeWorkerFromPickerTask(taskId, workerId);
  }

  @Test
  void testDeletePickerTask() throws Exception {
      long taskId = 1L;
      PickerTask task = new PickerTask();
      when(pickerTaskService.getPickerTaskById(taskId)).thenReturn(task);
      when(pickerTaskService.deletePickerTask(taskId)).thenReturn(task);

      mockMvc.perform(delete("/api/picker-tasks/{id}", taskId))
          .andExpect(status().isOk());
      verify(pickerTaskService, times(1)).deletePickerTask(taskId);
  }

  @Test
  void testUpdatePickerTask() throws Exception {

  }

  @Test
  void testCreatePickerTask() throws Exception {

  }
}