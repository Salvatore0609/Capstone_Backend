package it.epicode.Capstone.databasePucSassari.articoli;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/articoli")
@RequiredArgsConstructor
public class ArticoloController {

    private final ArticoloService articoloService;
    private final ArticoloMapper articoloMapper;

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

    @GetMapping("/{id}")
    public ResponseEntity<ArticoloResponse> getArticoloById(@PathVariable Long id) {
        try {
            Optional<ArticoloResponse> response = articoloService.findArticleById(id);
            return response.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Errore nel recupero articolo ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    //salvare un articolo e tutto il suo contenuto
    @PostMapping
    public ResponseEntity<ArticoloResponse> createArticolo(@RequestBody ArticoloRequest articoloRequest) {
        try {
            Articolo articolo = articoloMapper.toEntity(articoloRequest);
            Articolo savedArticolo = articoloService.saveArticoloCompleto(articolo);
            ArticoloResponse response = articoloMapper.toResponse(savedArticolo);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Errore nella creazione dell'articolo", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}