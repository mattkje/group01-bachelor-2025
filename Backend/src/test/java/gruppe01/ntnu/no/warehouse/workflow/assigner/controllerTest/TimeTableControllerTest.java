package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.TimetableController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Timetable;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TimetableService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@WebMvcTest(TimetableController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TimeTableControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TimetableService timetableService;

  @MockitoBean
  private ZoneService zoneService;

  @MockitoBean
  private WorkerService workerService;

  @Test
  void testGetTimetables() throws Exception {
    when(timetableService.getAllTimetables()).thenReturn(List.of(new Timetable(), new Timetable()));

    mockMvc.perform(get("/api/timetables"))
        .andExpect(status().isOk());
    verify(timetableService, times(1)).getAllTimetables();
  }

  @Test
  void testGetTimetableForToday() throws Exception {
      long zoneId = 1L;
      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
      when(timetableService.getTodayTimetablesByZone(zoneId)).thenReturn(List.of(new Timetable()));

      mockMvc.perform(get("/api/timetables/today/zone/{zoneId}", zoneId))
          .andExpect(status().isOk());
      verify(timetableService, times(1)).getTodayTimetablesByZone(zoneId);
  }

  @Test
  void testGetAllTimetablesByZone() throws Exception {
      long zoneId = 1L;
      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
      when(timetableService.getAllTimetablesByZone(zoneId)).thenReturn(List.of(new Timetable()));

      mockMvc.perform(get("/api/timetables/zone/{zoneId}", zoneId))
          .andExpect(status().isOk());
      verify(timetableService, times(1)).getAllTimetablesByZone(zoneId);
  }

  @Test
  void testGetTimetablesByDayAndZone() throws Exception {
      long zoneId = 1L;
      LocalDateTime day = LocalDateTime.now();
      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
      when(timetableService.getTimetablesByDayAndZone(day, zoneId)).thenReturn(List.of(new Timetable()));

      mockMvc.perform(get("/api/timetables/{day}/zone/{zoneId}", day, zoneId))
          .andExpect(status().isOk());
      verify(timetableService, times(1)).getTimetablesByDayAndZone(day, zoneId);
  }

  @Test
  void testGetTimetablesForOneWeek() throws Exception {
      long zoneId = 1L;
      LocalDate date = LocalDate.now();
      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
      when(timetableService.getTimetablesForOneWeek(date, zoneId)).thenReturn(List.of(new Timetable()));

      mockMvc.perform(get("/api/timetables/one-week/{date}/{zoneId}", date, zoneId))
          .andExpect(status().isOk());
      verify(timetableService, times(1)).getTimetablesForOneWeek(date, zoneId);
  }

  @Test
  void testSetStartTime() throws Exception {
      long timetableId = 1L;
      Timetable timetable = new Timetable();
      when(timetableService.getTimetableById(timetableId)).thenReturn(timetable);
      when(timetableService.setStartTime(timetableId)).thenReturn(timetable);

      mockMvc.perform(put("/api/timetables/{id}/set-start-time", timetableId))
          .andExpect(status().isOk());
      verify(timetableService, times(1)).setStartTime(timetableId);
  }

  @Test
  void testUpdateTimetable() throws Exception {
      long timetableId = 1L;
      Timetable timetable = new Timetable();
      timetable.setStartTime(LocalDateTime.now());
      timetable.setEndTime(LocalDateTime.now().plusHours(1));
      when(timetableService.getTimetableById(timetableId)).thenReturn(new Timetable());
      when(timetableService.updateTimetable(eq(timetableId), any(Timetable.class))).thenReturn(timetable);

      mockMvc.perform(put("/api/timetables/{id}", timetableId)
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"startTime\":\"2025-05-15T10:00:00\",\"endTime\":\"2025-05-15T11:00:00\"}"))
          .andExpect(status().isOk());
      verify(timetableService, times(1)).updateTimetable(eq(timetableId), any(Timetable.class));
  }

  @Test
  void testAddTimetable() throws Exception {
      long workerId = 1L;
      Timetable timetable = new Timetable();
      timetable.setStartTime(LocalDateTime.now());
      timetable.setEndTime(LocalDateTime.now().plusHours(1));
      when(workerService.getWorkerById(workerId)).thenReturn(Optional.of(new Worker()));
      when(timetableService.addTimetable(any(Timetable.class), eq(workerId))).thenReturn(timetable);

      mockMvc.perform(post("/api/timetables/{workerId}", workerId)
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"startTime\":\"2025-05-15T10:00:00\",\"endTime\":\"2025-05-15T11:00:00\"}"))
          .andExpect(status().isCreated());
      verify(timetableService, times(1)).addTimetable(any(Timetable.class), eq(workerId));
  }

  @Test
  void testDeleteTimetable() throws Exception {
      long timetableId = 1L;
      Timetable timetable = new Timetable();
      when(timetableService.getTimetableById(timetableId)).thenReturn(timetable);
      when(timetableService.deleteTimetable(timetableId)).thenReturn(timetable);

      mockMvc.perform(delete("/api/timetables/{id}", timetableId))
          .andExpect(status().isOk());
      verify(timetableService, times(1)).deleteTimetable(timetableId);
  }


}