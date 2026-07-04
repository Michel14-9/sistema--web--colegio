package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.model.*;
import com.universidad.sistema_academico.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsultaService {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HtmlAnalyzerService htmlAnalyzerService;

    @Autowired
    private ChatbotService chatbotService;

    public String buscarRespuesta(String pregunta) {
        System.out.println("🔍 CONSULTA SERVICE - Procesando: " + pregunta);
        String preguntaLower = pregunta.toLowerCase().trim();

        // ============================================================
        //  PRIORIDAD 1: GUÍA DE MATRÍCULA
        // ============================================================
        boolean esMatricula = preguntaLower.contains("matricula") ||
                preguntaLower.contains("matriculo") ||
                preguntaLower.contains("matrícula") ||
                preguntaLower.contains("matricul") ||
                preguntaLower.contains("matricular") ||
                preguntaLower.contains("matriculame");

        boolean esGuia = preguntaLower.contains("como") ||
                preguntaLower.contains("cómo") ||
                preguntaLower.contains("pasos") ||
                preguntaLower.contains("proceso") ||
                preguntaLower.contains("hacer") ||
                preguntaLower.contains("realizar") ||
                preguntaLower.contains("guia") ||
                preguntaLower.contains("guía");

        if (esMatricula && esGuia) {
            System.out.println(" CONSULTA SERVICE - Capturada pregunta de matrícula!");
            return generarGuiaMatriculaVirtual();
        }

        // ============================================================
        // 2. SALUDOS
        // ============================================================
        if (preguntaLower.matches(".*(hola|buenos dias|buenas tardes|buenas noches|buen dia|que tal|hey|saludos).*")) {
            return "¡Hola! Soy el asistente virtual de la I.E. San Carlos. " +
                    "Puedo ayudarte con información sobre matrículas, cursos, mensualidades, " +
                    "horarios y más. ¿Qué necesitas saber?";
        }

        // ============================================================
        // 3. MENSUALIDAD
        // ============================================================
        if (preguntaLower.matches(".*(mensualidad|pension|cuota|pago mensual|costo mensual|cuanto cuesta|cual es el costo).*")) {
            return " **Mensualidad - I.E. San Carlos**\n" +
                    "La mensualidad es de **S/. 200.00**\n\n" +
                    " El pago se realiza en el banco y se adjunta el voucher en el formulario de matrícula.";
        }

        // ============================================================
        // 4. DOCUMENTOS PARA MATRÍCULA
        // ============================================================
        if ((preguntaLower.contains("documento") || preguntaLower.contains("requisito") ||
                preguntaLower.contains("papeles") || preguntaLower.contains("necesito") ||
                preguntaLower.contains("necesita")) &&
                (preguntaLower.contains("matricula") || preguntaLower.contains("matrícula") ||
                        preguntaLower.contains("inscripcion") || preguntaLower.contains("matricular"))) {
            return " **Documentos necesarios para matrícula:**\n" +


                    "1. Voucher de pago de matrícula (S/. 200.00)\n\n" +
                    " Todos los documentos se adjuntan en el formulario de matrícula virtual.";
        }

        // ============================================================
        // 5. CONTACTO Y UBICACIÓN
        // ============================================================
        if (preguntaLower.matches(".*(contacto|telefono|correo|email|direccion|ubicacion|donde queda|donde esta|ubicado).*")) {
            return " **Contacto I.E. San Carlos**\n" +
                    " Dirección: Independencia 397-291, Ica - Perú\n" +
                    " Teléfono: (056) 123456\n" +
                    " Email: info@iesancarlos.edu.pe\n" +
                    " Atención: Lunes a Viernes de 8:00 AM a 5:00 PM";
        }

        // ============================================================
        // 6. HORARIOS
        // ============================================================
        if (preguntaLower.matches(".*(horario|clases|turno|hora|a que hora).*")) {
            return " **Horario General de Clases**\n" +
                    "Turno Mañana: 8:00 AM - 1:00 PM\n" +
                    "Turno Tarde: 2:00 PM - 7:00 PM\n" +
                    "Recreo: 10:00 AM - 10:30 AM\n\n" +
                    " Los horarios específicos se asignan al momento de la matrícula.";
        }

        // ============================================================
        // 7. ESTADÍSTICAS - CONTAR ESTUDIANTES
        // ============================================================
        if (preguntaLower.matches(".*(cuantos|cantidad|total|numero|cuántos).*(estudiante|alumno).*")) {
            long total = estudianteRepository.count();
            return " Hay un total de **" + total + " estudiantes** registrados en el sistema.";
        }

        // ============================================================
        // 8. ESTADÍSTICAS - CONTAR DOCENTES
        // ============================================================
        if (preguntaLower.matches(".*(cuantos|cantidad|total|numero|cuántos).*(docente|profesor).*")) {
            long total = docenteRepository.count();
            return " Hay un total de **" + total + " docentes** registrados en el sistema.";
        }

        // ============================================================
        // 9. ESTADÍSTICAS - CONTAR CURSOS
        // ============================================================
        if (preguntaLower.matches(".*(cuantos|cantidad|total|numero|cuántos).*(curso|materia|asignatura).*")) {
            long total = cursoRepository.count();
            return " Hay un total de **" + total + " cursos** disponibles en el sistema.";
        }

        // ============================================================
        // 10. LISTAR CURSOS
        // ============================================================
        if (preguntaLower.matches(".*(que|lista|cuales|mostrar|ofrece).*(curso|materia|asignatura).*")) {
            List<Curso> cursos = cursoRepository.findAll();
            if (cursos.isEmpty()) {
                return "No hay cursos registrados en el sistema.";
            }
            StringBuilder sb = new StringBuilder(" **Cursos disponibles:**\n\n");
            int count = 0;
            for (Curso c : cursos) {
                if (count >= 15) {
                    sb.append("... y " + (cursos.size() - 15) + " más\n");
                    break;
                }
                String gradoTexto = convertirGrado(c.getIdGrado());
                sb.append("• ").append(c.getNombreCurso())
                        .append(" | ").append(gradoTexto)
                        .append(" | Área: ").append(c.getArea())
                        .append("\n");
                count++;
            }
            return sb.toString();
        }

        // ============================================================
        // 11. INFORMACIÓN DE MATRÍCULAS
        // ============================================================
        if (preguntaLower.contains("matricula") || preguntaLower.contains("matrícula") ||
                preguntaLower.contains("matricular")) {
            List<Matricula> todasMatriculas = matriculaRepository.findAll();
            int totalMatriculas = todasMatriculas.size();
            int anioActual = java.time.Year.now().getValue();

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(2025|2026|2027|2028|2029|2030)");
            java.util.regex.Matcher matcher = pattern.matcher(preguntaLower);
            if (matcher.find()) {
                int anioBuscado = Integer.parseInt(matcher.group(1));
                long count = todasMatriculas.stream()
                        .filter(m -> m.getAnioAcademico() != null && m.getAnioAcademico().equals(anioBuscado))
                        .count();
                return " En el año **" + anioBuscado + "** hay **" + count + " matrículas** registradas.";
            }

            long countActual = todasMatriculas.stream()
                    .filter(m -> m.getAnioAcademico() != null && m.getAnioAcademico().equals(anioActual))
                    .count();

            return " **Información de Matrículas**\n" +
                    "• Total: **" + totalMatriculas + "**\n" +
                    "• Año " + anioActual + ": **" + countActual + "**\n" +
                    "• Las matrículas son **virtuales**\n\n" +
                    " 'Como me matriculo' → Ver guía completa";
        }

        // ============================================================
        // 12. AYUDA GENERAL
        // ============================================================
        if (preguntaLower.matches(".*(ayuda|que puedes hacer|que haces|funcionalidades|preguntas|que sabes).*")) {
            return "🤖 **¿Qué puedo hacer por ti?**\n\n" +
                    "📌 **Matrícula:** 'Como me matriculo'\n" +
                    "📌 **Documentos:** 'Que documentos necesito'\n" +
                    "📌 **Mensualidad:** 'Cual es la mensualidad'\n" +
                    "📌 **Contacto:** 'Donde queda el colegio'\n" +
                    "📌 **Horarios:** 'Horario de clases'\n" +
                    "📌 **Cursos:** 'Que cursos hay'\n" +
                    "📌 **Estadísticas:** 'Cuantos estudiantes hay'";
        }

        // ============================================================
        // 13. DESPEDIDA
        // ============================================================
        if (preguntaLower.matches(".*(gracias|adios|chao|hasta luego|bye).*")) {
            return "¡De nada! Fue un placer ayudarte. 😊\n" +
                    "Si necesitas algo más, aquí estoy. ¡Hasta luego!";
        }

        // ============================================================
        // 14. RESPUESTA POR DEFECTO
        // ============================================================
        System.out.println("❌ CONSULTA SERVICE - No se encontró respuesta para: " + pregunta);
        return "No encontré información sobre eso. ¿Puedes ser más específico?\n\n" +
                "📌 **Preguntas que puedo responder:**\n" +
                "• Matrícula: 'Como me matriculo'\n" +
                "• Mensualidad: 'Cual es la mensualidad'\n" +
                "• Documentos: 'Que documentos necesito'\n" +
                "• Contacto: 'Donde queda el colegio'\n" +
                "• Horarios: 'Horario de clases'\n" +
                "• Cursos: 'Que cursos hay'\n" +
                "• Ayuda: 'Que puedes hacer'";
    }

    // ============================================================
    // GUÍA DE MATRÍCULA VIRTUAL
    // ============================================================

    private String generarGuiaMatriculaVirtual() {
        return """
            📋 **Guía de Matrícula Virtual - I.E. San Carlos**

            La matrícula es **100% VIRTUAL**. Sigue estos pasos:

            1️⃣ **Ingresa al formulario:**
               🔗 [Matrícula Virtual](http://localhost:8080/api/matricula/matricula)
               💡 No necesitas usuario ni contraseña

            2️⃣ **Completa el formulario:**
               📝 DNI, nombres, apellidos, fecha de nacimiento, celular
               📝 Datos del apoderado (DNI, nombres, teléfono, email, dirección)
               📝 Selecciona el grado, sección y turno

            3️⃣ **Adjunta el voucher de pago:**
               💰 Pago: S/. 200.00 (mensualidad)
               📎 Sube el comprobante (JPG, PNG o PDF)

            4️⃣ **Acepta los términos y condiciones**
               ✅ Lee y acepta los términos antes de enviar

            5️⃣ **Revisa y envía:**
               ✅ Verifica todos los datos antes de enviar
               📨 Recibirás un correo de confirmación

            6️⃣ **Espera la aprobación:**
               ⏳ El administrador revisará tu solicitud (24-48 horas)
               📩 Te llegarán tus credenciales de acceso por correo

            7️⃣ **Accede a la Intranet:**
               🔗 [Iniciar Sesión](http://localhost:8080/login)

            📌 **¿Dudas?** Pregúntame sobre documentos, mensualidad o requisitos.
            🔗 [Ir a Matrícula Virtual](http://localhost:8080/api/matricula/matricula)
            """;
    }

    // ============================================================
    // MÉTODO PARA CONVERTIR ID DE GRADO A TEXTO (SISTEMA PERUANO)
    // ============================================================
    //  ESTE MÉTODO ESTÁ FUERA DE buscarRespuesta() - ¡CORRECTO!
    // ============================================================

    private String convertirGrado(Integer idGrado) {
        if (idGrado == null) return "Grado no especificado";

        // Primaria: 1-6
        if (idGrado >= 1 && idGrado <= 6) {
            return idGrado + "° de Primaria";
        }
        // Secundaria: 7-11 se convierten a 1°-5° de Secundaria
        if (idGrado >= 7 && idGrado <= 11) {
            int gradoSecundaria = idGrado - 6; // 7→1, 8→2, 9→3, 10→4, 11→5
            return gradoSecundaria + "° de Secundaria";
        }
        return "Grado " + idGrado;
    }

    // ============================================================
    // MÉTODOS AUXILIARES
    // ============================================================

    private String extraerNombre(String pregunta) {
        String[] indicadores = {"estudiante", "alumno", "profesor", "docente", "maestro",
                "curso", "materia", "asignatura", "llamado", "nombre",
                "de", "del", "de la"};

        String preguntaLower = pregunta.toLowerCase();

        for (String indicador : indicadores) {
            if (preguntaLower.contains(indicador)) {
                int idx = preguntaLower.indexOf(indicador) + indicador.length();
                if (idx < pregunta.length()) {
                    String resto = pregunta.substring(idx).trim();
                    resto = resto.replaceAll("[¿?¡!.,;]", "").trim();
                    if (!resto.isEmpty() && resto.length() > 1) {
                        return resto;
                    }
                }
            }
        }

        String[] palabras = pregunta.split("\\s+");
        for (String palabra : palabras) {
            palabra = palabra.replaceAll("[¿?¡!.,;]", "").trim();
            if (palabra.length() >= 3 && !esPalabraComun(palabra.toLowerCase())) {
                return palabra;
            }
        }

        return null;
    }

    private boolean esPalabraComun(String palabra) {
        String[] comunes = {"el", "la", "los", "las", "de", "del", "en", "con", "por", "para",
                "que", "cual", "quien", "como", "cuando", "donde", "hay", "son",
                "es", "esta", "estos", "estas", "tiene", "tienen", "tengo",
                "puedo", "puedes", "puede", "pueden", "quiero", "quieres", "quiere",
                "necesito", "necesitas", "necesita", "buscar", "consultar", "ver",
                "mostrar", "listar", "todos", "todas", "algunos", "algunas"};

        for (String c : comunes) {
            if (palabra.equals(c)) {
                return true;
            }
        }
        return false;
    }
}