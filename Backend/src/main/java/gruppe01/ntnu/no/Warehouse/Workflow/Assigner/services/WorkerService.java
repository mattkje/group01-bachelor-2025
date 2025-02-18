package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.LicenseRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WorkerService {

    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private LicenseRepository licenseRepository;

    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

    public List<Worker> getAvailableWorkers() {
        List<Worker> availableWorkers = new ArrayList<>();
        for (Worker worker : workerRepository.findAll()) {
            if (worker.isAvailability()) {
                availableWorkers.add(worker);
            }
        }
        return availableWorkers;
    }

    public List<Worker> getUnavailableWorkers() {
        List<Worker> unavailableWorkers = new ArrayList<>();
        for (Worker worker : workerRepository.findAll()) {
            if (!worker.isAvailability()) {
                unavailableWorkers.add(worker);
            }
        }
        return unavailableWorkers;
    }

    public Optional<Worker> getWorkerById(Long id) {
        return workerRepository.findById(id);
    }

    public Worker addWorker(Worker worker) {
        return workerRepository.save(worker);
    }

    public Worker updateWorker (Long id, Worker worker) {
        Worker updatedWorker = workerRepository.findById(id).get();
        updatedWorker.setAvailability(worker.isAvailability());
        updatedWorker.setName(worker.getName());
        updatedWorker.setEffectiveness(worker.getEffectiveness());
        updatedWorker.setLicenses(worker.getLicenses());
        updatedWorker.setZone(worker.getZone());
        updatedWorker.setWorkerType(worker.getWorkerType());
        return workerRepository.save(updatedWorker);
    }

    public Worker updateWorkerAvailability(Long id) {
        Worker updatedWorker = workerRepository.findById(id).get();
        updatedWorker.setAvailability(!updatedWorker.isAvailability());
        return workerRepository.save(updatedWorker);
    }

    public Worker addLicenseToWorker(Long id, Long licenseId) {
        Worker updatedWorker = workerRepository.findById(id).get();
        License license = licenseRepository.findById(licenseId).get();
        updatedWorker.getLicenses().add(license);
        return workerRepository.save(updatedWorker);
    }

    public void deleteWorker(Long id) {
        workerRepository.deleteById(id);
    }

    public Worker addWorkerToZone(Long workerId, Long zoneId) {
        Worker updatedWorker = workerRepository.findById(workerId).get();
        updatedWorker.setZone(zoneId);
        return workerRepository.save(updatedWorker);
    }
}
