package gruppe01.ntnu.no.Warehouse.Workflow.Assigner.services;

import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.entities.Notification;
import gruppe01.ntnu.no.Warehouse.Workflow.Assigner.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for managing notifications.
 */
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ZoneService zoneService;

    /**
     * Constructor for NotificationService.
     *
     * @param notificationRepository the repository for notifications
     * @param zoneService            the service for zones
     */
    public NotificationService(NotificationRepository notificationRepository, ZoneService zoneService) {
        this.notificationRepository = notificationRepository;
        this.zoneService = zoneService;
    }

    public List<Notification> getAllNotification() {
        return notificationRepository.findAll();
    }

    public Notification getNotificationById(long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    public List<Notification> getNotificationsByZoneId(long zoneId) {
        return notificationRepository.findAll().stream()
                .filter(notification -> notification.getZoneId() == zoneId)
                .toList();
    }

    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    public Notification createNotification(long zoneId, Notification notification) {
        if (zoneService.getZoneById(zoneId) == null) {
            return null;
        } else {
            notification.setZoneId(zoneId);
            return notificationRepository.save(notification);
        }
    }

    public Notification updateNotification(long id, Notification updatedNotification) {
        if (!notificationRepository.existsById(id)) {
            return null;
        } else {
            updatedNotification.setId(id);
            return notificationRepository.save(updatedNotification);
        }
    }

    public Notification deleteNotification(long id) {
        if (!notificationRepository.existsById(id)) {
            return null;
        } else {
            notificationRepository.deleteById(id);
            return notificationRepository.findById(id).orElse(null);
        }
    }

    public Map<Long, Notification> generateNotificationMapFromZones() {
        Map<Long, Notification> notificationMap = new HashMap<>();
        zoneService.getAllZones().forEach(zone -> {
            Notification notification = new Notification();
            notification.setZoneId(zone.getId());
            notification.setMessage("");
            notificationMap.put(zone.getId(), notification);
        });
        return notificationMap;
    }

    public void deleteAll() {
        notificationRepository.deleteAll();
    }

    public String getLastNotificationTime() {
        Notification lastNotification = notificationRepository.findTopByOrderByTimeDesc();
        if (lastNotification != null && lastNotification.getTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return lastNotification.getTime().format(formatter);
        }
        return "00:00";
    }

    public String getNotificationTimeByZone(long zoneId) {
        Notification lastNotification = notificationRepository.findTopByZoneIdOrderByTimeDesc(zoneId);
        if (lastNotification != null && lastNotification.getTime() != null && lastNotification.getZoneId() == zoneId) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return lastNotification.getTime().format(formatter);
        }
        return "00:00";
    }
}
