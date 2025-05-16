package it.epicode.Capstone.login.utenti.MyProject;

import it.epicode.Capstone.login.authGoogle.UtenteGoogle;
import it.epicode.Capstone.login.utenti.Utente;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public ProjectResponse create(ProjectRequest req, Object user) {
        var coordinates = geocodingService.geocodeAddress(req.getIndirizzo());

        Project p = new Project();
        p.setNomeProgetto(req.getNomeProgetto());
        p.setProgettista(req.getProgettista());
        p.setImpresaCostruttrice(req.getImpresaCostruttrice());
        p.setIndirizzo(req.getIndirizzo());
        p.setLat(coordinates.getLat());
        p.setLng(coordinates.getLng());

        if (user instanceof Utente utente) {
            p.setProprietario(utente);
        } else if (user instanceof UtenteGoogle utenteGoogle) {
            p.setProprietarioGoogle(utenteGoogle);
        }

        return mapToResponse(pRepo.save(p));
    }

    public List<ProjectResponse> listByUser(Object user) {
        if (user instanceof Utente utente) {
            return pRepo.findByProprietarioId(utente.getId())
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        } else if (user instanceof UtenteGoogle utenteGoogle) {
            return pRepo.findByProprietarioGoogleId(utenteGoogle.getId())
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }
        return List.of();
    }

    public ProjectResponse getById(Long id, Object user) {
        Project p = pRepo.findById(id)
                .filter(proj -> {
                    if (user instanceof Utente utente) {
                        return proj.getProprietario() != null &&
                                proj.getProprietario().getId().equals(utente.getId());
                    } else if (user instanceof UtenteGoogle utenteGoogle) {
                        return proj.getProprietarioGoogle() != null &&
                                proj.getProprietarioGoogle().getId().equals(utenteGoogle.getId());
                    }
                    return false;
                })
                .orElseThrow(() -> new RuntimeException("Progetto non trovato o non autorizzato"));
        return mapToResponse(p);
    }

    public ProjectResponse updateProject(Long id, ProjectRequest req, Object user) {
        Project project = pRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));

        boolean isAuthorized = false;
        if (user instanceof Utente utente) {
            isAuthorized = project.getProprietario() != null &&
                    project.getProprietario().getId().equals(utente.getId());
        } else if (user instanceof UtenteGoogle utenteGoogle) {
            isAuthorized = project.getProprietarioGoogle() != null &&
                    project.getProprietarioGoogle().getId().equals(utenteGoogle.getId());
        }

        if (!isAuthorized) throw new AccessDeniedException("Non autorizzato");

        project.setNomeProgetto(req.getNomeProgetto());
        project.setProgettista(req.getProgettista());
        project.setImpresaCostruttrice(req.getImpresaCostruttrice());
        project.setIndirizzo(req.getIndirizzo());

        return mapToResponse(pRepo.save(project));
    }

    public void deleteProject(Long id, String email) {
        Project project = pRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));

        String ownerEmail = null;
        if (project.getProprietario() != null) {
            ownerEmail = project.getProprietario().getEmail();
        } else if (project.getProprietarioGoogle() != null) {
            ownerEmail = project.getProprietarioGoogle().getEmail();
        }

        if (!email.equals(ownerEmail)) {
            throw new AccessDeniedException("Non puoi eliminare questo progetto");
        }

        pRepo.delete(project);
    }

    private ProjectResponse mapToResponse(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getNomeProgetto(),
                p.getProgettista(),
                p.getImpresaCostruttrice(),
                p.getIndirizzo(),
                p.getLat(),
                p.getLng()
        );
    }
}
