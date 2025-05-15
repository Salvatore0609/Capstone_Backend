package it.epicode.Capstone.login.utenti.MyProject;

import jakarta.persistence.Table;
import org.hibernate.annotations.TypeDef;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Type;
import it.epicode.Capstone.login.utenti.Utente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@TypeDef(name = "json", typeClass = JsonStringType.class)
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

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private Map<String, String> phases;

    @ManyToOne
    @JoinColumn(name = "utente_id")
    private Utente proprietario;
}
/*
Per poter usare questi annotazioni Hibernate per JSONB devo:
Aggiungere la dipendenza hibernate-types-52:

<dependency>
  <groupId>com.vladmihalcea</groupId>
  <artifactId>hibernate-types-60</artifactId>
  <version>2.21.1</version>
</dependency>

Mettere a livello di classe entità una definizione di tipo:

@TypeDef(name = "json", typeClass = JsonStringType.class)
@Type(type = "json")
@Column(columnDefinition = "jsonb")

POST /projects con body JSON → salva in DB, phases inizialmente vuoto.

DB tiene phases come JSONB.

GET /projects restituisce array di oggetti JS, phases già deserializzato.

Frontend legge phases da Redux e lo usa direttamente per popolare la UI delle fasi e delle task.

Così non serve alcuna logica extra:
JPA/Hibernate + Jackson si occupano della serializzazione,
e il tuo Redux store contiene già oggetti JS pronti per essere renderizzati.

Questo è per far si che l'array di stringhe delle fasi con le loro task sia letto e salvato come JSONB.
*/