package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.serviceTest;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.License;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Timetable;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Worker;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.WorkerTimeRange;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.LicenseRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.TimetableRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.WorkerRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.WorkerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private TimetableRepository timetableRepository;

    @InjectMocks
    private WorkerService workerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllWorkers() {
        List<Worker> workers = List.of(new Worker(), new Worker());
        when(workerRepository.findAllWithLicenses()).thenReturn(workers);

        List<Worker> result = workerService.getAllWorkers();

        assertEquals(2, result.size());
        verify(workerRepository, times(1)).findAllWithLicenses();
    }

    @Test
    void testGetAvailableWorkers() {
        Worker availableWorker = new Worker();
        availableWorker.setAvailability(true);
        when(workerRepository.findAll()).thenReturn(List.of(availableWorker));

        List<Worker> result = workerService.getAvailableWorkers();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailability());
        verify(workerRepository, times(1)).findAll();
    }

    @Test
    void testAddWorker() {
        Worker worker = new Worker();
        worker.setWorkSchedule(Map.of(DayOfWeek.MONDAY, new WorkerTimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))));
        when(workerRepository.save(worker)).thenReturn(worker);

        Worker result = workerService.addWorker(worker);

        assertNotNull(result);
        verify(timetableRepository, times(1)).saveAll(anyList());
        verify(workerRepository, times(1)).save(worker);
    }

    @Test
    void testUpdateWorker() {
        Worker existingWorker = new Worker();
        existingWorker.setId(1L);
        existingWorker.setWorkSchedule(Map.of(DayOfWeek.MONDAY, new WorkerTimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))));
        Worker updatedWorker = new Worker();
        updatedWorker.setWorkSchedule(Map.of(DayOfWeek.TUESDAY, new WorkerTimeRange(LocalTime.of(10, 0), LocalTime.of(18, 0))));

        Timetable timetable = new Timetable();
        timetable.setWorker(existingWorker);

        when(workerRepository.findById(1L)).thenReturn(Optional.of(existingWorker));
        when(timetableRepository.findAll()).thenReturn(List.of(timetable));
        when(workerRepository.save(existingWorker)).thenReturn(existingWorker);

        Worker result = workerService.updateWorker(1L, updatedWorker);

        assertNotNull(result);
        verify(timetableRepository, times(1)).delete(timetable);
        verify(timetableRepository, times(1)).saveAll(anyList());
        verify(workerRepository, times(1)).save(existingWorker);
    }

    @Test
    void testAddLicenseToWorker() {
        Worker worker = new Worker();
        License license = new License();
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(licenseRepository.findById(1L)).thenReturn(Optional.of(license));
        when(workerRepository.save(worker)).thenReturn(worker);

        Worker result = workerService.addLicenseToWorker(1L, 1L);

        assertNotNull(result);
        verify(workerRepository, times(1)).save(worker);
    }

    @Test
    void testDeleteWorker() {
        Worker worker = new Worker();
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(workerRepository.save(worker)).thenReturn(worker);

        Worker result = workerService.deleteWorker(1L);

        assertNotNull(result);
        assertTrue(result.isDead());
        verify(workerRepository, times(1)).save(worker);
    }
}