package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.TaskController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Task;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TaskService taskService;

  @MockitoBean
  private ZoneService zoneService;

  @Test
  void testGetAllTasks() throws Exception {
    Task task1 = new Task();
    Task task2 = new Task();
    when(taskService.getAllTasks()).thenReturn(List.of(task1, task2));

    mockMvc.perform(get("/api/tasks"))
        .andExpect(status().isOk());
    verify(taskService, times(1)).getAllTasks();
  }

  @Test
  void testGetTaskById() throws Exception {
    long taskId = 1L;
    Task task = new Task();
    when(taskService.getTaskById(taskId)).thenReturn(task);

    mockMvc.perform(get("/api/tasks/{id}", taskId))
        .andExpect(status().isOk());
    verify(taskService, times(1)).getTaskById(taskId);
  }

  @Test
  void testCreateTask() throws Exception {
    long zoneId = 1L;
    Task task = new Task();
    task.setName("Test Task");
    task.setMinWorkers(1);
    task.setMaxWorkers(5);
    Zone zone = new Zone();
    zone.setId(zoneId);
    task.setZone(zone);

    when(taskService.createTask(any(Task.class), eq(zoneId))).thenReturn(task);
    when(zoneService.getZoneById(zoneId)).thenReturn(zone);

    mockMvc.perform(post("/api/tasks/{zoneId}", zoneId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Test Task\",\"minWorkers\":1,\"maxWorkers\":5,\"zone\":{\"id\":1}}"))
        .andExpect(status().isOk());
    verify(taskService, times(1)).createTask(any(Task.class), eq(zoneId));
  }

  @Test
  void testUpdateTask() throws Exception {
    long taskId = 1L;
    long zoneId = 1L;
    Task task = new Task();
    task.setName("Updated Task");
    task.setMinWorkers(2);
    task.setMaxWorkers(6);
    Zone zone = new Zone();
    zone.setId(zoneId);
    task.setZone(zone);

    when(taskService.updateTask(eq(taskId), any(Task.class), eq(zoneId))).thenReturn(task);
    when(taskService.getTaskById(taskId)).thenReturn(task);
    when(zoneService.getZoneById(zoneId)).thenReturn(zone);

    mockMvc.perform(put("/api/tasks/{id}/{zoneId}", taskId, zoneId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                "{\"name\":\"Updated Task\",\"minWorkers\":2,\"maxWorkers\":6,\"zone\":{\"id\":1}}"))
        .andExpect(status().isOk());
    verify(taskService, times(1)).updateTask(eq(taskId), any(Task.class), eq(zoneId));
  }

  @Test
  void testDeleteTask() throws Exception {
    long taskId = 1L;
    Task task = new Task();
    when(taskService.getTaskById(taskId)).thenReturn(task);
    when(taskService.deleteTask(taskId)).thenReturn(task);

    mockMvc.perform(delete("/api/tasks/{id}", taskId))
        .andExpect(status().isOk());
    verify(taskService, times(1)).deleteTask(taskId);
  }

}