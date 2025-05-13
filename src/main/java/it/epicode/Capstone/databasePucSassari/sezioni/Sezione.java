package it.epicode.Capstone.databasePucSassari.sezioni;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import it.epicode.Capstone.databasePucSassari.articoli.Articolo;
import it.epicode.Capstone.databasePucSassari.sezioni.usipermessi.UsoPermesso;
import it.epicode.Capstone.databasePucSassari.sottozone.Sottozona;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "sezioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sezione {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "section_seq")
    @SequenceGenerator(name = "section_seq", sequenceName = "section_sequence", allocationSize = 1)
    private Long sezId;

    @Column(nullable = false)
    private String titolo;

    @ElementCollection
    @CollectionTable(name = "section_contenuti", joinColumns = @JoinColumn(name = "sezione_id"))
    @Column(columnDefinition = "TEXT")
    private List<String> contenuto;

    @ManyToOne
    @JoinColumn(name = "art_id")
    @JsonBackReference
    private Articolo articolo;

    @OneToMany(mappedBy = "sezione", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Sottozona> sottozone;

    @ElementCollection
    @CollectionTable(name = "section_categorie", joinColumns = @JoinColumn(name = "sezione_id"))
    @Column(name = "categorie")
    private List<String> categorie;

    @OneToMany(mappedBy = "sezione", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<UsoPermesso> usiPermessi;  // Aggiungi la relazione verso UsoPermesso

    @ElementCollection
    @CollectionTable(name = "section_parametri_urbanistici", joinColumns = @JoinColumn(name = "sezione_id"))
    @Column(name = "parametro")
    private List<String> parametriUrbanistici;
}