package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.ActiveTaskController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(ActiveTaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class ActiveTaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ActiveTaskService activeTaskService;

  @MockitoBean
  private ZoneService zoneService;

  @MockitoBean
  private TaskService taskService;

  @MockitoBean
  private WorkerService workerService;

  @Test
  void testGetAllActiveTasks() throws Exception {
    ActiveTask task1 = new ActiveTask();
    ActiveTask task2 = new ActiveTask();
    when(activeTaskService.getAllActiveTasks()).thenReturn(List.of(task1, task2));

    mockMvc.perform(get("/api/active-tasks"))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).getAllActiveTasks();
  }

  @Test
  void testGetActiveTaskById() throws Exception {
    long id = 1L;
    ActiveTask task = new ActiveTask();
    when(activeTaskService.getActiveTaskById(id)).thenReturn(task);

    mockMvc.perform(get("/api/active-tasks/{id}", id))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).getActiveTaskById(id);
  }

  @Test
  void testGetActiveTasksForToday() throws Exception {
    List<ActiveTask> tasks = List.of(new ActiveTask(), new ActiveTask());
    when(activeTaskService.getActiveTasksForToday(any())).thenReturn(tasks);

    mockMvc.perform(get("/api/active-tasks/today"))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).getActiveTasksForToday(any());
  }

  @Test
  void testGetCompletedActiveTasks() throws Exception {
    List<ActiveTask> tasks = List.of(new ActiveTask(), new ActiveTask());
    when(activeTaskService.getCompletedActiveTasks()).thenReturn(tasks);

    mockMvc.perform(get("/api/active-tasks/completed"))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).getCompletedActiveTasks();
  }

  @Test
  void testGetNotStartedActiveTasks() throws Exception {
    List<ActiveTask> tasks = List.of(new ActiveTask(), new ActiveTask());
    when(activeTaskService.getNotStartedActiveTasks()).thenReturn(tasks);

    mockMvc.perform(get("/api/active-tasks/not-started"))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).getNotStartedActiveTasks();
  }

  @Test
  void testGetActiveTasksInProgress() throws Exception {
    List<ActiveTask> tasks = List.of(new ActiveTask(), new ActiveTask());
    when(activeTaskService.getActiveTasksInProgress()).thenReturn(tasks);

    mockMvc.perform(get("/api/active-tasks/in-progress"))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).getActiveTasksInProgress();
  }

  @Test
  void testGetWorkersAssignedToActiveTask() throws Exception {
    long id = 1L;
    List<Worker> workers = List.of(new Worker(), new Worker());
    when(activeTaskService.getWorkersAssignedToActiveTask(id)).thenReturn(workers);

    mockMvc.perform(get("/api/active-tasks/{id}/workers", id))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).getWorkersAssignedToActiveTask(id);
  }

  @Test
  void testCreateActiveTask() throws Exception {
    long taskId = 1L;
    ActiveTask task = new ActiveTask();
    when(activeTaskService.createActiveTask(eq(taskId), any())).thenReturn(task);

    mockMvc.perform(post("/api/active-tasks/{taskId}", taskId)
            .contentType("application/json")
            .content("{}"))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).createActiveTask(eq(taskId), any());
  }

  @Test
  void testUpdateActiveTask() throws Exception {
    long id = 1L;
    ActiveTask task = new ActiveTask();
    when(activeTaskService.updateActiveTask(eq(id), any())).thenReturn(task);

    mockMvc.perform(put("/api/active-tasks/{id}", id)
            .contentType("application/json")
            .content("{}"))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).updateActiveTask(eq(id), any());
  }

  @Test
  void testAssignWorkerToTask() throws Exception {
    long id = 1L;
    long workerId = 2L;
    ActiveTask task = new ActiveTask();
    when(activeTaskService.assignWorkerToTask(id, workerId)).thenReturn(task);

    mockMvc.perform(put("/api/active-tasks/{id}/worker/{workerId}", id, workerId))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).assignWorkerToTask(id, workerId);
  }

  @Test
  void testRemoveWorkerFromTask() throws Exception {
    long id = 1L;
    long workerId = 2L;
    ActiveTask task = new ActiveTask();
    when(activeTaskService.removeWorkerFromTask(id, workerId)).thenReturn(task);

    mockMvc.perform(put("/api/active-tasks/{id}/worker/{workerId}/remove", id, workerId))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).removeWorkerFromTask(id, workerId);
  }

  @Test
  void testRemoveWorkersFromTask() throws Exception {
    long id = 1L;
    ActiveTask task = new ActiveTask();
    when(activeTaskService.removeWorkersFromTask(id)).thenReturn(task);

    mockMvc.perform(put("/api/active-tasks/{id}/workers/remove", id))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).removeWorkersFromTask(id);
  }

  @Test
  void testDeleteActiveTask() throws Exception {
    long id = 1L;
    ActiveTask task = new ActiveTask();
    when(activeTaskService.deleteActiveTask(id)).thenReturn(task);

    mockMvc.perform(delete("/api/active-tasks/{id}", id))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).deleteActiveTask(id);
  }
}