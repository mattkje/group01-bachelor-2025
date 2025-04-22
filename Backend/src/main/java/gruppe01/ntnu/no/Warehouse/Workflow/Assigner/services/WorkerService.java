package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorkerTimeRange;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.LicenseRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TimetableRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WorkerService {

    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private LicenseRepository licenseRepository;
    @Autowired
    private TimetableRepository timetableRepository;

    public List<Worker> getAllWorkers() {
        return workerRepository.findAllWithLicenses();
    }

    public List<Worker> getAvailableWorkers() {
        List<Worker> availableWorkers = new ArrayList<>();
        for (Worker worker : workerRepository.findAll()) {
            if (worker.isAvailability()) {
                availableWorkers.add(worker);
            }
        }
        return availableWorkers;
    }

    public List<Worker> getUnavailableWorkers() {
        List<Worker> unavailableWorkers = new ArrayList<>();
        for (Worker worker : workerRepository.findAll()) {
            if (!worker.isAvailability()) {
                unavailableWorkers.add(worker);
            }
        }
        return unavailableWorkers;
    }

    public Optional<Worker> getWorkerById(Long id) {
        return workerRepository.findById(id);
    }

    public Worker addWorker(Worker worker) {
        createTimetablesTillNextMonth(worker);
        return workerRepository.save(worker);
    }

    public Worker updateWorker (Long id, Worker worker) {
        Worker updatedWorker = workerRepository.findById(id).get();
        updatedWorker.setAvailability(worker.isAvailability());
        updatedWorker.setName(worker.getName());
        updatedWorker.setEfficiency(worker.getEfficiency());
        updatedWorker.setLicenses(worker.getLicenses());
        updatedWorker.setZone(worker.getZone());
        updatedWorker.setWorkerType(worker.getWorkerType());

        if (worker.getWorkSchedule() != updatedWorker.getWorkSchedule()) {
            for (Timetable timetable : timetableRepository.findAll()) {
                if (timetable.getWorker().getId().equals(updatedWorker.getId())) {
                    timetableRepository.delete(timetable);
                }
            }
            createTimetablesTillNextMonth(updatedWorker);
        }

        updatedWorker.setWorkSchedule(worker.getWorkSchedule());
        return workerRepository.save(updatedWorker);
    }

    public Worker updateWorkerAvailability(Long id) {
        Worker updatedWorker = workerRepository.findById(id).get();
        updatedWorker.setAvailability(!updatedWorker.isAvailability());
        return workerRepository.save(updatedWorker);
    }

    public Worker addLicenseToWorker(Long id, Long licenseId) {
        Worker updatedWorker = workerRepository.findById(id).get();
        License license = licenseRepository.findById(licenseId).get();
        if (updatedWorker.getLicenses().contains(license)) {
            updatedWorker.getLicenses().remove(license);
        }else {
            updatedWorker.getLicenses().add(license);
        }
        return workerRepository.save(updatedWorker);
    }

    public Worker deleteWorker(Long id) {
        Worker worker = workerRepository.findById(id).get();
        worker.setDead(true);
        return workerRepository.save(worker);
    }

    public Worker addWorkerToZone(Long workerId, Long zoneId) {
        Worker updatedWorker = workerRepository.findById(workerId).get();
        updatedWorker.setZone(zoneId);
        return workerRepository.save(updatedWorker);
    }

    /**
     * Creates timetables for all workers for the next month based on their work schedule.
     */
    public void createWorkerTimetablesForNextMonth() {
        List<Worker> workers = workerRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusMonths(1);

        List<Timetable> timetables = new ArrayList<>();

        for (Worker worker : workers) {
            Map<DayOfWeek, WorkerTimeRange> workSchedule = worker.getWorkSchedule();

            for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
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
        }

        timetableRepository.saveAll(timetables);
    }

    /**
     * Creates timetables for a specific worker for the next month based on their work schedule.
     * @param worker The worker for whom to create timetables.
     */
    private void createTimetablesTillNextMonth(Worker worker) {
        Map<DayOfWeek, WorkerTimeRange> workSchedule = worker.getWorkSchedule();
        LocalDate today = LocalDate.now();
        LocalDate startOfNextMonth = today.withDayOfMonth(1).plusMonths(1);

        List<Timetable> timetables = new ArrayList<>();

        for (LocalDate date = today; !date.isAfter(startOfNextMonth.minusDays(1)); date = date.plusDays(1)) {
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
        timetableRepository.saveAll(timetables);
    }
}
