package it.epicode.Capstone.login.authGoogle;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.util.Map;

@RestController
@RequestMapping("/utente")
public class AuthController {

    @GetMapping("/login/google")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Utente non autenticato");
        }

        return ResponseEntity.ok(Map.of(
                "email", principal.getAttribute("email"),
                "name", principal.getAttribute("name"),
                "avatar", principal.getAttribute("picture"),
                "attributes", principal.getAttributes()
        ));


    }

}

