package it.epicode.Capstone.login.utenti.MyCalendar;

import it.epicode.Capstone.login.authGoogle.UtenteGoogle;
import it.epicode.Capstone.login.utenti.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUtente(Utente utente);
    List<CalendarEvent> findByUtenteGoogle(UtenteGoogle utenteGoogle);
}