package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.vector.BaseVector;
import smile.data.vector.IntVector;
import smile.regression.RandomForest;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class handles the machine learning model for predicting time spent on tasks.
 * It uses a Random Forest model from the Smile library.
 */
@Component
public class MachineLearningModel {

    /**
     * Starts the machine learning model process.
     * It loads an existing model if available, otherwise it trains a new one.
     *
     * @throws IOException If there is an error reading the CSV or saving/loading the model.
     */
    public void startModel() throws IOException {
        String filePath = "random_forest_model.ser";

        // Attempt to load an existing model
        RandomForest model = loadModel(filePath);

        if (model == null) {
            // If no model exists, train a new one
            String csvFilePath = "csv_output/task_1.csv";

            // Parse CSV and create DataFrame
            DataFrame data = parseCsvToDataFrame(csvFilePath);

            // Train the model
            model = trainModel(data);

            // Save the trained model
            saveModel(model, filePath);
        }
    }

    /**
     * Parses a CSV file to create a DataFrame.
     * The CSV file should contain two columns: "TimeSpent" and "AmountWorkers".
     *
     * @param csvFilePath The path to the CSV file.
     * @return A DataFrame containing the parsed data.
     * @throws IOException If there is an error reading the CSV file.
     */
    private DataFrame parseCsvToDataFrame(String csvFilePath) throws IOException {
        FileReader reader = new FileReader(csvFilePath);
        CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

        List<Integer> timeSpentList = new ArrayList<>();
        List<Integer> amountWorkersList = new ArrayList<>();

        for (CSVRecord record : parser) {
            timeSpentList.add(Integer.parseInt(record.get("TimeSpent")));
            amountWorkersList.add(Integer.parseInt(record.get("AmountWorkers")));
        }

        // Convert to arrays and build DataFrame
        int[] timeSpentArray = timeSpentList.stream().mapToInt(i -> i).toArray();
        int[] amountWorkersArray = amountWorkersList.stream().mapToInt(i -> i).toArray();

        return DataFrame.of(
                IntVector.of("TimeSpent", timeSpentArray),
                IntVector.of("AmountWorkers", amountWorkersArray)
        );
    }

    /**
     * Trains a RandomForest model using the provided DataFrame.
     *
     * @param data The DataFrame containing the training data.
     * @return The trained RandomForest model.
     */
    private RandomForest trainModel(DataFrame data) {
        System.out.println("Detected columns: " + Arrays.toString(data.names()));
        System.out.println("DataFrame: " + data.toString());

        String target = "TimeSpent";
        if (!Arrays.asList(data.names()).contains(target)) {
            throw new IllegalArgumentException(
                    "Column '" + target + "' not found in CSV. Detected columns: " +
                            Arrays.toString(data.names()));
        }

        Formula formula = Formula.lhs(target);

        // Train the RandomForest model
        RandomForest model = RandomForest.fit(formula, data);

        // Calculate prediction boundaries
        calculatePredictionBoundaries(model, data);

        return model;
    }

    /**
     * Calculates the prediction boundaries for the RandomForest model.
     * This is done by sorting the predictions and finding the 5th and 95th percentiles.
     *
     * @param model The trained RandomForest model.
     * @param data  The DataFrame used for training.
     */
    private void calculatePredictionBoundaries(RandomForest model, DataFrame data) {
        double[] predictions = model.predict(data);

        // Sort predictions and compute bounds
        Arrays.sort(predictions);
        double minBound = predictions[(int) (0.05 * predictions.length)];
        double maxBound = predictions[(int) (0.95 * predictions.length)];

        System.out.printf("RandomForest Predicted Boundaries: Min = %.2f, Max = %.2f\n", minBound, maxBound);
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
        try {
            ModelSaver.saveModel(model, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
