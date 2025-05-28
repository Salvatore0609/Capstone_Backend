package it.epicode.Capstone.MyCalendar;

import it.epicode.Capstone.auth.JwtTokenUtil;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogleRepository;
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
    private final GoogleCalendarService googleCalendarService;
    private final UtenteGoogleRepository utenteGoogleRepository;
    private final JwtTokenUtil jwtTokenUtil;

    // Estrae il token dalla header Authorization
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

    // GET /calendar-event: restituisce eventi interni + Google Calendar
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<CalendarEventResponse> getAllEvents(@RequestHeader("Authorization") String authHeader) throws Exception {
        // Estraggo il token e ricavo l'email dell'utente
        String token = extractToken(authHeader);
        String email = jwtTokenUtil.getUsernameFromToken(token);

        // 1) Leggo gli eventi dal database interno
        List<CalendarEvent> internalEvents = calendarEventService.findEvents(token);
        List<CalendarEventResponse> result = internalEvents.stream()
                .map(this::mapToResponseInternal)
                .collect(Collectors.toList());

        // 2) Leggo gli eventi da Google Calendar (ora → +1 mese)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthLater = now.plusMonths(1);
        List<com.google.api.services.calendar.model.Event> googleEvents =
                googleCalendarService.getGoogleEvents(email, now, oneMonthLater);

        // Se non ci sono eventi Google (utente non Google), evito NullPointer
        if (googleEvents != null) {
            // 3) Converto gli eventi Google in CalendarEventResponse
            for (com.google.api.services.calendar.model.Event ge : googleEvents) {
                CalendarEventResponse dto = new CalendarEventResponse();
                dto.setId(null);  // non c’è ID interno
                dto.setTitle(ge.getSummary());
                dto.setDescription(ge.getDescription());
                dto.setStartTime(ge.getStart().getDateTime().toStringRfc3339());
                dto.setEndTime(ge.getEnd().getDateTime().toStringRfc3339());
                dto.setGoogleEventId(ge.getId());  // ID Google
                dto.setSource("google");            // per distinguerli
                result.add(dto);
            }
        }

        return result;
    }

    // POST /calendar-event?toGoogle={true|false}: crea evento interno e opzionalmente su Google
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CalendarEventResponse createEvent(
            @RequestBody CalendarEventRequest eventRequest,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(name = "toGoogle", defaultValue = "false") boolean toGoogle
    ) throws Exception {
        String token = extractToken(authHeader);

        // 1) Salvo l’evento nel database interno
        CalendarEvent savedInternal = calendarEventService.createEvent(mapToEntity(eventRequest), token);
        CalendarEventResponse resp = mapToResponseInternal(savedInternal);

        // 2) Se richiesto, creo anche su Google Calendar
        if (toGoogle) {
            String email = jwtTokenUtil.getUsernameFromToken(token);
            com.google.api.services.calendar.model.Event ge =
                    googleCalendarService.createGoogleEvent(email, eventRequest);
            if (ge != null) {
                resp.setGoogleEventId(ge.getId());
                resp.setSource("internal+google");
            }
        }

        return resp;
    }

    // PUT /calendar-event/{eventId}?updateGoogle={true|false}&googleEventId={id}
    @PutMapping("/{eventId}")
    public CalendarEventResponse updateEvent(
            @PathVariable Long eventId,
            @RequestBody CalendarEventRequest eventRequest,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(name = "updateGoogle", defaultValue = "false") boolean updateGoogle,
            @RequestParam(name = "googleEventId", required = false) String googleEventId
    ) throws Exception {
        String token = extractToken(authHeader);

        // 1) Aggiorno l’evento nel database interno
        CalendarEvent updatedInternal = calendarEventService.updateEvent(eventId, mapToEntity(eventRequest), token);
        CalendarEventResponse resp = mapToResponseInternal(updatedInternal);

        // 2) Se richiesto e ho ID Google, aggiorno anche su Google Calendar
        if (updateGoogle && googleEventId != null) {
            String email = jwtTokenUtil.getUsernameFromToken(token);
            com.google.api.services.calendar.model.Event ge =
                    googleCalendarService.updateGoogleEvent(email, googleEventId, eventRequest);
            if (ge != null) {
                resp.setGoogleEventId(ge.getId());
                resp.setSource("internal+google");
            }
        }

        return resp;
    }

    // DELETE /calendar-event/{eventId}?deleteGoogle={true|false}&googleEventId={id}
    @DeleteMapping("/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(
            @PathVariable Long eventId,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(name = "deleteGoogle", defaultValue = "false") boolean deleteGoogle,
            @RequestParam(name = "googleEventId", required = false) String googleEventId
    ) throws Exception {
        String token = extractToken(authHeader);

        // 1) Elimino l’evento dal database interno
        calendarEventService.deleteEvent(eventId, token);

        // 2) Se richiesto e ho ID Google, elimino anche da Google Calendar
        if (deleteGoogle && googleEventId != null) {
            String email = jwtTokenUtil.getUsernameFromToken(token);
            googleCalendarService.deleteGoogleEvent(email, googleEventId);
        }
    }

    // Converte DTO in entity per salvataggio interno
    private CalendarEvent mapToEntity(CalendarEventRequest req) {
        CalendarEvent evt = new CalendarEvent();
        evt.setTitle(req.getTitle());
        evt.setDescription(req.getDescription());
        evt.setStartTime(LocalDateTime.parse(req.getStartTime()));
        evt.setEndTime(LocalDateTime.parse(req.getEndTime()));
        return evt;
    }

    // Converte entity interna in DTO di risposta
    private CalendarEventResponse mapToResponseInternal(CalendarEvent evt) {
        CalendarEventResponse dto = new CalendarEventResponse();
        dto.setId(evt.getId());
        dto.setTitle(evt.getTitle());
        dto.setDescription(evt.getDescription());
        dto.setStartTime(evt.getStartTime().toString());
        dto.setEndTime(evt.getEndTime().toString());
        dto.setSource("internal");
        return dto;
    }
}
