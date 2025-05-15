package gruppe01.ntnu.no.warehouse.workflow.assigner.services;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.License;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Timetable;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.WorkerTimeRange;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.LicenseRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.TimetableRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.WorkerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for managing Worker entities.
 */
@Service
public class WorkerService {

  private final WorkerRepository workerRepository;

  private final LicenseRepository licenseRepository;

  private final TimetableRepository timetableRepository;

  /**
   * Constructor for WorkerService.
   *
   * @param workerRepository    the repository for Worker entity
   * @param licenseRepository   the repository for License entity
   * @param timetableRepository the repository for Timetable entity
   */
  public WorkerService(WorkerRepository workerRepository, LicenseRepository licenseRepository,
                       TimetableRepository timetableRepository) {
    this.workerRepository = workerRepository;
    this.licenseRepository = licenseRepository;
    this.timetableRepository = timetableRepository;
  }

  /**
   * Retrieves all workers from the repository.
   *
   * @return a list of all workers
   */
  public List<Worker> getAllWorkers() {
    return workerRepository.findAllWithLicenses();
  }

  /**
   * Retrieves all available workers from the repository.
   *
   * @return a list of all available workers
   */
  public List<Worker> getAvailableWorkers() {
    return workerRepository.findAvailableWorkers();
  }

  /**
   * Retrieves all unavailable workers from the repository.
   *
   * @return a list of all unavailable workers
   */
  public List<Worker> getUnavailableWorkers() {
    return workerRepository.findUnavailableWorkers();
  }

  /**
   * Retrieves a worker by their ID.
   *
   * @param id the ID of the worker
   * @return an Optional containing the worker if found, or empty if not found
   */
  public Optional<Worker> getWorkerById(Long id) {
    return workerRepository.findById(id);
  }

  /**
   * Creates a new worker.
   *
   * @param worker the worker to create
   * @return the created worker
   */
  public Worker addWorker(Worker worker) {
    createTimetablesTillNextMonth(LocalDate.now(), worker);
    return workerRepository.save(worker);
  }

  /**
   * Updates an existing worker.
   *
   * @param id     the ID of the worker to update
   * @param updatedData the updated worker
   * @return the updated worker
   */
  @Transactional
  public Worker updateWorker(Long id, Worker updatedData) {
    Optional<Worker> optionalWorker = workerRepository.findById(id);
    if (optionalWorker.isEmpty()) {
      return null;
    }

    Worker existingWorker = optionalWorker.get();

    // Check if workSchedule changed BEFORE updating it
    boolean workScheduleChanged = !Objects.equals(
        existingWorker.getWorkSchedule(),
        updatedData.getWorkSchedule()
    );

    // Update fields
    existingWorker.setAvailability(updatedData.isAvailability());
    existingWorker.setName(updatedData.getName());
    existingWorker.setEfficiency(updatedData.getEfficiency());
    existingWorker.setLicenses(updatedData.getLicenses());
    existingWorker.setZone(updatedData.getZone());
    existingWorker.setWorkerType(updatedData.getWorkerType());
    existingWorker.setCurrentPickerTask(updatedData.getCurrentPickerTask());
    existingWorker.setCurrentTask(updatedData.getCurrentActiveTask());
    existingWorker.setWorkSchedule(updatedData.getWorkSchedule());

    // Only if schedule changed, update related timetables
    if (workScheduleChanged) {
      timetableRepository.deleteAllByWorkerId(existingWorker.getId());
      createTimetablesTillNextMonth(LocalDate.now(), existingWorker);
    }

    return workerRepository.save(existingWorker);
  }


  /**
   * Updates the availability of a worker.
   *
   * @param id the ID of the worker
   * @return the updated worker
   */
  public Worker updateWorkerAvailability(Long id) {
      if (workerRepository.findById(id).isEmpty()) {
          return null;
      }
    Worker updatedWorker = workerRepository.findById(id).get();
    updatedWorker.setAvailability(!updatedWorker.isAvailability());
    return workerRepository.save(updatedWorker);
  }

