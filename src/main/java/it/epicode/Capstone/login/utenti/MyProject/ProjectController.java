package it.epicode.Capstone.login.utenti.MyProject;

import it.epicode.Capstone.login.utenti.Utente;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody ProjectRequest req,
            @AuthenticationPrincipal Object user) {
        return ResponseEntity.ok(projectService.create(req, user));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProjectResponse>> list(
            @AuthenticationPrincipal Object user) {
        return ResponseEntity.ok(projectService.listByUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getOne(
            @PathVariable Long id,
            @AuthenticationPrincipal Object user) {
        return ResponseEntity.ok(projectService.getById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest req,
            @AuthenticationPrincipal Object user) {
        return ResponseEntity.ok(projectService.updateProject(id, req, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Principal principal) {
        projectService.deleteProject(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}