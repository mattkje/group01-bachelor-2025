package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.PickerTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.PickerTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PickerTaskService {

    @Autowired
    private PickerTaskRepository pickerTaskRepository;

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
}
