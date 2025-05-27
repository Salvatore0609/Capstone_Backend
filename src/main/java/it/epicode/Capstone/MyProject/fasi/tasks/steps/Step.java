package it.epicode.Capstone.MyProject.fasi.tasks.steps;

import it.epicode.Capstone.MyProject.fasi.tasks.Task;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step {
    @Id
    @GeneratedValue
    private Long id;
    private String label;
    private String type; // es. file, text, select
    private String placeholder;
    private String accept;

    @ManyToOne
    private Task task;
}