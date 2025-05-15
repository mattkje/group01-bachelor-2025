package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.TaskController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Task;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TaskService;
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
    long id = 1L;
    Task task = new Task();
    when(taskService.getTaskById(id)).thenReturn(task);

    mockMvc.perform(get("/api/tasks/{id}", id))
        .andExpect(status().isOk());
    verify(taskService, times(1)).getTaskById(id);
  }

  @Test
  void testCreateTask() throws Exception {
    long zoneId = 1L;
    Task task = new Task();
    when(taskService.createTask(any(), eq(zoneId))).thenReturn(task);

    mockMvc.perform(post("/api/tasks/{zoneId}", zoneId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk());
    verify(taskService, times(1)).createTask(any(), eq(zoneId));
  }

  @Test
  void testUpdateTask() throws Exception {
    long id = 1L;
    long zoneId = 2L;
    Task task = new Task();
    when(taskService.updateTask(eq(id), any(), eq(zoneId))).thenReturn(task);

    mockMvc.perform(put("/api/tasks/{id}/{zoneId}", id, zoneId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk());
    verify(taskService, times(1)).updateTask(eq(id), any(), eq(zoneId));
  }

  @Test
  void testDeleteTask() throws Exception {
    long id = 1L;
    Task task = new Task();
    when(taskService.deleteTask(id)).thenReturn(task);

    mockMvc.perform(delete("/api/tasks/{id}", id))
        .andExpect(status().isOk());
    verify(taskService, times(1)).deleteTask(id);
  }
}