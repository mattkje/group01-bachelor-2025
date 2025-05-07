package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.*;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TimetableRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class for managing Timetable entities.
 */
@Service
public class TimetableService {

    private final TimetableRepository timetableRepository;

    private final WorkerRepository workerRepository;

    /**
     * Constructor for TimetableService.
     *
     * @param timetableRepository the repository for Timetable
     * @param workerRepository    the repository for Worker
     */
    public TimetableService(TimetableRepository timetableRepository, WorkerRepository workerRepository) {
        this.timetableRepository = timetableRepository;
        this.workerRepository = workerRepository;
    }

    /**
     * Retrieves all Timetable entities.
     *
     * @return a list of all Timetable entities
     */
    public List<Timetable> getAllTimetables() {
        return timetableRepository.findAll();
    }

    /**
     * Adds a new Timetable entity.
     *
     * @param timetable the Timetable entity to add
     * @param workerId  the ID of the Worker associated with the Timetable
     * @return the added Timetable entity
     */
    public Timetable addTimetable(Timetable timetable, long workerId) {
        Worker worker = workerRepository.findById(workerId).orElse(null);
        if (worker != null) {
            timetable.setWorker(worker);
            return timetableRepository.save(timetable);
        }
        return null;
    }

    /**
     * Retrieves today's Timetable entities.
     *
     * @return a list of Timetable entities for today
     */
    public List<Timetable> getTodaysTimetables() {
        List<Timetable> todaysTimetables = new ArrayList<>();
        for (Timetable timetable : timetableRepository.findAll()) {
            if (timetable.getStartTime().toLocalDate().equals(LocalDate.now())) {
                todaysTimetables.add(timetable);
            }
        }
        return todaysTimetables;
    }

    /**
     * Retrieves today's Timetable entities for a specific zone.
     *
     * @param zoneId the ID of the zone
     * @return a list of Timetable entities for today in the specified zone
     */
    public List<Timetable> getTodayTimetablesByZone(Long zoneId) {
        List<Timetable> todayTimetables = new ArrayList<>();
        for (Timetable timetable : timetableRepository.findAll()) {
            if (timetable.getWorker().getZone().equals(zoneId) &&
                    timetable.getStartTime().toLocalDate().equals(LocalDate.now())) {
                todayTimetables.add(timetable);
            }
        }
        return todayTimetables;
    }

    /**
     * Retrieves all Timetable entities for a specific zone.
     *
     * @param zoneId the ID of the zone
     * @return a list of Timetable entities for the specified zone
     */
    public List<Timetable> getAllTimetablesByZone(Long zoneId) {
        List<Timetable> allTimetablesByZone = new ArrayList<>();
        for (Timetable timetable : timetableRepository.findAll()) {
            if (timetable.getWorker().getZone().equals(zoneId)) {
                allTimetablesByZone.add(timetable);
            }
        }
        return allTimetablesByZone;
    }

    /**
     * Retrieves all Timetable entities for a specific date.
     *
     * @param date the date to filter by
     * @return a list of Timetable entities for the specified date
     */
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

    /**
     * Retrieves all Timetable entities for the current month.
     *
     * @param date the date to filter by
     * @return a list of Timetable entities for the current month
     */
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

    /**
     * Sets the start time of a Timetable entity to the current time.
     *
     * @param id the ID of the Timetable entity
     * @return the updated Timetable entity
     */
    public Timetable setStartTime(Long id) {
        Timetable timetable = timetableRepository.findById(id).orElse(null);
        if (timetable != null) {
            timetable.setStartTime(LocalDateTime.now());
            return timetableRepository.save(timetable);
        }
        return null;
    }

    /**
     * Sets the end time of a Timetable entity to the current time.
     *
     * @param id        the ID of the Timetable entity
     * @param timetable the Timetable entity to update
     * @return the updated Timetable entity
     */
    public Timetable updateTimetable(Long id, Timetable timetable) {
        Timetable existingTimetable = timetableRepository.findById(id).orElse(null);
        if (existingTimetable != null) {
            existingTimetable.setStartTime(timetable.getStartTime());
            existingTimetable.setEndTime(timetable.getEndTime());
            return timetableRepository.save(existingTimetable);
        }
        return null;
    }

    /**
     * Deletes all Timetable entities.
     */
    public void deleteAllTimetables() {
        timetableRepository.deleteAll();
    }

    /**
     * Deletes all Timetable entities for today.
     */
    public void deleteAllTimetablesForToday() {
        for (Timetable timetable : timetableRepository.findAll()) {
            if (timetable.getStartTime().toLocalDate().equals(LocalDate.now())) {
                timetableRepository.delete(timetable);
            }
        }
    }

