package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities;

        import io.swagger.v3.oas.annotations.media.Schema;
        import jakarta.persistence.*;

        import java.time.LocalDateTime;

        @Entity
        @Table(name = "error_message")
        @Schema(description = "Represents an error message entity in the system.")
        public class ErrorMessage {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private long id;

            @Column(name = "message", columnDefinition = "LONGTEXT")
            @Schema(description = "The error message.")
            private String message;

            @Column(name = "zone_id")
            @Schema(description = "The ID of the zone where the error occurred.")
            private long zoneId;

            @Column(name = "time")
            @Schema(description = "The time when the error occurred.")
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