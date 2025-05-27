package it.epicode.Capstone.MyProject;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    // Create (POST /projects)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(
            @Valid @RequestBody ProjectRequest req,
            Principal principal) {
        return projectService.create(req, principal.getName());
    }

    // List (GET /projects)
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectResponse> list(Principal principal) {
        return projectService.listByUser(principal.getName());
    }

    // Get single (GET /projects/{id})
    @GetMapping("/{id}")
    public ProjectResponse getOne(
            @PathVariable Long id,
            Principal principal) {
        return projectService.getById(id, principal.getName());
    }

    //Endpoint per ottenere il solo numero di completati
    @GetMapping("/count-completati")
    public long countCompletati(Principal principal) {
        return projectService.countCompletatiByUser(principal.getName());
    }

    // Update (PUT /projects/{id})
    @PutMapping("/{id}")
    public ProjectResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest req,
            Principal principal) {
        return projectService.updateProject(id, req, principal.getName());
    }

    // Delete (DELETE /projects/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Principal principal) {
        projectService.deleteProject(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}