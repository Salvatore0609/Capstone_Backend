package it.epicode.Capstone.notifiche;

import it.epicode.Capstone.MyCalendar.CalendarEvent;
import it.epicode.Capstone.MyCalendar.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderScheduler {
    private final CalendarEventRepository eventRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000) // Esegue ogni minuto
    public void checkEventReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderTime = now.plusMinutes(15); // Imposta il reminder a 15 minuti prima dell'inizio dell'evento

            List<CalendarEvent> upcomingEvents = eventRepository
                    .findByStartTimeBetweenAndReminderSentFalse(now, reminderTime);

            upcomingEvents.forEach(event -> {
                try {
                    notificationService.createEventReminder(event);
                    event.setReminderSent(true);
                    eventRepository.save(event);
                } catch (Exception e) {
                    log.error("Errore nell'invio del reminder per l'evento {}", event.getId(), e);
                }
            });
        } catch (Exception e) {
            log.error("Errore nel reminder scheduler", e);
            throw new SchedulingException("Errore nel scheduler", e);
        }
    }
}
