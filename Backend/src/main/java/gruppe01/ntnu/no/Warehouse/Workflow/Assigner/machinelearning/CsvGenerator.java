package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.machinelearning;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.ActiveTask;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Task;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CsvGenerator {

    @Autowired
    private ActiveTaskService activeTaskService;

    public void createCsvForML() {
        List<ActiveTask> activeTasks = activeTaskService.getAllActiveTasks();
        activeTasks = activeTasks.stream().filter(activeTask -> (activeTask.getEndTime() != null &&
                activeTask.getStartTime() != null)).toList();

        // Group active tasks by their task
        Map<Task, List<ActiveTask>> tasksGroupedByTask = activeTasks.stream()
                .collect(Collectors.groupingBy(ActiveTask::getTask));

        // Directory to save the CSV files
        String directoryPath = "csv_output";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Create a CSV file for each unique task
        tasksGroupedByTask.forEach((task, taskList) -> {
            String fileName = directoryPath + File.separator + "task_" + task.getId() + ".csv";
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.append("TimeSpent,AmountWorkers,MinTime,MaxTime\n");
                for (ActiveTask activeTask : taskList) {
                    int workerCount = activeTask.getWorkers().size() / (4 * activeTask.getTask().getMinWorkers());
                    writer.append(String.valueOf(calculateTimeSpent(activeTask))).append(",")
                            .append(String.valueOf(workerCount)).append(",")
                            .append(String.valueOf(activeTask.getTask().getMinTime())).append(",")
                            .append(String.valueOf(activeTask.getTask().getMaxTime())).append("\n");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static long calculateTimeSpent(ActiveTask activeTask) {
        LocalDateTime startTime = activeTask.getStartTime();
        LocalDateTime endTime = activeTask.getEndTime();
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime).toMinutes();
        } else {
            throw new IllegalArgumentException("Start time or end time is null");
        }
    }
}