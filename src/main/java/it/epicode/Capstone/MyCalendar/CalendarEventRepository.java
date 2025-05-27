package it.epicode.Capstone.MyCalendar;

import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import it.epicode.Capstone.login.utenti.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    @Query("SELECT e FROM CalendarEvent e WHERE e.startTime BETWEEN :start AND :end AND e.reminderSent = false")
    List<CalendarEvent> findByStartTimeBetweenAndReminderSentFalse(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<CalendarEvent> findByUtente(Utente utente);
    List<CalendarEvent> findByUtenteGoogle(UtenteGoogle utenteGoogle);
}