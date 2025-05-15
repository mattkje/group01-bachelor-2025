package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.ActiveTaskController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Task;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
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
    long taskId = 1L;
    ActiveTask task = new ActiveTask();

    when(activeTaskService.getActiveTaskById(taskId)).thenReturn(task);

    mockMvc.perform(get("/api/active-tasks/{id}", taskId))

        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).getActiveTaskById(taskId);
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
   long taskId = 1L;
   List<Worker> workers = List.of(new Worker(), new Worker());
   ActiveTask activeTask = new ActiveTask(); // Mock the active task

   when(activeTaskService.getActiveTaskById(taskId)).thenReturn(activeTask); // Mock this call
   when(activeTaskService.getWorkersAssignedToActiveTask(taskId)).thenReturn(workers);

   mockMvc.perform(get("/api/active-tasks/{id}/workers", taskId))
       .andExpect(status().isOk());
   verify(activeTaskService, times(1)).getWorkersAssignedToActiveTask(taskId);
 }

  @Test
  void testCreateActiveTask() throws Exception {
    long taskId = 1L;
    ActiveTask task = new ActiveTask();
    when(taskService.getTaskById(taskId)).thenReturn(new Task()); // Mock task retrieval
    when(activeTaskService.createActiveTask(eq(taskId), any())).thenReturn(task);

    mockMvc.perform(post("/api/active-tasks/{taskId}", taskId)
            .contentType("application/json")
            .content("{\"date\":\"2023-01-01\"}")) // Provide valid JSON content
        .andExpect(status().isCreated()); // Expect 201 Created
    verify(activeTaskService, times(1)).createActiveTask(eq(taskId), any());
  }

  @Test
  void testUpdateActiveTask() throws Exception {
    long taskId = 1L;
    ActiveTask task = new ActiveTask();
    when(activeTaskService.getActiveTaskById(taskId)).thenReturn(task);
    when(activeTaskService.updateActiveTask(eq(taskId), any())).thenReturn(task);

    mockMvc.perform(put("/api/active-tasks/{id}", taskId)
            .contentType("application/json")
            .content("{\"date\":\"2023-01-01\"}"))
        .andExpect(status().isOk());
    verify(activeTaskService, times(1)).updateActiveTask(eq(taskId), any());
  }

  @Test
  void testGetActiveTasksForTodayByZone() throws Exception {
      long zoneId = 1L;
      List<ActiveTask> tasks = List.of(new ActiveTask(), new ActiveTask());
      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone()); // Mock zone retrieval
      when(activeTaskService.getActiveTasksForTodayByZone(eq(zoneId), any())).thenReturn(tasks);

      mockMvc.perform(get("/api/active-tasks/today/{zoneId}", zoneId))
          .andExpect(status().isOk());
      verify(activeTaskService, times(1)).getActiveTasksForTodayByZone(eq(zoneId), any());
  }

  @Test
  void testAssignWorkerToTask() throws Exception {
      long taskId = 1L;
      long workerId = 2L;
      ActiveTask task = new ActiveTask();
      when(activeTaskService.getActiveTaskById(taskId)).thenReturn(task);
      when(workerService.getWorkerById(workerId)).thenReturn(java.util.Optional.of(new Worker()));
      when(activeTaskService.assignWorkerToTask(taskId, workerId)).thenReturn(task);

      mockMvc.perform(put("/api/active-tasks/{id}/worker/{workerId}", taskId, workerId))
          .andExpect(status().isOk());
      verify(activeTaskService, times(1)).assignWorkerToTask(taskId, workerId);
  }

  @Test
  void testRemoveWorkerFromTask() throws Exception {
      long taskId = 1L;
      long workerId = 2L;
      ActiveTask task = new ActiveTask();
      when(activeTaskService.getActiveTaskById(taskId)).thenReturn(task);
      when(workerService.getWorkerById(workerId)).thenReturn(java.util.Optional.of(new Worker()));
      when(activeTaskService.removeWorkerFromTask(taskId, workerId)).thenReturn(task);

      mockMvc.perform(put("/api/active-tasks/{id}/worker/{workerId}/remove", taskId, workerId))
          .andExpect(status().isOk());
      verify(activeTaskService, times(1)).removeWorkerFromTask(taskId, workerId);
  }

  @Test
  void testRemoveWorkersFromTask() throws Exception {
      long taskId = 1L;
      ActiveTask task = new ActiveTask();
      when(activeTaskService.getActiveTaskById(taskId)).thenReturn(task);
      when(activeTaskService.removeWorkersFromTask(taskId)).thenReturn(task);

      mockMvc.perform(put("/api/active-tasks/{id}/workers/remove", taskId))
          .andExpect(status().isOk());
      verify(activeTaskService, times(1)).removeWorkersFromTask(taskId);
  }

  @Test
  void testDeleteActiveTask() throws Exception {
      long taskId = 1L;
      ActiveTask task = new ActiveTask();
      when(activeTaskService.getActiveTaskById(taskId)).thenReturn(task);
      when(activeTaskService.deleteActiveTask(taskId)).thenReturn(task);

      mockMvc.perform(delete("/api/active-tasks/{id}", taskId))
          .andExpect(status().isOk());
      verify(activeTaskService, times(1)).deleteActiveTask(taskId);
  }
}