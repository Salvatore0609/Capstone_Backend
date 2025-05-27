package it.epicode.Capstone.MyProject.fasi.tasks;

import it.epicode.Capstone.MyProject.fasi.tasks.steps.Step;
import it.epicode.Capstone.MyProject.fasi.Fase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue
    private Long id;
    private String titolo;
    private String descrizione;

    @ManyToOne
    private Fase fase;

    @OneToMany(mappedBy = "task")
    private List<Step> steps;
}