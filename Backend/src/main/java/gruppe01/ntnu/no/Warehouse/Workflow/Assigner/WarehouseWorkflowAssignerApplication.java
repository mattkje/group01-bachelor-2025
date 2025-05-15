package gruppe01.ntnu.no.warehouse.workflow.assigner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Warehouse Workflow Assigner.
 * This class serves as the entry point for the Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling
public class WarehouseWorkflowAssignerApplication {

  public static void main(String[] args) {
    SpringApplication.run(WarehouseWorkflowAssignerApplication.class, args);
  }

}
