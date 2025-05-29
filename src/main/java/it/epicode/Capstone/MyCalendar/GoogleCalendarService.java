package it.epicode.Capstone.MyCalendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final UtenteGoogleRepository utenteGoogleRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    // Cerca di costruire un client Calendar per l'utente con email specificata.
    // Se l'utente non è presente in UtenteGoogleRepository, restituisce null.
    // Se il token di accesso è scaduto o sta per scadere, lo rinnova.
    private Calendar getCalendarClient(String email) throws IOException, GeneralSecurityException {
        // Recupera l'UtenteGoogle dal database
        Optional<UtenteGoogle> optionalUg = utenteGoogleRepository.findByEmail(email);
        if (optionalUg.isEmpty()) {
            // Utente non autenticato con Google: ritorna null
            return null;
        }
        UtenteGoogle ug = optionalUg.get();

        // Verifica se l'access token è scaduto o manca
        Instant now = Instant.now();
        if (ug.getTokenExpiry() == null || ug.getTokenExpiry().isBefore(now.plusSeconds(60))) {
            // Rinnova il token usando GoogleCredential
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setClientSecrets(clientId, clientSecret)
                    .setJsonFactory(JacksonFactory.getDefaultInstance())
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .build()
                    .setRefreshToken(ug.getRefreshToken());

            credential.refreshToken();
            String newAccessToken = credential.getAccessToken();
            Instant newExpiry = Instant.now().plusSeconds(credential.getExpiresInSeconds());

            // Salva i nuovi token nel database
            ug.setAccessToken(newAccessToken);
            ug.setTokenExpiry(newExpiry);
            utenteGoogleRepository.save(ug);
        }

        // Crea un GoogleCredential valido con l'access token aggiornato
        GoogleCredential validCredential = new GoogleCredential().setAccessToken(ug.getAccessToken());

        // Costruisce e restituisce il client Calendar
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                validCredential
        )
                .setApplicationName("CapstoneCalendarApp")
                .build();
    }

    // Ottiene gli eventi da Google Calendar (calendarId = "primary") tra timeMin e timeMax.
    // Se l'utente non è un UtenteGoogle, restituisce null.
    public List<Event> getGoogleEvents(String email, LocalDateTime timeMin, LocalDateTime timeMax)
            throws IOException, GeneralSecurityException {

        Calendar client = getCalendarClient(email);
        if (client == null) {
            // Utente non autenticato con Google
            return null;
        }

        DateTime min = new DateTime(timeMin.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        DateTime max = new DateTime(timeMax.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        Events events = client.events()
                .list("primary")
                .setTimeMin(min)
                .setTimeMax(max)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return events.getItems();
    }

    // Crea un nuovo evento su Google Calendar.
    // Se l'utente non è un UtenteGoogle, restituisce null.
    public Event createGoogleEvent(String email, CalendarEventRequest dto)
            throws IOException, GeneralSecurityException {

        Calendar client = getCalendarClient(email);
        if (client == null) {
            // Utente non autenticato con Google
            return null;
        }

        Event event = new Event();
        event.setSummary(dto.getTitle());
        event.setDescription(dto.getDescription());

        LocalDateTime startLocal = LocalDateTime.parse(dto.getStartTime());
        Instant startInstant = startLocal.atZone(ZoneId.of("Europe/Rome")).toInstant();
        DateTime startDateTime = new DateTime(startInstant.toEpochMilli());
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Europe/Rome");

        LocalDateTime endLocal = LocalDateTime.parse(dto.getEndTime());
        Instant endInstant = endLocal.atZone(ZoneId.of("Europe/Rome")).toInstant();
        DateTime endDateTime = new DateTime(endInstant.toEpochMilli());
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Europe/Rome");

        event.setStart(start);
        event.setEnd(end);

        return client.events().insert("primary", event).execute();
    }

    // Aggiorna un evento esistente su Google Calendar.
    // Se l'utente non è un UtenteGoogle, restituisce null.
    public Event updateGoogleEvent(String email, String googleEventId, CalendarEventRequest dto)
            throws IOException, GeneralSecurityException {

        Calendar client = getCalendarClient(email);
        if (client == null) {
            // Utente non autenticato con Google
            return null;
        }

        Event event = client.events().get("primary", googleEventId).execute();
        event.setSummary(dto.getTitle());
        event.setDescription(dto.getDescription());


        LocalDateTime startLocal = LocalDateTime.parse(dto.getStartTime());
        Instant startInstant = startLocal.atZone(ZoneId.of("Europe/Rome")).toInstant();
        DateTime startDateTime = new DateTime(startInstant.toEpochMilli());
        event.setStart(new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Europe/Rome"));

        LocalDateTime endLocal = LocalDateTime.parse(dto.getEndTime());
        Instant endInstant = endLocal.atZone(ZoneId.of("Europe/Rome")).toInstant();
        DateTime endDateTime = new DateTime(endInstant.toEpochMilli());
        event.setEnd(new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Europe/Rome"));

        return client.events().update("primary", googleEventId, event).execute();
    }

    // Elimina un evento da Google Calendar.
    // Se l'utente non è un UtenteGoogle, non fa nulla.
    public void deleteGoogleEvent(String email, String googleEventId)
            throws IOException, GeneralSecurityException {

        Calendar client = getCalendarClient(email);
        if (client == null) {
            // Utente non autenticato con Google
            return;
        }

        client.events().delete("primary", googleEventId).execute();
    }
}
