package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.EstudianteDTO;
import com.universidad.sistema_academico.dto.NotaDTO;
import com.universidad.sistema_academico.model.*;
import com.universidad.sistema_academico.repository.*;
import jakarta.validation.Valid;
import com.universidad.sistema_academico.service.EstudianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador unificado para Estudiantes.
 * Maneja tanto vistas MVC como API REST.
 */
@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    // ==================== SERVICIOS Y REPOSITORIOS ====================

    private final EstudianteService estudianteService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // ==================== MÉTODO AUXILIAR ====================

    /**
     * Método para obtener el estudiante autenticado
     */
    private Estudiante getEstudianteAutenticado(Authentication authentication) {
        System.out.println("=== getEstudianteAutenticado ===");
        String email = authentication.getName();
        System.out.println("Email autenticado: " + email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        System.out.println("Usuario encontrado: ID=" + usuario.getId() + ", Rol=" + usuario.getRol());

        Estudiante estudiante = estudianteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado para usuario ID: " + usuario.getId()));

        System.out.println("Estudiante encontrado: ID=" + estudiante.getIdEstudiante() +
                ", Nombre=" + estudiante.getNombres() + " " + estudiante.getApellidoPaterno());
        System.out.println("=== Fin getEstudianteAutenticado ===");

        return estudiante;
    }

    /**
     * Método helper para convertir número de grado a texto
     */
    private String obtenerNombreGrado(Integer idGrado) {
        if (idGrado == null) return "Sin asignar";

        return switch (idGrado) {
            case 1 -> "Primero de Primaria";
            case 2 -> "Segundo de Primaria";
            case 3 -> "Tercero de Primaria";
            case 4 -> "Cuarto de Primaria";
            case 5 -> "Quinto de Primaria";
            case 6 -> "Sexto de Primaria";
            case 7 -> "Primero de Secundaria";
            case 8 -> "Segundo de Secundaria";
            case 9 -> "Tercero de Secundaria";
            case 10 -> "Cuarto de Secundaria";
            case 11 -> "Quinto de Secundaria";
            default -> "Grado " + idGrado;
        };
    }

    /**
     * Método para obtener el periodo académico actual
     */
    private String getPeriodoAcademicoActual() {
        return String.valueOf(java.time.Year.now().getValue());
    }

    // ==================== VISTAS MVC ====================

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        try {
            Estudiante estudiante = getEstudianteAutenticado(authentication);

            Optional<Matricula> matriculaActivaOpt = matriculaRepository.findMatriculaActivaByEstudianteId(estudiante.getIdEstudiante());

            boolean tieneMatriculaActiva = matriculaActivaOpt.isPresent();

            final Integer gradoActual;
            final String turnoActual;
            String nombreGradoActual = "Sin asignar";

            if (matriculaActivaOpt.isPresent()) {
                Matricula matriculaActiva = matriculaActivaOpt.get();
                gradoActual = matriculaActiva.getIdGrado();
                turnoActual = matriculaActiva.getTurno();
                nombreGradoActual = obtenerNombreGrado(gradoActual);
            } else {
                gradoActual = null;
                turnoActual = null;
            }

            List<Curso> cursos = new ArrayList<>();
            if (tieneMatriculaActiva && gradoActual != null && turnoActual != null) {
                final Integer grado = gradoActual;
                final String turno = turnoActual;

                cursos = cursoRepository.findAll().stream()
                        .filter(curso -> curso.getIdGrado() != null &&
                                curso.getIdGrado().equals(grado) &&
                                curso.getTurno() != null &&
                                curso.getTurno().equalsIgnoreCase(turno))  // <-- equalsIgnoreCase
                        .collect(Collectors.toList());
            }

            int totalCursos = cursos.size();
            long cursosActivos = cursos.stream()
                    .filter(c -> "ACTIVO".equals(c.getEstado()) || "ACTIVA".equals(c.getEstado()))
                    .count();

            model.addAttribute("estudiante", estudiante);
            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("totalCursos", totalCursos);
            model.addAttribute("cursosActivos", cursosActivos);
            model.addAttribute("gradoActual", gradoActual);
            model.addAttribute("turnoActual", turnoActual);
            model.addAttribute("nombreGradoActual", nombreGradoActual);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("modulo", "dashboard");
            model.addAttribute("tituloPagina", "Dashboard");

            return "estudiante/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/cursos")
    public String misCursos(Authentication authentication,
                            @RequestParam(required = false) String buscar,
                            @RequestParam(required = false) String area,
                            Model model) {
        try {
            System.out.println("\n\n========== INICIO MIS CURSOS ==========");

            Estudiante estudiante = getEstudianteAutenticado(authentication);
            System.out.println("Estudiante ID: " + estudiante.getIdEstudiante());
            System.out.println("Estudiante: " + estudiante.getNombres() + " " + estudiante.getApellidoPaterno());
            System.out.println("Email institucional: " + estudiante.getEmailInstitucional());

            Optional<Matricula> matriculaActivaOpt = matriculaRepository.findMatriculaActivaByEstudianteId(estudiante.getIdEstudiante());

            boolean tieneMatriculaActiva = matriculaActivaOpt.isPresent();
            System.out.println("¿Tiene matrícula activa? " + tieneMatriculaActiva);

            final Integer gradoActual;
            final String turnoActual;
            String nombreGradoActual = "Sin asignar";

            if (matriculaActivaOpt.isPresent()) {
                Matricula matriculaActiva = matriculaActivaOpt.get();
                gradoActual = matriculaActiva.getIdGrado();
                turnoActual = matriculaActiva.getTurno();
                nombreGradoActual = obtenerNombreGrado(gradoActual);
                System.out.println("Grado en matrícula: " + gradoActual);
                System.out.println("Turno en matrícula: " + turnoActual);
                System.out.println("Nombre grado: " + nombreGradoActual);
            } else {
                gradoActual = null;
                turnoActual = null;
                System.out.println("NO hay matrícula activa para este estudiante");
            }

            List<Curso> cursos = new ArrayList<>();
            if (tieneMatriculaActiva && gradoActual != null && turnoActual != null) {
                final Integer grado = gradoActual;
                final String turno = turnoActual;

                // Obtener todos los cursos y filtrar
                List<Curso> todosLosCursos = cursoRepository.findAll();
                System.out.println("Total cursos en BD: " + todosLosCursos.size());

                cursos = todosLosCursos.stream()
                        .filter(curso -> curso.getIdGrado() != null &&
                                curso.getIdGrado().equals(grado) &&
                                curso.getTurno() != null &&
                                curso.getTurno().equalsIgnoreCase(turno))  // <-- equalsIgnoreCase
                        .collect(Collectors.toList());

                System.out.println("Cursos encontrados para grado " + grado + " y turno " + turno + ": " + cursos.size());
                for (Curso c : cursos) {
                    System.out.println("  - " + c.getNombreCurso() + " | ID: " + c.getIdCurso() +
                            " | Grado: " + c.getIdGrado() + " | Turno: " + c.getTurno() +
                            " | Estado: " + c.getEstado() + " | Eliminado: " + c.isEliminado());
                }
            } else {
                System.out.println("No se puede filtrar cursos: tieneMatriculaActiva=" + tieneMatriculaActiva +
                        ", gradoActual=" + gradoActual + ", turnoActual=" + turnoActual);
            }

            if (buscar != null && !buscar.isEmpty()) {
                System.out.println("Aplicando filtro de búsqueda: " + buscar);
                cursos = cursos.stream()
                        .filter(c -> c.getNombreCurso().toLowerCase().contains(buscar.toLowerCase()) ||
                                c.getCodigoCurso().toLowerCase().contains(buscar.toLowerCase()))
                        .collect(Collectors.toList());
                System.out.println("Cursos después de filtro de búsqueda: " + cursos.size());
            }

            if (area != null && !area.isEmpty()) {
                System.out.println("Aplicando filtro de área: " + area);
                cursos = cursos.stream()
                        .filter(c -> c.getArea() != null &&
                                c.getArea().toLowerCase().contains(area.toLowerCase()))
                        .collect(Collectors.toList());
                System.out.println("Cursos después de filtro de área: " + cursos.size());
            }

            long cursosActivos = cursos.stream()
                    .filter(c -> "ACTIVO".equals(c.getEstado()) || "ACTIVA".equals(c.getEstado()))
                    .count();

            System.out.println("Cursos activos: " + cursosActivos);
            System.out.println("Total cursos a mostrar en vista: " + cursos.size());
            System.out.println("========== FIN MIS CURSOS ==========\n\n");

            model.addAttribute("estudiante", estudiante);
            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("totalCursosInscritos", cursos.size());
            model.addAttribute("cursosActivos", cursosActivos);
            model.addAttribute("gradoActual", gradoActual);
            model.addAttribute("turnoActual", turnoActual);
            model.addAttribute("nombreGradoActual", nombreGradoActual);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("modulo", "mis-cursos");
            model.addAttribute("tituloPagina", "Mis Cursos");

            return "estudiante/mis-cursos";
        } catch (Exception e) {
            System.out.println("ERROR en misCursos: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/matricula")
    public String miMatricula(Authentication authentication, Model model) {
        try {
            Estudiante estudiante = getEstudianteAutenticado(authentication);

            List<Matricula> matriculas = matriculaRepository.findHistorialByEstudianteId(estudiante.getIdEstudiante());

            Optional<Matricula> matriculaActivaOpt = matriculas.stream()
                    .filter(m -> "ACTIVA".equals(m.getEstado()))
                    .findFirst();

            Matricula matriculaActiva = matriculaActivaOpt.orElse(null);
            boolean tieneMatriculaActiva = matriculaActiva != null;

            model.addAttribute("estudiante", estudiante);
            model.addAttribute("matriculas", matriculas != null ? matriculas : List.of());
            model.addAttribute("matriculaActiva", matriculaActiva);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("modulo", "mi-matricula");
            model.addAttribute("tituloPagina", "Mi Matrícula");

            if (matriculaActiva != null) {
                model.addAttribute("nombreGradoActual", obtenerNombreGrado(matriculaActiva.getIdGrado()));
            } else {
                model.addAttribute("nombreGradoActual", "Sin asignar");
            }

            Map<Long, String> nombresGrados = new HashMap<>();
            if (matriculas != null) {
                for (Matricula m : matriculas) {
                    nombresGrados.put(m.getIdMatricula(), obtenerNombreGrado(m.getIdGrado()));
                }
            }
            model.addAttribute("nombresGrados", nombresGrados);

            return "estudiante/mi-matricula";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/curso/{id}/detalle")
    public String detalleCurso(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            System.out.println("\n\n========== INICIO DETALLE CURSO ==========");
            System.out.println("ID Curso solicitado: " + id);

            Estudiante estudiante = getEstudianteAutenticado(authentication);
            System.out.println("Estudiante ID: " + estudiante.getIdEstudiante());
            System.out.println("Estudiante: " + estudiante.getNombres() + " " + estudiante.getApellidoPaterno());

            // Buscar el curso
            Optional<Curso> cursoOpt = cursoRepository.findById(id);
            if (!cursoOpt.isPresent()) {
                System.out.println("ERROR: Curso no encontrado con ID: " + id);
                throw new RuntimeException("Curso no encontrado");
            }

            Curso curso = cursoOpt.get();
            System.out.println("Curso encontrado: " + curso.getNombreCurso());
            System.out.println("  - Código: " + curso.getCodigoCurso());
            System.out.println("  - Grado: " + curso.getIdGrado());
            System.out.println("  - Turno: " + curso.getTurno());
            System.out.println("  - Sección: " + curso.getSeccion());
            System.out.println("  - Estado: " + curso.getEstado());
            System.out.println("  - Capacidad Máxima: " + curso.getCapacidadMaxima());
            System.out.println("  - Alumnos Actuales: " + curso.getAlumnosActuales());
            System.out.println("  - Cupos Disponibles: " + curso.getCuposDisponibles());
            System.out.println("  - Porcentaje Ocupación: " + curso.getPorcentajeOcupacion() + "%");
            System.out.println("  - ¿Hay cupo?: " + curso.hayCupo());

            // Obtener información del docente
            String docenteNombre = "Sin asignar";
            if (curso.getDocente() != null) {
                docenteNombre = curso.getDocente().getNombres() + " " +
                        curso.getDocente().getApellidoPaterno();
                System.out.println("  - Docente: " + docenteNombre);
                System.out.println("  - Docente ID: " + curso.getDocente().getIdDocente());
            } else {
                System.out.println("  - Docente: Sin asignar");
            }

            // Verificar si el estudiante ya está matriculado en este curso
            boolean yaMatriculado = false;
            // Aquí puedes agregar lógica para verificar si el estudiante ya está en este curso
            // Por ejemplo, si tienes una tabla de matrícula de cursos
            System.out.println("  - ¿Ya matriculado?: " + yaMatriculado);

            // ========== SIMULAR ACTUALIZACIÓN DE CUPOS ==========
            // Si el estudiante decide matricularse, esto es lo que pasaría
            System.out.println("\n=== SIMULACIÓN DE ACTUALIZACIÓN DE CUPOS ===");
            System.out.println("Si el estudiante se matricula en este curso:");

            int alumnosActuales = curso.getAlumnosActuales() != null ? curso.getAlumnosActuales() : 0;
            int capacidadMaxima = curso.getCapacidadMaxima() != null ? curso.getCapacidadMaxima() : 36;
            int cuposDisponibles = capacidadMaxima - alumnosActuales;

            System.out.println("  - Alumnos actuales: " + alumnosActuales);
            System.out.println("  - Capacidad máxima: " + capacidadMaxima);
            System.out.println("  - Cupos disponibles: " + cuposDisponibles);

            if (cuposDisponibles > 0) {
                System.out.println("  -  Hay cupo disponible. Se puede matricular.");
                System.out.println("  - Nuevos alumnos actuales: " + (alumnosActuales + 1));
                System.out.println("  - Nuevos cupos disponibles: " + (cuposDisponibles - 1));
                System.out.println("  - Nuevo porcentaje ocupación: " +
                        String.format("%.1f", ((alumnosActuales + 1) * 100.0 / capacidadMaxima)) + "%");
            } else {
                System.out.println("  -  NO hay cupo disponible. Curso lleno.");
            }

            // ========== ACTUALIZAR CUPOS EN LA BASE DE DATOS (SI SE MATRICULA) ==========
            // Si quieres que el estudiante se matricule automáticamente al ver el detalle,
            // puedes descomentar este código:
        /*
        if (cuposDisponibles > 0 && !yaMatriculado) {
            System.out.println("\n=== ACTUALIZANDO CUPOS EN BD ===");
            curso.setAlumnosActuales(alumnosActuales + 1);
            cursoRepository.save(curso);
            System.out.println("  - Alumnos actuales actualizados a: " + curso.getAlumnosActuales());
            System.out.println("  - Cupos disponibles actualizados a: " + curso.getCuposDisponibles());

            // Registrar en actividad
            registrarActividad(estudiante.getEmailInstitucional(),
                "MATRICULAR_CURSO",
                "Curso",
                "Estudiante " + estudiante.getNombres() + " se matriculó en " + curso.getNombreCurso());
        }
        */

            // Agregar atributos al modelo
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("curso", curso);
            model.addAttribute("docenteNombre", docenteNombre);
            model.addAttribute("hayCupo", curso.hayCupo());
            model.addAttribute("cuposDisponibles", curso.getCuposDisponibles());
            model.addAttribute("porcentajeOcupacion", curso.getPorcentajeOcupacion());
            model.addAttribute("yaMatriculado", yaMatriculado);
            model.addAttribute("modulo", "mis-cursos");
            model.addAttribute("tituloPagina", "Detalle del Curso");

            System.out.println("========== FIN DETALLE CURSO ==========\n\n");

            return "estudiante/curso-detalle";
        } catch (Exception e) {
            System.out.println("ERROR en detalleCurso: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/horario")
    public String miHorario(Authentication authentication, Model model) {
        try {
            System.out.println("\n\n========== INICIO MI HORARIO ==========");

            Estudiante estudiante = getEstudianteAutenticado(authentication);
            System.out.println("Estudiante ID: " + estudiante.getIdEstudiante());
            System.out.println("Estudiante: " + estudiante.getNombres() + " " + estudiante.getApellidoPaterno());

            Optional<Matricula> matriculaActivaOpt = matriculaRepository.findMatriculaActivaByEstudianteId(estudiante.getIdEstudiante());

            boolean tieneMatriculaActiva = matriculaActivaOpt.isPresent();
            System.out.println("¿Tiene matrícula activa? " + tieneMatriculaActiva);

            final Integer gradoActual;
            final String turnoActual;
            String nombreGradoActual = "Sin asignar";
            List<Curso> cursos = new ArrayList<>();

            if (matriculaActivaOpt.isPresent()) {
                Matricula matriculaActiva = matriculaActivaOpt.get();
                gradoActual = matriculaActiva.getIdGrado();
                turnoActual = matriculaActiva.getTurno();
                nombreGradoActual = obtenerNombreGrado(gradoActual);

                System.out.println("Grado en matrícula: " + gradoActual);
                System.out.println("Turno en matrícula: " + turnoActual);
                System.out.println("Nombre grado: " + nombreGradoActual);

                final Integer grado = gradoActual;
                final String turno = turnoActual;

                List<Curso> todosLosCursos = cursoRepository.findAll();
                System.out.println("Total cursos en BD: " + todosLosCursos.size());

                cursos = todosLosCursos.stream()
                        .filter(curso -> curso.getIdGrado() != null &&
                                curso.getIdGrado().equals(grado) &&
                                curso.getTurno() != null &&
                                curso.getTurno().equalsIgnoreCase(turno))
                        .collect(Collectors.toList());

                System.out.println("Cursos encontrados para grado " + grado + " y turno " + turno + ": " + cursos.size());
                for (Curso c : cursos) {
                    System.out.println("  - " + c.getNombreCurso() + " | ID: " + c.getIdCurso() +
                            " | Grado: " + c.getIdGrado() + " | Turno: " + c.getTurno() +
                            " | Horario: " + c.getHorario());
                }
            } else {
                gradoActual = null;
                turnoActual = null;
                System.out.println("NO hay matrícula activa para este estudiante");
            }

            // ========== PROCESAR HORARIOS DE MANERA DINÁMICA ==========
            // Estructura: Dia -> HoraInicio -> Lista de cursos
            // Usamos TreeMap para ordenar automáticamente por hora
            Map<String, Map<Integer, List<Curso>>> horarioPorDia = new LinkedHashMap<>();

            // Días de la semana en orden
            String[] dias = {"LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES"};

            // Inicializar mapa para cada día
            for (String dia : dias) {
                horarioPorDia.put(dia, new TreeMap<>());
            }

            System.out.println("\n=== PROCESANDO HORARIOS DE CURSOS (DINÁMICO) ===");
            for (Curso curso : cursos) {
                String horario = curso.getHorario();
                System.out.println("Procesando curso: " + curso.getNombreCurso());
                System.out.println("  Horario original: " + horario);

                if (horario != null && !horario.isEmpty()) {
                    // Limpiar y normalizar
                    String horarioClean = horario.replaceAll("\\s+", "").toUpperCase();
                    boolean asignado = false;

                    // Buscar el día
                    for (String dia : dias) {
                        if (horarioClean.contains(dia)) {
                            // Extraer las horas del horario (ej: "7-10" -> horaInicio=7, horaFin=10)
                            String[] partes = horarioClean.split(dia);
                            if (partes.length > 1) {
                                String rangoHoras = partes[1].trim();
                                // Buscar patrón de horas (ej: "7-10", "8-9", "7-12")
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{1,2})[-–](\\d{1,2})");
                                java.util.regex.Matcher matcher = pattern.matcher(rangoHoras);

                                if (matcher.find()) {
                                    int horaInicio = Integer.parseInt(matcher.group(1));
                                    int horaFin = Integer.parseInt(matcher.group(2));

                                    // Agregar el curso a la hora de inicio
                                    Map<Integer, List<Curso>> cursosPorHora = horarioPorDia.get(dia);
                                    if (!cursosPorHora.containsKey(horaInicio)) {
                                        cursosPorHora.put(horaInicio, new ArrayList<>());
                                    }
                                    cursosPorHora.get(horaInicio).add(curso);

                                    System.out.println("  -> Asignado a: " + dia + " - " + horaInicio + ":00 a " + horaFin + ":00");
                                    asignado = true;
                                } else {
                                    // Si no encuentra patrón, intentar con formato "7-9" sin espacio
                                    if (rangoHoras.contains("-") || rangoHoras.contains("–")) {
                                        String[] horas = rangoHoras.replace("–", "-").split("-");
                                        if (horas.length == 2) {
                                            try {
                                                int horaInicio = Integer.parseInt(horas[0].trim());
                                                int horaFin = Integer.parseInt(horas[1].trim());

                                                Map<Integer, List<Curso>> cursosPorHora = horarioPorDia.get(dia);
                                                if (!cursosPorHora.containsKey(horaInicio)) {
                                                    cursosPorHora.put(horaInicio, new ArrayList<>());
                                                }
                                                cursosPorHora.get(horaInicio).add(curso);

                                                System.out.println("  -> Asignado a: " + dia + " - " + horaInicio + ":00 a " + horaFin + ":00");
                                                asignado = true;
                                            } catch (NumberFormatException e) {
                                                System.out.println("  -> Error al parsear horas: " + rangoHoras);
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (!asignado) {
                        System.out.println("  -> NO SE PUDO PROCESAR el horario: " + horario);
                    }
                } else {
                    System.out.println("  -> Sin horario asignado");
                }
            }

            // Construir lista de horas únicas para la tabla
            Set<Integer> horasUnicas = new TreeSet<>();
            for (Map<Integer, List<Curso>> mapaDia : horarioPorDia.values()) {
                horasUnicas.addAll(mapaDia.keySet());
            }

            // Crear un mapa de horas con las horas formateadas
            Map<Integer, String> horasFormateadas = new LinkedHashMap<>();
            List<Integer> horasOrdenadas = new ArrayList<>(horasUnicas);
            Collections.sort(horasOrdenadas);

            // Para cada hora, mostrar el rango (hasta la siguiente hora o hasta que termine el curso)
            for (Integer hora : horasOrdenadas) {
                // Determinar la duración del bloque (podría ser variable)
                // Usamos 1 hora por defecto, pero el curso define su duración
                horasFormateadas.put(hora, String.format("%02d:00 - %02d:00", hora, hora + 1));
            }

            System.out.println("\n=== RESUMEN HORARIO COMPLETO (DINÁMICO) ===");
            for (String dia : dias) {
                System.out.println(dia + ":");
                Map<Integer, List<Curso>> cursosPorHora = horarioPorDia.get(dia);
                if (cursosPorHora.isEmpty()) {
                    System.out.println("  Sin cursos");
                } else {
                    for (Integer hora : horasOrdenadas) {
                        List<Curso> cursosEnHora = cursosPorHora.get(hora);
                        if (cursosEnHora != null && !cursosEnHora.isEmpty()) {
                            System.out.println("  " + hora + ":00 - " + (hora+1) + ":00: " + cursosEnHora.size() + " curso(s)");
                            for (Curso c : cursosEnHora) {
                                System.out.println("    - " + c.getNombreCurso() + " (" + c.getHorario() + ")");
                            }
                        }
                    }
                }
            }
            System.out.println("========== FIN MI HORARIO ==========\n\n");

            model.addAttribute("estudiante", estudiante);
            model.addAttribute("cursos", cursos);
            model.addAttribute("horarioPorDia", horarioPorDia);
            model.addAttribute("horasFormateadas", horasFormateadas);
            model.addAttribute("horasOrdenadas", horasOrdenadas);
            model.addAttribute("dias", dias);
            model.addAttribute("nombreGradoActual", nombreGradoActual);
            model.addAttribute("turnoActual", turnoActual);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("modulo", "mi-horario");
            model.addAttribute("tituloPagina", "Mi Horario");

            return "estudiante/mi-horario";
        } catch (Exception e) {
            System.out.println("ERROR en miHorario: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    @GetMapping("/notas")
    public String misNotas(Authentication authentication, Model model) {
        try {
            Estudiante estudiante = getEstudianteAutenticado(authentication);
            String periodoActual = getPeriodoAcademicoActual();

            Optional<Matricula> matriculaActivaOpt = matriculaRepository
                    .findMatriculaActivaByEstudianteId(estudiante.getIdEstudiante());

            List<NotaDTO> notas = new ArrayList<>();
            String nombreGradoActual = "Sin asignar";
            boolean tieneMatriculaActiva = matriculaActivaOpt.isPresent();

            // Contadores
            long totalAprobados = 0;
            long totalRecuperacion = 0;
            long totalDesaprobados = 0;

            if (matriculaActivaOpt.isPresent()) {
                Matricula matricula = matriculaActivaOpt.get();
                nombreGradoActual = obtenerNombreGrado(matricula.getIdGrado());

                List<Curso> cursos = cursoRepository.findAll().stream()
                        .filter(curso -> curso.getIdGrado() != null &&
                                curso.getIdGrado().equals(matricula.getIdGrado()) &&
                                curso.getTurno() != null &&
                                curso.getTurno().equalsIgnoreCase(matricula.getTurno()))  // <-- equalsIgnoreCase
                        .collect(Collectors.toList());

                for (Curso curso : cursos) {
                    NotaDTO nota = new NotaDTO();
                    nota.setIdCurso(curso.getIdCurso());
                    nota.setNombreCurso(curso.getNombreCurso());
                    nota.setCodigoCurso(curso.getCodigoCurso());

                    List<Nota> notasDB = notaRepository
                            .findByEstudianteIdEstudianteAndCursoIdCursoAndPeriodoAcademico(
                                    estudiante.getIdEstudiante(),
                                    curso.getIdCurso(),
                                    periodoActual);

                    Double b1 = null, b2 = null, b3 = null, b4 = null;

                    for (Nota n : notasDB) {
                        double valor = n.getNota().doubleValue();
                        switch (n.getBimestre()) {
                            case 1 -> b1 = valor;
                            case 2 -> b2 = valor;
                            case 3 -> b3 = valor;
                            case 4 -> b4 = valor;
                        }
                    }

                    nota.setBimestre1(b1);
                    nota.setBimestre2(b2);
                    nota.setBimestre3(b3);
                    nota.setBimestre4(b4);

                    List<Double> notasValidas = new ArrayList<>();
                    if (b1 != null) notasValidas.add(b1);
                    if (b2 != null) notasValidas.add(b2);
                    if (b3 != null) notasValidas.add(b3);
                    if (b4 != null) notasValidas.add(b4);

                    if (!notasValidas.isEmpty()) {
                        double promedio = notasValidas.stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0);
                        nota.setPromedioFinal(Math.round(promedio * 10.0) / 10.0);

                        if (nota.getPromedioFinal() >= 11) {
                            nota.setEstado("APROBADO");
                            totalAprobados++;
                        } else if (nota.getPromedioFinal() >= 7) {
                            nota.setEstado("RECUPERACIÓN");
                            totalRecuperacion++;
                        } else {
                            nota.setEstado("DESAPROBADO");
                            totalDesaprobados++;
                        }
                    } else {
                        nota.setPromedioFinal(null);
                        nota.setEstado("SIN NOTAS");
                    }

                    notas.add(nota);
                }
            }

            model.addAttribute("estudiante", estudiante);
            model.addAttribute("notas", notas);
            model.addAttribute("totalAprobados", totalAprobados);
            model.addAttribute("totalRecuperacion", totalRecuperacion);
            model.addAttribute("totalDesaprobados", totalDesaprobados);
            model.addAttribute("nombreGradoActual", nombreGradoActual);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("periodoActual", periodoActual);
            model.addAttribute("modulo", "mis-notas");
            model.addAttribute("tituloPagina", "Mis Notas");

            return "estudiante/mis-notas";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/asistencia")
    public String miAsistencia(Authentication authentication, Model model) {
        try {
            Estudiante estudiante = getEstudianteAutenticado(authentication);

            Long estudianteId = estudiante.getIdEstudiante();

            System.out.println("=== DEBUG ASISTENCIA ===");
            System.out.println("Estudiante ID: " + estudianteId);

            Optional<Matricula> matriculaActivaOpt = matriculaRepository
                    .findMatriculaActivaByEstudianteId(estudianteId);

            boolean tieneMatriculaActiva = matriculaActivaOpt.isPresent();
            String nombreGradoActual = "Sin asignar";
            Map<String, Object> asistenciaData = new HashMap<>();

            if (matriculaActivaOpt.isPresent()) {
                Matricula matricula = matriculaActivaOpt.get();
                nombreGradoActual = obtenerNombreGrado(matricula.getIdGrado());

                List<Curso> cursos = cursoRepository.findAll().stream()
                        .filter(curso -> curso.getIdGrado() != null &&
                                curso.getIdGrado().equals(matricula.getIdGrado()) &&
                                curso.getTurno() != null &&
                                curso.getTurno().equalsIgnoreCase(matricula.getTurno()))
                        .collect(Collectors.toList());

                System.out.println("Cursos encontrados: " + cursos.size());

                List<Map<String, Object>> asistenciaCursos = new ArrayList<>();
                for (Curso curso : cursos) {
                    Map<String, Object> asistenciaCurso = new HashMap<>();
                    asistenciaCurso.put("curso", curso);

                    Long cursoId = curso.getIdCurso();
                    System.out.println("Procesando curso: " + curso.getNombreCurso() + " (ID: " + cursoId + ")");

                    // Cambiar de long a Long para evitar problemas de casting
                    Long totalClases = asistenciaRepository
                            .countTotalClasesByCurso(estudianteId, cursoId);

                    System.out.println("Total clases: " + totalClases);

                    if (totalClases != null && totalClases > 0) {
                        long asistencias = asistenciaRepository
                                .countByEstudianteIdEstudianteAndCursoIdCursoAndEstado(
                                        estudianteId,
                                        cursoId,
                                        "PRESENTE");

                        long faltas = totalClases - asistencias;
                        double porcentaje = (asistencias * 100.0) / totalClases;

                        asistenciaCurso.put("totalClases", totalClases);
                        asistenciaCurso.put("asistencias", asistencias);
                        asistenciaCurso.put("faltas", faltas);
                        asistenciaCurso.put("porcentaje", Math.round(porcentaje * 10.0) / 10.0);
                    } else {
                        asistenciaCurso.put("totalClases", 0L);
                        asistenciaCurso.put("asistencias", 0L);
                        asistenciaCurso.put("faltas", 0L);
                        asistenciaCurso.put("porcentaje", 0.0);
                    }

                    asistenciaCursos.add(asistenciaCurso);
                }

                asistenciaData.put("cursos", asistenciaCursos);

                // ========== CORREGIDO: Calcular promedio general correctamente ==========
                double promedioGeneral = 0.0;
                int cursosConClases = 0;

                for (Map<String, Object> cursoData : asistenciaCursos) {
                    Long totalClases = (Long) cursoData.get("totalClases");
                    if (totalClases != null && totalClases > 0) {
                        Double porcentaje = (Double) cursoData.get("porcentaje");
                        if (porcentaje != null) {
                            promedioGeneral += porcentaje;
                            cursosConClases++;
                        }
                    }
                }

                if (cursosConClases > 0) {
                    promedioGeneral = promedioGeneral / cursosConClases;
                }

                asistenciaData.put("promedioGeneral", Math.round(promedioGeneral * 10.0) / 10.0);

                System.out.println("Promedio general: " + asistenciaData.get("promedioGeneral"));
            }

            model.addAttribute("estudiante", estudiante);
            model.addAttribute("asistenciaData", asistenciaData);
            model.addAttribute("nombreGradoActual", nombreGradoActual);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("modulo", "mi-asistencia");
            model.addAttribute("tituloPagina", "Mi Asistencia");

            return "estudiante/mi-asistencia";
        } catch (Exception e) {
            System.out.println("ERROR en miAsistencia: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    // ==================== API REST ====================

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<EstudianteDTO>> listarTodos() {
        List<EstudianteDTO> estudiantes = estudianteService.listarTodos();
        return ResponseEntity.ok(estudiantes);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            EstudianteDTO estudiante = estudianteService.buscarPorId(id);
            return ResponseEntity.ok(estudiante);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> guardar(@Valid @RequestBody EstudianteDTO estudianteDTO) {
        try {
            EstudianteDTO estudianteGuardado = estudianteService.guardar(estudianteDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(estudianteGuardado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody EstudianteDTO estudianteDTO) {
        try {
            estudianteDTO.setIdEstudiante(id);
            EstudianteDTO estudianteActualizado = estudianteService.guardar(estudianteDTO);
            return ResponseEntity.ok(estudianteActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            estudianteService.eliminar(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Estudiante eliminado exitosamente");
            response.put("id", id.toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    // ==================== MÉTODO AUXILIAR ====================

    private Map<String, String> crearMensajeError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}