package it.epicode.Capstone.login.utenti.MyProject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String nomeProgetto;
    private String progettista;
    private String impresaCostruttrice;
    private String indirizzo;
    Double lat;
    Double lng;
    Map<String, String> phases;
}