package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.CsvGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/create-csv")
    public void createCsv() {
        csvGenerator.createCsvForML();
    }

    @GetMapping("/start-model")
    public void startModel() throws IOException, URISyntaxException {
        machineLearningModel.startModel();
    }
}
