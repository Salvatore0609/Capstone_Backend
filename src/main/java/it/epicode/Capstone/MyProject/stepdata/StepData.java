package it.epicode.Capstone.MyProject.stepdata;

import it.epicode.Capstone.databasePucSassari.articoli.Articolo;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import it.epicode.Capstone.MyProject.Project;
import it.epicode.Capstone.MyProject.fasi.Fase;
import it.epicode.Capstone.MyProject.fasi.tasks.Task;
import it.epicode.Capstone.MyProject.fasi.tasks.steps.Step;
import it.epicode.Capstone.login.utenti.Utente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "stepsData")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepData {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Project progetto;

    @ManyToOne
    private Utente utente;
    @ManyToOne
    private UtenteGoogle utenteGoogle;

    @ManyToOne
    private Fase fase;

    @ManyToOne
    private Task task;

    @ManyToOne
    private Step step;

    @ManyToOne
    private Articolo articolo;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String articoloSnapshot;

    private String fileName;
    private String fileUrl;
    private String fileType;
    private Integer fileSize;

    private String textareaValue;
    private String dropdownSelected;
    private Boolean checkboxValue;

    //LA DATA DELL'ULTIMO AGGIORNAMENTO
    private LocalDateTime updatedAt;
}
