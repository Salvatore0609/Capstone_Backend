package it.epicode.Capstone.login.utenti;

/*import com.google.api.client.json.gson.GsonFactory;*/

import it.epicode.Capstone.login.auth.AuthResponse;
import it.epicode.Capstone.login.auth.LoginRequest;
import it.epicode.Capstone.login.common.CommonResponse;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/utenti")
public class UtenteController {
    @Autowired
    private UtenteService utenteService;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<Utente> getAllUtenti() {return utenteService.getAllUtenti();}

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Utente findById(@PathVariable Long id) {
        return utenteService.getUtenteById(id);
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current-user")
    @Transactional
    public UtenteResponse getCurrentUser(@AuthenticationPrincipal Utente utente) {
        return utenteService.mapToResponse(utente);
    }


    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UtenteResponse> createUtente(
            @RequestPart("request") UtenteAuthRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file)
            throws MessagingException {

        UtenteResponse response = utenteService.registerUtente(request, file);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request ricevuta per username: " + loginRequest.getUsername());

        String token = utenteService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Login fallito"));
        }

        return ResponseEntity.ok(new AuthResponse(token));
    }


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UtenteResponse updateUtente(
            @PathVariable Long id,
            @ModelAttribute UtenteRequest request,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile,
            @AuthenticationPrincipal Utente utenteCorrente
    ) {
        // Se arriva un file avatar, lo aggiungo alla request
        if (avatarFile != null && !avatarFile.isEmpty()) {
            request.setAvatarFile(avatarFile);
        }
        return utenteService.updateCurrentUser(id, request, utenteCorrente);
    }


    //aggiunte
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUtente(@PathVariable Long id) {
        utenteService.deleteUtente(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/exists/{username}")
    @ResponseStatus(HttpStatus.OK)
    public void existsByUsername(@PathVariable String username) {
        utenteService.existsByUsername(username);
    }
}
