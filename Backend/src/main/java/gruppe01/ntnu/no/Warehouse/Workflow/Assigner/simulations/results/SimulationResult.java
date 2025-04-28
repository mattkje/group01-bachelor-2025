package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results;

    import java.time.LocalDateTime;
    import java.util.Map;

    public record SimulationResult(
          Object latestEndTime, Map<Long, ZoneSimResult> zoneSimResults) {

        public LocalDateTime getLatestEndTime() {
            if (latestEndTime == null || !(latestEndTime instanceof LocalDateTime)) {
                throw new IllegalStateException("Latest end time is not set.");
            }
            return (LocalDateTime) latestEndTime;
        }

        public Map<Long, ZoneSimResult> getZoneSimResults() {
            return zoneSimResults;
        }

        public String getErrorMessage(Long zoneId) {
            ZoneSimResult zoneSimResult = zoneSimResults.get(zoneId);
            if (zoneSimResult != null) {
                return zoneSimResult.getErrorMessage();
            }
            return null;
        }
    }