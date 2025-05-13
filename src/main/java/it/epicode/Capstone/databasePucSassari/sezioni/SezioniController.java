package it.epicode.Capstone.databasePucSassari.sezioni;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sezioni")
@RequiredArgsConstructor
public class SezioniController {

    private final SezioniService sezioniService;

    // Ottieni tutte le sezioni in formato DTO
    @GetMapping
    public ResponseEntity<List<SezioneResponse>> getAllSezioni() {
        return ResponseEntity.ok(sezioniService.getAllSezioni());
    }
}