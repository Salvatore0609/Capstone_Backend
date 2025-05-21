package it.epicode.Capstone.login.utenti.MyProject;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
    @NotBlank
    private String nomeProgetto;
    @NotBlank
    private String progettista;
    @NotBlank
    private String impresaCostruttrice;
    @NotBlank
    private String indirizzo;
    private Boolean completato;

}