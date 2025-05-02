package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TimetableRepository;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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


  public List<Timetable> getTodaysTimetables() {
    List<Timetable> todaysTimetables = new ArrayList<>();
    for (Timetable timetable : timetableRepository.findAll()) {
      if (timetable.getStartTime().toLocalDate().equals(LocalDate.now())) {
        todaysTimetables.add(timetable);
      }
    }
    return todaysTimetables;
  }

  public List<Timetable> getTodaysTimetablesByZone(Long zoneId) {
    List<Timetable> todaysTimetables = new ArrayList<>();
    for (Timetable timetable : timetableRepository.findAll()) {
      if (timetable.getWorker().getZone().equals(zoneId) &&
          timetable.getStartTime().toLocalDate().equals(LocalDate.now())) {
        todaysTimetables.add(timetable);
      }
    }
    return todaysTimetables;
  }

  public List<Timetable> getAllTimetablesByZone(Long zoneId) {
    List<Timetable> allTimetablesByZone = new ArrayList<>();
    for (Timetable timetable : timetableRepository.findAll()) {
      if (timetable.getWorker().getZone().equals(zoneId)) {
        allTimetablesByZone.add(timetable);
      }
    }
    return allTimetablesByZone;
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

  public List<Timetable> getAllTimetablesThisMonth(LocalDate date) {
    List<Timetable> timetables = timetableRepository.findAll();
    List<Timetable> timetablesThisMonth = new ArrayList<>();
    for (Timetable timetable : timetables) {
      if (timetable.getStartTime().getMonthValue() == date.getMonthValue() &&
          timetable.getStartTime().getYear() == date.getYear()) {
        timetablesThisMonth.add(timetable);
      }
    }
    return timetablesThisMonth;
  }

  public Timetable setStartTime(Long id) {
    Timetable timetable = timetableRepository.findById(id).orElse(null);
    if (timetable != null) {
      timetable.setStartTime(LocalDateTime.now());
      return timetableRepository.save(timetable);
    }
    return null;
  }

  public Timetable updateTimetable(Long id, Timetable timetable) {
    Timetable existingTimetable = timetableRepository.findById(id).orElse(null);
    if (existingTimetable != null) {
      existingTimetable.setStartTime(timetable.getStartTime());
      existingTimetable.setEndTime(timetable.getEndTime());
      existingTimetable.setWorker(timetable.getWorker());
      return timetableRepository.save(existingTimetable);
    }
    return null;
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

  public List<Timetable> getTimetablesByDayAndZone(LocalDateTime day, Long zoneId) {
    List<Timetable> timetables = timetableRepository.findAll();
    List<Timetable> timetablesByDayAndZone = new ArrayList<>();
    for (Timetable timetable : timetables) {
      if (timetable.getStartTime().toLocalDate().equals(day.toLocalDate()) &&
          timetable.getWorker().getZone().equals(zoneId)) {
        timetablesByDayAndZone.add(timetable);
      }
    }
    return timetablesByDayAndZone;
  }

  /**
   * Gets all workers working on a specific day and zone.
   * @param day Converted to LocalDate inside the method
   * @param zoneId The zone id to get workers from
   * @return A set of workers working on the specified day and zone
   */
  public Set<Worker> getWorkersWorkingByDayAndZone(LocalDateTime day, Long zoneId) {
    List<Timetable> timetables = timetableRepository.findByStartDate(day.toLocalDate());
    Set<Worker> workers = new HashSet<>();
    for (Timetable timetable : timetables) {
      if (timetable.getWorker().getZone().equals(zoneId)) {
        workers.add(timetable.getWorker());
      }
    }
    return workers;
  }

  public boolean workerIsWorking(LocalDateTime time, Long workerId) {
    List<Timetable> timetables = timetableRepository.findByWorkerAndDateTime(workerId, time);
    return !timetables.isEmpty();
  }

  public boolean workerHasFinishedShift(Long workerId, LocalDateTime time) {
    List<Timetable> timetables = timetableRepository.findByWorkerAndDateTime(workerId, time);
    if (timetables.isEmpty()) {
      return false;
    }
    for (Timetable timetable : timetables) {
      if (timetable.getEndTime() != null && timetable.getEndTime().isBefore(time)) {
        return true;
      }
    }
    return false;
  }

  public LocalDateTime getFirstStartTimeByZoneAndDay(Long zoneId, LocalDateTime day) {
      List<Timetable> timetables = timetableRepository.findByStartDate(day.toLocalDate());
      return timetables.stream()
          .filter(timetable -> timetable.getWorker().getZone().equals(zoneId))
          .map(Timetable::getStartTime)
          .min(LocalDateTime::compareTo)
          .orElse(null);
  }
}
