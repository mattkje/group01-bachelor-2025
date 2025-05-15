package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.ZoneController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.*;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@WebMvcTest(ZoneController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ZoneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ZoneService zoneService;

    @Test
    void testGetAllZones() throws Exception {
        when(zoneService.getAllZones()).thenReturn(List.of(new Zone(), new Zone()));

        mockMvc.perform(get("/api/zones"))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getAllZones();
    }

    @Test
    void testGetZoneById() throws Exception {
        long id = 1L;
        when(zoneService.getZoneById(id)).thenReturn(new Zone());

        mockMvc.perform(get("/api/zones/{id}", id))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getZoneById(id);
    }

    @Test
    void testGetWorkersByZoneId() throws Exception {
        long id = 1L;
        when(zoneService.getWorkersByZoneId(id)).thenReturn(Set.of(new Worker()));

        mockMvc.perform(get("/api/zones/{id}/workers", id))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getWorkersByZoneId(id);
    }

    @Test
    void testGetTasksByZoneId() throws Exception {
        long id = 1L;
        when(zoneService.getTasksByZoneId(id)).thenReturn(Set.of(new Task()));

        mockMvc.perform(get("/api/zones/{id}/tasks", id))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getTasksByZoneId(id);
    }

    @Test
    void testGetActiveTasksByZoneId() throws Exception {
        long id = 1L;
        when(zoneService.getActiveTasksByZoneId(id)).thenReturn(Set.of(new ActiveTask()));

        mockMvc.perform(get("/api/zones/{id}/active-tasks", id))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getActiveTasksByZoneId(id);
    }

    @Test
    void testGetAllTaskZones() throws Exception {
        when(zoneService.getAllTaskZones()).thenReturn(List.of(new Zone(), new Zone()));

        mockMvc.perform(get("/api/zones/task-zones"))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getAllTaskZones();
    }

    @Test
    void testGetAllPickerZones() throws Exception {
        when(zoneService.getAllPickerZones()).thenReturn(List.of(new Zone(), new Zone()));

        mockMvc.perform(get("/api/zones/picker-zones"))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getAllPickerZones();
    }

    @Test
    void testGetActiveTasksByZoneIdNow() throws Exception {
        long id = 1L;
        when(zoneService.getTodayUnfinishedTasksByZoneId(id)).thenReturn(Set.of(new ActiveTask()));

        mockMvc.perform(get("/api/zones/{id}/active-tasks-now", id))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getTodayUnfinishedTasksByZoneId(id);
    }

    @Test
    void testGetPickerTasksByZoneIdNow() throws Exception {
        long id = 1L;
        when(zoneService.getTodayUnfinishedPickerTasksByZoneId(id)).thenReturn(Set.of(new PickerTask()));

        mockMvc.perform(get("/api/zones/{id}/picker-tasks-now", id))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getTodayUnfinishedPickerTasksByZoneId(id);
    }

    @Test
    void testGetActiveTasksByZoneIdAndDate() throws Exception {
        long id = 1L;
        String date = "2023-10-01";
        when(zoneService.getAllTasksByZoneIdAndDate(id, LocalDate.parse(date))).thenReturn(Set.of(new ActiveTask()));

        mockMvc.perform(get("/api/zones/{id}/active-tasks/{date}", id, date))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getAllTasksByZoneIdAndDate(id, LocalDate.parse(date));
    }

    @Test
    void testGetPickerTasksByZoneIdAndDate() throws Exception {
        long id = 1L;
        String date = "2023-10-01";
        when(zoneService.getAllPickerTasksByZoneIdAndDate(id, LocalDate.parse(date))).thenReturn(Set.of(new PickerTask()));

        mockMvc.perform(get("/api/zones/{id}/picker-tasks/{date}", id, date))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getAllPickerTasksByZoneIdAndDate(id, LocalDate.parse(date));
    }

    @Test
    void testGetNumberOfTasksByDateByZone() throws Exception {
        long zoneId = 1L;
        LocalDate date = LocalDate.now();
        when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
        when(zoneService.getNumberOfTasksForTodayByZone(zoneId, date)).thenReturn(5);

        mockMvc.perform(get("/api/zones/{zoneId}/{date}", zoneId, date))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getNumberOfTasksForTodayByZone(zoneId, date);
    }

    @Test
    void testGetMinTimeForActiveTasksByZoneIdNow() throws Exception {
        long zoneId = 1L;
        when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
        when(zoneService.getMinTimeForActiveTasksByZoneIdNow(zoneId)).thenReturn(10);

        mockMvc.perform(get("/api/zones/{zoneId}/active-tasks-now-min-time", zoneId))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getMinTimeForActiveTasksByZoneIdNow(zoneId);
    }

    @Test
    void testAddZone() throws Exception {
        Zone zone = new Zone();
        zone.setName("Zone A");
        zone.setCapacity(100);
        when(zoneService.addZone(any(Zone.class))).thenReturn(zone);

        mockMvc.perform(post("/api/zones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Zone A\",\"capacity\":100}"))
                .andExpect(status().isCreated());
        verify(zoneService, times(1)).addZone(any(Zone.class));
    }

    @Test
    void testUpdateZone() throws Exception {
        long id = 1L;
        Zone zone = new Zone();
        zone.setName("Updated Zone");
        zone.setCapacity(200);
        when(zoneService.getZoneById(id)).thenReturn(new Zone());
        when(zoneService.updateZone(eq(id), any(Zone.class))).thenReturn(zone);

        mockMvc.perform(put("/api/zones/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated Zone\",\"capacity\":200}"))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).updateZone(eq(id), any(Zone.class));
    }

    @Test
    void testUpdatePickerZone() throws Exception {
        long id = 1L;
        when(zoneService.getZoneById(id)).thenReturn(new Zone());
        when(zoneService.changeIsPickerZone(id)).thenReturn(new Zone());

        mockMvc.perform(put("/api/zones/is-picker-zone/{id}", id))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).changeIsPickerZone(id);
    }

    @Test
    void testDeleteZone() throws Exception {
        long id = 1L;
        when(zoneService.getZoneById(id)).thenReturn(new Zone());
        when(zoneService.deleteZone(id)).thenReturn(new Zone());

        mockMvc.perform(delete("/api/zones/{id}", id))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).deleteZone(id);
    }

    @Test
    void testUpdateMachineLearningModel() throws Exception {
        doNothing().when(zoneService).updateMachineLearningModel(any(LocalDateTime.class));

        mockMvc.perform(put("/api/zones/update-machine-learning-model"))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).updateMachineLearningModel(any(LocalDateTime.class));
    }
}