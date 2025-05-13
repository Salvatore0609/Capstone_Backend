package it.epicode.Capstone.databasePucSassari.sezioni.usipermessi;

import it.epicode.Capstone.databasePucSassari.sezioni.Sezione;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "usiPermessi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsoPermesso {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String zona;
    private String macrocategorie;
    private String descrizione;

    @ElementCollection
    @CollectionTable(name = "usi_permessi_usi", joinColumns = @JoinColumn(name = "uso_permesso_id"))
    @Column(name = "uso")
    private List<String> usi;

    private String note;

    @ManyToOne
    @JoinColumn(name = "sez_id", nullable = true)  // Cambia da "sottoz_id" a "sez_id"
    private Sezione sezione;  // Modifica la relazione per legare all'entità Sezione
}