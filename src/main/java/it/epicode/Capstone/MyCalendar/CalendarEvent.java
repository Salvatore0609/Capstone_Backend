package it.epicode.Capstone.MyCalendar;


import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import it.epicode.Capstone.login.utenti.Utente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CalenderEvent")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;
    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private boolean reminderSent = false;

    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente utente;

    @ManyToOne
    @JoinColumn(name = "utente_google_id")
    private UtenteGoogle utenteGoogle;
}
