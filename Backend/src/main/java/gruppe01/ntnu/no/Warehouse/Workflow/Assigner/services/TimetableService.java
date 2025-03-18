package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TimetableRepository;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimetableService {

    @Autowired
    private TimetableRepository timetableRepository;

    public List<Timetable> getAllTimetables() {
        return timetableRepository.findAll();
    }

    public Timetable addTimetable(Timetable timetable) {
        return timetableRepository.save(timetable);
    }


    public Timetable getTodaysTimetable(Long workerId) {
        for (Timetable timetable : timetableRepository.findAll()) {
            if (timetable.getWorker().getId().equals(workerId) &&
                    timetable.getStartTime().toLocalDate().equals(LocalDate.now())) {
                return timetable;
            }
        }
        return null;
    }


}
