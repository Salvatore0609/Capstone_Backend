package it.epicode.Capstone.databasePucSassari.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.epicode.Capstone.databasePucSassari.articoli.ArticoloRequest;
import it.epicode.Capstone.databasePucSassari.articoli.ArticoloService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoaderPuc implements CommandLineRunner {

    private final ArticoloService articoloService;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        File jsonFile = new File("src/main/resources/puc_sassari_data.json");

        if (!jsonFile.exists()) {
            log.warn("⚠ File JSON non trovato: {}", jsonFile.getAbsolutePath());
            return;
        }

        try {
            List<ArticoloRequest> articoli = objectMapper.readValue(jsonFile, new TypeReference<>() {});
            for (ArticoloRequest request : articoli) {
                if (request.getTitolo() == null || request.getTitolo().isBlank()) {
                    log.warn(" Titolo mancante, articolo ignorato.");
                    continue;
                }

                articoloService.save(request);
                /*log.info("✔ Articolo salvato: {}", request.getTitolo());*/
            }

        } catch (Exception e) {
            log.error(" Errore durante il caricamento dei dati: {}", e.getMessage(), e);
        }
    }
}