    /**
     * Retrieves all Timetable entities for a specific day and zone.
     *
     * @param day    the date to filter by
     * @param zoneId the ID of the zone
     * @return a list of Timetable entities for the specified day and zone
     */
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
     * Retrieves all Timetable entities for a specific week and zone.
     *
     * @param date   the date to filter by
     * @param zoneId the ID of the zone
     * @return a list of Timetable entities for the specified week and zone
     */
    public List<Timetable> getTimetablesForOneWeek(LocalDate date, Long zoneId) {
        List<Timetable> timetables = timetableRepository.findAll();
        List<Timetable> timetablesForOneWeek = new ArrayList<>();
        for (Timetable timetable : timetables) {
            if (((timetable.getStartTime().toLocalDate().isAfter(date) &&
                    timetable.getStartTime().toLocalDate().isBefore(date.plusDays(7))) ||
                    (timetable.getStartTime().toLocalDate().equals(date))) &&
                    timetable.getWorker().getZone().equals(zoneId)) {
                timetablesForOneWeek.add(timetable);
            }
        }
        return timetablesForOneWeek;
    }

    /**
     * Gets all workers working on a specific day and zone.
     *
     * @param day    Converted to LocalDate inside the method
     * @param zoneId The zone id to get workers from
     * @return A set of workers working on the specified day and zone
     */
    public Set<Worker> getWorkersWorkingByDayAndZone(LocalDateTime day, Long zoneId) {
        List<Timetable> timetables = timetableRepository.findByStartDate(day.toLocalDate());
        Set<Worker> workers = new HashSet<>();
        for (Timetable timetable : timetables) {
            if (timetable.getWorker().getZone().equals(zoneId) &&
                    timetable.getWorker().isAvailability()) {
                workers.add(timetable.getWorker());
            }
        }
        return workers;
    }

    /**
     * Checks if a worker is working at a specific time.
     *
     * @param time     the time to check
     * @param workerId the ID of the worker
     * @return true if the worker is working, false otherwise
     */
    public boolean workerIsWorking(LocalDateTime time, Long workerId) {
        List<Timetable> timetables = timetableRepository.findByWorkerAndDateTime(workerId, time);
        return timetables.stream()
                .anyMatch(timetable -> timetable.getWorker().isAvailability());
    }

    /**
     * Checks if a worker has finished their shift at a specific time.
     *
     * @param workerId the ID of the worker
     * @param time     the time to check
     * @return true if the worker has finished their shift, false otherwise
     */
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

    /**
     * Retrieves the first start time for a specific zone and day.
     *
     * @param zoneId the ID of the zone
     * @param day    the date to filter by
     * @return the first start time for the specified zone and day
     */
    public LocalDateTime getFirstStartTimeByZoneAndDay(Long zoneId, LocalDateTime day) {
        List<Timetable> timetables = timetableRepository.findByStartDate(day.toLocalDate());
        return timetables.stream()
                .filter(timetable -> timetable.getWorker().getZone().equals(zoneId) && timetable.getWorker().isAvailability())
                .map(Timetable::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(day.toLocalDate().atStartOfDay());
    }

    /**
     * Checks if everyone has finished working in a specific zone at a specific time.
     *
     * @param zoneId the ID of the zone
     * @param time   the time to check
     * @return true if everyone has finished working, false otherwise
     */
    public boolean isEveryoneFinishedWorking(Long zoneId, LocalDateTime time) {
        List<Timetable> timetables = timetableRepository.findByStartDate(time.toLocalDate());
        for (Timetable timetable : timetables) {
            if (timetable.getWorker().getZone().equals(zoneId) && timetable.getEndTime().isAfter(time) && timetable.getWorker().isAvailability()) {
                return false;
            }
        }
        return true;
    }

    public void deleteTimetable(Long id) {
        timetableRepository.deleteById(id);
    }


   public boolean isAnyoneQualifiedWorkingToday(Long zoneId, LocalDateTime localDateTime, ActiveTask activeTask) {
       int qualificationCount = 0;
       List<Timetable> timetables = timetableRepository.findByStartDate(localDateTime.toLocalDate());
       if (activeTask != null) {
           for (Timetable timetable : timetables) {
               if (timetable.getWorker().getZone().equals(zoneId) && timetable.getWorker().isAvailability()) {
                   if (timetable.getWorker().getLicenses().containsAll(activeTask.getTask().getRequiredLicense())) {
                       qualificationCount++;
                   }
               }
           }
           return qualificationCount >= activeTask.getTask().getMinWorkers();
       }
       return false;
   }
}
