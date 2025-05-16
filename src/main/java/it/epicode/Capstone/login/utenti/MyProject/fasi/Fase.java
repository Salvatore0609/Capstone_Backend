package it.epicode.Capstone.login.utenti.MyProject.fasi;

import it.epicode.Capstone.login.utenti.MyProject.fasi.tasks.Task;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Entity
@Table(name = "fasi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fase {
    @Id
    @GeneratedValue
    private Long id;
    private String titolo;
    private int numero;

    @OneToMany(mappedBy = "fase")
    private List<Task> tasks;
}