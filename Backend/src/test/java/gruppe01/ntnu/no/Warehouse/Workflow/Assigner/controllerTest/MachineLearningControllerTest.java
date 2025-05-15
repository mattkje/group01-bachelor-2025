package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.MachineLearningController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.machinelearning.MachineLearningModelPicking;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

@WebMvcTest(MachineLearningController.class)
@AutoConfigureMockMvc(addFilters = false)
class MachineLearningControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MachineLearningModelPicking machineLearningModelPicking;

  @Test
  void testGetStartingParameters() throws Exception {
    // Arrange
    String department = "IT";
    when(machineLearningModelPicking.createModel(department, false)).thenReturn("Model Created");

    // Act & Assert
    mockMvc.perform(get("/api/ml/train-model/{department}", department))
        .andExpect(status().isOk())
        .andExpect(content().string("Model Created"));
    verify(machineLearningModelPicking, times(1)).createModel(department, false);
  }

  @Test
  void testGetMcValues() throws Exception {
    // Arrange
    String department = "IT";
    Map<List<Double>, List<List<Double>>> mockValues = Map.of();
    when(machineLearningModelPicking.getMcValues(department)).thenReturn(mockValues);

    // Act & Assert
    mockMvc.perform(get("/api/ml/get-mc-values/{department}", department))
        .andExpect(status().isOk());
    verify(machineLearningModelPicking, times(1)).getMcValues(department);
  }

  @Test
  void testGetWeights() throws Exception {
    // Arrange
    String department = "IT";
    List<Double> mockWeights = List.of(1.0, 2.0, 3.0);
    when(machineLearningModelPicking.getMcWeights(department)).thenReturn(mockWeights);

    // Act & Assert
    mockMvc.perform(get("/api/ml/get-weights/{department}", department))
        .andExpect(status().isOk());
    verify(machineLearningModelPicking, times(1)).getMcWeights(department);
  }

  @Test
  void testGetStartingEfficiency() throws Exception {
    // Arrange
    String department = "IT";
    List<Double> mockEfficiency = List.of(0.8, 0.9);
    when(machineLearningModelPicking.getMcWorkerEfficiency(department)).thenReturn(mockEfficiency);

    // Act & Assert
    mockMvc.perform(get("/api/ml/get-starting-efficiency/{department}", department))
        .andExpect(status().isOk());
    verify(machineLearningModelPicking, times(1)).getMcWorkerEfficiency(department);
  }
}