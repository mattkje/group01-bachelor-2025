package gruppe01.ntnu.no.Warehouse.Workflow.Assigner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WarehouseWorkflowAssignerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseWorkflowAssignerApplication.class, args);
	}

}
