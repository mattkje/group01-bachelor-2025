package gruppe01.ntnu.no.warehouse.workflow.assigner.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity class representing a notification in the system.
 */
@Entity
@Table(name = "notifications")
@Schema(description = "Represents an notification entity in the system.")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "message", columnDefinition = "LONGTEXT")
  @Schema(description = "The notification message.")
  private String message;

  @Column(name = "zone_id")
  @Schema(description = "The ID of the zone where the notification belongs.")
  private long zoneId;

  @Column(name = "time")
  @Schema(description = "The time when the notification was sent.")
  private LocalDateTime time;

  /**
   * Default constructor for JPA.
   */
  public Notification() {
  }

  /**
   * Constructor for creating a new notification with the specified parameters.
   *
   * @param id      The ID of the notification.
   * @param message The notification message.
   * @param zoneId  The ID of the zone where the notification belongs.
   * @param time    The time when the notification was sent.
   */
  public Notification(long id, String message, long zoneId, LocalDateTime time) {
    this.id = id;
    this.message = message;
    this.zoneId = zoneId;
    this.time = time;
  }

  /**
   * Gets the ID of the notification.
   *
   * @return The ID of the notification.
   */
  public long getId() {
    return id;
  }

  /**
   * Sets the ID of the notification.
   *
   * @param id The ID of the notification.
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Gets the message of the notification.
   *
   * @return The message of the notification.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the message of the notification.
   *
   * @param message The message of the notification.
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Gets the ID of the zone where the notification belongs.
   *
   * @return The ID of the zone.
   */
  public long getZoneId() {
    return zoneId;
  }

  /**
   * Sets the ID of the zone where the notification belongs.
   *
   * @param zoneId The ID of the zone.
   */
  public void setZoneId(long zoneId) {
    this.zoneId = zoneId;
  }

  /**
   * Gets the time when the notification was sent.
   *
   * @return The time of the notification.
   */
  public LocalDateTime getTime() {
    return time;
  }

  /**
   * Sets the time when the notification was sent.
   *
   * @param time The time of the notification.
   */
  public void setTime(LocalDateTime time) {
    this.time = time;
  }
}