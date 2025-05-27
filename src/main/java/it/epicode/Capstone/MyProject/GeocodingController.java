package it.epicode.Capstone.MyProject;


import it.epicode.Capstone.login.exceptions.NotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/geocode")
@RequiredArgsConstructor
public class GeocodingController {
    private final GeocodingService geocodingService;


    @GetMapping
    public ResponseEntity<?> geocodeAddress(
            @RequestParam @NotBlank @Size(min = 20) String address // Validazione lunghezza
    ) {
        try {
            if (address.length() < 20) {
                throw new IllegalArgumentException("Inserire almeno 5 caratteri");
            }

            GeocodingService.Coordinates coordinates = geocodingService.geocodeAddress(address);
            return ResponseEntity.ok(coordinates);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", "Indirizzo non trovato")
            );
        }
    }
}
