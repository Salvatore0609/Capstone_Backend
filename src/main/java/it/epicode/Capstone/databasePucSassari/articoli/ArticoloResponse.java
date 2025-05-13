package it.epicode.Capstone.databasePucSassari.articoli;

import it.epicode.Capstone.databasePucSassari.sezioni.SezioneResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticoloResponse {
    private Long id;
    private String titolo;
    private List<SezioneResponse> sezioni;
}