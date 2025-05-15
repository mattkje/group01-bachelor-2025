package gruppe01.ntnu.no.warehouse.workflow.assigner.serviceTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.MonteCarloData;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.repositories.MonteCarloDataRepository;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.MonteCarloDataService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorldSimDataService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MonteCarloDataServiceTest {

  @Mock
  private MonteCarloDataRepository monteCarloDataRepository;

  @Mock
  private ZoneService zoneService;

  @Mock
  private WorldSimDataService worldSimDataService;

  @InjectMocks
  private MonteCarloDataService monteCarloDataService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetMCDataValues() {
    long zoneId = 1L;
    List<Integer> worldSimValues = List.of(10, 20, 30);
    List<MonteCarloData> monteCarloDataList = List.of(
        new MonteCarloData(1, LocalDateTime.now(), 5, 0, zoneId),
        new MonteCarloData(2, LocalDateTime.now(), 10, 0, zoneId)
    );

    when(worldSimDataService.getWorldSimValues(zoneId)).thenReturn(worldSimValues);
    when(monteCarloDataRepository.findAll()).thenReturn(monteCarloDataList);
    when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());

    List<List<Integer>> result = monteCarloDataService.getMCDataValues(zoneId);

    assertEquals(2, result.size());
    assertEquals(List.of(35), result.get(0));
    assertEquals(List.of(40), result.get(1));

    verify(worldSimDataService, times(1)).getWorldSimValues(zoneId);
    verify(monteCarloDataRepository, times(1)).findAll();
    verify(zoneService, times(2)).getZoneById(zoneId);
  }

  @Test
  void testFlushMCData() {
    monteCarloDataService.flushMCData();

    verify(monteCarloDataRepository, times(1)).deleteAll();
  }
}