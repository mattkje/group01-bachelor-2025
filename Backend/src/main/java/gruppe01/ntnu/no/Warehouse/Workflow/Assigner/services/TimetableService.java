package gruppe01.ntnu.no.warehouse.workflow.assigner.services;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.*;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.TimetableRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.WorkerRepository;
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
   * Retrieves a Timetable entity by its ID.
   *
   * @param id the ID of the Timetable entity
   * @return a Timetable entity if found, null otherwise
   */
  public Timetable getTimetableById(Long id) {
    return timetableRepository.findById(id).orElse(null);
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
    return timetableRepository.findByRealStartDate(LocalDate.now());
  }

  /**
   * Retrieves today's Timetable entities for a specific zone.
   *
   * @param zoneId the ID of the zone
   * @return a list of Timetable entities for today in the specified zone
   */
  public List<Timetable> getTodayTimetablesByZone(Long zoneId) {
    return timetableRepository.findTodayTimetablesByZone(zoneId, LocalDate.now());
  }

  /**
   * Retrieves all Timetable entities for a specific zone.
   *
   * @param zoneId the ID of the zone
   * @return a list of Timetable entities for the specified zone
   */
  public List<Timetable> getAllTimetablesByZone(Long zoneId) {
    return timetableRepository.findAllByZoneId(zoneId);
  }

  /**
   * Retrieves all Timetable entities for a specific date.
   *
   * @param date the date to filter by
   * @return a list of Timetable entities for the specified date
   */
  public List<Timetable> getTimetablesByDate(LocalDate date) {
    return timetableRepository.findByStartDate(date);
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
   * Retrieves all Timetable entities for a specific day and zone.
   *
   * @param day    the date to filter by
   * @param zoneId the ID of the zone
   * @return a list of Timetable entities for the specified day and zone
   */
  public List<Timetable> getTimetablesByDayAndZone(LocalDateTime day, Long zoneId) {
    return timetableRepository.findByDayAndZone(day.toLocalDate(), zoneId);
  }

  public List<Timetable> getTimetabelsOfWorkersWorkingByZone(LocalDateTime day, Long zoneId) {
    return timetableRepository.findTimetablesOfWorkersWorkingByZone(day, zoneId);
  }

  /**
   * Retrieves all Timetable entities for a specific week and zone.
   *
   * @param date   the date to filter by
   * @param zoneId the ID of the zone
   * @return a list of Timetable entities for the specified week and zone
   */
  public List<Timetable> getTimetablesForOneWeek(LocalDate date, Long zoneId) {
    LocalDateTime startDateTime = date.atStartOfDay();
    LocalDateTime endDateTime = date.plusDays(7).atStartOfDay();
    return timetableRepository.findTimetablesForOneWeek(startDateTime, endDateTime, zoneId);
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
    List<Timetable> timetables = timetableRepository.findWorkerTimetableForDay(workerId, time);
    return timetables.stream()
        .anyMatch(timetable ->
            !timetable.getRealStartTime().isAfter(time) &&
                (timetable.getRealEndTime() == null || !timetable.getRealEndTime().isBefore(time))
        );
  }

  /**
   * Checks if a worker has finished their shift at a specific time.
   *
   * @param workerId the ID of the worker
   * @param time     the time to check
   * @return true if the worker has finished their shift, false otherwise
   */
  public boolean workerHasFinishedShift(Long workerId, LocalDateTime time) {
    List<Timetable> timetables = timetableRepository.findWorkerTimetableForDay(workerId, time);
    if (timetables.isEmpty()) {
      return false;
    }
    for (Timetable timetable : timetables) {
      if (timetable.getRealEndTime() != null && !timetable.getRealEndTime().isAfter(time)) {
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
        .filter(timetable -> timetable.getWorker().getZone().equals(zoneId) &&
            timetable.getWorker().isAvailability())
        .map(Timetable::getRealStartTime)
        .min(LocalDateTime::compareTo)
        .orElse(day.toLocalDate().atStartOfDay());
  }


  public LocalDateTime getEarliestTimeWithMinWorkers(int minWorkers, Long zoneId, LocalDateTime day,
                                                     List<ActiveTask> activeTasks) {
    if (day == null) {
      throw new IllegalArgumentException("The 'day' parameter cannot be null.");
    }
    if (activeTasks == null || activeTasks.isEmpty()) {
      throw new IllegalArgumentException("The 'activeTasks' parameter cannot be null or empty.");
    }

    List<Timetable> timetables = timetableRepository.findByStartDateSortedByTime(day.toLocalDate());

    int totalQualifiedWorkers = 0;
    for (Timetable timetable : timetables) {

      Worker worker = timetable.getWorker();
      if (worker == null || timetable.getRealStartTime() == null) {
        continue; // Skip invalid timetables
      }
      if (worker.getZone().equals(zoneId) && worker.isAvailability()) {
        if (activeTasks.stream().anyMatch(activeTask ->
            worker.getLicenses().containsAll(activeTask.getTask().getRequiredLicense()))) {
          totalQualifiedWorkers++;
        }
        if (totalQualifiedWorkers >= minWorkers) {
          return timetable.getRealStartTime();
        }
      }
    }

    return null; // No time found with the required number of workers
  }

  /**
   * Counts how many workers have not finished working in a specific zone at a specific time.
   *
   * @param zoneId the ID of the zone
   * @param time   the time to check
   * @return the number of workers who have not finished working, or 0 if none
   */
  public int countWorkersNotFinished(Long zoneId, LocalDateTime time) {

    List<Timetable> timetables = timetableRepository.findByStartDate(time.toLocalDate());
    int count = 0;
    for (Timetable timetable : timetables) {
      if (timetable.getWorker().getZone().equals(zoneId) &&
          timetable.getRealEndTime().isAfter(time) && timetable.getWorker().isAvailability()) {
        count++;
      }
    }

    return count;
  }

  /**
   * Deletes a Timetable entity by its ID.
   *
   * @param id the ID of the Timetable entity to delete
   * @return the deleted Timetable entity
   */
  public Timetable deleteTimetable(Long id) {
    timetableRepository.deleteById(id);
    return timetableRepository.findById(id).orElse(null);
  }

  /**
   * Counts the number of qualified workers for a specific task today.
   *
   * @param zoneId the ID of the zone
   * @param localDateTime the date and time to check
   * @param activeTask the active task to check against
   * @return the number of qualified workers
   */
  public int countQualifiedWorkersToday(Long zoneId, LocalDateTime localDateTime,
                                        ActiveTask activeTask) {
    int qualificationCount = 0;
    List<Timetable> timetables = timetableRepository.findByStartDate(localDateTime.toLocalDate());
    if (activeTask != null) {
      for (Timetable timetable : timetables) {
        if (timetable.getWorker().getZone().equals(zoneId) &&
            timetable.getWorker().isAvailability()) {
          // Check if the worker's end time has not passed
          if ((timetable.getRealEndTime() == null ||
              timetable.getRealEndTime().isAfter(localDateTime)) &&
              timetable.getWorker().getLicenses()
                  .containsAll(activeTask.getTask().getRequiredLicense())) {
            qualificationCount++;
          }
        }
      }
    }
    return qualificationCount;
  }
}
