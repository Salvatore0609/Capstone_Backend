package it.epicode.Capstone.databasePucSassari.articoli;

import it.epicode.Capstone.databasePucSassari.sezioni.Sezione;
import it.epicode.Capstone.databasePucSassari.sottozone.Sottozona;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticoloService {

    // Unico repository per Articolo
    private final ArticoloRepository articoloRepository;
    private final ArticoloMapper articoloMapper;

    public Articolo getById(Long id) {
        return articoloRepository.findByIdWithSezioni(id)
                .orElseThrow(() -> new RuntimeException("Articolo non trovato con ID: " + id));
    }

    //Recupera tutti gli articoli (con tutte le loro sezioni, sottozone, parametri, usiPermessi, ecc.).
    public List<ArticoloResponse> findAllArticles() {
        // 1) Ottieni tutti gli Articolo (senza fetch delle sezioni)
        List<Articolo> articoli = articoloRepository.findAllArticoli();

        // 2) Carica tutte le sezioni per ogni articolo
        List<Sezione> sezioni = articoloRepository.findSezioniByArticoli(articoli);

        // 3) Carica tutte le sottozone per ogni sezione
        List<Sottozona> sottozone = articoloRepository.findSottozoneBySezioni(sezioni);

        // 4) Forza il caricamento delle relazioni UsiPermessi per ciascuna sezione
        articoloRepository.findSezioneWithUsiPermessi(sezioni);

        // 5) Forza il caricamento dei Parametri per ciascuna sottozona
        articoloRepository.findSottozoneWithParametri(sottozone);

        // 6) Mappa ciascun Articolo in ArticoloResponse
        return articoli.stream()
                .map(articoloMapper::toResponse)
                .collect(Collectors.toList());
    }


    //Recupera un singolo articolo (identificato da id) con tutte le sue sezioni, sottozone, parametri, usiPermessi, ecc.
    @Transactional
    public Optional<ArticoloResponse> findArticleById(Long id) {
        // 1) Carica l’entità Articolo con fetch delle sezioni
        Optional<Articolo> optArticolo = articoloRepository.findByIdWithSezioni(id);
        if (optArticolo.isEmpty()) {
            return Optional.empty();
        }

        Articolo articolo = optArticolo.get();

        // 2) Prendi la lista di Sezione collegate all’Articolo
        List<Sezione> sezioni = articolo.getSezioni();
        if (sezioni == null) {
            sezioni = new ArrayList<>();
        }

        // 3) Carica tutte le sottozone relative a quelle sezioni
        List<Sottozona> sottozone = articoloRepository.findSottozoneBySezioni(sezioni);

        // 4) Carica gli UsiPermessi per ciascuna sezione
        articoloRepository.findSezioneWithUsiPermessi(sezioni);

        // 5) Carica i Parametri per ciascuna sottozona
        articoloRepository.findSottozoneWithParametri(sottozone);

        // 6) Mappa il dominio Articolo → ArticoloResponse (con tutto il contenuto)
        ArticoloResponse response = articoloMapper.toResponse(articolo);
        return Optional.of(response);
    }

    @Transactional
    public Articolo saveArticoloCompleto(Articolo articolo) {
        // 1. Assicurati che tutte le relazioni siano gestite correttamente
        if (articolo.getSezioni() != null) {
            articolo.getSezioni().forEach(sezione -> {
                sezione.setArticolo(articolo); // Imposta la relazione inversa
                // Gestisci sottozone e usi permessi
                if (sezione.getSottozone() != null) {
                    sezione.getSottozone().forEach(sottozona -> {
                        sottozona.setSezione(sezione);
                        if (sottozona.getParametri() != null) {
                            sottozona.getParametri().forEach(p -> p.setSottozona(sottozona));
                        }
                    });
                }
                if (sezione.getUsiPermessi() != null) {
                    sezione.getUsiPermessi().forEach(u -> u.setSezione(sezione));
                }
            });
        }

        // 2. Salva l'articolo (le cascade propagheranno il salvataggio)
        return articoloRepository.save(articolo);
    }

     //Salva un nuovo articolo (o ne aggiorna uno esistente, se l'ID è valorizzato).
    @Transactional
    public void save(ArticoloRequest request) {
        Articolo articolo = articoloMapper.toEntity(request);
        articoloRepository.save(articolo);
    }
}
