package com.example.demo.service;

import com.example.demo.entity.Conversacion;
import com.example.demo.entity.PreguntaFrecuente;
import com.example.demo.repository.ConversacionRepository;
import com.example.demo.repository.PreguntaFrecuenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    @Autowired
    private PreguntaFrecuenteRepository preguntaFrecuenteRepository;

    @Autowired
    private ConversacionRepository conversacionRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String MONOLITO_URL = "http://localhost:8080/api/consultar";

    public String procesarPregunta(String pregunta, String usuario) {
        System.out.println("📥 CHATBOT - Procesando pregunta: " + pregunta);
        String respuesta = null;

        // 1. BUSCAR EN CACHÉ (preguntas_frecuentes)
        respuesta = buscarEnCache(pregunta);
        if (respuesta != null) {
            System.out.println("✅ CHATBOT - Respuesta encontrada en CACHÉ: " + respuesta);
        } else {
            System.out.println("⏳ CHATBOT - No está en caché, consultando al monolito...");
        }

        // 2. SI NO ESTÁ EN CACHÉ, CONSULTAR AL MONOLITO
        if (respuesta == null) {
            respuesta = consultarMonolito(pregunta);
        }

        // 3. SI EL MONOLITO TAMBIÉN FALLA
        if (respuesta == null || respuesta.isEmpty()) {
            System.out.println("❌ CHATBOT - No se obtuvo respuesta del monolito");
            respuesta = "Lo siento, no pude obtener información sobre eso. Intenta ser más específico.";
        }

        // 4. GUARDAR CONVERSACIÓN
        guardarConversacion(pregunta, respuesta, usuario);

        System.out.println("📤 CHATBOT - Respuesta final: " + respuesta);
        return respuesta;
    }

    private String buscarEnCache(String pregunta) {
        List<PreguntaFrecuente> faqs = preguntaFrecuenteRepository.findByActivoTrue();
        String preguntaLower = pregunta.toLowerCase();

        for (PreguntaFrecuente faq : faqs) {
            String faqPreguntaLower = faq.getPregunta().toLowerCase();
            if (preguntaLower.contains(faqPreguntaLower) ||
                    faqPreguntaLower.contains(preguntaLower)) {
                return faq.getRespuesta();
            }
        }
        return null;
    }

    private String consultarMonolito(String pregunta) {
        try {
            System.out.println("🔍 CHATBOT - Consultando al monolito en: " + MONOLITO_URL);
            System.out.println("🔍 CHATBOT - Pregunta enviada: " + pregunta);

            Map<String, String> request = new HashMap<>();
            request.put("pregunta", pregunta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            // Llamar al monolito
            ResponseEntity<Map> response = restTemplate.exchange(
                    MONOLITO_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            System.out.println("✅ CHATBOT - Respuesta del monolito: " + response.getBody());
            System.out.println("✅ CHATBOT - Status code: " + response.getStatusCode());

            if (response.getBody() != null && response.getBody().get("respuesta") != null) {
                String respuesta = response.getBody().get("respuesta").toString();
                System.out.println("✅ CHATBOT - Respuesta obtenida del monolito: " + respuesta);

                // Guardar en caché para futuras consultas
                guardarEnCache(pregunta, respuesta);
                return respuesta;
            }

            System.out.println("⚠️ CHATBOT - El monolito devolvió respuesta vacía");
            return null;

        } catch (Exception e) {
            System.out.println("❌ CHATBOT - ERROR al consultar monolito: " + e.getMessage());
            e.printStackTrace();
            return "Error al conectar con el sistema académico: " + e.getMessage();
        }
    }

    private void guardarEnCache(String pregunta, String respuesta) {
        try {
            List<PreguntaFrecuente> existentes = preguntaFrecuenteRepository.findByActivoTrue();
            for (PreguntaFrecuente faq : existentes) {
                if (faq.getPregunta().equalsIgnoreCase(pregunta)) {
                    return;
                }
            }

            PreguntaFrecuente nueva = new PreguntaFrecuente();
            nueva.setPregunta(pregunta);
            nueva.setRespuesta(respuesta);
            nueva.setCategoria("consultas");
            nueva.setActivo(true);
            preguntaFrecuenteRepository.save(nueva);
            System.out.println("💾 CHATBOT - Guardado en caché: " + pregunta);

        } catch (Exception e) {
            System.out.println("⚠️ CHATBOT - No se pudo guardar en caché: " + e.getMessage());
        }
    }

    private void guardarConversacion(String pregunta, String respuesta, String usuario) {
        Conversacion conversacion = new Conversacion();
        conversacion.setUsuario(usuario != null ? usuario : "anonimo");
        conversacion.setPregunta(pregunta);
        conversacion.setRespuesta(respuesta);
        conversacion.setFecha(LocalDateTime.now());
        conversacionRepository.save(conversacion);
        System.out.println("💾 CHATBOT - Conversación guardada en BD");
    }
}