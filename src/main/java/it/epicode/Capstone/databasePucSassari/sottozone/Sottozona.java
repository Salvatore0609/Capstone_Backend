package it.epicode.Capstone.databasePucSassari.sottozone;

import com.fasterxml.jackson.annotation.JsonBackReference;
import it.epicode.Capstone.databasePucSassari.sezioni.Sezione;
import it.epicode.Capstone.databasePucSassari.sottozone.parametri.Parametro;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Table(name = "sottozone")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sottozona {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sottozone_seq")
    @SequenceGenerator(name = "sottozone_seq", sequenceName = "sottozone_sequence", allocationSize = 1)
    private Long sottozId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String descrizione;

    @ElementCollection
    @CollectionTable(name = "sottozone_parametri", joinColumns = @JoinColumn(name = "sottozone_id"))
    private List<Parametro> parametri;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String note;

    @ManyToOne
    @JoinColumn(name = "sez_id", nullable = true)  // relazione con Sezione
    @JsonBackReference
    private Sezione sezione; // relazione con Sezione

}
