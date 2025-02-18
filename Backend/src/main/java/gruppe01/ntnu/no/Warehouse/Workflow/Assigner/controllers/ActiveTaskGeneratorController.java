package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.dummydata.ActiveTaskGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/active-task-generator")
public class ActiveTaskGeneratorController {

    @Autowired
    private ActiveTaskGenerator activeTaskGeneratorService;

    @PostMapping("/generate")
    public void generateActiveTasks() throws Exception {
        activeTaskGeneratorService.generateActiveTasks();
    }
}
