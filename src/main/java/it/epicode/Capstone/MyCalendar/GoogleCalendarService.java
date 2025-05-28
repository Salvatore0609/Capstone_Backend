package it.epicode.Capstone.MyCalendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;            // <<< Import corretto per gli eventi Google
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;     // <<< Import corretto di @Value
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final UtenteGoogleRepository utenteGoogleRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    /**
     * Costruisce un client Calendar pronto all'uso per l'utente con email specificata.
     * Se l'accessToken è scaduto o sta per scadere, lo rinnova usando il refreshToken.
     */
    private Calendar getCalendarClient(String email) throws IOException, GeneralSecurityException {
        // 1) Recupera l'UtenteGoogle dal DB (così abbiamo accessToken/refreshToken)
        UtenteGoogle ug = utenteGoogleRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("UtenteGoogle non trovato: " + email));

        // 2) Controllo se l'accessToken è in scadenza (entro 60 secondi)
        Instant now = Instant.now();
        if (ug.getTokenExpiry() == null || ug.getTokenExpiry().isBefore(now.plusSeconds(60))) {
            // 2.1) Rinnovo l'accessToken tramite GoogleCredential
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setClientSecrets(clientId, clientSecret)
                    .setJsonFactory(JacksonFactory.getDefaultInstance())
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .build()
                    .setRefreshToken(ug.getRefreshToken());

            credential.refreshToken();
            String newAccessToken = credential.getAccessToken();
            Instant newExpiry = Instant.now().plusSeconds(credential.getExpiresInSeconds());

            // 2.2) Salvo i nuovi token su DB
            ug.setAccessToken(newAccessToken);
            ug.setTokenExpiry(newExpiry);
            utenteGoogleRepository.save(ug);
        }

        // 3) Creo un GoogleCredential solo con accessToken valido (per l'HTTP initializer)
        GoogleCredential validCredential = new GoogleCredential().setAccessToken(ug.getAccessToken());

        // 4) Costruisco e restituisco il client Calendar
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                validCredential
        )
                .setApplicationName("CapstoneCalendarApp")
                .build();
    }

    /**
     * Ottiene gli eventi da Google Calendar (calendarId = "primary") tra timeMin e timeMax.
     * @param email       email dell'UtenteGoogle
     * @param timeMin     data/ora di inizio (LocalDateTime)
     * @param timeMax     data/ora di fine (LocalDateTime)
     * @return            lista di Event (Google)
     */
    public List<Event> getGoogleEvents(String email, LocalDateTime timeMin, LocalDateTime timeMax)
            throws IOException, GeneralSecurityException {
        Calendar client = getCalendarClient(email);

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

    /**
     * Crea un nuovo evento su Google Calendar.
     * @param email       email dell'UtenteGoogle
     * @param dto         oggetto CalendarEventRequest con title, description, startTime, endTime
     * @return            l'Evento creato (Google)
     */
    public Event createGoogleEvent(String email, CalendarEventRequest dto)
            throws IOException, GeneralSecurityException {
        Calendar client = getCalendarClient(email);

        Event event = new Event();  // <<< Uso dell'Event corretto da Google API
        event.setSummary(dto.getTitle());
        event.setDescription(dto.getDescription());

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(dto.getStartTime()))
                .setTimeZone(ZoneId.systemDefault().toString());
        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(dto.getEndTime()))
                .setTimeZone(ZoneId.systemDefault().toString());

        event.setStart(start);
        event.setEnd(end);

        return client.events().insert("primary", event).execute();
    }

    /**
     * Aggiorna un evento esistente su Google Calendar.
     * @param email          email dell'UtenteGoogle
     * @param googleEventId  l'ID dell'evento su Google Calendar
     * @param dto            oggetto CalendarEventRequest con dati aggiornati
     * @return               l'Evento aggiornato (Google)
     */
    public Event updateGoogleEvent(String email, String googleEventId, CalendarEventRequest dto)
            throws IOException, GeneralSecurityException {
        Calendar client = getCalendarClient(email);

        Event event = client.events().get("primary", googleEventId).execute();  // <<< Event di Google
        event.setSummary(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStart(new EventDateTime()
                .setDateTime(new DateTime(dto.getStartTime()))
                .setTimeZone(ZoneId.systemDefault().toString()));
        event.setEnd(new EventDateTime()
                .setDateTime(new DateTime(dto.getEndTime()))
                .setTimeZone(ZoneId.systemDefault().toString()));

        return client.events().update("primary", googleEventId, event).execute();
    }

    /**
     * Elimina un evento da Google Calendar.
     * @param email          email dell'UtenteGoogle
     * @param googleEventId  l'ID dell'evento su Google Calendar
     */
    public void deleteGoogleEvent(String email, String googleEventId)
            throws IOException, GeneralSecurityException {
        Calendar client = getCalendarClient(email);
        client.events().delete("primary", googleEventId).execute();
    }
}
