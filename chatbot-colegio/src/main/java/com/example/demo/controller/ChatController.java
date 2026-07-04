package com.example.demo.controller;

import com.example.demo.entity.PreguntaFrecuente;
import com.example.demo.repository.PreguntaFrecuenteRepository;
import com.example.demo.service.ChatbotService;
import com.example.demo.service.GroqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private GroqService groqService;

    @Autowired
    private PreguntaFrecuenteRepository preguntaFrecuenteRepository;

    @PostMapping
    public Map<String, String> responder(@RequestBody Map<String, String> request) {
        String pregunta = request.get("pregunta");
        String usuario = request.get("usuario");

        String respuesta;

        // ============================================================
        // 🔥 VERIFICAR SI ES UNA PREGUNTA DE MATRÍCULA
        // ============================================================
        String preguntaLower = pregunta.toLowerCase();
        if ((preguntaLower.contains("matricula") || preguntaLower.contains("matrícula") ||
                preguntaLower.contains("inscripcion") || preguntaLower.contains("inscripción")) &&
                (preguntaLower.contains("como") || preguntaLower.contains("cómo") ||
                        preguntaLower.contains("pasos") || preguntaLower.contains("proceso") ||
                        preguntaLower.contains("hacer") || preguntaLower.contains("realizar") ||
                        preguntaLower.contains("guia") || preguntaLower.contains("guía"))) {

            respuesta = "📋 **Para información sobre matrícula, consulta el sistema principal.**\n\n" +
                    "💡 Ve a la sección de chat en el sistema principal para obtener la guía completa de matrícula virtual.\n" +
                    "🔗 [Ir a Matrícula Virtual](http://localhost:8080/api/matricula/matricula)";

            Map<String, String> response = new HashMap<>();
            response.put("respuesta", respuesta);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return response;
        }

        // ============================================================
        // PASO 2: INTENTAR CON GROQ
        // ============================================================
        try {
            respuesta = groqService.procesarPregunta(pregunta);
            if (respuesta == null || respuesta.contains("No hay API Key") || respuesta.contains("Error")) {
                respuesta = chatbotService.procesarPregunta(pregunta, usuario);
            }
        } catch (Exception e) {
            respuesta = chatbotService.procesarPregunta(pregunta, usuario);
        }

        // ============================================================
        // PASO 3: RESPUESTA POR DEFECTO
        // ============================================================
        if (respuesta == null || respuesta.isEmpty()) {
            respuesta = "Lo siento, no pude procesar tu pregunta. Intenta de nuevo o contacta con administración.";
        }

        Map<String, String> response = new HashMap<>();
        response.put("respuesta", respuesta);
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        return response;
    }

    @GetMapping("/faqs")
    public List<PreguntaFrecuente> obtenerFaqs() {
        return preguntaFrecuenteRepository.findByActivoTrue();
    }
}