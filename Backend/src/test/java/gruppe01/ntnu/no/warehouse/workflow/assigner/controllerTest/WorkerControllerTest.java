package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.WorkerController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.LicenseService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

@WebMvcTest(WorkerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WorkerControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WorkerService workerService;

  @MockitoBean
  private LicenseService licenseService;

  @Test
  void testGetAllWorkers() throws Exception {
    when(workerService.getAllWorkers()).thenReturn(List.of(new Worker(), new Worker()));

    mockMvc.perform(get("/api/workers"))
        .andExpect(status().isOk());
    verify(workerService, times(1)).getAllWorkers();
  }

  @Test
  void testGetAvailableWorkers() throws Exception {
    when(workerService.getAvailableWorkers()).thenReturn(List.of(new Worker()));

    mockMvc.perform(get("/api/workers/available"))
        .andExpect(status().isOk());
    verify(workerService, times(1)).getAvailableWorkers();
  }

  @Test
  void testGetUnavailableWorkers() throws Exception {
    when(workerService.getUnavailableWorkers()).thenReturn(List.of(new Worker()));

    mockMvc.perform(get("/api/workers/unavailable"))
        .andExpect(status().isOk());
    verify(workerService, times(1)).getUnavailableWorkers();
  }

  @Test
  void testGetWorkerById() throws Exception {
    long id = 1L;
    when(workerService.getWorkerById(id)).thenReturn(Optional.of(new Worker()));

    mockMvc.perform(get("/api/workers/{id}", id))
        .andExpect(status().isOk());
    verify(workerService, times(1)).getWorkerById(id);
  }

  @Test
  void testAddWorker() throws Exception {
    Worker worker = new Worker();
    when(workerService.addWorker(any())).thenReturn(worker);

    mockMvc.perform(post("/api/workers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk());
    verify(workerService, times(1)).addWorker(any());
  }

  @Test
  void testUpdateWorker() throws Exception {
    long id = 1L;
    Worker worker = new Worker();
    when(workerService.updateWorker(eq(id), any())).thenReturn(worker);

    mockMvc.perform(put("/api/workers/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk());
    verify(workerService, times(1)).updateWorker(eq(id), any());
  }

  @Test
  void testUpdateWorkerAvailability() throws Exception {
    long id = 1L;
    Worker worker = new Worker();
    when(workerService.updateWorkerAvailability(id)).thenReturn(worker);

    mockMvc.perform(put("/api/workers/{id}/availability", id))
        .andExpect(status().isOk());
    verify(workerService, times(1)).updateWorkerAvailability(id);
  }

  @Test
  void testAddWorkerToZone() throws Exception {
    long workerId = 1L;
    long zoneId = 2L;
    Worker worker = new Worker();
    when(workerService.addWorkerToZone(workerId, zoneId)).thenReturn(worker);

    mockMvc.perform(put("/api/workers/{workerId}/zone/{zoneId}", workerId, zoneId))
        .andExpect(status().isOk());
    verify(workerService, times(1)).addWorkerToZone(workerId, zoneId);
  }

  @Test
  void testAddLicenseToWorker() throws Exception {
    long id = 1L;
    long licenseId = 2L;
    Worker worker = new Worker();
    when(workerService.addLicenseToWorker(id, licenseId)).thenReturn(worker);

    mockMvc.perform(put("/api/workers/{id}/license/{licenseId}", id, licenseId))
        .andExpect(status().isOk());
    verify(workerService, times(1)).addLicenseToWorker(id, licenseId);
  }

  @Test
  void testDeleteWorker() throws Exception {
    long id = 1L;
    Worker worker = new Worker();
    when(workerService.deleteWorker(id)).thenReturn(worker);

    mockMvc.perform(delete("/api/workers/{id}", id))
        .andExpect(status().isOk());
    verify(workerService, times(1)).deleteWorker(id);
  }
}