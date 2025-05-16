package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.WorldSimulationController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.SimulationService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.worldsimulation.WorldSimulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import smile.regression.RandomForest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorldSimulationController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorldSimulationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WorldSimulation worldSimulation;

  @MockitoBean
  private SimulationService simulationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testStartSimulation() throws Exception {
    doNothing().when(worldSimulation).runWorldSimulation(anyInt(), any(LocalDate.class), eq(false));
    doNothing().when(simulationService).setSimCount(anyInt());

    mockMvc.perform(post("/api/simulation/start")
            .param("simulationTime", "10")
            .param("simCount", "5"))
        .andExpect(status().isOk());

    verify(simulationService, times(1)).setSimCount(5);
    verify(worldSimulation, times(1)).runWorldSimulation(10, LocalDate.now(), false);
  }

  @Test
  void testPauseSimulation() throws Exception {
    doNothing().when(worldSimulation).pauseSimulation();

    mockMvc.perform(post("/api/simulation/pause"))
        .andExpect(status().isOk());

    verify(worldSimulation, times(1)).pauseSimulation();
  }

  @Test
  void testStopSimulation() throws Exception {
    doNothing().when(worldSimulation).stopSimulation();

    mockMvc.perform(post("/api/simulation/stop"))
        .andExpect(status().isOk());

    verify(worldSimulation, times(1)).stopSimulation();
  }

  @Test
  void testGetSimulationStatus() throws Exception {
    when(worldSimulation.getPauseStatus()).thenReturn(1);

    mockMvc.perform(get("/api/simulation/getStatus"))
        .andExpect(status().isOk())
        .andExpect(content().string("1"));

    verify(worldSimulation, times(1)).getPauseStatus();
  }

  @Test
  void testFastForwardSimulation() throws Exception {
    doNothing().when(worldSimulation).changeSimulationSpeed(anyDouble());

    mockMvc.perform(post("/api/simulation/fastForward")
            .param("speedMultiplier", "2.0"))
        .andExpect(status().isOk());

    verify(worldSimulation, times(1)).changeSimulationSpeed(2.0);
  }

  @Test
  void testGetCurrentSimulationTime() throws Exception {
    when(worldSimulation.getCurrentTime()).thenReturn(LocalTime.of(12, 0));

    mockMvc.perform(get("/api/simulation/currentTime"))
        .andExpect(status().isOk())
        .andExpect(content().string("\"12:00:00\""));

    verify(worldSimulation, times(1)).getCurrentTime();
  }

  @Test
  void testGetCurrentDate() throws Exception {
    when(worldSimulation.getCurrentDate()).thenReturn(LocalDate.of(2023, 1, 1));

    mockMvc.perform(get("/api/simulation/currentDate"))
        .andExpect(status().isOk())
        .andExpect(content().string("\"2023-01-01\""));

    verify(worldSimulation, times(1)).getCurrentDate();
  }

  @Test
  void testGetCurrentDateTime() throws Exception {
    when(worldSimulation.getCurrentDateTime()).thenReturn(LocalDateTime.of(2023, 1, 1, 12, 0));

    mockMvc.perform(get("/api/simulation/CurrentDateTime"))
        .andExpect(status().isOk())
        .andExpect(content().string("\"2023-01-01T12:00:00\""));

    verify(worldSimulation, times(1)).getCurrentDateTime();
  }

  @Test
  void testSetIntervalId() throws Exception {
    doNothing().when(worldSimulation).setIntervalId(anyInt());

    mockMvc.perform(post("/api/simulation/setIntervalId")
            .param("intervalId", "1000"))
        .andExpect(status().isOk());

    verify(worldSimulation, times(1)).setIntervalId(1000);
  }

  @Test
  void testGetIntervalId() throws Exception {
    when(worldSimulation.getIntervalId()).thenReturn(1000);

    mockMvc.perform(get("/api/simulation/getIntervalId"))
        .andExpect(status().isOk())
        .andExpect(content().string("1000"));

    verify(worldSimulation, times(1)).getIntervalId();
  }

  @Test
  void testGetModels() throws Exception {
    when(worldSimulation.getModels()).thenReturn(Map.of("zone1", mock(RandomForest.class)));

    mockMvc.perform(get("/api/simulation/getModels"))
        .andExpect(status().isOk());

    verify(worldSimulation, times(1)).getModels();
  }

  @Test
  void testGetModel() throws Exception {
    RandomForest mockModel = mock(RandomForest.class);
    when(worldSimulation.getModels()).thenReturn(Map.of("1", mockModel));

    mockMvc.perform(get("/api/simulation/getModel/1"))
        .andExpect(status().isOk());

    verify(worldSimulation, times(1)).getModels();
  }

  @Test
  void testGetSpeed() throws Exception {
    when(worldSimulation.getSpeedFactory()).thenReturn(2);

    mockMvc.perform(get("/api/simulation/getSpeed"))
        .andExpect(status().isOk())
        .andExpect(content().string("2"));

    verify(worldSimulation, times(1)).getSpeedFactory();
  }
}