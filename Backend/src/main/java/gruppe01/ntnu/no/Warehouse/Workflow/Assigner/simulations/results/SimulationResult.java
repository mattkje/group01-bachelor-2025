package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.simulations.results;

    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    public class SimulationResult {
        private double averageCompletionTime;
        private Map<Long, Double> zoneDurations;

        public SimulationResult(double averageCompletionTime, Map<Long, Double> zoneDurations) {
            this.averageCompletionTime = averageCompletionTime;
            this.zoneDurations = zoneDurations;
        }

        public double getAverageCompletionTime() {
            return averageCompletionTime;
        }

        public Map<Long, Double> getZoneDurations() {
            return zoneDurations;
        }

        public static Map<Long, Double> calculateAverageZoneDurations(List<SimulationResult> results) {
            Map<Long, Double> totalDurations = new HashMap<>();
            Map<Long, Integer> countDurations = new HashMap<>();

            for (SimulationResult result : results) {
                for (Map.Entry<Long, Double> entry : result.getZoneDurations().entrySet()) {
                    Long zoneId = entry.getKey();
                    Double duration = entry.getValue();

                    totalDurations.put(zoneId, totalDurations.getOrDefault(zoneId, 0.0) + duration);
                    countDurations.put(zoneId, countDurations.getOrDefault(zoneId, 0) + 1);
                }
            }

            Map<Long, Double> averageDurations = new HashMap<>();
            for (Map.Entry<Long, Double> entry : totalDurations.entrySet()) {
                Long zoneId = entry.getKey();
                Double totalDuration = entry.getValue();
                Integer count = countDurations.get(zoneId);

                averageDurations.put(zoneId, totalDuration / count);
            }

            return averageDurations;
        }
    }