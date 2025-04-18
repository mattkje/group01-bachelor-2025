package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;


import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for debugging
 * Contains godly methods for debugging
 * REMOVE BEFORE PRODUCTION
 * TODO: REMOVE THIS SOMETIME
 */
@Service
public class DebugService {

  @Autowired
  private WorkerRepository workerRepository;

  @Autowired
  private LicenseService licenseService;

  public void removeAllWorkerLicenses(){
    for (Worker worker : workerRepository.findAll()) {
      worker.setLicenses(new HashSet<>());
      workerRepository.save(worker);
    }
  }

  public void addAllWorkerLicenses(){
    for (Worker worker : workerRepository.findAll()) {
      List<License> licenses = licenseService.getAllLicenses();
      worker.setLicenses(new HashSet<>(licenses));
      workerRepository.save(worker);
    }
  }
}
