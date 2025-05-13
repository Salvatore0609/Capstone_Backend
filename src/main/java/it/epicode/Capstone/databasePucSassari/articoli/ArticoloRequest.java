package it.epicode.Capstone.databasePucSassari.articoli;

import it.epicode.Capstone.databasePucSassari.sezioni.SezioneRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticoloRequest {
    private String titolo;
    private List<SezioneRequest> sezioni;
}