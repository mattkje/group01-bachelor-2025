package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TimetableRepository;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public List<Timetable> getTimetablesByDate(LocalDate date) {
        List<Timetable> timetables = timetableRepository.findAll();
        List<Timetable> timetablesByDate = new ArrayList<>();
        for (Timetable timetable : timetables) {
            if (timetable.getStartTime().toLocalDate().equals(date)) {
                timetablesByDate.add(timetable);
            }
        }
        return timetablesByDate;
    }

    public void deleteAllTimetables() {
        timetableRepository.deleteAll();
    }

    public void deleteAllTimetablesForToday() {
        for (Timetable timetable : timetableRepository.findAll()) {
            if (timetable.getStartTime().toLocalDate().equals(LocalDate.now())) {
                timetableRepository.delete(timetable);
            }
        }
    }

}
