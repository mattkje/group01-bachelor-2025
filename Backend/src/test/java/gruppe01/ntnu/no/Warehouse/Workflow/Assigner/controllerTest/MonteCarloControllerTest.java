package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers.MonteCarloController;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorldSimData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.worldsimulation.WorldSimulation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    void testGetWorldSimData() throws Exception {
        // Arrange
        long zoneId = 1L;
        List<WorldSimData> mockData = List.of(new WorldSimData());
        when(worldSimDataService.getMostRecentWorldSimDataByZone(zoneId)).thenReturn(mockData);

        // Act & Assert
        mockMvc.perform(get("/api/data/{zoneId}", zoneId))
                .andExpect(status().isOk());
        verify(worldSimDataService, times(1)).getMostRecentWorldSimDataByZone(zoneId);
    }

    @Test
    void testGetWorldSimValues() throws Exception {
        // Arrange
        long zoneId = 1L;
        List<Integer> mockValues = List.of(1, 2, 3);
        when(worldSimDataService.getWorldSimValues(zoneId)).thenReturn(mockValues);

        // Act & Assert
        mockMvc.perform(get("/api/data/{zoneId}/values", zoneId))
                .andExpect(status().isOk());
        verify(worldSimDataService, times(1)).getWorldSimValues(zoneId);
    }

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
    void testGetMCSimValues() throws Exception {
        // Arrange
        long zoneId = 1L;
        List<List<Integer>> mockValues = List.of(List.of(1, 2), List.of(3, 4));
        when(monteCarloDataService.getMCDataValues(zoneId)).thenReturn(mockValues);

        // Act & Assert
        mockMvc.perform(get("/api/data/{zoneId}/mcvalues", zoneId))
                .andExpect(status().isOk());
        verify(monteCarloDataService, times(1)).getMCDataValues(zoneId);
    }

    @Test
    void testGetAllData() throws Exception {
        // Arrange
        long zoneId = 1L;
        List<Integer> realData = List.of(1, 2, 3);
        List<List<Integer>> simulationData = List.of(List.of(4, 5), List.of(6, 7));
        LocalDateTime currentDate = LocalDateTime.now();
        int activeTasks = 5;

        when(worldSimDataService.getWorldSimValues(zoneId)).thenReturn(realData);
        when(monteCarloDataService.getMCDataValues(zoneId)).thenReturn(simulationData);
        when(worldSimulation.getCurrentDate()).thenReturn(LocalDate.from(currentDate));
        when(zoneService.getNumberOfTasksForTodayByZone(zoneId, LocalDate.from(currentDate))).thenReturn(activeTasks);

        // Act & Assert
        mockMvc.perform(get("/api/data/{zoneId}/graph-data", zoneId))
                .andExpect(status().isOk());
        verify(worldSimDataService, times(1)).getWorldSimValues(zoneId);
        verify(monteCarloDataService, times(1)).getMCDataValues(zoneId);
        verify(worldSimulation, times(1)).getCurrentDate();
        verify(zoneService, times(1)).getNumberOfTasksForTodayByZone(zoneId, LocalDate.from(currentDate));
    }
}