package it.epicode.Capstone.notifiche;

import it.epicode.Capstone.common.WebSocketService;
import it.epicode.Capstone.auth.JwtTokenUtil;
import it.epicode.Capstone.MyCalendar.CalendarEvent;
import it.epicode.Capstone.login.utenti.Utente;
import it.epicode.Capstone.login.utenti.UtenteRepository;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UtenteRepository utenteRepository;
    private final UtenteGoogleRepository utenteGoogleRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final WebSocketService webSocketService;

    /*Crea una notifica di tipo EVENT_REMINDER basata su CalendarEvent.
    Rimane identico a prima (interno, chiamato dallo scheduler).*/
    @Transactional
    public void createEventReminder(CalendarEvent event) {
        Notification notification = new Notification();
        notification.setMessage("Ricordati: " + event.getTitle() + " inizia a " + event.getStartTime());
        notification.setType(NotificationType.EVENT_REMINDER);

        if (event.getUtente() != null) {
            notification.setUtente(event.getUtente());
        } else {
            notification.setUtenteGoogle(event.getUtenteGoogle());
        }

        notificationRepository.save(notification);
        sendRealTimeNotification(notification);
    }

    private void sendRealTimeNotification(Notification notification) {
        String recipient = notification.getUtente() != null
                ? notification.getUtente().getUsername()
                : notification.getUtenteGoogle().getEmail();

        // creo il payload ridotto
        NotificationPayload payload = new NotificationPayload(
                notification.getId(),
                notification.getMessage(),
                notification.getType(),
                notification.getCreatedAt()
        );
        webSocketService.sendToUser(recipient, payload);
    }

    /*Crea una notifica “manuale” partendo da NotificationRequest. Il client invia:
      - message
     - type
     - recipientIdentifier (username oppure email di UtenteGoogle)
     */

    @Transactional
    public NotificationResponse createNotification(NotificationRequest requestDTO, String token) {
        // (opzionale) verificare il token: qui assumiamo che solo l’admin possa creare manualmente.
        String creatorUsername = jwtTokenUtil.getUsernameFromToken(token);
        // --- TROVA IL RECIPIENT ---
        Optional<Utente> utenteOpt = utenteRepository.findByUsername(requestDTO.getRecipientIdentifier());
        Notification notification = new Notification();
        notification.setMessage(requestDTO.getMessage());
        notification.setType(requestDTO.getType());

        if (utenteOpt.isPresent()) {
            notification.setUtente(utenteOpt.get());
        } else {
            UtenteGoogle ug = utenteGoogleRepository
                    .findByEmail(requestDTO.getRecipientIdentifier())
                    .orElseThrow(() -> new RuntimeException("Recipient non trovato"));
            notification.setUtenteGoogle(ug);
        }

        notificationRepository.save(notification);

        // mappo l’entità su ResponseDTO
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getType(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getUtente() != null
                        ? notification.getUtente().getUsername()
                        : notification.getUtenteGoogle().getEmail()
        );
    }

    /*Restituisce tutte le notifiche dell’utente autenticato, già ordinate per createdAt desc.*/
    @Transactional
    public List<NotificationResponse> getUserNotifications(String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token);

        List<Notification> notifications = utenteRepository.findByUsername(username)
                .map(notificationRepository::findByUtenteOrderByCreatedAtDesc)
                .orElseGet(() -> utenteGoogleRepository.findByEmail(username)
                        .map(notificationRepository::findByUtenteGoogleOrderByCreatedAtDesc)
                        .orElseThrow(() -> new RuntimeException("User non trovato")));

        return notifications.stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getMessage(),
                        n.getType(),
                        n.getIsRead(),
                        n.getCreatedAt(),
                        n.getUtente() != null
                                ? n.getUtente().getUsername()
                                : n.getUtenteGoogle().getEmail()
                ))
                .collect(Collectors.toList());
    }

    /*Restituisce solo le notifiche non lette, ordinate per createdAt desc.*/
    @Transactional
    public List<NotificationResponse> getUnreadNotifications(String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token);

        List<Notification> notifications = utenteRepository.findByUsername(username)
                .map(notificationRepository::findByUtenteAndIsReadFalseOrderByCreatedAtDesc)
                .orElseGet(() -> utenteGoogleRepository.findByEmail(username)
                        .map(notificationRepository::findByUtenteGoogleAndIsReadFalseOrderByCreatedAtDesc)
                        .orElseThrow(() -> new RuntimeException("User non trovato")));

        return notifications.stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getMessage(),
                        n.getType(),
                        n.getIsRead(),
                        n.getCreatedAt(),
                        n.getUtente() != null
                                ? n.getUtente().getUsername()
                                : n.getUtenteGoogle().getEmail()
                ))
                .collect(Collectors.toList());
    }

    /* Marca una notifica come letta (isRead = true), solo se appartiene all’utente.*/
    @Transactional
    public void markAsRead(Long id, String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification non trovata"));

        boolean belongsToUser = (notification.getUtente() != null &&
                notification.getUtente().getUsername().equals(username))
                || (notification.getUtenteGoogle() != null &&
                notification.getUtenteGoogle().getEmail().equals(username));

        if (!belongsToUser) {
            throw new AccessDeniedException("Non puoi modificare questa notifica");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}

