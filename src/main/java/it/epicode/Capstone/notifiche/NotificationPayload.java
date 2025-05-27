package it.epicode.Capstone.notifiche;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*Payload minimo inviato via WebSocket al client.*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {
    private Long id;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;
}