package it.epicode.Capstone.MyProject;

import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogleRepository;
import it.epicode.Capstone.login.utenti.Utente;
import it.epicode.Capstone.login.utenti.UtenteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final ProjectRepository pRepo;
    private final GeocodingService geocodingService;
    private final UtenteRepository utenteRepo;
    private final UtenteGoogleRepository utenteGoogleRepo;

    // Creazione progetto
    public ProjectResponse create(ProjectRequest req, String username) {
        var coordinates = geocodingService.geocodeAddress(req.getIndirizzo());

        // Trova l’utente: prima utente “normale”, se non esiste cerca Google
        Utente utente = utenteRepo.findByUsername(username).orElse(null);
        UtenteGoogle utenteGoogle = null;
        if (utente == null) {
            utenteGoogle = utenteGoogleRepo.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        }

        // Costruisci il progetto
        Project p = new Project();
        p.setNomeProgetto(req.getNomeProgetto());
        p.setProgettista(req.getProgettista());
        p.setImpresaCostruttrice(req.getImpresaCostruttrice());
        p.setIndirizzo(req.getIndirizzo());
        p.setLat(coordinates.getLat());
        p.setLng(coordinates.getLng());

        // Associa il proprietario giusto
        if (utente != null) {
            p.setProprietario(utente);
        } else {
            p.setProprietarioGoogle(utenteGoogle);
        }

        return mapToResponse(pRepo.save(p));
    }

    // Lista progetti per utente (normale o Google)
    public List<ProjectResponse> listByUser(String username) {
        // Stessa logica: prima utente normale
        Utente utente = utenteRepo.findByUsername(username).orElse(null);
        if (utente != null) {
            List<Project> projects = pRepo.findByProprietarioIdOrderByCreatedAtDesc(utente.getId());
            return projects.stream().map(this::mapToResponse).toList();
        }
        // Altrimenti Google
        UtenteGoogle utenteGoogle = utenteGoogleRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utente Google non trovato"));
        List<Project> projects = pRepo.findByProprietarioGoogleIdOrderByCreatedAtDesc(utenteGoogle.getId());
        return projects.stream().map(this::mapToResponse).toList();
    }

    // Recupera singolo progetto (controllo di proprietà)
    public ProjectResponse getById(Long id, String username) {
        Utente utente = utenteRepo.findByUsername(username).orElse(null);
        UtenteGoogle utenteGoogle;
        if (utente == null) {
            utenteGoogle = utenteGoogleRepo.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Utente Google non trovato"));
        } else {
            utenteGoogle = null;
        }

        Project p = pRepo.findById(id)
                .filter(proj -> {
                    if (utente != null) {
                        return proj.getProprietario() != null &&
                                proj.getProprietario().getId().equals(utente.getId());
                    } else {
                        return proj.getProprietarioGoogle() != null &&
                                proj.getProprietarioGoogle().getId().equals(utenteGoogle.getId());
                    }
                })
                .orElseThrow(() -> new RuntimeException("Progetto non trovato o non autorizzato"));

        return mapToResponse(p);
    }

    // Aggiorna progetto (controllo di proprietà)
    public ProjectResponse updateProject(Long id, ProjectRequest req, String username) {
        Utente utente = utenteRepo.findByUsername(username).orElse(null);
        UtenteGoogle utenteGoogle = null;
        if (utente == null) {
            utenteGoogle = utenteGoogleRepo.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Utente Google non trovato"));
        }

        Project project = pRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));

        boolean isAuthorized = (utente != null && project.getProprietario() != null &&
                project.getProprietario().getId().equals(utente.getId()))
                || (utenteGoogle != null && project.getProprietarioGoogle() != null &&
                project.getProprietarioGoogle().getId().equals(utenteGoogle.getId()));

        if (!isAuthorized) {
            throw new AccessDeniedException("Non autorizzato");
        }

        project.setNomeProgetto(req.getNomeProgetto());
        project.setProgettista(req.getProgettista());
        project.setImpresaCostruttrice(req.getImpresaCostruttrice());
        project.setIndirizzo(req.getIndirizzo());

        if (req.getInProgress() != null) {
            project.setInProgress(req.getInProgress());
        }

        if (req.getCompletato() != null) {
            project.setCompletato(req.getCompletato());
        }

        return mapToResponse(pRepo.save(project));
    }

    // Elimina progetto (controllo di proprietà)
    public void deleteProject(Long id, String username) {
        Utente utente = utenteRepo.findByUsername(username).orElse(null);
        UtenteGoogle utenteGoogle = null;
        if (utente == null) {
            utenteGoogle = utenteGoogleRepo.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Utente Google non trovato"));
        }

        Project project = pRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));

        boolean isOwner = (utente != null && project.getProprietario() != null &&
                project.getProprietario().getId().equals(utente.getId()))
                || (utenteGoogle != null && project.getProprietarioGoogle() != null &&
                project.getProprietarioGoogle().getId().equals(utenteGoogle.getId()));

        if (!isOwner) {
            throw new AccessDeniedException("Non puoi eliminare questo progetto");
        }

        pRepo.delete(project);
    }

    public long countCompletatiByUser(String username) {
        Utente utente = utenteRepo.findByUsername(username).orElse(null);
        if (utente != null) {
            return pRepo.findByProprietarioIdAndCompletatoTrue(utente.getId()).size();
        }
        UtenteGoogle utenteGoogle = utenteGoogleRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Utente Google non trovato"));
        return pRepo.findByProprietarioGoogleIdAndCompletatoTrue(utenteGoogle.getId()).size();
    }

    // Mapper di utilità
    private ProjectResponse mapToResponse(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getNomeProgetto(),
                p.getProgettista(),
                p.getImpresaCostruttrice(),
                p.getIndirizzo(),
                p.getLat(),
                p.getLng(),
                p.getCreatedAt(),
                p.getInProgress(),
                p.getCompletato()
        );
    }
}

