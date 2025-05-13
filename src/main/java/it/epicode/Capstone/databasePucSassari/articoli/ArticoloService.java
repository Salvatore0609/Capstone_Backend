package it.epicode.Capstone.databasePucSassari.articoli;

import it.epicode.Capstone.databasePucSassari.sezioni.Sezione;
import it.epicode.Capstone.databasePucSassari.sottozone.Sottozona;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticoloService {

    private final ArticoloRepository aRepo;
    private final ArticoloMapper articoloMapper;

    public List<ArticoloResponse> findAllArticles() {
        // Ottieni tutti gli articoli
        List<Articolo> articoli = aRepo.findAllArticoli();

        // Carica tutte le sezioni per ogni articolo
        List<Sezione> sezioni = aRepo.findSezioniByArticoli(articoli);

        // Carica tutte le sottozone per ogni sezione
        List<Sottozona> sottozone = aRepo.findSottozoneBySezioni(sezioni);

        // Forza il caricamento delle relazioni per evitare LazyInitializationException
        aRepo.findSezioneWithUsiPermessi(sezioni); // Carica usi permessi per ogni sezione
        aRepo.findSottozoneWithParametri(sottozone);  // Carica parametri per ogni sottozona

        // Ora i dati sono caricati e possiamo mappare gli articoli in risposta
        return articoli.stream()
                .map(articoloMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void save(ArticoloRequest request) {
        // Mappa la richiesta a un'entità Articolo e salvala
        Articolo articolo = articoloMapper.toEntity(request);
        aRepo.save(articolo);
    }
}