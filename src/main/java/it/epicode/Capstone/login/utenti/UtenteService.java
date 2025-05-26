package it.epicode.Capstone.login.utenti;

import it.epicode.Capstone.cloudinary.CloudinaryService;
import it.epicode.Capstone.login.auth.JwtTokenUtil;
import it.epicode.Capstone.login.auth.Role;
import it.epicode.Capstone.login.common.CommonResponse;
import it.epicode.Capstone.login.common.EmailSenderService;
import it.epicode.Capstone.login.exceptions.NotFoundException;
import it.epicode.Capstone.login.exceptions.UsernameException;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Validated
public class UtenteService {
    @Autowired
    private UtenteRepository utenteRepository;
   @Autowired
    private CloudinaryService cloudinaryService;
   @Autowired
    private EmailSenderService emailSenderService;
   @Autowired
    private PasswordEncoder passwordEncoder;
   @Autowired
    private AuthenticationManager authenticationManager;
   @Autowired
    private JwtTokenUtil jwtTokenUtil;


    public UtenteResponse registerUtente(UtenteAuthRequest request, MultipartFile file) throws MessagingException {
        // Verifica username esistente
        if (utenteRepository.existsByUsername(request.getUsername())) {
            throw new EntityExistsException("Username già in uso");
        }
        // Crea nuovo utente e copia le proprietà
        Utente utente = new Utente();
        BeanUtils.copyProperties(request, utente);

        // Gestione avatar
        if (file != null && !file.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadImage(file);
            utente.setAvatar(avatarUrl);
        } else {
            utente.setAvatar("https://ui-avatars.com/api/?name=" +
                    URLEncoder.encode(utente.getNome() + "+" + utente.getCognome(), StandardCharsets.UTF_8));
        }
        // Codifica password e imposta ruoli
        utente.setPassword(passwordEncoder.encode(request.getPassword()));
        utente.setRoles(Set.of(Role.ROLE_USER));
        // Salva l'utente
        Utente savedUtente = utenteRepository.save(utente);
        // Invia email di benvenuto
        emailSenderService.sendEmail(
                savedUtente.getEmail(),
                "Benvenuto",
                "Ciao " + savedUtente.getNome() + " " + savedUtente.getCognome() + "! Benvenuto in Archiplanner!"
        );
        // Costruisci e restituisci la risposta completa
        return mapToResponse(savedUtente);
    }
    // Metodo helper per la conversione da Utente a UtenteResponse
    public UtenteResponse mapToResponse(Utente utente) {
        UtenteResponse response = new UtenteResponse();
        response.setId(utente.getId());
        response.setUsername(utente.getUsername());
        response.setEmail(utente.getEmail());
        response.setNome(utente.getNome());
        response.setCognome(utente.getCognome());
        response.setAvatar(utente.getAvatar());
        response.setDataNascita(utente.getDataNascita());
        response.setLuogoNascita(utente.getLuogoNascita());
        response.setResidenza(utente.getResidenza());
        response.setNomeCompagnia(utente.getNomeCompagnia());
        response.setLingua(utente.getLingua());
        // Nota: escludiamo la password per sicurezza
        return response;
    }

    //ALTRI METODI
    public Optional<Utente> findByUsername(String username) {
        return utenteRepository.findByUsername(username);
    }

    //LOGIN UTENTE
    public String authenticateUser(String username, String password)  {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtTokenUtil.generateToken(userDetails);
        } catch (AuthenticationException e) {
            throw new SecurityException("Credenziali non valide", e);
        }
    }


    public List<Utente> getAllUtenti() {return utenteRepository.findAll();}

    public Utente getUtenteById(Long id) {return utenteRepository.findById(id).orElseThrow(() -> new RuntimeException("Utente non trovato"));   }

    public Utente updateUtente(Long id, UtenteRequest request, Utente utenteCorrente) {
        boolean isAdmin = utenteCorrente.getRoles().contains(Role.ROLE_ADMIN);
        if(utenteCorrente.getId() == id || isAdmin) {
            Utente utente= utenteRepository.findById(id).orElseThrow(() -> new NotFoundException("Utente non trovato"));
            BeanUtils.copyProperties(request, utente);
            return utenteRepository.save(utente);
        } else {
            throw new IllegalArgumentException("Non sei autorizzato a modificare questo utente");
        }
    }

    public UtenteResponse updateCurrentUser(Long id, UtenteRequest request, Utente utenteCorrente) {
        // Verifico che l’utente corrente sia effettivamente il proprietario
        if (!utenteCorrente.getId().equals(id)) {
            throw new IllegalArgumentException("Non sei autorizzato a modificare questo profilo");
        }

        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));

        // Copio tutti i campi consentiti (escluse password e ruoli)
        utente.setUsername(request.getUsername());
        utente.setEmail(request.getEmail());
        utente.setNome(request.getNome());
        utente.setCognome(request.getCognome());
        utente.setDataNascita(request.getDataNascita());
        utente.setLuogoNascita(request.getLuogoNascita());
        utente.setResidenza(request.getResidenza());
        utente.setNomeCompagnia(request.getNomeCompagnia());
        utente.setLingua(request.getLingua());

        // Se è arrivato un file avatar, lo carico su Cloudinary e imposto l’URL
        MultipartFile avatarFile = request.getAvatarFile();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadImage(avatarFile);
            utente.setAvatar(avatarUrl);
        }

        Utente saved = utenteRepository.save(utente);
        return mapToResponse(saved);
    }

    public void deleteUtente(Long id) {
        if (!utenteRepository.existsById(id)) {
            throw new NotFoundException("Utente non trovato");
        }
        utenteRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return utenteRepository.existsByUsername(username);
    }

    public String uploadImage(Long id, MultipartFile file) {
        // Trova l'utente nel database
        Utente utente = utenteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));

        // Carica l'immagine su Cloudinary e ottieni l'URL
        String avatarUrl = cloudinaryService.uploadImage(file);

        // Aggiorna l'avatar dell'utente con l'URL dell'immagine
        utente.setAvatar(avatarUrl);

        // Salva l'utente con il nuovo avatar nel database
        utenteRepository.save(utente);

        // Restituisci l'URL dell'avatar caricato
        return avatarUrl;
    }


}
