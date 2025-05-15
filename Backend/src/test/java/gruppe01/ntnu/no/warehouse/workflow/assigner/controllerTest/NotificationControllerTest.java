package gruppe01.ntnu.no.warehouse.workflow.assigner.controllerTest;

import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.ActiveTaskController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.controllers.NotificationController;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.ActiveTask;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Notification;
import gruppe01.ntnu.no.warehouse.workflow.assigner.entities.Zone;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ActiveTaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.NotificationService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.TaskService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.WorkerService;
import gruppe01.ntnu.no.warehouse.workflow.assigner.services.ZoneService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActiveTaskService activeTaskService;

    @MockitoBean
    private ZoneService zoneService;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private WorkerService workerService;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void testGetAllNotifications() throws Exception {
        Notification notification1 = new Notification();
        Notification notification2 = new Notification();
        when(notificationService.getAllNotification()).thenReturn(List.of(notification1, notification2));

        mockMvc.perform(get("/api/error-messages"))
                .andExpect(status().isOk());
        verify(notificationService, times(1)).getAllNotification();
    }

    @Test
    void testGetNotificationById() throws Exception {
        long id = 1L;
        Notification notification = new Notification();
        when(notificationService.getNotificationById(id)).thenReturn(notification);

        mockMvc.perform(get("/api/error-messages/{id}", id))
                .andExpect(status().isOk());
        verify(notificationService, times(1)).getNotificationById(id);
    }

    @Test
    void testGetNotificationsByZoneId() throws Exception {
        long zoneId = 1L;
        when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
        when(notificationService.getNotificationsByZoneId(zoneId)).thenReturn(List.of(new Notification()));

        mockMvc.perform(get("/api/error-messages/zone/{zoneId}", zoneId))
                .andExpect(status().isOk());
        verify(notificationService, times(1)).getNotificationsByZoneId(zoneId);
    }

    @Test
    void testUpdateNotification() throws Exception {
        long id = 1L;
        Notification notification = new Notification();
        notification.setMessage("Updated message");
        when(notificationService.getNotificationById(id)).thenReturn(notification);
        when(notificationService.updateNotification(eq(id), any(Notification.class))).thenReturn(notification);

        mockMvc.perform(put("/api/error-messages/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Updated message\",\"time\":\"2023-10-01T10:00:00\"}"))
                .andExpect(status().isOk());
        verify(notificationService, times(1)).updateNotification(eq(id), any(Notification.class));
    }

    @Test
    void testCreateNotification() throws Exception {
        long zoneId = 1L;
        Notification notification = new Notification();
        notification.setMessage("New notification");
        when(zoneService.getZoneById(zoneId)).thenReturn(new Zone());
        when(notificationService.createNotification(eq(zoneId), any(Notification.class))).thenReturn(notification);

        mockMvc.perform(post("/api/error-messages/{zoneId}", zoneId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"New notification\",\"time\":\"2023-10-01T10:00:00\"}"))
                .andExpect(status().isOk());
        verify(notificationService, times(1)).createNotification(eq(zoneId), any(Notification.class));
    }

    @Test
    void testDeleteNotification() throws Exception {
        long id = 1L;
        when(notificationService.getNotificationById(id)).thenReturn(new Notification());

        mockMvc.perform(delete("/api/error-messages/{id}", id))
                .andExpect(status().isOk());
        verify(notificationService, times(1)).deleteNotification(id);
    }

    @Test
    void testGetLastNotificationTime() throws Exception {
        when(notificationService.getLastNotificationTime()).thenReturn("2023-10-01T10:00:00");

        mockMvc.perform(get("/api/error-messages/done-by"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value("2023-10-01T10:00:00"));
        verify(notificationService, times(1)).getLastNotificationTime();
    }

    @Test
    void testGetLastNotificationTimeByZone() throws Exception {
        long zoneId = 1L;
        when(notificationService.getNotificationTimeByZone(zoneId)).thenReturn("2023-10-01T10:00:00");

        mockMvc.perform(get("/api/error-messages/done-by/{zoneId}", zoneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value("2023-10-01T10:00:00"));
        verify(notificationService, times(1)).getNotificationTimeByZone(zoneId);
    }
}