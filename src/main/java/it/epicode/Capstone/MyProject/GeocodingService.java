package it.epicode.Capstone.MyProject;

import it.epicode.Capstone.login.exceptions.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public Coordinates geocodeAddress(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s", encodedAddress, apiKey
            );

            ResponseEntity<GeocodingResponse> response = restTemplate.getForEntity(url, GeocodingResponse.class);

            if (response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    !response.getBody().getResults().isEmpty()) {

                var location = response.getBody().getResults().get(0).getGeometry().getLocation();
                return new Coordinates(location.getLat(), location.getLng());
            }
            throw new NotFoundException("Indirizzo non valido");

        } catch (Exception e) {
            throw new NotFoundException("Errore durante il geocoding: " + e.getMessage());
        }
    }

    // DTO per la risposta
    @Data
    private static class GeocodingResponse {
        private List<Result> results;
    }

    @Data
    private static class Result {
        private Geometry geometry;
    }

    @Data
    private static class Geometry {
        private Location location;
    }

    @Data
    private static class Location {
        private double lat;
        private double lng;
    }

    @Data
    @AllArgsConstructor
    public static class Coordinates {
        private double lat;
        private double lng;
    }
}
