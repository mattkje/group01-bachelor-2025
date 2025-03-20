package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulatorTests;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.SimulationService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services.ZoneService;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Zone;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SimulationServiceTest {

    @Mock
    private ActiveTaskService activeTaskService;

    @Mock
    private ZoneService zoneService;

    @InjectMocks
    private SimulationService simulationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRunZoneSimulation() {
        Long zoneId = zoneService.getAllZones().getFirst().getId();
        Zone mockZone = new Zone();
        when(zoneService.getZoneById(zoneId)).thenReturn(mockZone);
        when(activeTaskService.getRemainingTasksForTodayByZone(zoneId)).thenReturn(new ArrayList<>());

        List<String> result = simulationService.runZoneSimulation(zoneId,true);
        System.out.println(result);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size()); // Assuming no error messages
        verify(zoneService, times(1)).getZoneById(zoneId);
        verify(activeTaskService, times(1)).getRemainingTasksForTodayByZone(zoneId);
    }

    @Test
     void testRunZoneSimulationWithInvalidZoneId() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simulationService.runZoneSimulation(null,false);
        });

        assertEquals("Zone ID cannot be null and must be a real zone", exception.getMessage());
    }
}