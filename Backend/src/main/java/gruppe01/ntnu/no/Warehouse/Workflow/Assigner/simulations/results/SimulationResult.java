package gruppe01.ntnu.no.warehouse.workflow.assigner.simulations.results;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record SimulationResult(
    Object latestEndTime, Map<Long, ZoneSimResult> zoneSimResults) {

  public LocalDateTime getLatestEndTime() {
    if (!(latestEndTime instanceof LocalDateTime)) {
      throw new IllegalStateException("Latest end time is not set.");
    }
    return (LocalDateTime) latestEndTime;
  }

  public Map<Long, ZoneSimResult> getZoneSimResults() {
    return zoneSimResults;
  }

  public List<ZoneSimResult> getZoneSimResultList() {
    return zoneSimResults.values().stream().toList();
  }

  public List<String> getErrorMessage(Long zoneId) {
    ZoneSimResult zoneSimResult = zoneSimResults.get(zoneId);
    if (zoneSimResult != null) {
      return zoneSimResult.getErrorMessage();
    }
    return null;
  }
}