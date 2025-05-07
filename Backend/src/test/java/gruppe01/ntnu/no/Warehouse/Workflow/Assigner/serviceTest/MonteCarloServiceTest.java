package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.serviceTest;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.MonteCarloData;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.MonteCarloDataRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.ZoneRepository;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.MonteCarloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class MonteCarloServiceTest {

    @Mock
    private MonteCarloDataRepository monteCarloDataRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private MonteCarloService monteCarloService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateSimulationDataPoint_WithZoneId() {
        int simNo = 1;
        LocalDateTime now = LocalDateTime.now();
        int tasksCompleted = 5;
        Long zoneId = 1L;

        monteCarloService.generateSimulationDataPoint(simNo, now, tasksCompleted, zoneId);

        verify(monteCarloDataRepository, times(1)).save(any(MonteCarloData.class));
    }

    @Test
    void testGenerateSimulationDataPoint_WithoutZoneId() {
        int simNo = 1;
        LocalDateTime now = LocalDateTime.now();
        int tasksCompleted = 5;
        Long zoneId = null;

        monteCarloService.generateSimulationDataPoint(simNo, now, tasksCompleted, zoneId);

        verify(monteCarloDataRepository, times(1)).save(any(MonteCarloData.class));
    }

    @Test
    void testDropAllData() {
        monteCarloService.dropAllData();

        verify(monteCarloDataRepository, times(1)).deleteAll();
    }
}