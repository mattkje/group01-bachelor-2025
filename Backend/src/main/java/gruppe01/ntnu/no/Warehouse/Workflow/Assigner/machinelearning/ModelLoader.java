package gruppe01.ntnu.no.warehouse.workflow.assigner.machinelearning;

import smile.regression.RandomForest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * This class is responsible for loading a pre-trained Random Forest model from a file.
 * It uses Java's serialization mechanism to read the model from a file.
 */
public class ModelLoader {
  /**
   * Loads a Random Forest model from the specified file path.
   *
   * @param filePath The path to the file containing the serialized model.
   * @return The loaded Random Forest model.
   * @throws IOException            If there is an error reading the file.
   * @throws ClassNotFoundException If the class of a serialized object cannot be found.
   */
  public static RandomForest loadModel(String filePath) throws IOException, ClassNotFoundException {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
      RandomForest model = (RandomForest) ois.readObject();
      System.out.println("Model loaded from " + filePath);
      return model;
    }
  }
}