  /**
   * Adds or removes a license from a worker.
   *
   * @param id        the ID of the worker
   * @param licenseId the ID of the license
   * @return the updated worker
   */
  public Worker addLicenseToWorker(Long id, Long licenseId) {
      if (workerRepository.findById(id).isEmpty() ||
          licenseRepository.findById(licenseId).isEmpty()) {
          return null;
      }
    Worker updatedWorker = workerRepository.findById(id).get();
    License license = licenseRepository.findById(licenseId).get();
    if (updatedWorker.getLicenses().contains(license)) {
      updatedWorker.getLicenses().remove(license);
    } else {
      updatedWorker.getLicenses().add(license);
    }
    return workerRepository.save(updatedWorker);
  }

  /**
   * Deletes a worker by their ID.
   *
   * @param id the ID of the worker to delete
   * @return the deleted worker
   */
  public Worker deleteWorker(Long id) {
      if (workerRepository.findById(id).isEmpty()) {
          return null;
      }
    Worker worker = workerRepository.findById(id).get();
    worker.setDead(true);
    return workerRepository.save(worker);
  }

  /**
   * Adds a zone to a worker.
   *
   * @param workerId the ID of the worker
   * @param zoneId   the ID of the zone
   * @return the updated worker
   */
  public Worker addWorkerToZone(Long workerId, Long zoneId) {
      if (workerRepository.findById(workerId).isEmpty()) {
          return null;
      }
    Worker updatedWorker = workerRepository.findById(workerId).get();
    updatedWorker.setZone(zoneId);
    return workerRepository.save(updatedWorker);
  }

  /**
   * Creates timetables for all workers for the next month based on their work schedule.
   *
   * @param currentDate The current date to calculate the next month from.
   */
  public void createWorkerTimetablesForNextMonth(LocalDate currentDate) {
    List<Worker> workers = workerRepository.findAll();
    LocalDate today = currentDate.plusMonths(1).withDayOfMonth(1);
    LocalDate endDate = today.plusMonths(2).withDayOfMonth(1).minusDays(1);

    List<Timetable> timetables = new ArrayList<>();

    for (Worker worker : workers) {
      Map<DayOfWeek, WorkerTimeRange> workSchedule = worker.getWorkSchedule();

      for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
        createTimetableForDay(timetables, worker, workSchedule, date);
      }
    }

    timetableRepository.saveAll(timetables);
  }

  /**
   * Helper method to create a timetable for a specific worker on a specific date.
   *
   * @param timetables   The list of timetables to add to.
   * @param worker       The worker for whom to create the timetable.
   * @param workSchedule The work schedule of the worker.
   * @param date         The date for which to create the timetable.
   */
  private void createTimetableForDay(List<Timetable> timetables, Worker worker,
                                     Map<DayOfWeek, WorkerTimeRange> workSchedule, LocalDate date) {
    DayOfWeek dayOfWeek = date.getDayOfWeek();

    if (workSchedule.containsKey(dayOfWeek)) {
      WorkerTimeRange timeRange = workSchedule.get(dayOfWeek);

      Timetable timetable = new Timetable();
      timetable.setWorker(worker);
      timetable.setStartTime(date.atTime(timeRange.getStartTime()));
      timetable.setEndTime(date.atTime(timeRange.getEndTime()));

      timetables.add(timetable);
    }
  }

  /**
   * Creates timetables for a specific worker for the next month based on their work schedule.
   *
   * @param worker The worker for whom to create timetables.
   */
  public void createTimetablesTillNextMonth(LocalDate dateNow, Worker worker) {
    Map<DayOfWeek, WorkerTimeRange> workSchedule = worker.getWorkSchedule();
    LocalDate startOfNextMonth = dateNow.withDayOfMonth(1).plusMonths(1);

    List<Timetable> timetables = new ArrayList<>();

    for (LocalDate date = dateNow; !date.isAfter(startOfNextMonth.minusDays(1));
         date = date.plusDays(1)) {
      createTimetableForDay(timetables, worker, workSchedule, date);
    }
    timetableRepository.saveAll(timetables);
  }

  public List<Worker> addAllLicensesToWorkers() {
    List<Worker> workers = workerRepository.findAll();
    for (Worker worker : workers) {
      List<License> licenses = licenseRepository.findAll();
      for (License license : licenses) {
        if (!worker.getLicenses().contains(license)) {
          worker.getLicenses().add(license);
        }
      }
      workerRepository.save(worker);
    }
    return workers;
  }

  public void removeTasks() {
    List<Worker> workers = workerRepository.findAll();
    for (Worker worker : workers) {
      worker.setCurrentTask(null);
      worker.setCurrentPickerTask(null);
      workerRepository.save(worker);
    }
  }
}
