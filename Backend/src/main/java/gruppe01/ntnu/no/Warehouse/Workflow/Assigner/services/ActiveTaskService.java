package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ActiveTaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TaskRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActiveTaskService {

    @Autowired
    private ActiveTaskRepository activeTaskRepository;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private TaskRepository taskRepository;

    public List<ActiveTask> getAllActiveTasks() {
        return activeTaskRepository.findAll();
    }

    public ActiveTask getActiveTaskById(Long id) {
        return activeTaskRepository.findById(id).orElse(null);
    }

    public List<ActiveTask> getActiveTasksForToday() {
        LocalDate currentDate = LocalDate.now();
        List<ActiveTask> activeTasks = new ArrayList<>();
        for (ActiveTask activeTask : activeTaskRepository.findAll()) {
            if (activeTask.getDate().equals(currentDate)) {
                activeTasks.add(activeTask);
            }
        }
        return activeTasks;
    }

    public ActiveTask createActiveTask(Long taskId, ActiveTask activeTask) {
        if (activeTask != null) {
            if (activeTask.getTask() == null){
                Task task = taskRepository.findById(taskId).get();
                activeTask.setTask(task);
            }
            return activeTaskRepository.save(activeTask);
        }
        return null;
    }

    public ActiveTask updateActiveTask(Long id, ActiveTask activeTask) {
        ActiveTask updatedActiveTask = activeTaskRepository.findById(id).get();
        updatedActiveTask.setTask(activeTask.getTask());
        updatedActiveTask.setWorkers(activeTask.getWorkers());
        updatedActiveTask.setStartTime(activeTask.getStartTime());
        updatedActiveTask.setEndTime(activeTask.getEndTime());
        updatedActiveTask.setDueDate(activeTask.getDueDate());
        updatedActiveTask.setStrictStart(activeTask.getStrictStart());
        return activeTaskRepository.save(updatedActiveTask);
    }

    public ActiveTask assignWorkerToTask(Long id, Long workerId) {
        ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
        if (activeTask != null) {
            Worker worker = workerRepository.findById(workerId).orElse(null);
            if (worker != null) {
                worker.setAvailability(false);
                activeTask.getWorkers().add(worker);
                return activeTaskRepository.save(activeTask);
            }
            return null;
        }
        return null;
    }

    public ActiveTask removeWorkerFromTask(Long id, Long workerId) {
        ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
        if (activeTask != null) {
            Worker worker = workerRepository.findById(workerId).orElse(null);
            if (worker != null) {
                worker.setAvailability(true);
                activeTask.getWorkers().remove(worker);
                return activeTaskRepository.save(activeTask);
            }
            return null;
        }
        return null;
    }

    public ActiveTask removeWorkersFromTask(Long id) {
        ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
        if (activeTask != null) {
            for (Worker worker : activeTask.getWorkers()) {
                worker.setAvailability(true);
            }
            activeTask.getWorkers().clear();
            return activeTaskRepository.save(activeTask);
        }
        return null;
    }

    public ActiveTask addTaskToActiveTask(Long id, Long taskId) {
        ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
        if (activeTask != null) {
            activeTask.setTask(taskRepository.findById(taskId).orElse(null));
            return activeTaskRepository.save(activeTask);
        }
        return null;
    }

    public ActiveTask removeTaskFromActiveTask(Long id) {
        ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
        if (activeTask != null) {
            activeTask.setTask(null);
            return activeTaskRepository.save(activeTask);
        }
        return null;
    }

    public ActiveTask deleteActiveTask(Long id) {
        ActiveTask activeTask = activeTaskRepository.findById(id).orElse(null);
        if (activeTask != null) {
            activeTaskRepository.delete(activeTask);
        }
        return activeTask;
    }
}
