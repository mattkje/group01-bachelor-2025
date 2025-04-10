package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.RandomForest;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class handles the machine learning model for predicting time spent on tasks.
 * It uses a Random Forest model from the Smile library.
 * <p>
 * The output determine the current weights for each attribute for a picking operation
 * The result of the ML model is used within the MC simulations to calculate a predicted time outcome
 * based on the weights of the attributes.
 */
@Component
public class MachineLearningModelPicking {

  /**
   * Creates a model if one does not exist
   * It loads an existing model if available, otherwise it trains a new one.
   *
   * @throws IOException If there is an error reading the CSV or saving/loading the model.
   * @param department The department for which the model is being created.
   * @return A message indicating the result of the model creation.
   */
  public String createModel(String department) throws IOException {
    // If a model exists, update it
    // TODO: Add method for updating the ML Model
    String filePath = "pickroute_"+department.toUpperCase()+".ser";

    // Attempt to load an existing model
    RandomForest model = loadModel(filePath);

    // If model is null, train a new one
    if (model == null) {

      // If no model exists, train a new one on an existing dataset
      String csvFilePath = "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_"+department.toUpperCase()+"_time.csv";

      // Parse CSV and create DataFrame
      DataFrame data = parseCsvToDataFrame(csvFilePath);
      if (data != null) {
        // Train the model
        model = trainModel(data);
        // Save the trained model
        saveModel(model, filePath);
        return "";
      }
      return "Error: No data exists for: " + department.toUpperCase() + ".";
    }
    return "Model already exists. No new model trained.";
  }

  private List<Double> getWeights(RandomForest model) {

    // Get feature importance (weights)
    double[] importances = model.importance();
    double sum = Arrays.stream(importances).sum();
    // Convert to a list of doubles
    List<Double> importanceList = new ArrayList<>();
    for (double importance : importances) {
      double normalizedImportance = importance / sum;
      importanceList.add(normalizedImportance);
    }

    return importanceList;
  }

  /**
   * Parses a CSV file to create a DataFrame, excluding the first header (e.g., an ID column).
   * The CSV file should contain the following columns:
   * distance_m (distance in meters),
   * dpack_equivalent_amount (equivalent amount of packages),
   * lines (Stops for a worker),
   * weight_g (weight in grams),
   * volume_ml (volume in milliliters),
   * avg_height (average height of the packages),
   * picker (Employee ID),
   * time_s (time in seconds).
   *
   * @param csvFilePath The path to the CSV file.
   * @return A DataFrame containing the parsed data.
   * @throws IOException If there is an error reading the CSV file.
   */
private DataFrame parseCsvToDataFrame(String csvFilePath) {
      FileReader reader = null;
      CSVParser parser = null;

      try {
          reader = new FileReader(csvFilePath);
          parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

          // Get headers from the CSV file and exclude the first one
          List<String> headers = parser.getHeaderNames().subList(1, parser.getHeaderNames().size());
          List<List<Double>> columnData = new ArrayList<>();

          // Initialize lists for each column (excluding the first header)
          for (String header : headers) {
              columnData.add(new ArrayList<>());
          }

          // Parse each record and populate the columns (excluding the first column)
          for (CSVRecord record : parser) {
              for (int i = 1; i < record.size(); i++) { // Start from index 1 to skip the first column
                  String value = record.get(i); // Skip the first column
                  columnData.get(i - 1).add(Double.parseDouble(value)); // Adjust index for columnData
              }
          }

          // Convert lists to arrays and build the DataFrame
          smile.data.vector.DoubleVector[] vectors = new smile.data.vector.DoubleVector[headers.size()];
          for (int i = 0; i < headers.size(); i++) {
              double[] columnArray = columnData.get(i).stream().mapToDouble(Double::doubleValue).toArray();
              vectors[i] = smile.data.vector.DoubleVector.of(headers.get(i), columnArray);
          }

          return DataFrame.of(vectors);

      } catch (IOException | NumberFormatException e) {
          System.err.println("Error parsing CSV file: " + e.getMessage());
          e.printStackTrace();
          return null;
      } finally {
          try {
              if (parser != null) {
                  parser.close();
              }
              if (reader != null) {
                  reader.close();
              }
          } catch (IOException e) {
              System.err.println("Error closing resources: " + e.getMessage());
          }
      }
  }
  /**
   * Trains a RandomForest model using the provided DataFrame.
   *
   * @param data The DataFrame containing the training data.
   * @return The trained RandomForest model.
   */
  private RandomForest trainModel(DataFrame data) {
    String target = "time_s"; // Updated to match the final attribute in the CSV
    if (!Arrays.asList(data.names()).contains(target)) {
      throw new IllegalArgumentException(
          "Column '" + target + "' not found in CSV. Detected columns: " +
              Arrays.toString(data.names()));
    }

    Formula formula = Formula.lhs(target);

    // Train the RandomForest model
    return RandomForest.fit(formula, data);
  }

  /**
   * Loads a RandomForest model from a file.
   *
   * @param filePath The path to the model file.
   * @return The loaded RandomForest model, or null if no model was found.
   */
  public RandomForest loadModel(String filePath) {
    try {
      return ModelLoader.loadModel(filePath);
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("No existing model found. A new model will be trained.");
      return null;
    }
  }

  /**
   * Saves the RandomForest model to a file.
   *
   * @param model    The RandomForest model to save.
   * @param filePath The path to save the model file.
   */
  public void saveModel(RandomForest model, String filePath) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
      oos.writeObject(model);
      System.out.println("Model saved successfully to " + filePath);
    } catch (IOException e) {
      System.err.println("Error saving model: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Returns a list of lists containing the lowest and highest numerical values
   * from each attribute in the DataFrame.
   *
   * @param data The DataFrame containing the data.
   * @return A list of lists, where each inner list contains the minimum and maximum
   * values for a column in the DataFrame.
   */
  public List<List<Double>> getMinMaxValues(DataFrame data) {
    List<List<Double>> minMaxValues = new ArrayList<>();

    // Iterate through each column in the DataFrame
    for (String columnName : data.names()) {
      double[] columnData = data.column(columnName).toDoubleArray();

      // Calculate the minimum and maximum values for the column
      double min = Arrays.stream(columnData).min().orElse(Double.NaN);
      double max = Arrays.stream(columnData).max().orElse(Double.NaN);

      // Add the min and max values as a list to the result
      minMaxValues.add(Arrays.asList(min, max));
    }

    return minMaxValues;
  }

  public Map<List<Double>, List<List<Double>>> getMcValues(String department) throws IOException {
    String filePath = "pickroute_" + department.toUpperCase() + ".ser";
    // Attempt to load an existing model
    RandomForest model = loadModel(filePath);
    Map<List<Double>, List<List<Double>>> mcValues = new HashMap<>();
    if (model != null) {

      List<Double> weights = getWeights(model);
      String csvFilePath =
          "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_" +
              department.toUpperCase() + "_time.csv";
      DataFrame data = parseCsvToDataFrame(csvFilePath);
      List<List<Double>> minMaxValues = getMinMaxValues(data);

      mcValues.put(weights, minMaxValues);
    } else {
      // If no model exists, train a new one on an existing dataset
      if (createModel(department).isEmpty()){
        // if the model is created, get the mc values
        return getMcValues(department);
      }
    }
    return mcValues;
  }
}
