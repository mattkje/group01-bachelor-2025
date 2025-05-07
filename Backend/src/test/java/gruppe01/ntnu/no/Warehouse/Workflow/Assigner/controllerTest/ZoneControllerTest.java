package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers.ZoneController;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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
    void testAddZone() throws Exception {
        Zone zone = new Zone();
        when(zoneService.addZone(any())).thenReturn(zone);

        mockMvc.perform(post("/api/zones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).addZone(any());
    }

    @Test
    void testUpdateZone() throws Exception {
        long id = 1L;
        Zone zone = new Zone();
        when(zoneService.updateZone(eq(id), any())).thenReturn(zone);

        mockMvc.perform(put("/api/zones/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).updateZone(eq(id), any());
    }

    @Test
    void testDeleteZone() throws Exception {
        long id = 1L;
        Zone zone = new Zone();
        when(zoneService.deleteZone(id)).thenReturn(zone);

        mockMvc.perform(delete("/api/zones/{id}", id))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).deleteZone(id);
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
    void testGetNumberOfTasksForTodayByZone() throws Exception {
        long zoneId = 1L;
        LocalDate date = LocalDate.now();
        when(zoneService.getNumberOfTasksForTodayByZone(zoneId, date)).thenReturn(5);

        mockMvc.perform(get("/api/zones/{zoneId}/{date}", zoneId, date))
                .andExpect(status().isOk());
        verify(zoneService, times(1)).getNumberOfTasksForTodayByZone(zoneId, date);
    }
}