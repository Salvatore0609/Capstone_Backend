package it.epicode.Capstone.notifiche;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {

    @NotBlank
    private String message;
    @NotNull
    private NotificationType type;

    /*Identifica il destinatario:username di Utente oppure email di UtenteGoogle*/
    @NotBlank
    private String recipientIdentifier;
}