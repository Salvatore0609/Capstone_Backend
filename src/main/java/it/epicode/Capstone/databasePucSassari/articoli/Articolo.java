package it.epicode.Capstone.databasePucSassari.articoli;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import it.epicode.Capstone.databasePucSassari.sezioni.Sezione;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "articoli")
@Data
@NoArgsConstructor
public class Articolo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long artId;

    @Column(nullable = true)
    private String titolo;

    @OneToMany(mappedBy = "articolo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Sezione> sezioni;

}
