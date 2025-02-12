package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZoneService {

    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private WorkerRepository workerRepository;

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public Zone getZoneById(Long id) {
        return zoneRepository.findById(id).orElse(null);
    }

    public List<Worker> getWorkersByZoneId(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            return zone.getWorkers();
        }
        return null;
    }

    public List<Task> getTasksByZoneId(Long id) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            return zone.getTasks();
        }
        return null;
    }

    public Zone addZone(Zone zone) {
        return zoneRepository.save(zone);
    }

    public Zone updateZone(Long id, Zone zone) {
        Zone updatedZone = zoneRepository.findById(id).get();
        updatedZone.setName(zone.getName());
        updatedZone.setWorkers(zone.getWorkers());
        updatedZone.setTasks(zone.getTasks());
        return zoneRepository.save(updatedZone);
    }

    public Zone addWorkerToZone(Long id, int workerId) {
        Zone zone = zoneRepository.findById(id).orElse(null);
        if (zone != null) {
            Worker worker = workerRepository.findById((long) workerId).orElse(null);
            zone.getWorkers().add(worker);
            return zoneRepository.save(zone);
        }
        return null;
    }
}
