package it.epicode.Capstone.login.utenti.MyCalendar;

import it.epicode.Capstone.login.auth.JwtTokenUtil;
import it.epicode.Capstone.login.authGoogle.UtenteGoogle;
import it.epicode.Capstone.login.authGoogle.UtenteGoogleRepository;
import it.epicode.Capstone.login.utenti.Utente;
import it.epicode.Capstone.login.utenti.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final UtenteRepository utenteRepository;
    private final UtenteGoogleRepository utenteGoogleRepository;
    private final JwtTokenUtil jwtTokenUtil;

    // Creazione evento
    public CalendarEvent createEvent(CalendarEvent event, String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token non può essere vuoto");
        }

        String username = jwtTokenUtil.getUsernameFromToken(token);
        Optional<Utente> utenteOpt = utenteRepository.findByUsername(username);

        if (utenteOpt.isPresent()) {
            event.setUtente(utenteOpt.get());
        } else {
            UtenteGoogle utenteGoogle = utenteGoogleRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Utente Google non trovato"));
            event.setUtenteGoogle(utenteGoogle);
        }

        return calendarEventRepository.save(event);
    }

    // Recupera gli eventi per l'utente loggato
    public List<CalendarEvent> findEvents(String token) {
        String email = jwtTokenUtil.getUsernameFromToken(token);

        List<CalendarEvent> events = utenteRepository.findByUsername(email)
                .map(calendarEventRepository::findByUtente)
                .orElseGet(() -> {
                    UtenteGoogle utenteGoogle = utenteGoogleRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("Utente Google non trovato"));
                    return calendarEventRepository.findByUtenteGoogle(utenteGoogle);
                });

        if (events.isEmpty()) {
            return new ArrayList<>();
        }

        return events;
    }

    // Modifica un evento esistente
    public CalendarEvent updateEvent(Long id, CalendarEvent event, String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token);

        CalendarEvent existingEvent = calendarEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        Optional<Utente> utenteOpt = utenteRepository.findByUsername(username);

        if (utenteOpt.isPresent()) {
            if (!existingEvent.getUtente().equals(utenteOpt.get())) {
                throw new RuntimeException("Non puoi modificare un evento che non ti appartiene");
            }
        } else {
            UtenteGoogle utenteGoogle = utenteGoogleRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Utente Google non trovato"));

            if (!existingEvent.getUtenteGoogle().equals(utenteGoogle)) {
                throw new RuntimeException("Non puoi modificare un evento che non ti appartiene");
            }
        }

        existingEvent.setTitle(event.getTitle());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setStartTime(event.getStartTime());
        existingEvent.setEndTime(event.getEndTime());

        return calendarEventRepository.save(existingEvent);
    }

    public void deleteEvent(Long eventId, String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token);

        CalendarEvent existingEvent = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        Optional<Utente> utenteOpt = utenteRepository.findByUsername(username);

        if (utenteOpt.isPresent()) {
            if (!existingEvent.getUtente().equals(utenteOpt.get())) {
                throw new RuntimeException("Non puoi eliminare un evento che non ti appartiene");
            }
        } else {
            UtenteGoogle utenteGoogle = utenteGoogleRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Utente Google non trovato"));

            if (!existingEvent.getUtenteGoogle().equals(utenteGoogle)) {
                throw new RuntimeException("Non puoi eliminare un evento che non ti appartiene");
            }
        }

        calendarEventRepository.deleteById(eventId);
    }
}
