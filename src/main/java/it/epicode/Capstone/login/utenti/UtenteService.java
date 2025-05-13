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


   //REGISTRAZIONE UTENTE

    public Utente registerUtente(UtenteAuthRequest request) throws MessagingException {
        if (utenteRepository.existsByUsername(request.getUsername())) {
            throw new EntityExistsException("Username già in uso");
        }

        Utente utente = new Utente();
        utente.setNome(request.getNome());
        utente.setCognome(request.getCognome());
        utente.setEmail(request.getEmail());
        utente.setUsername(request.getUsername());
        utente.setPassword(passwordEncoder.encode(request.getPassword()));
        utente.setRoles(Set.of(Role.ROLE_USER));
        utente.setAvatar("https://ui-avatars.com/api/?name=" + utente.getNome() + "+" + utente.getCognome());
        emailSenderService.sendEmail(utente.getEmail(), "Benvenuto", "Ciao "+utente.getNome() + " " + utente.getCognome()+"! Benvenuto in Archiplanner!" );
        return utenteRepository.save(utente);
    }

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
    public CommonResponse createUtente(UtenteRequest request) throws MessagingException {
        Utente utente= new Utente();
        BeanUtils.copyProperties(request, utente);
        if (utenteRepository.existsByUsername(utente.getUsername())) {
            throw new UsernameException("Username già in uso");
        }
        if (utenteRepository.existsByEmail(utente.getEmail())) {
            throw new UsernameException("Email già in uso");
        }
        utente.setAvatar("https://ui-avatars.com/api/?name=" + utente.getNome() + "+" + utente.getCognome());
        utente = utenteRepository.save(utente);
        emailSenderService.sendEmail(utente.getEmail(), "Benvenuto", "Ciao "+utente.getNome() + " " + utente.getCognome()+"! Benvenuto nella nostra azienda!" );
        return new CommonResponse(utente.getId());
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

    public void deleteUtente(Long id) {
        if (!utenteRepository.existsById(id)) {
            throw new NotFoundException("Utente non trovato");
        }
        utenteRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return utenteRepository.existsByUsername(username);
    }

    public void uploadImage(Long id, MultipartFile file) {
        Utente utente= utenteRepository.findById(id).orElseThrow(() -> new NotFoundException("Utente non trovato"));
        utente.setAvatar(cloudinaryService.uploadImage(file));
        utenteRepository.save(utente);
    }


}
