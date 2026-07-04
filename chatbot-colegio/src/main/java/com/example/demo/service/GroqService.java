package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key:#{null}}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MONOLITO_URL = "http://localhost:8080/api/consultar";

    public String procesarPregunta(String pregunta) {
        System.out.println("🔑 API Key: " + (apiKey != null ? "Configurada ✅" : "No configurada ❌"));

        if (apiKey == null || apiKey.isEmpty()) {
            return "⚠️ No hay API Key de Groq configurada. Usa el servicio antiguo.";
        }

        try {
            System.out.println("🤖 GROQ - Procesando: " + pregunta);

            // 1. Construir el prompt
            String prompt = construirPrompt(pregunta);

            // 2. Llamar a Groq para que genere SQL
            String respuestaGroq = llamarGroq(prompt);
            System.out.println("✅ GROQ - Respuesta recibida: " + respuestaGroq);

            // 3. Si Groq generó SQL, consultar al monolito
            if (respuestaGroq.contains("SQL:")) {
                String sql = extraerSQL(respuestaGroq);
                System.out.println("📊 SQL generado: " + sql);

                // 🔥 CONSULTAR AL MONOLITO (NO a la BD directo)
                return consultarMonolito(pregunta);
            }

            return respuestaGroq;

        } catch (Exception e) {
            System.out.println("❌ GROQ - Error: " + e.getMessage());
            e.printStackTrace();
            return "❌ Error al procesar tu pregunta: " + e.getMessage();
        }
    }

    /**
     * Extrae el SQL de la respuesta de Groq
     */
    private String extraerSQL(String respuesta) {
        String sql = respuesta.substring(respuesta.indexOf("SQL:") + 4).trim();
        sql = sql.replace("```sql", "").replace("```", "").trim();
        return sql.replaceAll("\\n\\s*\\n", "\n");
    }

    /**
     * Consulta al monolito en lugar de ejecutar SQL directamente
     */
    private String consultarMonolito(String pregunta) {
        try {
            System.out.println("🔍 CHATBOT - Consultando al monolito en: " + MONOLITO_URL);

            Map<String, String> request = new HashMap<>();
            request.put("pregunta", pregunta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    MONOLITO_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            System.out.println("✅ CHATBOT - Respuesta del monolito: " + response.getBody());

            if (response.getBody() != null && response.getBody().get("respuesta") != null) {
                return response.getBody().get("respuesta").toString();
            }

            return "No se pudo obtener respuesta del sistema académico.";

        } catch (Exception e) {
            System.out.println("❌ CHATBOT - Error al consultar monolito: " + e.getMessage());
            return "Error al conectar con el sistema académico: " + e.getMessage();
        }
    }

    private String construirPrompt(String pregunta) {
        return """
            Eres un asistente virtual de la I.E. San Carlos.
            Tu trabajo es ayudar a los usuarios con preguntas sobre el colegio.
            
            IMPORTANTE: Tu trabajo es ENTENDER la pregunta y decidir si es una consulta a la BD.
            Las tablas están en el esquema 'academico': estudiante, docente, curso, matricula, notas.
            
            Si la pregunta requiere consultar la base de datos, responde con:
            "SQL: [tu consulta SQL aquí]"
            
            Si la pregunta es general (saludo, contacto, horarios, documentos), responde normalmente.
            
            Pregunta del usuario: """ + pregunta;
    }

    private String llamarGroq(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-8b-instant");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "Eres un asistente útil y preciso."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 500);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GROQ_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error en Groq: " + response.getStatusCode());
        }

        JsonNode json = objectMapper.readTree(response.getBody());
        return json.get("choices").get(0).get("message").get("content").asText();
    }
}