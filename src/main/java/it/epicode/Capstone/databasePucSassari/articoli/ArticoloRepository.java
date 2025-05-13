package it.epicode.Capstone.databasePucSassari.articoli;

import it.epicode.Capstone.databasePucSassari.sezioni.Sezione;
import it.epicode.Capstone.databasePucSassari.sottozone.Sottozona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticoloRepository extends JpaRepository<Articolo, Long> {


    // Query per ottenere gli Articoli
    @Query("SELECT a FROM Articolo a")
    List<Articolo> findAllArticoli();

    // Query per ottenere tutte le Sezioni per ogni Articolo
    @Query("SELECT s FROM Sezione s JOIN FETCH s.articolo a WHERE a IN :articoli")
    List<Sezione> findSezioniByArticoli(List<Articolo> articoli);

    // Query per ottenere tutte le Sottozone per ogni Sezione
    @Query("SELECT z FROM Sottozona z JOIN FETCH z.sezione s WHERE s IN :sezioni")
    List<Sottozona> findSottozoneBySezioni(List<Sezione> sezioni);


    // Query per ottenere tutte le Sezioni con gli Usi Permessi associati
    @Query("SELECT s FROM Sezione s LEFT JOIN FETCH s.usiPermessi u WHERE s IN :sezioni")
    List<Sezione> findSezioneWithUsiPermessi(List<Sezione> sezioni);

    // Query per ottenere tutte le Sottozone con Parametri associati
    @Query("SELECT z FROM Sottozona z LEFT JOIN FETCH z.parametri p WHERE z IN :sottozone")
    List<Sottozona> findSottozoneWithParametri(List<Sottozona> sottozone);

}