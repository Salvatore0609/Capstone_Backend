package it.epicode.Capstone.login.utenti.MyProject;

import it.epicode.Capstone.login.authGoogle.UtenteGoogle;
import jakarta.persistence.Table;
import it.epicode.Capstone.login.utenti.Utente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column
    private String nomeProgetto;

    @Column
    private String progettista;

    @Column
    private String impresaCostruttrice;

    @Column
    private String indirizzo;

    @Column
    private Double lat;
    @Column
    private Double lng;


    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente proprietario;
    @ManyToOne
    @JoinColumn(name = "utente_google_id")
    private UtenteGoogle proprietarioGoogle;
}
