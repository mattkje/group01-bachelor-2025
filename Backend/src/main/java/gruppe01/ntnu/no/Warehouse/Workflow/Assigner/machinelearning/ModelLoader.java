package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import smile.regression.RandomForest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ModelLoader {
    public static RandomForest loadModel(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            RandomForest model = (RandomForest) ois.readObject();
            System.out.println("Model loaded from " + filePath);
            return model;
        }
    }
}