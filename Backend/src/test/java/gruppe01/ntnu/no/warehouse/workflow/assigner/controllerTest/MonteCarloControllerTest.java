package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.MonteCarloController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.WorldSimData;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.*;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.worldsimulation.WorldSimulation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito .MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(MonteCarloController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MonteCarloControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WorldSimDataService worldSimDataService;

  @MockitoBean
  private MonteCarloService monteCarloService;

  @MockitoBean
  private ZoneService zoneService;

  @MockitoBean
  private MonteCarloDataService monteCarloDataService;

  @MockitoBean
  private WorldSimulation worldSimulation;

  @Test
  void testGenerateWorldSimData() throws Exception {
    // Arrange
    LocalDateTime now = LocalDateTime.now();

    // Act & Assert
    mockMvc.perform(post("/api/data/world-sim")
            .param("now", now.toString()))
        .andExpect(status().isOk());
    verify(worldSimDataService, times(1)).generateWorldSimData(any(LocalDateTime.class), eq(true));
  }

  @Test
  void testGetWorldSimData() throws Exception {
      long zoneId = 1L;
      List<WorldSimData> worldSimData = List.of(new WorldSimData());
      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
      when(worldSimDataService.getMostRecentWorldSimDataByZone(zoneId)).thenReturn(worldSimData);

      mockMvc.perform(get("/api/data/{zoneId}", zoneId))
          .andExpect(status().isOk());
      verify(worldSimDataService, times(1)).getMostRecentWorldSimDataByZone(zoneId);
  }

  @Test
  void testGetWorldSimValues() throws Exception {
      long zoneId = 1L;
      List<Integer> values = List.of(1, 2, 3);
      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
      when(worldSimDataService.getWorldSimValues(zoneId)).thenReturn(values);

      mockMvc.perform(get("/api/data/{zoneId}/values", zoneId))
          .andExpect(status().isOk());
      verify(worldSimDataService, times(1)).getWorldSimValues(zoneId);
  }

  @Test
  void testGetMonteCarloSimulationValues() throws Exception {
      long zoneId = 1L;
      List<List<Integer>> mcValues = List.of(List.of(1, 2), List.of(3, 4));
      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
      when(monteCarloDataService.getMCDataValues(zoneId)).thenReturn(mcValues);

      mockMvc.perform(get("/api/data/{zoneId}/mcvalues", zoneId))
          .andExpect(status().isOk());
      verify(monteCarloDataService, times(1)).getMCDataValues(zoneId);
  }

  @Test
  void testGetAllData() throws Exception {
      long zoneId = 1L;
      List<Integer> realData = List.of(1, 2, 3);
      List<List<Integer>> simulationData = List.of(List.of(4, 5), List.of(6, 7));
      LocalDate currentDate = LocalDate.now();
      int activeTasks = 5;

      when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
      when(worldSimDataService.getWorldSimValues(zoneId)).thenReturn(realData);
      when(monteCarloDataService.getMCDataValues(zoneId)).thenReturn(simulationData);
      when(worldSimulation.getCurrentDate()).thenReturn(currentDate);
      when(zoneService.getNumberOfTasksForTodayByZone(zoneId, currentDate)).thenReturn(activeTasks);

      mockMvc.perform(get("/api/data/{zoneId}/graph-data", zoneId))
          .andExpect(status().isOk());
      verify(worldSimDataService, times(1)).getWorldSimValues(zoneId);
      verify(monteCarloDataService, times(1)).getMCDataValues(zoneId);
      verify(worldSimulation, times(1)).getCurrentDate();
      verify(zoneService, times(1)).getNumberOfTasksForTodayByZone(zoneId, currentDate);
  }
}