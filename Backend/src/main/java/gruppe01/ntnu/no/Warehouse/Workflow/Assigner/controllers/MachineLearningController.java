package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.machinelearning.MachineLearningModelPicking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MachineLearningController handles HTTP requests related to machine learning operations.
 * It provides endpoints to create CSV files, start models, and retrieve model parameters.
 */
@RestController
@RequestMapping("/api/ml")
@Tag(name = "MachineLearningController", description
    = "Controller for managing machine learning operations")
public class MachineLearningController {

  private final MachineLearningModelPicking machineLearningModelPicking;

  /**
   * Constructor for MachineLearningController.
   */
  public MachineLearningController(
      MachineLearningModelPicking machineLearningModelPicking) {
    this.machineLearningModelPicking = machineLearningModelPicking;
  }

  /**
   * Creates CSV files for machine learning models.
   *
   * @param department the department for which to create CSV files
   * @return a response entity containing the result of the operation
   * @throws IOException if an I/O error occurs
   */
  @Operation(
      summary = "Create .ser files for machine learning models",
      description = "Creates .ser files for machine learning "
          + "models based on the specified department."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully created .ser files"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/train-model/{department}")
  public ResponseEntity<String> getStartingParameters(
      @Parameter(description = "Department for which to create .ser files")
      @PathVariable String department)
      throws IOException {
    return ResponseEntity.ok(machineLearningModelPicking.createModel(department, false));
  }

  /**
   * Starts the machine learning model for a specific department.
   *
   * @param department the department for which to start the model
   * @return a response entity containing the result of the operation
   * @throws IOException if an I/O error occurs
   */
  @Operation(
      summary = "Fetch mc values for a department",
      description = "Fetches mc values for a department."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched mc values"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/get-mc-values/{department}")
  public ResponseEntity<Map<List<Double>, List<List<Double>>>> getMcValues(
      @Parameter(description = "Department for which to fetch mc values")
      @PathVariable String department)
      throws IOException {
    return ResponseEntity.ok(machineLearningModelPicking.getMcValues(department));
  }

  /**
   * Fetches the mc weights for a specific department.
   *
   * @param department the department for which to fetch mc weights
   * @return a response entity containing the mc weights
   * @throws IOException if an I/O error occurs
   */
  @Operation(
      summary = "Fetch mc weights for a department",
      description = "Fetches mc weights for a department."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched mc weights"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/get-weights/{department}")
  public ResponseEntity<List<Double>> getWeights(
      @Parameter(description = "Department for which to fetch mc weights")
      @PathVariable String department)
      throws IOException {
    return ResponseEntity.ok(machineLearningModelPicking.getMcWeights(department));
  }

  /**
   * Fetches the mc worker efficiency for a specific department.
   *
   * @param department the department for which to fetch mc worker efficiency
   * @return a response entity containing the mc worker efficiency
   * @throws IOException if an I/O error occurs
   */
  @Operation(
      summary = "Fetch mc worker efficiency for a department",
      description = "Fetches mc worker efficiency for a department."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully fetched mc worker efficiency"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/get-starting-efficiency/{department}")
  public ResponseEntity<List<Double>> getStartingEfficiency(
      @Parameter(description = "Department for which to fetch mc worker efficiency")
      @PathVariable String department)
      throws IOException {
    return ResponseEntity.ok(machineLearningModelPicking.getMcWorkerEfficiency(department));
  }


}
