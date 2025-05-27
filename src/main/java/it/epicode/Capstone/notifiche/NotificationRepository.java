package it.epicode.Capstone.notifiche;

import it.epicode.Capstone.login.utenti.Utente;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUtenteOrderByCreatedAtDesc(Utente utente);
    List<Notification> findByUtenteGoogleOrderByCreatedAtDesc(UtenteGoogle utenteGoogle);
    List<Notification> findByUtenteAndIsReadFalseOrderByCreatedAtDesc(Utente utente);
    List<Notification> findByUtenteGoogleAndIsReadFalseOrderByCreatedAtDesc(UtenteGoogle utenteGoogle);
}