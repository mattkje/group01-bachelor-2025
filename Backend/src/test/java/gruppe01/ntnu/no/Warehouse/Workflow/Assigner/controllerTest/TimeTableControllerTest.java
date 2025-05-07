package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers.TimetableController;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.TimetableService;
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

@WebMvcTest(TimetableController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TimeTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimetableService timetableService;

    @Test
    void testGetTimetables() throws Exception {
        when(timetableService.getAllTimetables()).thenReturn(List.of(new Timetable(), new Timetable()));

        mockMvc.perform(get("/api/timetables"))
                .andExpect(status().isOk());
        verify(timetableService, times(1)).getAllTimetables();
    }

    @Test
    void testGetTodaysTimetable() throws Exception {
        long zoneId = 1L;
        when(timetableService.getTodaysTimetablesByZone(zoneId)).thenReturn(List.of(new Timetable()));

        mockMvc.perform(get("/api/timetables/today/zone/{zoneId}", zoneId))
                .andExpect(status().isOk());
        verify(timetableService, times(1)).getTodaysTimetablesByZone(zoneId);
    }

    @Test
    void testGetAllTimetablesByZone() throws Exception {
        long zoneId = 1L;
        when(timetableService.getAllTimetablesByZone(zoneId)).thenReturn(List.of(new Timetable()));

        mockMvc.perform(get("/api/timetables/zone/{zoneId}", zoneId))
                .andExpect(status().isOk());
        verify(timetableService, times(1)).getAllTimetablesByZone(zoneId);
    }

    @Test
    void testGetTimetablesByDayAndZone() throws Exception {
        long zoneId = 1L;
        LocalDateTime day = LocalDateTime.now();
        when(timetableService.getTimetablesByDayAndZone(day, zoneId)).thenReturn(List.of(new Timetable()));

        mockMvc.perform(get("/api/timetables/{day}/zone/{zoneId}", day, zoneId))
                .andExpect(status().isOk());
        verify(timetableService, times(1)).getTimetablesByDayAndZone(day, zoneId);
    }

    @Test
    void testGetTimetablesForOneWeek() throws Exception {
        long zoneId = 1L;
        LocalDate date = LocalDate.now();
        when(timetableService.getTimetablesForOneWeek(date, zoneId)).thenReturn(List.of(new Timetable()));

        mockMvc.perform(get("/api/timetables/one-week/{date}/{zoneId}", date, zoneId))
                .andExpect(status().isOk());
        verify(timetableService, times(1)).getTimetablesForOneWeek(date, zoneId);
    }

    @Test
    void testSetStartTime() throws Exception {
        long id = 1L;
        Timetable timetable = new Timetable();
        when(timetableService.setStartTime(id)).thenReturn(timetable);

        mockMvc.perform(put("/api/timetables/{id}/set-start-time", id))
                .andExpect(status().isOk());
        verify(timetableService, times(1)).setStartTime(id);
    }

    @Test
    void testUpdateTimetable() throws Exception {
        long id = 1L;
        Timetable timetable = new Timetable();
        when(timetableService.updateTimetable(eq(id), any())).thenReturn(timetable);

        mockMvc.perform(put("/api/timetables/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
        verify(timetableService, times(1)).updateTimetable(eq(id), any());
    }

    @Test
    void testAddTimetable() throws Exception {
        long workerId = 1L;
        Timetable timetable = new Timetable();
        when(timetableService.addTimetable(any(), eq(workerId))).thenReturn(timetable);

        mockMvc.perform(post("/api/timetables/{workerId}", workerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
        verify(timetableService, times(1)).addTimetable(any(), eq(workerId));
    }

    @Test
    void testDeleteTimetable() throws Exception {
        long id = 1L;

        mockMvc.perform(delete("/api/timetables/{id}", id))
                .andExpect(status().isOk());
        verify(timetableService, times(1)).deleteTimetable(id);
    }
}