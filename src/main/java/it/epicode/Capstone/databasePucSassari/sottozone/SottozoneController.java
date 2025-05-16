package it.epicode.Capstone.databasePucSassari.sottozone;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sottozone")
@RequiredArgsConstructor
public class SottozoneController {

    private final SottozoneService sottozoneService;

    // Ottieni tutte le sottozone
    @GetMapping
    public ResponseEntity<List<SottozoneResponse>> getAllSottozone() {
        return ResponseEntity.ok(sottozoneService.findAllSottozone());
    }
    @GetMapping("/{nome}")
    public ResponseEntity<SottozoneResponse> getSottozoneByNome(String nome) {
        return ResponseEntity.ok(sottozoneService.findByNome(nome));
    }
}