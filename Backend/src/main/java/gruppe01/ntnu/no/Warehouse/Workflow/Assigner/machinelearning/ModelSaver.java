package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import smile.regression.RandomForest;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/*
 * This class is responsible for saving a trained Random Forest model to a file.
 * It uses Java's serialization mechanism to write the model to a file.
 */
public class ModelSaver {
    /**
     * Saves a Random Forest model to the specified file path.
     *
     * @param model    The Random Forest model to save.
     * @param filePath The path to the file where the model will be saved.
     * @throws IOException If there is an error writing to the file.
     */
    public static void saveModel(RandomForest model, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(model);
            System.out.println("Model saved to " + filePath);
        }
    }
}