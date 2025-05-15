package it.epicode.Capstone.login.utenti.MyProject;

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

    public ProjectResponse create(ProjectRequest req, Utente utente) {
        Project p = new Project();
        p.setNomeProgetto(req.getNomeProgetto());
        p.setProgettista(req.getProgettista());
        p.setImpresaCostruttrice(req.getImpresaCostruttrice());
        p.setIndirizzo(req.getIndirizzo());
        p.setLat(req.getLat());
        p.setLng(req.getLng());
        p.setPhases(req.getPhases());
        p.setProprietario(utente);
        Project saved = pRepo.save(p);
        return mapToResponse(saved);
    }

    public List<ProjectResponse> listByUser(Utente utente) {
        return pRepo.findByProprietarioId(utente.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProjectResponse getById(Long id, Utente utente) {
        Project p = pRepo.findById(id)
                .filter(prj -> prj.getProprietario().getId().equals(utente.getId()))
                .orElseThrow(() -> new RuntimeException("Progetto non trovato o non autorizzato"));
        return mapToResponse(p);
    }

    public ProjectResponse updateProject(Long id, ProjectRequest req, Utente utente) {
        Project existing = pRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));
        if (!existing.getProprietario().getId().equals(utente.getId())) {
            throw new AccessDeniedException("Non autorizzato ad aggiornare questo progetto");
        }
        existing.setNomeProgetto(req.getNomeProgetto());
        existing.setProgettista(req.getProgettista());
        existing.setImpresaCostruttrice(req.getImpresaCostruttrice());
        existing.setIndirizzo(req.getIndirizzo());
        existing.setLat(req.getLat());
        existing.setLng(req.getLng());
        existing.setPhases(req.getPhases());
        Project saved = pRepo.save(existing);
        return mapToResponse(saved);
    }

    public void deleteProject(Long id, String email) {
        Project project = pRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato"));
        if (!project.getProprietario().getEmail().equals(email)) {
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
                p.getLng(),
                p.getPhases()
        );
    }
}