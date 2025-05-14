package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.PickerTaskGenerator;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.measure.NominalScale;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.data.vector.DoubleVector;

import smile.io.Read;
import smile.io.Write;
import smile.regression.RandomForest;
import smile.validation.metric.MSE;
import smile.validation.metric.R2;

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

  @Autowired
  @Lazy
  private PickerTaskGenerator pickerTaskGenerator;

  Map<String, DataFrame> dataFrames = new HashMap<>();
  Map<String, RandomForest> randomForests = new HashMap<>();
  private static final String METRICS_FILE = "model_performance_metrics.csv";

    public MachineLearningModelPicking() {}

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
    if (isWorkerEfficiency) {
      filePath = "worker_efficiency_" + department.toUpperCase() + "_worker.ser";
    } else {
      filePath = "pickroute_" + department.toUpperCase() + ".ser";
    }

    // Attempt to load an existing model
    RandomForest model = loadModel(department, filePath);

    // If model is null, train a new one
    if (model == null) {

      // If no model exists, train a new one on an existing dataset
      String csvFilePath =
          "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_" +
              department.toUpperCase() + "_time.csv";

      // Parse CSV and create DataFrame
      DataFrame data = parseCsvToDataFrame(department, csvFilePath);
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

  public void createSynetheticData(String department) throws IOException, URISyntaxException {
    DataFrame data = parseCsvToDataFrame(department, "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_" +
            department.toUpperCase() + "_time.csv");

    // Columns used for regression
    String[] featureCols = new String[] {
            "distance_m", "dpack_equivalent_amount", "lines",
            "weight_g", "volume_ml", "avg_height", "picker"
    };
    String targetCol = "time_s";

    // Train regression model on original data
    Formula formula = Formula.of(targetCol, featureCols);
    RandomForest model = RandomForest.fit(formula, data);

    // Prepare feature value samples
    Map<String, double[]> colSamples = new HashMap<>();
    for (String col : featureCols) {
      colSamples.put(col, data.column(col).toDoubleArray());
    }

    Random random = new Random();
    List<double[]> syntheticRows = new ArrayList<>();
    int syntheticCount = 50; // number of synthetic rows

    for (int i = 0; i < syntheticCount; i++) {
      Map<String, DoubleVector> sampleVectors = new HashMap<>();
      double[] inputRow = new double[featureCols.length];

      // Randomly sample values from each feature column
      for (int j = 0; j < featureCols.length; j++) {
        String col = featureCols[j];
        double[] colData = colSamples.get(col);
        double sampled = colData[random.nextInt(colData.length)];
        inputRow[j] = sampled;
        sampleVectors.put(col, DoubleVector.of(col, new double[]{sampled}));
      }

      // Create single-row DataFrame
      DoubleVector[] allVectors = Stream.concat(
              sampleVectors.values().stream(),
              Stream.of(DoubleVector.of("time_s", new double[] {0.0}))
      ).toArray(DoubleVector[]::new);

      // Create the DataFrame
      DataFrame sampleDF = DataFrame.of(allVectors);

      // Predict time_s for the synthetic row
      double predictedTime = model.predict(sampleDF)[0];

      // Add full synthetic row including predicted time
      double[] fullRow = Arrays.copyOf(inputRow, inputRow.length + 1);
      fullRow[fullRow.length - 1] = predictedTime;
      syntheticRows.add(fullRow);
    }

    // Convert synthetic data to DataFrame
    String[] fullColumns = Arrays.copyOf(featureCols, featureCols.length + 1);
    fullColumns[fullColumns.length - 1] = targetCol;

    double[][] syntheticDataArray = syntheticRows.toArray(new double[0][]);
    DataFrame syntheticDF = DataFrame.of(syntheticDataArray, fullColumns);

    // Save synthetic dataset
    Write.csv(syntheticDF, Paths.get("Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/testData/synthetic_pickroutes_" +
            department.toUpperCase() + "_time.csv"));
    System.out.println("Synthetic data saved to synthetic_output.csv");
    System.out.println(createDBModel(department));
  }

  public String createDBModel(String department) throws IOException {
    // Define the file path for the model
    String filePath = "pickroute_database_" + department.toUpperCase() + ".ser";

    DataFrame dataFrame = parseCsvToDataFrame(department, "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/testData/synthetic_pickroutes_" +
            department.toUpperCase() + "_time.csv");

    // Check if the model file exists
    File modelFile = new File(filePath);

    List<PickerTask> pickerTasks = pickerTaskGenerator.generatePickerTasks(LocalDate.now(), 1, 20, this, true);

    // If the model file doesn't exist, train a new model
    if (!modelFile.exists()) {
      Formula formula = Formula.of("time_s", new String[] {
              "distance_m", "dpack_equivalent_amount", "lines", "weight_g",
              "volume_ml", "avg_height", "picker"
      });

      // Train a new RandomForest model with the provided data
      RandomForest randomForestModel = RandomForest.fit(formula, dataFrame);

      // Save the new model to the file
      saveModel(randomForestModel, filePath);
      compareModels(department, pickerTasks);

      return "New model has been created and saved for department: " + department.toUpperCase();
    } else {
      // Load the existing model
      RandomForest model = loadModel(department, filePath);
      compareModels(department, pickerTasks);

      // If the model is null or invalid, log an error
      if (model == null) {
        return "Error: Existing model could not be loaded for department: " + department.toUpperCase();
      }

      // Train the model with the new data
      model = trainModel(dataFrame);
      compareModels(department, pickerTasks);

      // Save the updated model to the file
      saveModel(model, filePath);

      return "Model has been updated with new data for department: " + department.toUpperCase();
    }
  }

  public void compareModels(String department, List<PickerTask> testData) throws IOException {
    // Load the models
    RandomForest model1 = loadModel(department, "pickroute_" + department.toUpperCase() + ".ser");
    RandomForest model2 = loadModel(department, "pickroute_database_" + department.toUpperCase() + ".ser");

    if (model1 == null || model2 == null) {
      throw new IllegalStateException("One or both models not found for department: " + department);
    }

    System.out.println("Model1 trees: " + model1.size());
    System.out.println("Model2 trees: " + model2.size());

    double totalErrorModel2 = 0.0;

    for (PickerTask pickerTask : testData) {
      double predictedByModel1 = estimateTimeUsingModel(model1, pickerTask, pickerTask.getWorker().getId());
      double predictedByModel2 = estimateTimeUsingModel(model2, pickerTask, pickerTask.getWorker().getId());

      System.out.println("Model 1 Prediction: " + predictedByModel1);
      System.out.println("Model 2 Prediction: " + predictedByModel2);

      totalErrorModel2 += Math.abs(predictedByModel1 - predictedByModel2);
    }

    System.out.println("Total error model2: " + totalErrorModel2);

    double maeModel2 = totalErrorModel2 / testData.size();

    System.out.println("Model 2 of " + department + " MAE compared to Model 1: " + maeModel2);

    saveMetrics(department, maeModel2);
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
  private DataFrame parseCsvToDataFrame(String department, String csvFilePath) {
    if (dataFrames.containsKey(department)) {
      return dataFrames.get(department);
    }

    FileReader reader = null;
    CSVParser parser = null;

    try {
      reader = new FileReader(csvFilePath);
      parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

      // Get headers from the CSV file (convert to a modifiable list)
      List<String> headers = new ArrayList<>(parser.getHeaderNames());  // Convert to ArrayList
      List<List<Double>> columnData = new ArrayList<>();

      // Initialize lists for each column
      for (String header : headers) {
        columnData.add(new ArrayList<>());
      }

      // Parse each record and populate the columns
      for (CSVRecord record : parser) {
        for (int i = 0; i < record.size(); i++) {
          String value = record.get(i);
          columnData.get(i).add(Double.parseDouble(value));
        }
      }

      // Convert lists to arrays and build the DataFrame
      smile.data.vector.DoubleVector[] vectors = new smile.data.vector.DoubleVector[headers.size()];
      for (int i = 0; i < headers.size(); i++) {
        double[] columnArray = columnData.get(i).stream().mapToDouble(Double::doubleValue).toArray();
        vectors[i] = smile.data.vector.DoubleVector.of(headers.get(i), columnArray);
      }

      DataFrame dataFrame = DataFrame.of(vectors);

      dataFrames.put(department, dataFrame);
      return dataFrame;

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
    // Train the RandomForest model
    String[] featureColumns = new String[] {
            "distance_m",
            "dpack_equivalent_amount",
            "lines",
            "weight_g",
            "volume_ml",
            "avg_height",
            "picker"
    };

    Formula formula = Formula.of(target, featureColumns);
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
    int[] factorized = data.stringVector(target)
        .factorize(new NominalScale(data.stringVector(target).distinct().toArray(new String[0])))
        .toIntArray();

    // Create dummy variables (one-hot encoding)
    DataFrame dummyVariables = DataFrame.of(
        Arrays.stream(factorized).mapToObj(j -> new int[] {j}).toArray(int[][]::new));
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
  public RandomForest loadModel(String department, String filePath) {
    if (randomForests.containsKey(department.toUpperCase())) {
      return randomForests.get(department.toUpperCase());
    }

    try {
      RandomForest model = ModelLoader.loadModel(filePath);
      if (model != null) {
        randomForests.put(department.toUpperCase(), model);
      } else {
        copyDefaultModelIfMissing(department.toUpperCase(), filePath);
      }
      return model;
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("Failed to load model for department: " + department);
      return null;
    }
  }


  private void copyDefaultModelIfMissing(String department, String filePath) {
    Path modelPath = Path.of(filePath);
    if (!Files.exists(modelPath)) {
      System.out.println("No model found for " + department + ". Copying default DRY model.");
      try {
        Path defaultModel = Path.of("pickroute_DRY.ser");
        Files.copy(defaultModel, modelPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        System.err.println("Error copying default model for " + department + ": " + e.getMessage());
      }
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
    RandomForest model = loadModel(department, filePath);
    Map<List<Double>, List<List<Double>>> mcValues = new HashMap<>();
    if (model != null) {

      List<Double> weights = getWeights(model);
            String csvFilePath =
            "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_" +
              department.toUpperCase() + "_time.csv";
      DataFrame data = parseCsvToDataFrame(department, csvFilePath);
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
    RandomForest model = loadModel(department, filePath);
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
    RandomForest model = loadModel(department, filePath);

    if (model == null) {
      // Train a new model if it doesn't exist
      if (createModel(department, true).isEmpty()) {
        return getMcWorkerEfficiency(department);
      }
    }

    // Parse the dataset to create a DataFrame for predictions
    String csvFilePath =
        "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_"
            + department.toUpperCase() + "_time.csv";
    DataFrame data = parseCsvToDataFrame(department, csvFilePath);

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

  public long estimateTimeUsingModel(
          RandomForest model, PickerTask pickerTask, long workerId
  ) throws IOException {
    if (model == null) {
      throw new IllegalStateException("Model not provided for department");
    }

    // Extract features from the PickerTask object
    double[] features = new double[] {
            pickerTask.getDistance(),
            pickerTask.getPackAmount(),
            pickerTask.getLinesAmount(),
            pickerTask.getWeight(),
            pickerTask.getVolume(),
            pickerTask.getAvgHeight(),
            workerId
    };

    // Create a DataFrame with only the feature columns (exclude time_s)
    DataFrame featureDataFrame = DataFrame.of(
            DoubleVector.of("distance_m", new double[] {features[0]}),
            DoubleVector.of("dpack_equivalent_amount", new double[] {features[1]}),
            DoubleVector.of("lines", new double[] {features[2]}),
            DoubleVector.of("weight_g", new double[] {features[3]}),
            DoubleVector.of("volume_ml", new double[] {features[4]}),
            DoubleVector.of("avg_height", new double[] {features[5]}),
            DoubleVector.of("picker", new double[] {features[6]}),
            DoubleVector.of("time_s", new double[] {0.0})
    );
    // Predict the time using the model
    double[] predictions = model.predict(featureDataFrame);

    // Convert to long and return the predicted time
    return (long) predictions[0];
  }


  /**
   * Retrieves the trained RandomForest model for a given department.
   *
   * @param department         The department for which to retrieve the model.
   * @param isWorkerEfficiency Whether the model is for worker efficiency or picking routes.
   * @return The trained RandomForest model.
   * @throws IOException If there is an error loading the model.
   */
  public RandomForest getModel(String department, boolean isWorkerEfficiency) throws IOException {
    String filePath;
    if (isWorkerEfficiency) {
      filePath = "worker_efficiency_" + department.toUpperCase() + "_worker.ser";
    } else {
      filePath = "pickroute_" + department.toUpperCase() + ".ser";
    }

    // Attempt to load the model
    RandomForest model = loadModel(department, filePath);

    // If the model does not exist, create and return a new one
    if (model == null) {
      createModel(department, isWorkerEfficiency);
      model = loadModel(department, filePath);
    }

    return model;
  }

  public Map<String,RandomForest> getAllModels() throws IOException {
    Map<String,RandomForest> models = new HashMap<>();
    String[] departments = {"DRY", "FREEZE", "FRUIT"};
    for (String department : departments) {
      RandomForest model = getModel(department, false);
      models.put(department, model);
    }
    return models;
  }


  /**
   * Updates the machine learning model with new data from picker tasks.
   *
   * @param pickerTasks The list of picker tasks to update the model with.
   * @param department The department for which the model is being updated.
   */
  public void updateMachineLearningModel(List<PickerTask> pickerTasks, String department) throws IOException {
    List<double[]> rows = new ArrayList<>();

    if (!pickerTasks.isEmpty()) {
      for (PickerTask pickerTask : pickerTasks) {
        rows.add(new double[]{
                pickerTask.getDistance(),
                pickerTask.getPackAmount(),
                pickerTask.getLinesAmount(),
                pickerTask.getWeight(),
                pickerTask.getVolume(),
                pickerTask.getAvgHeight(),
                (double) pickerTask.getWorker().getId(),
                pickerTask.getTime()
        });
      }

      try (FileReader reader = new FileReader(
              "Backend/src/main/java/gruppe01/ntnu/no/Warehouse/Workflow/Assigner/machinelearning/datasets/synthetic_pickroutes_" +
                      department.toUpperCase() + "_time.csv");
        CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
          for (CSVRecord record : parser) {
            rows.add(new double[]{
                    Double.parseDouble(record.get("distance_m")),
                    Double.parseDouble(record.get("dpack_equivalent_amount")),
                    Double.parseDouble(record.get("lines")),
                    Double.parseDouble(record.get("weight_g")),
                    Double.parseDouble(record.get("volume_ml")),
                    Double.parseDouble(record.get("avg_height")),
                    Double.parseDouble(record.get("picker")),
                    Double.parseDouble(record.get("time_s"))
            });
          }
        }
      }

    if (!rows.isEmpty()) {
      double[][] data = rows.toArray(new double[0][]);

      String[] columnNames = {
              "distance_m", "dpack_equivalent_amount", "lines",
              "weight_g", "volume_ml", "avg_height", "picker", "time_s"
      };

      DataFrame dataFrame = DataFrame.of(data, columnNames);

      Formula formula = Formula.of("time_s", new String[] {
              "distance_m", "dpack_equivalent_amount", "lines", "weight_g",
              "volume_ml", "avg_height", "picker"
      });
      RandomForest model = RandomForest.fit(formula, dataFrame);

      saveModel(model, "pickroute_" + department.toUpperCase() + ".ser");
    }
  }

  public void saveMetrics(String department, double mae) throws IOException {
    try (FileWriter writer = new FileWriter(METRICS_FILE, true)) {
      writer.write(department + "," + System.currentTimeMillis() + "," + mae + "\n");
    }
  }

  public List<String[]> loadMetrics() throws IOException {
    List<String[]> metrics = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(METRICS_FILE))) {
      String line;
      while ((line = reader.readLine()) != null) {
        metrics.add(line.split(","));
      }
    }
    return metrics;
  }
}
