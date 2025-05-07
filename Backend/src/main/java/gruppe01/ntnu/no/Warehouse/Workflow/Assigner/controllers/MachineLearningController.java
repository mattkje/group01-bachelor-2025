package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning.MachineLearningModelPicking;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * MachineLearningController handles HTTP requests related to machine learning operations.
 * It provides endpoints to create CSV files, start models, and retrieve model parameters.
 */
@RestController
@RequestMapping("/api/ml")
public class MachineLearningController {

    private final MachineLearningModelPicking machineLearningModelPicking;

    /**
     * Constructor for MachineLearningController.
     */
    public MachineLearningController(
            MachineLearningModelPicking machineLearningModelPicking) {
        this.machineLearningModelPicking = machineLearningModelPicking;
    }

    @GetMapping("/train-model/{department}")
    public String getStartingParameters(@PathVariable String department)
            throws IOException {
        return machineLearningModelPicking.createModel(department, false);
    }

    @GetMapping("/get-mc-values/{department}")
    public Map<List<Double>, List<List<Double>>> getMcValues(@PathVariable String department)
            throws IOException {
        return machineLearningModelPicking.getMcValues(department);
    }

    @GetMapping("/get-weights/{department}")
    public List<Double> getWeights(@PathVariable String department)
            throws IOException {
        return machineLearningModelPicking.getMcWeights(department);
    }

    @GetMapping("/get-starting-efficiency/{department}")
    public List<Double> getStartingEfficiency(@PathVariable String department)
            throws IOException {
        return machineLearningModelPicking.getMcWorkerEfficiency(department);
    }


}
