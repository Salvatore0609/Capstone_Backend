package it.epicode.Capstone.databasePucSassari.sezioni;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SezioniService {

    private final SezioniRepository sezioniRepository;

    // Ritorna tutte le sezioni
    public List<SezioneResponse> getAllSezioni() {
        return sezioniRepository.findAll().stream()
                .map(this::mapToSezioneResponse)
                .toList();
    }

    // Mapping entity -> response DTO
    private SezioneResponse mapToSezioneResponse(Sezione sezione) {
        SezioneResponse response = new SezioneResponse();
        response.setSezId(sezione.getSezId());
        response.setTitolo(sezione.getTitolo());
        response.setContenuto(sezione.getContenuto());
        // Qui puoi mappare anche sottozone o parametri, se necessario
        return response;
    }
}