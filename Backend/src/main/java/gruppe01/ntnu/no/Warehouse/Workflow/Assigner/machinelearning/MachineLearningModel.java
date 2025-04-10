package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.io.Read;
import smile.regression.RandomForest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class MachineLearningModel {

    public void startModel() throws IOException, URISyntaxException {
        // Read the CSV file with headers
        DataFrame data = Read.csv("csv_output/task_2.csv");

        // Debug: print column names to ensure headers are correctly read
        System.out.println("Detected columns: " + Arrays.toString(data.names()));

        // Define target and predictors based on your CSV header
        String target = "V1";
        String[] predictors = {"V2", "V3", "V4"};

        // Validate predictor columns exist
        for (String col : predictors) {
            if (!Arrays.asList(data.names()).contains(col)) {
                throw new IllegalArgumentException("Column '" + col + "' not found in CSV. Detected columns: " + Arrays.toString(data.names()));
            }
        }

        // Set up the formula for training
        Formula formula = Formula.lhs(target);

        // Train the RandomForest model
        RandomForest model = RandomForest.fit(formula, data);

        // Predict on the training data (for testing/debug)
        double[] predictions = model.predict(data);

        Set<Double> distinctPredictions = new HashSet<>();
        for (double prediction : predictions) {
            distinctPredictions.add(prediction);
        }
        for (double distinctPrediction : distinctPredictions) {
            System.out.printf("%.2f, ", distinctPrediction);
        }

        // OPTIONAL: Predict on new input values
//        System.out.println("\nPredicting TimeSpent for new input rows:");
//        double[][] newInput = {
//                {2, 40, 100},  // Workers=2, MinTime=40, MaxTime=100
//                {3, 35, 95}
//        };
//
//        for (double[] inputRow : newInput) {
//            double predicted = model.predict(inputRow);
//            System.out.printf("Input %s => Predicted TimeSpent: %.2f\n", Arrays.toString(inputRow), predicted);
//        }
    }
}
