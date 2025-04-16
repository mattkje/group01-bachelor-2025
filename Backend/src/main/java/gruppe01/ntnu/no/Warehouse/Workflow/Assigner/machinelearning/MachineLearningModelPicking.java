package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.measure.NominalScale;
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
   * @param department The department for which the model is being created.
   * @return A message indicating the result of the model creation.
   * @throws IOException If there is an error reading the CSV or saving/loading the model.
   */
  public String createModel(String department, boolean isWorkerEfficiency) throws IOException {
    // If a model exists, update it
    // TODO: Add method for updating the ML Model
    String filePath = "";
    if (isWorkerEfficiency){
      filePath = "worker_efficiency_" + department.toUpperCase() + "_worker.ser";
    } else {
       filePath = "pickroute_" + department.toUpperCase() + ".ser";
    }

    // Attempt to load an existing model
    RandomForest model = loadModel(filePath);

    // If model is null, train a new one
    if (model == null) {

      // If no model exists, train a new one on an existing dataset
      String csvFilePath =
          "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_" +
              department.toUpperCase() + "_time.csv";

      // Parse CSV and create DataFrame
      DataFrame data = parseCsvToDataFrame(csvFilePath);
      if (data != null) {
        // Train the model
        if (isWorkerEfficiency) {
          model = trainModelForWorkerEfficiency(data);

        } else {
          model = trainModel(data);
        }
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
        double[] columnArray =
            columnData.get(i).stream().mapToDouble(Double::doubleValue).toArray();
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
    String target = "time_s";

    if (!Arrays.asList(data.names()).contains(target)) {
      throw new IllegalArgumentException(
          "Column '" + target + "' not found in CSV. Detected columns: " +
              Arrays.toString(data.names()));
    }

    Formula formula = Formula.lhs(target);

    // Train the RandomForest model
    return RandomForest.fit(formula, data);
  }

private RandomForest trainModelForWorkerEfficiency(DataFrame data) {
    String target = "picker";

    // Check if the target column exists
    if (!Arrays.asList(data.names()).contains(target)) {
        throw new IllegalArgumentException(
            "Column '" + target + "' not found in CSV. Detected columns: " +
                Arrays.toString(data.names()));
    }

    // Factorize the target column (picker) into numerical categories
    int[] factorized = data.stringVector(target).factorize(new NominalScale(data.stringVector(target).distinct().toArray(new String[0]))).toIntArray();

    // Create dummy variables (one-hot encoding)
    DataFrame dummyVariables = DataFrame.of(
        IntStream.range(0, factorized.length).mapToObj(i -> new int[]{factorized[i]}).toArray(int[][]::new));
    // Merge dummy variables with the original data
    DataFrame encodedData = data.merge(dummyVariables);

    // Remove the original target column (picker) from the dataset
    encodedData = encodedData.drop(target);

    // Create a formula for training (e.g., "picker_0 + picker_1 + ... ~ other features")
    Formula formula = Formula.lhs(dummyVariables.names()[0]); // Adjust based on encoding

    // Train the RandomForest model
    return RandomForest.fit(formula, encodedData);
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

  /**
   * Returns a map of weights and min-max values for the specified department.
   *
   * @param department The department for which to get the weights and min-max values.
   * @return A map where the key is a list of weights and the value is a list of lists
   * containing the min-max values for each attribute.
   * @throws IOException If there is an error loading the model or parsing the CSV file.
   */
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
      if (createModel(department, false).isEmpty()) {
        // if the model is created, get the mc values
        return getMcValues(department);
      }
    }
    return mcValues;
  }

  /**
   * Returns the weights of the RandomForest model for a given department.
   *
   * @param department The department for which to get the weights.
   * @return A list of weights for the specified department.
   * @throws IOException If there is an error loading the model.
   */
  public List<Double> getMcWeights(String department) throws IOException {
    String filePath = "pickroute_" + department.toUpperCase() + ".ser";
    // Attempt to load an existing model
    RandomForest model = loadModel(filePath);
    List<Double> weights = new ArrayList<>();
    if (model != null) {
      weights = getWeights(model);
    } else {
      // If no model exists, train a new one on an existing dataset
      if (createModel(department, false).isEmpty()) {
        // if the model is created, get the mc values
        return getMcWeights(department);
      }
    }
    return weights;
  }

  public List<Double> getMcWorkerEfficiency(String department) throws IOException {
      String filePath = "worker_efficiency_" + department.toUpperCase() + "_worker.ser";
      RandomForest model = loadModel(filePath);

      if (model == null) {
          // Train a new model if it doesn't exist
          if (createModel(department, true).isEmpty()) {
              return getMcWorkerEfficiency(department);
          }
      }

      // Parse the dataset to create a DataFrame for predictions
      String csvFilePath = "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_"
                           + department.toUpperCase() + "_time.csv";
      DataFrame data = parseCsvToDataFrame(csvFilePath);

      if (data == null) {
          throw new IllegalStateException("Failed to parse the dataset for predictions.");
      }

      // Use the model to predict worker efficiency
    assert model != null;
    double[] predictions = model.predict(data);

      // Convert predictions to a list of doubles
      List<Double> efficiencyValues = new ArrayList<>();
      for (double prediction : predictions) {
          efficiencyValues.add(prediction);
      }

      return efficiencyValues;
  }

  public long estimateTimeUsingWeights(
          String department,
          double distance, double dpack, double lines,
          double weight, double volume, double avgHeight, long picker
  ) throws IOException {
    // Get weights from trained model
    List<Double> weights = getMcWeights(department);

    // Load CSV data to get min-max values for normalization
    String csvFilePath =
            "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_" +
                    department.toUpperCase() + "_time.csv";

    DataFrame data = parseCsvToDataFrame(csvFilePath);
    List<List<Double>> minMaxValues = getMinMaxValues(data);

    // Your input features
    double[] features = new double[]{distance, dpack, lines, weight, volume, avgHeight, picker};

    double estimatedTime = 0.0;
    for (int i = 0; i < features.length; i++) {
      double min = minMaxValues.get(i).get(0);
      double max = minMaxValues.get(i).get(1);

      double normalized = (features[i] - min) / (max - min);
      estimatedTime += weights.get(i) * normalized;
    }

    // Optional: scale to actual average time
    double[] actualTimes = data.column("time_s").toDoubleArray();
    double averageTime = Arrays.stream(actualTimes).average().orElse(1.0); // fallback
    return (long) (estimatedTime * averageTime);
  }


}
