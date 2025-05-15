package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.SimulationController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.WorldSimulationController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.ActiveTaskGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.PickerTaskGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.dummydata.TimeTableGenerator;
import gruppe01.ntnu.no.warehouse.workflow.assigner.machinelearning.MachineLearningModelPicking;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.SimulationService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.results.ZoneSimResult;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.worldsimulation.WorldSimulation;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import smile.regression.RandomForest;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@WebMvcTest(SimulationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SimulationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ActiveTaskGenerator activeTaskGeneratorService;

  @MockitoBean
  private TimeTableGenerator timeTableGenerator;

  @MockitoBean
  private WorldSimulation worldSimulation;

  @MockitoBean
  private SimulationService simulationService;

  @MockitoBean
  private PickerTaskGenerator pickerTaskGenerator;

  @MockitoBean
  private WorldSimulationController worldSimulationController;

  @MockitoBean
  private MachineLearningModelPicking machineLearningModelPicking;


  @Test
  void testGenerateActiveTasks() throws Exception {
    String date = "2023-10-01";
    int numDays = 5;

    mockMvc.perform(get("/api/generate-active-tasks/{date}/{numDays}", date, numDays))
        .andExpect(status().isOk());

    verify(activeTaskGeneratorService, times(1)).generateActiveTasks(LocalDate.parse(date),
        numDays);
  }

  @Test
  void testMonteCarlo() throws Exception {
      when(worldSimulationController.getCurrentDateTime()).thenReturn(ResponseEntity.ok(LocalDateTime.now()));
      when(worldSimulationController.getModels()).thenReturn(Map.of("model1", mock(RandomForest.class)));
      when(simulationService.runCompleteSimulation(any(), any())).thenReturn(Map.of(1L, List.of("Result1")));

      mockMvc.perform(get("/api/monte-carlo"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.1[0]").value("Result1"));
      verify(simulationService, times(1)).runCompleteSimulation(any(), any());
  }

  @Test
  void testMonteCarloStartOfDay() throws Exception {
      when(simulationService.runCompleteSimulation(isNull(), any()))
          .thenReturn(Map.of(1L, List.of("StartOfDayResult")));

      mockMvc.perform(get("/api/monte-carlo-start-of-day"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.1[0]").value("StartOfDayResult"));
      verify(simulationService, times(1)).runCompleteSimulation(isNull(), any());
  }

  @Test
  void testMonteCarloZone() throws Exception {
      when(worldSimulationController.getCurrentDateTime()).thenReturn(ResponseEntity.ok(LocalDateTime.now()));
      when(worldSimulationController.getModels()).thenReturn(Map.of("model1", mock(RandomForest.class)));
      when(simulationService.runZoneSimulation(anyLong(), any(), any())).thenReturn(List.of(new ZoneSimResult()));

      mockMvc.perform(get("/api/monte-carlo/zones/{id}", 1L))
          .andExpect(status().isOk());
      verify(simulationService, times(1)).runZoneSimulation(anyLong(), any(), any());
  }

  @Test
  void testMonteCarloZoneOnSpecificDay() throws Exception {
      when(simulationService.runZoneSimulation(anyLong(), any(), any())).thenReturn(List.of());

      mockMvc.perform(get("/api/monte-carlo/zones/{id}/day/{day}", 1L, "2023-10-01"))
          .andExpect(status().isOk());
      verify(simulationService, times(1)).runZoneSimulation(anyLong(), any(), any());
  }

  @Test
  void testGenerateTimeTable() throws Exception {
      mockMvc.perform(get("/api/generate-timetable/{date}", "2023-10-01"))
          .andExpect(status().isOk());
      verify(timeTableGenerator, times(1)).generateTimeTable(LocalDate.parse("2023-10-01"));
  }

  @Test
  void testGeneratePickerTasks() throws Exception {
      mockMvc.perform(get("/api/generate-picker-tasks/{date}/{numDays}/{numOfTasksPerDay}", "2023-10-01", 5, 10))
          .andExpect(status().isOk());
      verify(pickerTaskGenerator, times(1)).generatePickerTasks(any(), eq(5), eq(10), any(), eq(false));
  }

  @Test
  void testRunWorldSimulation() throws Exception {
      mockMvc.perform(get("/api/run-world-simulation"))
          .andExpect(status().isOk());
      verify(worldSimulation, times(1)).runWorldSimulation(2, LocalDate.now(), false);
  }

  @Test
  void testSimulateOneYear() throws Exception {
      mockMvc.perform(get("/api/simulate-one-year"))
          .andExpect(status().isOk());
      verify(worldSimulation, times(1)).simulateOneYear();
  }

  @Test
  void testSetSimCount() throws Exception {
      mockMvc.perform(post("/api/setSimCount").param("simCount", "5"))
          .andExpect(status().isOk());
      verify(simulationService, times(1)).setSimCount(5);
  }

  @Test
  void testGetSimCount() throws Exception {
      when(simulationService.getSimCount()).thenReturn(10);

      mockMvc.perform(get("/api/getSimCount"))
          .andExpect(status().isOk())
          .andExpect(content().string("10"));
      verify(simulationService, times(1)).getSimCount();
  }

  @Test
  void testSetPrediction() throws Exception {
      mockMvc.perform(post("/api/setPrediction").param("prediction", "true"))
          .andExpect(status().isOk());
      verify(simulationService, times(1)).setPrediction(true);
  }

  @Test
  void testGetPrediction() throws Exception {
      when(simulationService.isPrediction()).thenReturn(true);

      mockMvc.perform(get("/api/getPrediction"))
          .andExpect(status().isOk())
          .andExpect(content().string("true"));
      verify(simulationService, times(1)).isPrediction();
  }

  @Test
  void testResetSim() throws Exception {
      mockMvc.perform(get("/api/resetSim"))
          .andExpect(status().isOk());
      verify(worldSimulation, times(1)).resetSimulationDate();
  }

  @Test
  void testGetZoneData() throws Exception {
      when(worldSimulation.getData(anyLong())).thenReturn(List.of(1, 2, 3));

      mockMvc.perform(get("/api/zone-data/{zoneId}", 1L))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0]").value(1))
          .andExpect(jsonPath("$[1]").value(2))
          .andExpect(jsonPath("$[2]").value(3));
      verify(worldSimulation, times(1)).getData(1L);
  }

  @Test
  void testCreateData() throws Exception {
      mockMvc.perform(get("/api/create-data/{department}", "logistics"))
          .andExpect(status().isOk())
          .andExpect(content().string("Data created"));
      verify(machineLearningModelPicking, times(1)).createSynetheticData("logistics");
  }
}