package com.universidad.sistema_academico.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reniec")
public class ReniecController {


    private static final String DECOLECTA_API_KEY = "sk_11001.1e4nTAnJyl7DP5y2EqEQOmFA1ttrERon";

    @GetMapping("/consultar/{dni}")
    public ResponseEntity<?> consultarReniec(@PathVariable String dni) {
        Map<String, Object> response = new HashMap<>();

        try {
            // URL CORRECTA según documentación
            String url = "https://api.decolecta.com/v1/reniec/dni?numero=" + dni;

            System.out.println("Consultando URL: " + url);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + DECOLECTA_API_KEY);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            // Realizar la consulta
            ResponseEntity<String> apiResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            System.out.println("Respuesta código: " + apiResponse.getStatusCode());
            System.out.println("Respuesta body: " + apiResponse.getBody());

            if (apiResponse.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(apiResponse.getBody());

                // Mapear la respuesta según documentación
                response.put("success", true);
                response.put("nombres", rootNode.get("first_name").asText());
                response.put("apellidoPaterno", rootNode.get("first_last_name").asText());
                response.put("apellidoMaterno", rootNode.get("second_last_name").asText());
                response.put("fullName", rootNode.get("full_name").asText());
                response.put("dni", rootNode.get("document_number").asText());
                response.put("message", "DNI encontrado");

                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Error al consultar DNI");
                return ResponseEntity.status(apiResponse.getStatusCode()).body(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al consultar RENIEC: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}