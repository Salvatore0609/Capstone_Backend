package it.epicode.Capstone.databasePucSassari.sottozone;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SottozoneService {

    private final SottozoneRepository sRepo;

    // Recupera tutte le sottozone
    public List<SottozoneResponse> findAllSottozone() {
        return sRepo.findAll().stream()
                .map(this::mapToResponse)  // Puoi lasciare il mapping se vuoi mappare la risposta
                .toList();
    }
    // Recupera una sottozona per nome
    public SottozoneResponse findByNome(String nome) {
        Sottozona sottozona = sRepo.findByNome(nome);
        if (sottozona == null) {
            throw new RuntimeException("Sottozona non trovata");
        }
        return mapToResponse(sottozona);
    }

    // Mappa una Sottozona in SottozoneResponse DTO
    private SottozoneResponse mapToResponse(Sottozona sottozona) {
        SottozoneResponse response = new SottozoneResponse();
        response.setSottozId(sottozona.getSottozId());
        response.setNome(sottozona.getNome());
        response.setNote(sottozona.getNote());

        return response;  // Puoi rimuovere il mapping di parametri, categorie e usi permessi se non ti servono
    }
}