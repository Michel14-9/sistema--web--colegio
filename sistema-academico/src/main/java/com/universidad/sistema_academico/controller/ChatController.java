package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.service.ChatbotService;
import com.universidad.sistema_academico.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private ConsultaService consultaService;

    @GetMapping("")
    public String mostrarChat(
            @RequestParam(value = "embed", required = false, defaultValue = "false") boolean embed,
            Model model) {

        String mensajeBienvenida = """
            👋 ¡Hola! Soy el asistente virtual de la I.E. San Carlos.
            
            ¿En qué puedo ayudarte?
            
            📌 **Preguntas que puedes hacerme:**
            • ¿Cómo me matrículo?
            • ¿Cuánto es la mensualidad?
            • ¿Qué documentos necesito?
            • ¿Dónde queda el colegio?
            • Horario de clases
            • ¿Qué cursos hay?
            • ¿Cuántos estudiantes hay?
            • ¿Qué puedes hacer?
            
            💡 Escribe tu pregunta o selecciona una de las opciones.
            """;

        model.addAttribute("bienvenida", mensajeBienvenida);
        model.addAttribute("mostrarBienvenida", true);
        model.addAttribute("preguntasFrecuentes", getDefaultPreguntasFrecuentes());
        model.addAttribute("embed", embed);
        return "chat";
    }

    @PostMapping("/preguntar")
    public String preguntar(
            @RequestParam(value = "pregunta", required = false) String pregunta,
            @RequestParam(value = "preguntaSeleccionada", required = false) String preguntaSeleccionada,
            @RequestParam(value = "embed", required = false, defaultValue = "false") boolean embed,
            Model model) {

        // Siempre propagar embed al modelo, en cualquier return de este método
        model.addAttribute("embed", embed);

        // Si viene de un botón de pregunta frecuente
        if (preguntaSeleccionada != null && !preguntaSeleccionada.isEmpty()) {
            pregunta = preguntaSeleccionada;
        }

        System.out.println("🔍 DEBUG - Pregunta recibida: '" + pregunta + "'");

        if (pregunta == null || pregunta.trim().isEmpty()) {
            model.addAttribute("error", "⚠ Por favor, escribe una pregunta.");
            model.addAttribute("preguntasFrecuentes", getDefaultPreguntasFrecuentes());
            return "chat";
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String usuario = auth != null ? auth.getName() : "anonimo";

            // ============================================================
            // 🔥 PASO 1: SIEMPRE CONSULTAR RESPUESTA LOCAL PRIMERO
            // ============================================================
            System.out.println("📥 MONOLITO - Buscando respuesta LOCAL en ConsultaService...");
            String respuestaLocal = consultaService.buscarRespuesta(pregunta);
            System.out.println("📥 MONOLITO - Respuesta LOCAL: " + respuestaLocal);

            boolean esRespuestaLocalValida = respuestaLocal != null &&
                    !respuestaLocal.contains("No encontré información sobre eso") &&
                    !respuestaLocal.contains("Puedes preguntarme sobre");

            if (esRespuestaLocalValida) {
                System.out.println("✅ MONOLITO - Usando respuesta LOCAL");
                model.addAttribute("pregunta", pregunta);
                model.addAttribute("respuesta", respuestaLocal);
                model.addAttribute("usuario", usuario);
                model.addAttribute("mostrarBienvenida", false);
                model.addAttribute("preguntasFrecuentes", getDefaultPreguntasFrecuentes());
                return "chat";
            }

            // ============================================================
            // PASO 2: SI NO HAY RESPUESTA LOCAL, USAR EL CHATBOT EXTERNO
            // ============================================================
            System.out.println("⏳ MONOLITO - No hay respuesta local, consultando chatbot externo...");
            String respuestaChatbot = chatbotService.preguntar(pregunta, usuario);

            model.addAttribute("pregunta", pregunta);
            model.addAttribute("respuesta", respuestaChatbot);
            model.addAttribute("usuario", usuario);
            model.addAttribute("mostrarBienvenida", false);
            model.addAttribute("preguntasFrecuentes", getDefaultPreguntasFrecuentes());

        } catch (Exception e) {
            System.out.println("❌ MONOLITO - Error: " + e.getMessage());
            model.addAttribute("error", "❌ Error al procesar tu pregunta: " + e.getMessage());
            model.addAttribute("pregunta", pregunta);
            model.addAttribute("preguntasFrecuentes", getDefaultPreguntasFrecuentes());
        }

        return "chat";
    }

    private List<PreguntaFrecuenteDTO> getDefaultPreguntasFrecuentes() {
        List<PreguntaFrecuenteDTO> lista = new ArrayList<>();
        lista.add(new PreguntaFrecuenteDTO("¿Cómo me matrículo?", "Guía de matrícula virtual"));
        lista.add(new PreguntaFrecuenteDTO("¿Cuánto es la mensualidad?", "Información de costos"));
        lista.add(new PreguntaFrecuenteDTO("¿Qué documentos necesito?", "Requisitos para matrícula"));
        lista.add(new PreguntaFrecuenteDTO("¿Dónde queda el colegio?", "Dirección y contacto"));
        lista.add(new PreguntaFrecuenteDTO("Horario de clases", "Horarios generales"));
        lista.add(new PreguntaFrecuenteDTO("¿Qué cursos hay?", "Lista de cursos disponibles"));
        lista.add(new PreguntaFrecuenteDTO("¿Cuántos estudiantes hay?", "Estadísticas del colegio"));
        return lista;
    }

    public static class PreguntaFrecuenteDTO {
        private String pregunta;
        private String descripcion;

        public PreguntaFrecuenteDTO(String pregunta, String descripcion) {
            this.pregunta = pregunta;
            this.descripcion = descripcion;
        }

        public String getPregunta() { return pregunta; }
        public String getDescripcion() { return descripcion; }
    }
}