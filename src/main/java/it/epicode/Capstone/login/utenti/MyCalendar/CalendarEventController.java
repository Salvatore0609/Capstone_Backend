package it.epicode.Capstone.login.utenti.MyCalendar;

import it.epicode.Capstone.login.auth.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/calendar-event")
@RequiredArgsConstructor
public class CalendarEventController {
    private final CalendarEventService calendarEventService;
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

    // Crea un evento
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CalendarEventRequest createEvent(
            @RequestBody CalendarEventRequest eventRequest,
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("Authorization Header: " + authHeader); // Debug

        String token = extractToken(authHeader);
        System.out.println("Token estratto: " + token); // Debug

        CalendarEvent event = mapToEntity(eventRequest);
        CalendarEvent savedEvent = calendarEventService.createEvent(event, token);
        return mapToDTO(savedEvent);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<CalendarEventRequest> getEvents(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        List<CalendarEvent> events = calendarEventService.findEvents(token);
        return events.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public CalendarEventRequest updateEvent(@PathVariable Long eventId,
                                            @RequestBody CalendarEventRequest eventRequest,
                                            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        CalendarEvent event = mapToEntity(eventRequest);
        CalendarEvent updatedEvent = calendarEventService.updateEvent(eventId, event, token);
        return mapToDTO(updatedEvent);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCalendarEvent(@PathVariable Long eventId,
                                    @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        calendarEventService.deleteEvent(eventId, token);
    }

    private CalendarEvent mapToEntity(CalendarEventRequest eventRequest) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());

        // Si assume che il front-end invii startTime ed endTime già composti in formato ISO,
        // ad esempio "2025-05-09T06:00:00".
        LocalDateTime startDateTime = LocalDateTime.parse(eventRequest.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.parse(eventRequest.getEndTime());

        event.setStartTime(startDateTime);
        event.setEndTime(endDateTime);

        // Non è più necessario impostare un campo 'date',
        // poiché la data è già inclusa in startTime ed endTime

        return event;
    }

    // Mappatura da Entity a DTO (rimuovendo il campo 'date')
    private CalendarEventRequest mapToDTO(CalendarEvent event) {
        CalendarEventRequest eventRequest = new CalendarEventRequest();
        eventRequest.setId(event.getId());
        eventRequest.setTitle(event.getTitle());
        eventRequest.setDescription(event.getDescription());
        eventRequest.setStartTime(event.getStartTime().toString());
        eventRequest.setEndTime(event.getEndTime().toString());

        if (event.getUtente() != null) {
            eventRequest.setUtenteId(event.getUtente().getId());
        }
        if (event.getUtenteGoogle() != null) {
            eventRequest.setUtenteGoogleId(event.getUtenteGoogle().getId());
        }

        return eventRequest;
    }
}
