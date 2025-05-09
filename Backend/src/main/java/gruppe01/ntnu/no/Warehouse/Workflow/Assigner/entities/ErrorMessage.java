package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

        import jakarta.persistence.Entity;
        import jakarta.persistence.GeneratedValue;
        import jakarta.persistence.GenerationType;
        import jakarta.persistence.Id;

        import java.time.LocalDateTime;

        @Entity
        public class ErrorMessage {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private long id;

            private String message;

            private long zoneId;

            private LocalDateTime time;

            public ErrorMessage() {
            }

            public ErrorMessage(long id, String message, long zoneId, LocalDateTime time) {
                this.id = id;
                this.message = message;
                this.zoneId = zoneId;
                this.time = time;
            }

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }

            public long getZoneId() {
                return zoneId;
            }

            public void setZoneId(long zoneId) {
                this.zoneId = zoneId;
            }

            public LocalDateTime getTime() {
                return time;
            }

            public void setTime(LocalDateTime time) {
                this.time = time;
            }
        }