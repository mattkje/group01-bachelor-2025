package gruppe01.ntnu.no.warehouse.workflow.assigner.controllers;

import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Notification;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.NotificationService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NotificationController handles HTTP requests related to notifications.
 * It provides endpoints to create, read, update, and delete notifications.
 */
@RestController
@RequestMapping("/api/error-messages")
@Tag(name = "NotificationController", description = "Controller for managing notifications.")
public class NotificationController {

  private final NotificationService notificationService;
  private final ZoneService zoneService;

  /**
   * Constructor for NotificationController.
   *
   * @param notificationService The service to handle notification operations.
   * @param zoneService         The service to handle zone operations.
   */
  public NotificationController(NotificationService notificationService, ZoneService zoneService) {
    this.notificationService = notificationService;
    this.zoneService = zoneService;
  }

  @Operation(
      summary = "Get all notifications",
      description = "Retrieve a list of all notifications in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<List<Notification>> getAllNotifications() {
    return ResponseEntity.ok(notificationService.getAllNotification());
  }

  /**
   * Retrieves a notification by its ID.
   *
   * @param id the ID of the notification
   * @return the notification with the specified ID, or null if not found
   */
  @Operation(
      summary = "Get notification by ID",
      description = "Retrieve an notification by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved notification"),
      @ApiResponse(responseCode = "404", description = "Notification not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Optional<Notification>> getNotificationById(
      @Parameter(description = "ID of the notification to retrieve")
      @PathVariable long id) {
    Optional<Notification> notification = Optional.ofNullable(notificationService.getNotificationById(id));
    if (notification.isPresent()) {
      return ResponseEntity.ok(notification);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Optional.empty());
    }
  }

  /**
   * Retrieves a list of notifications associated with a specific zone ID.
   *
   * @param zoneId the ID of the zone to retrieve notifications for
   * @return a list of notifications associated with the specified zone ID
   */
  @Operation(
      summary = "Get notifications by zone ID",
      description = "Retrieve a list of notifications associated with a specific zone ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description
          = "Successfully retrieved notifications"),
      @ApiResponse(responseCode = "404", description
          = "No notifications found for the specified zone ID"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping("/zone/{zoneId}")
  public ResponseEntity<List<Notification>> getNotificationsByZoneId(
      @Parameter(description = "ID of the zone to retrieve notification for")
      @PathVariable long zoneId) {
    List<Notification> notifications = notificationService.getNotificationsByZoneId(zoneId);
    if (zoneService.getZoneById(zoneId) == null) {
      if (zoneId == 0) {
        return ResponseEntity.ok(notificationService.getAllNotification());
      }
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(notifications);
    }
  }

  /**
   * Updates an existing notification.
   *
   * @param id           the ID of the notification to update
   * @param notification the updated notification object
   * @return the updated notification, or null if not found
   */
  @Operation(
      summary = "Update an already existing notification",
      description = "Update an existing notification in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated notification"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Notification not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PutMapping("/{id}")
  public ResponseEntity<Notification> updateNotification(
      @Parameter(description = "ID of the notification to update")
      @PathVariable long id,
      @Parameter(description = "Updated notification object")
      @RequestBody Notification notification) {
    if (notificationService.getNotificationById(id) == null) {
      return ResponseEntity.notFound().build();
    } else if (notification.getTime() == null || notification.getMessage().isEmpty()
        || notification.getMessage().isBlank()) {
      return ResponseEntity.badRequest().build();
    } else {
      return ResponseEntity.ok(notificationService.updateNotification(id, notification));
    }
  }

  /**
   * Creates a new notification.
   *
   * @param zoneId       the ID of the zone to create a notification for
   * @param notification the new notification object
   * @return the created notification
   */
  @Operation(
      summary = "Create a new notification",
      description = "Create a new notification in the system."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully created notification"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Zone not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/{zoneId}")
  public ResponseEntity<Notification> createNotification(
      @Parameter(description = "ID of the zone to create a notification for")
      @PathVariable long zoneId,
      @Parameter(description = "New notification object")
      @RequestBody Notification notification) {
    if (notification.getTime() == null || notification.getMessage().isEmpty()
        || notification.getMessage().isBlank()) {
      return ResponseEntity.badRequest().build();
    } else if (zoneService.getZoneById(zoneId) == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(notificationService.createNotification(zoneId, notification));
    }
  }

  /**
   * Deletes a notification by its ID.
   *
   * @param id the ID of the notification to delete
   * @return a ResponseEntity indicating the result of the deletion
   */
  @Operation(
      summary = "Delete a notification",
      description = "Delete a notification by its ID."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted error message"),
      @ApiResponse(responseCode = "404", description = "Error message not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Notification> deleteNotification(
      @Parameter(description = "ID of the notification to delete")
      @PathVariable long id) {
    if (notificationService.getNotificationById(id) == null) {
      return ResponseEntity.notFound().build();
    } else {
      notificationService.deleteNotification(id);
      return ResponseEntity.ok().build();
    }
  }

  /**
   * Retrieves the last notification time.
   *
   * @return a ResponseEntity containing the last notification time
   */
  @GetMapping("/done-by")
  public ResponseEntity<Map<String, String>> getLastNotificationTime() {
    String lastNotificationTime = notificationService.getLastNotificationTime();
    if (lastNotificationTime == null) {
      return ResponseEntity.notFound().build();
    } else {
      Map<String, String> response = new HashMap<>();
      response.put("time", lastNotificationTime);
      return ResponseEntity.ok(response);
    }
  }

  /**
   * Retrieves the last notification time for a specific zone.
   *
   * @param zoneId the ID of the zone to retrieve the last notification time for
   * @return a ResponseEntity containing the last notification time for the specified zone
   */
  @GetMapping("/done-by/{zoneId}")
  public ResponseEntity<Map<String, String>> getLastNotificationTimeByZone(
      @PathVariable long zoneId) {
    String lastNotificationTime;
    if (zoneId == 0) {
      lastNotificationTime = notificationService.getLastNotificationTime();
    } else {
      lastNotificationTime = notificationService.getNotificationTimeByZone(zoneId);
    }

    if (lastNotificationTime == null) {
      return ResponseEntity.notFound().build();
    } else {
      Map<String, String> response = new HashMap<>();
      response.put("time", lastNotificationTime);
      return ResponseEntity.ok(response);
    }
  }


}
