package gruppe01.ntnu.no.warehouse.workflow.assigner.simulations;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.PickerTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.*;
import gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.results.SimulationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import smile.regression.RandomForest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class MonteCarloTest {

  @Mock
  private ZoneService zoneService;

  @Mock
  private ActiveTaskService activeTaskService;

  @Mock
  private PickerTaskService pickerTaskService;

  @Mock
  private Utils utils;

  @Mock
  private TimetableService timetableService;

  @InjectMocks
  private MonteCarlo monteCarlo;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testMonteCarlo() throws InterruptedException, ExecutionException, IOException {
    // Arrange
    int simCount = 1;
    Map<String, RandomForest> models = Collections.emptyMap();
    LocalDateTime currentTime = LocalDateTime.now();

    when(zoneService.getAllZones()).thenReturn(Collections.emptyList());
    when(activeTaskService.getUnfinishedActiveTasksForToday(currentTime)).thenReturn(
        Collections.emptyList());
    when(pickerTaskService.getUnfinishedPickerTasksForToday(currentTime)).thenReturn(
        Collections.emptySet());
    when(utils.getLatestEndTime(any())).thenReturn(currentTime);

    // Act
    List<SimulationResult> results =
        monteCarlo.monteCarlo(simCount, models, currentTime, timetableService);

    // Assert
    assertNotNull(results);
    verify(zoneService, times(1)).getAllZones();
    verify(activeTaskService, times(1)).getUnfinishedActiveTasksForToday(currentTime);
    verify(pickerTaskService, times(1)).getUnfinishedPickerTasksForToday(currentTime);
  }
}