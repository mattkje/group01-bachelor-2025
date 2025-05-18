package gruppe01.ntnu.no.warehouse.workflow.assigner.serviceTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.MonteCarloData;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.MonteCarloDataRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.ZoneRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.MonteCarloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class MonteCarloServiceTest {

  @Mock
  private MonteCarloDataRepository monteCarloDataRepository;

  @InjectMocks
  private MonteCarloService monteCarloService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testMonteCarloServiceConstructor() {
    MonteCarloService service = new MonteCarloService(monteCarloDataRepository);
    assertNotNull(service);
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