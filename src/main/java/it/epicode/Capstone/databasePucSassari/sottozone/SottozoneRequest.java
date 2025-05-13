package it.epicode.Capstone.databasePucSassari.sottozone;

import it.epicode.Capstone.databasePucSassari.sottozone.parametri.ParametriRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SottozoneRequest {
    private String nome;
    private String descrizione;
    private List<ParametriRequest> parametri;
    private String note;
}