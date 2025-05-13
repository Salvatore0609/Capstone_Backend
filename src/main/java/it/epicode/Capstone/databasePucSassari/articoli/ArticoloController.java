package it.epicode.Capstone.databasePucSassari.articoli;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/articoli")
@RequiredArgsConstructor
public class ArticoloController {

    private final ArticoloService articoloService;

    // Ottieni tutti gli articoli (con tutte le loro sezioni, sottozone, parametri, etc.)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ArticoloResponse>> getAllArticoli() {
        try {
            List<ArticoloResponse> articoli = articoloService.findAllArticles();

            // Verifica se la lista è vuota
            if (articoli.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204 No Content
            }

            return ResponseEntity.ok(articoli); // 200 OK con la lista
        } catch (Exception e) {
            // Log degli errori
            log.error("Errore nel recupero degli articoli", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }
}