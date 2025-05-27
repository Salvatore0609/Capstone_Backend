package it.epicode.Capstone.notifiche;

import it.epicode.Capstone.auth.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenUtil jwtTokenUtil;

    private String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new IllegalArgumentException("Authorization header mancante");
        }
        if (!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Formato Authorization header non valido");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Token mancante dopo Bearer");
        }
        return token;
    }

    /*GET /notifications Restituisce tutte le notifiche dell’utente autenticato.*/
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        List<NotificationResponse> dtos = notificationService.getUserNotifications(token);
        return ResponseEntity.ok(dtos);
    }

    /*GET /notifications/unread Restituisce solo le notifiche non lette dell’utente autenticato.*/
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(
            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        List<NotificationResponse> dtos = notificationService.getUnreadNotifications(token);
        return ResponseEntity.ok(dtos);
    }

    /*PUT /notifications/{id}/read Marca come letta una notifica di proprietà dell’utente autenticato.*/
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        notificationService.markAsRead(id, token);
        return ResponseEntity.ok().build();
    }

    /*POST /notifications Endpoint opzionale per creare manualmente una notifica (usando NotificationRequest).*/
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @RequestBody NotificationRequest requestDTO,
            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        NotificationResponse created = notificationService.createNotification(requestDTO, token);
        return ResponseEntity.status(201).body(created);
    }
}
