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

@Component
public class MachineLearningModel {

    public void startModel() throws IOException {
        String filePath = "random_forest_model.ser";

        // Attempt to load an existing model
        RandomForest model = loadModel(filePath);

        if (model == null) {
            // If no model exists, train a new one
            String csvFilePath = "csv_output/task_1.csv";

            // Read the CSV with Apache Commons CSV
            FileReader reader = new FileReader(csvFilePath);
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            List<Integer> timeSpentList = new ArrayList<>();
            List<Integer> amountWorkersList = new ArrayList<>();

            for (CSVRecord record : parser) {
                timeSpentList.add(Integer.parseInt(record.get("TimeSpent")));
                amountWorkersList.add(Integer.parseInt(record.get("AmountWorkers")));
            }

            // Convert to arrays
            int[] timeSpentArray = timeSpentList.stream().mapToInt(i -> i).toArray();
            int[] amountWorkersArray = amountWorkersList.stream().mapToInt(i -> i).toArray();

            // Build DataFrame manually for Smile
            DataFrame data = DataFrame.of(
                    IntVector.of("TimeSpent", timeSpentArray),
                    IntVector.of("AmountWorkers", amountWorkersArray)
            );

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
            model = RandomForest.fit(formula, data);

            // Make predictions
            double[] predictions = model.predict(data);

            // Sort predictions and compute bounds
            Arrays.sort(predictions);
            double minBound = predictions[(int) (0.05 * predictions.length)];
            double maxBound = predictions[(int) (0.95 * predictions.length)];

            System.out.printf("RandomForest Predicted Boundaries: Min = %.2f, Max = %.2f\n", minBound, maxBound);

            // Save the trained model
            saveModel(model, filePath);
        }
    }

    public RandomForest loadModel(String filePath) {
        try {
            return ModelLoader.loadModel(filePath);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing model found. A new model will be trained.");
            return null;
        }
    }

    public void saveModel(RandomForest model, String filePath) {
        try {
            ModelSaver.saveModel(model, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
