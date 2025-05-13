package it.epicode.Capstone.databasePucSassari.sottozone;

import it.epicode.Capstone.databasePucSassari.sottozone.parametri.ParametriResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SottozoneResponse {
    private Long sottozId;
    private String nome;
    private String descrizione;
    private List<ParametriResponse> parametri;
    private String note;
}