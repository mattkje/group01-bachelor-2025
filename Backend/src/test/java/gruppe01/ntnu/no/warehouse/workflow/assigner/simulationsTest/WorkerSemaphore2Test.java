package gruppe01.ntnu.no.warehouse.workflow.assigner.simulationsTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Worker;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TimetableService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.semaphores.WorkerSemaphore2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkerSemaphore2Test {

    @Mock
    private TimetableService timetableService;

    private WorkerSemaphore2 workerSemaphore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workerSemaphore = new WorkerSemaphore2(timetableService);
    }

    @Test
    void testInitialize() {
        Set<Worker> workersSet = new HashSet<>();
        Worker worker = mock(Worker.class);
        when(worker.getZone()).thenReturn(1L);
        workersSet.add(worker);

        LocalDateTime startTime = LocalDateTime.now();
        when(timetableService.getWorkersWorkingByDayAndZone(startTime, 1L)).thenReturn(workersSet);

        workerSemaphore.initialize(workersSet, startTime);

        assertNotNull(workerSemaphore.getWorkers());
        verify(timetableService, times(1)).getWorkersWorkingByDayAndZone(startTime, 1L);
    }

    @Test
    void testAcquireMultiple_NotEnoughWorkers() throws InterruptedException {
        ActiveTask activeTask = mock(ActiveTask.class);
        PickerTask pickerTask = null;
        AtomicReference<LocalDateTime> startTime = new AtomicReference<>(LocalDateTime.now());
        Long zoneId = 1L;

        when(timetableService.countWorkersNotFinished(zoneId, startTime.get())).thenReturn(0);

        String result = workerSemaphore.acquireMultiple(activeTask, pickerTask, startTime, zoneId);

        assertEquals("104:null:null", result);
    }

    @Test
    void testRelease() {
        Worker worker = mock(Worker.class);
        when(worker.getId()).thenReturn(1L); // Mock worker ID
        Set<Worker> workersSet = new HashSet<>();
        workersSet.add(worker);

        workerSemaphore.initialize(workersSet, LocalDateTime.now());
        workerSemaphore.release(worker);

        assertTrue(workerSemaphore.getWorkers().contains(String.valueOf(worker.getId())));
    }

    @Test
    void testReleaseAll() {
        Worker worker1 = mock(Worker.class);
        Worker worker2 = mock(Worker.class);
        when(worker1.getId()).thenReturn(1L); // Mock worker1 ID
        when(worker2.getId()).thenReturn(2L); // Mock worker2 ID
        Set<Worker> workersSet = new HashSet<>();
        workersSet.add(worker1);
        workersSet.add(worker2);

        workerSemaphore.initialize(workersSet, LocalDateTime.now());
        workerSemaphore.releaseAll(Collections.singletonList(worker1));

        assertTrue(workerSemaphore.getWorkers().contains(String.valueOf(worker1.getId())));
    }
}