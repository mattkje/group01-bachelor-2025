package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.PickerTaskRepository;
import java.time.LocalDate;
import java.util.Set;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PickerTaskService {

    @Autowired
    private PickerTaskRepository pickerTaskRepository;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private ZoneRepository zoneRepository;

    public List<PickerTask> getAllPickerTasks() {
        return pickerTaskRepository.findAll();
    }

    public PickerTask getPickerTaskById(Long id) {
        return pickerTaskRepository.findById(id).orElse(null);
    }

    public List<PickerTask> getPickerTaskByZoneId(Long id) {
        List<PickerTask> pickerTasks = new ArrayList<>();
        for (PickerTask pickerTask : pickerTaskRepository.findAll()) {
            if (pickerTask.getZone().getId().equals(id)) {
                pickerTasks.add(pickerTask);
            }
        }
        return pickerTasks;
    }

    public Set<PickerTask> getPickerTasksForToday() {
        List<PickerTask> pickerTasks = new ArrayList<>();
        for (PickerTask pickerTask : pickerTaskRepository.findAll()) {
            if (pickerTask.getDate().isEqual(LocalDate.now())) {
                pickerTasks.add(pickerTask);
            }
        }
        return Set.copyOf(pickerTasks);
    }

    public PickerTask savePickerTask(PickerTask pickerTask) {
        if (pickerTask != null) {
            return pickerTaskRepository.save(pickerTask);
        }
        return null;
    }

    public PickerTask assignWorkerToPickerTask(Long pickerTaskId, Long workerId) {
        PickerTask pickerTask = getPickerTaskById(pickerTaskId);
        Worker worker = workerRepository.findById(workerId).orElse(null);
        if (pickerTask != null && worker != null) {
            pickerTask.setWorker(worker);
            worker.setCurrentPickerTask(pickerTask);
            workerRepository.save(worker);
            return savePickerTask(pickerTask);
        }
        return null;
    }

    public PickerTask removeWorkerFromPickerTask(Long pickerTaskId, Long workerId) {
        PickerTask pickerTask = getPickerTaskById(pickerTaskId);
        Worker worker = workerRepository.findById(workerId).orElse(null);
        if (pickerTask.getWorker().getId().equals(workerId) && worker != null) {
            worker.setCurrentPickerTask(null);
            pickerTask.setWorker(null);
            workerRepository.save(worker);
            return savePickerTask(pickerTask);
        }
        return null;
    }

    public PickerTask updatePickerTask(Long pickerTaskId, Long zoneId, PickerTask pickerTask) {
        return pickerTaskRepository.findById(pickerTaskId).map(existingTask -> {
            // Update fields
            existingTask.setDistance(pickerTask.getDistance());
            existingTask.setPackAmount(pickerTask.getPackAmount());
            existingTask.setLinesAmount(pickerTask.getLinesAmount());
            existingTask.setWeight(pickerTask.getWeight());
            existingTask.setVolume(pickerTask.getVolume());
            existingTask.setAvgHeight(pickerTask.getAvgHeight());
            existingTask.setTime(pickerTask.getTime());
            existingTask.setStartTime(pickerTask.getStartTime());
            existingTask.setEndTime(pickerTask.getEndTime());
            existingTask.setDate(pickerTask.getDate());
            existingTask.setWorker(pickerTask.getWorker());

            // Handle zone update
            if (!existingTask.getZone().getId().equals(zoneId)) {
                Zone zone = zoneRepository.findById(zoneId)
                        .orElseThrow(() -> new IllegalArgumentException("Zone not found with id: " + pickerTask.getZoneId()));
                if (zone.getIsPickerZone()) {
                    existingTask.setZone(zone);
                }
            }

            // Save and return the updated task
            return pickerTaskRepository.save(existingTask);
        }).orElseThrow(() -> new IllegalArgumentException("PickerTask not found with id: " + pickerTaskId));
    }

    public void deletePickerTask(Long id) {
        PickerTask pickerTask = pickerTaskRepository.findById(id).orElse(null);
        if (pickerTask != null) {
            if (pickerTask.getWorker() != null) {
                pickerTask.getWorker().setCurrentPickerTask(null);
                workerRepository.save(pickerTask.getWorker());
            }
            pickerTaskRepository.delete(pickerTask);
        }
    }
}
