package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.CsvGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModel;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/ml")
public class MachineLearningController {

  @Autowired
  private CsvGenerator csvGenerator;
  @Autowired
  private MachineLearningModel machineLearningModel;
  @Autowired
  private MachineLearningModelPicking machineLearningModelPicking;

  @GetMapping("/create-csv")
  public void createCsv() {
    csvGenerator.createCsvForML();
  }

  @GetMapping("/start-model")
  public void startModel() throws IOException, URISyntaxException {
    machineLearningModel.startModel();
  }

  @GetMapping("/train-model/{department}")
  public String getStartingParameters(@PathVariable String department)
      throws IOException, URISyntaxException {
    return machineLearningModelPicking.createModel(department, false);
  }

  @GetMapping("/get-mc-values/{department}")
  public Map<List<Double>, List<List<Double>>> getMcValues(@PathVariable String department)
      throws IOException, URISyntaxException {
    return machineLearningModelPicking.getMcValues(department);
  }

  @GetMapping("/get-weights/{department}")
  public List<Double> getWeights(@PathVariable String department)
      throws IOException, URISyntaxException {
    return machineLearningModelPicking.getMcWeights(department);
  }

  @GetMapping("/get-starting-efficiency/{department}")
  public List<Double> getStartingEfficiency(@PathVariable String department)
      throws IOException, URISyntaxException {
    return machineLearningModelPicking.getMcWorkerEfficiency(department);
  }


}
