package com.universidad.sistema_academico.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatbotService {

    @Autowired
    private RestTemplate restTemplate;




    @Value("${chatbot.api.url:http://localhost:8090/api/chat}")
    private String CHATBOT_URL;

    /**
     * Envía una pregunta al chatbot y obtiene la respuesta
     */
    public String preguntar(String pregunta, String usuario) {
        try {
            System.out.println("📤 MONOLITO - Enviando pregunta al chatbot: " + pregunta);
            System.out.println("👤 MONOLITO - Usuario: " + usuario);
            System.out.println("🔗 MONOLITO - URL del chatbot: " + CHATBOT_URL);

            // 1. Preparar la petición
            Map<String, String> request = new HashMap<>();
            request.put("pregunta", pregunta);
            request.put("usuario", usuario != null ? usuario : "anonimo");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // 2. Llamar al chatbot
            ResponseEntity<Map> response = restTemplate.exchange(
                    CHATBOT_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // 3. Procesar la respuesta
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String respuesta = response.getBody().get("respuesta").toString();
                System.out.println("✅ MONOLITO - Respuesta recibida: " + respuesta);
                return respuesta;
            }

            return "No se pudo obtener respuesta del chatbot.";

        } catch (Exception e) {
            System.out.println("❌ MONOLITO - Error al conectar con el chatbot: " + e.getMessage());
            return "Error al conectar con el asistente virtual: " + e.getMessage();
        }
    }

    /**
     * Verifica si el chatbot está disponible
     */
    public boolean isChatbotAvailable() {
        try {
            restTemplate.getForEntity("http://localhost:8090/actuator/health", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene las preguntas frecuentes desde el chatbot (puerto 8090)
     */
    public String obtenerPreguntasFrecuentes() {
        try {
            // Llamar al endpoint de preguntas frecuentes del chatbot
            String url = "http://localhost:8090/api/faqs";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return "[]";

        } catch (Exception e) {
            System.out.println("❌ MONOLITO - Error al obtener FAQs: " + e.getMessage());
            return "[]";
        }
    }
}