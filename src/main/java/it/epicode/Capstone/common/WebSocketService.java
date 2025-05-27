package it.epicode.Capstone.common;

import it.epicode.Capstone.notifiche.Notification;
import it.epicode.Capstone.notifiche.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(String recipient, NotificationPayload payload) {
        messagingTemplate.convertAndSendToUser(
                recipient,
                "/queue/notifications",
                payload
        );
    }
}
