package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.PickerTaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
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

    public PickerTask updatePickerTask(Long id, PickerTask pickerTask) {
        PickerTask updatedActiveTask = pickerTaskRepository.findById(id).get();
        if (updatedActiveTask.getId().equals(pickerTask.getId())) {
            updatedActiveTask = pickerTask;
        }
        return pickerTaskRepository.save(updatedActiveTask);
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
