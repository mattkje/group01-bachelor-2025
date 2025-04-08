package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import smile.regression.RandomForest;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class ModelSaver {
    public static void saveModel(RandomForest model, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(model);
            System.out.println("Model saved to " + filePath);
        }
    }
}