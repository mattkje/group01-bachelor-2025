package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import org.springframework.stereotype.Component;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.io.Read;
import smile.regression.RandomForest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

@Component
public class MachineLearningModel {

    public void startModel() throws IOException, URISyntaxException {
        // Ensure the CSV file is read with headers
        DataFrame data = Read.csv("csv_output/task_1.csv");

        // Print detected column names to debug issues
        System.out.println("Detected columns: " + Arrays.toString(data.names()));

        // Ensure the column name is correctly recognized
        String target = "TimeSpent";
        if (!Arrays.asList(data.names()).contains(target)) {
            throw new IllegalArgumentException("Column '" + target + "' not found in CSV. Detected columns: " + Arrays.toString(data.names()));
        }

        Formula formula = Formula.lhs(target);

        // Train the RandomForest model
        RandomForest model = RandomForest.fit(formula, data);

        // Make predictions
        double[] predictions = model.predict(data);

        // Sort predictions and compute bounds
        Arrays.sort(predictions);
        double minBound = predictions[(int) (0.05 * predictions.length)];
        double maxBound = predictions[(int) (0.95 * predictions.length)];

        System.out.printf("RandomForest Predicted Boundaries: Min = %.2f, Max = %.2f\n", minBound, maxBound);
    }
}