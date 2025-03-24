package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.controllers;


import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.DebugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

  @Autowired
  private DebugService debugService;

  @GetMapping("/remove-licenses")
  public void removeAllWorkerLicenses() {
    debugService.removeAllWorkerLicenses();
  }

  @GetMapping("/add-licenses")
  public void addAllWorkerLicenses() {
    debugService.addAllWorkerLicenses();
  }

}
