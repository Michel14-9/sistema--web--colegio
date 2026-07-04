package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.DocenteDTO;
import com.universidad.sistema_academico.dto.NotaDTO;
import com.universidad.sistema_academico.model.*;
import com.universidad.sistema_academico.repository.*;
import com.universidad.sistema_academico.service.DocenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/docente")
public class DocenteController {

    private final DocenteService docenteService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Docente getDocenteAutenticado(Authentication authentication) {
        System.out.println("=== getDocenteAutenticado ===");
        String email = authentication.getName();
        System.out.println("Email autenticado: " + email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        System.out.println("Usuario encontrado: ID=" + usuario.getId() + ", Rol=" + usuario.getRol());

        Docente docente = docenteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Docente no encontrado para usuario ID: " + usuario.getId()));

        System.out.println("Docente encontrado: ID=" + docente.getIdDocente() +
                ", Nombre=" + docente.getNombres() + " " + docente.getApellidoPaterno());
        System.out.println("=== Fin getDocenteAutenticado ===");

        return docente;
    }

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

    private String getPeriodoAcademicoActual() {
        return String.valueOf(java.time.Year.now().getValue());
    }

    // ==================== VISTAS MVC ====================

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        try {
            Docente docente = getDocenteAutenticado(authentication);

            List<Curso> cursos = cursoRepository.findByIdDocente(docente.getIdDocente());
            int totalCursos = cursos != null ? cursos.size() : 0;

            int totalAlumnos = matriculaRepository.countAlumnosByDocenteId(docente.getIdDocente());

            Map<Long, Integer> alumnosPorCurso = new HashMap<>();
            if (cursos != null) {
                for (Curso curso : cursos) {
                    int count = matriculaRepository.countByGradoAndTurno(curso.getIdGrado(), curso.getTurno());
                    alumnosPorCurso.put(curso.getIdCurso(), count);
                }
            }

            model.addAttribute("docente", docente);
            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("totalCursos", totalCursos);
            model.addAttribute("totalAlumnos", totalAlumnos);
            model.addAttribute("alumnosPorCurso", alumnosPorCurso);
            model.addAttribute("modulo", "dashboard");
            model.addAttribute("tituloPagina", "Dashboard");

            return "docente/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/mis-cursos")
    public String misCursos(Authentication authentication,
                            @RequestParam(required = false) String buscar,
                            @RequestParam(required = false) String area,
                            Model model) {
        try {
            System.out.println("\n\n========== INICIO MIS CURSOS DOCENTE ==========");

            Docente docente = getDocenteAutenticado(authentication);
            System.out.println("Docente ID: " + docente.getIdDocente());
            System.out.println("Docente: " + docente.getNombres() + " " + docente.getApellidoPaterno());

            List<Curso> cursos = cursoRepository.findByIdDocente(docente.getIdDocente());
            System.out.println("Total cursos asignados: " + (cursos != null ? cursos.size() : 0));

            if (cursos == null) {
                cursos = new ArrayList<>();
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

            Map<Long, Integer> alumnosPorCurso = new HashMap<>();
            for (Curso curso : cursos) {
                int count = matriculaRepository.countByGradoAndTurno(curso.getIdGrado(), curso.getTurno());
                alumnosPorCurso.put(curso.getIdCurso(), count);
            }

            System.out.println("Total cursos a mostrar: " + cursos.size());
            System.out.println("========== FIN MIS CURSOS DOCENTE ==========\n\n");

            model.addAttribute("docente", docente);
            model.addAttribute("cursos", cursos);
            model.addAttribute("alumnosPorCurso", alumnosPorCurso);
            model.addAttribute("modulo", "cursos");
            model.addAttribute("tituloPagina", "Mis Cursos");

            return "docente/mis-cursos";

        } catch (Exception e) {
            System.out.println("ERROR en misCursos: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/horario")
    public String miHorario(Authentication authentication, Model model) {
        try {
            System.out.println("\n\n========== INICIO MI HORARIO DOCENTE ==========");

            Docente docente = getDocenteAutenticado(authentication);
            System.out.println("Docente ID: " + docente.getIdDocente());
            System.out.println("Docente: " + docente.getNombres() + " " + docente.getApellidoPaterno());

            List<Curso> cursos = cursoRepository.findByIdDocente(docente.getIdDocente());
            System.out.println("Total cursos asignados: " + (cursos != null ? cursos.size() : 0));

            if (cursos == null) {
                cursos = new ArrayList<>();
            }

            Map<String, Map<Integer, List<Curso>>> horarioPorDia = new LinkedHashMap<>();
            String[] dias = {"LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES"};

            for (String dia : dias) {
                horarioPorDia.put(dia, new TreeMap<>());
            }

            System.out.println("\n=== PROCESANDO HORARIOS DE CURSOS (DINÁMICO) ===");
            int totalHoras = 0;
            for (Curso curso : cursos) {
                String horario = curso.getHorario();
                System.out.println("Procesando curso: " + curso.getNombreCurso());
                System.out.println("  Horario original: " + horario);

                if (horario != null && !horario.isEmpty()) {
                    String horarioClean = horario.replaceAll("\\s+", "").toUpperCase();
                    boolean asignado = false;

                    for (String dia : dias) {
                        if (horarioClean.contains(dia)) {
                            String[] partes = horarioClean.split(dia);
                            if (partes.length > 1) {
                                String rangoHoras = partes[1].trim();
                                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{1,2})[-–](\\d{1,2})");
                                java.util.regex.Matcher matcher = pattern.matcher(rangoHoras);

                                if (matcher.find()) {
                                    int horaInicio = Integer.parseInt(matcher.group(1));
                                    int horaFin = Integer.parseInt(matcher.group(2));
                                    totalHoras += (horaFin - horaInicio);

                                    Map<Integer, List<Curso>> cursosPorHora = horarioPorDia.get(dia);
                                    if (!cursosPorHora.containsKey(horaInicio)) {
                                        cursosPorHora.put(horaInicio, new ArrayList<>());
                                    }
                                    cursosPorHora.get(horaInicio).add(curso);

                                    System.out.println("  -> Asignado a: " + dia + " - " + horaInicio + ":00 a " + horaFin + ":00");
                                    asignado = true;
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

            Set<Integer> horasUnicas = new TreeSet<>();
            for (Map<Integer, List<Curso>> mapaDia : horarioPorDia.values()) {
                horasUnicas.addAll(mapaDia.keySet());
            }

            List<Integer> horasOrdenadas = new ArrayList<>(horasUnicas);
            Collections.sort(horasOrdenadas);

            // ========== CALCULAR TOTAL DE ALUMNOS ==========
            int totalAlumnos = 0;
            Set<Long> estudiantesUnicos = new HashSet<>();
            for (Curso curso : cursos) {
                int anioActual = java.time.Year.now().getValue();
                List<Estudiante> estudiantes = estudianteRepository.findByGradoYAnio(curso.getIdGrado(), anioActual);
                for (Estudiante e : estudiantes) {
                    estudiantesUnicos.add(e.getIdEstudiante());
                }
            }
            totalAlumnos = estudiantesUnicos.size();

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
                            System.out.println("  " + hora + ":00: " + cursosEnHora.size() + " curso(s)");
                            for (Curso c : cursosEnHora) {
                                System.out.println("    - " + c.getNombreCurso() + " (" + c.getHorario() + ")");
                            }
                        }
                    }
                }
            }
            System.out.println("========== FIN MI HORARIO DOCENTE ==========\n\n");

            model.addAttribute("docente", docente);
            model.addAttribute("cursos", cursos);
            model.addAttribute("horarioPorDia", horarioPorDia);
            model.addAttribute("horasOrdenadas", horasOrdenadas);
            model.addAttribute("dias", dias);
            model.addAttribute("totalAlumnos", totalAlumnos);     // <-- NUEVO
            model.addAttribute("totalHoras", totalHoras);         // <-- NUEVO
            model.addAttribute("modulo", "horario");
            model.addAttribute("tituloPagina", "Mi Horario");

            return "docente/horario";

        } catch (Exception e) {
            System.out.println("ERROR en miHorario: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    @GetMapping("/alumnos")
    public String misAlumnos(Authentication authentication,
                             @RequestParam(required = false) Long idCurso,
                             Model model) {
        try {
            System.out.println("\n\n========== INICIO MIS ALUMNOS DOCENTE ==========");

            Docente docente = getDocenteAutenticado(authentication);
            System.out.println("Docente ID: " + docente.getIdDocente());

            List<Curso> cursos = cursoRepository.findByIdDocente(docente.getIdDocente());
            System.out.println("Total cursos asignados: " + (cursos != null ? cursos.size() : 0));

            if (cursos == null) {
                cursos = new ArrayList<>();
            }

            List<Estudiante> estudiantes = new ArrayList<>();
            Curso cursoSeleccionado = null;
            String nombreGradoActual = "Todos los cursos";
            Map<Long, String> nombresGrados = new HashMap<>(); // <-- NUEVO

            if (idCurso != null && idCurso > 0) {
                cursoSeleccionado = cursoRepository.findById(idCurso).orElse(null);
                if (cursoSeleccionado != null) {
                    nombreGradoActual = cursoSeleccionado.getNombreCurso();
                    int anioActual = java.time.Year.now().getValue();
                    estudiantes = estudianteRepository.findByGradoYAnio(cursoSeleccionado.getIdGrado(), anioActual);
                    System.out.println("Estudiantes encontrados para curso " + cursoSeleccionado.getNombreCurso() + ": " + estudiantes.size());
                }
            } else {
                Set<Long> estudiantesIds = new HashSet<>();
                for (Curso curso : cursos) {
                    int anioActual = java.time.Year.now().getValue();
                    List<Estudiante> ests = estudianteRepository.findByGradoYAnio(curso.getIdGrado(), anioActual);
                    for (Estudiante e : ests) {
                        estudiantesIds.add(e.getIdEstudiante());
                    }
                }
                for (Long id : estudiantesIds) {
                    estudianteRepository.findById(id).ifPresent(estudiantes::add);
                }
                System.out.println("Total estudiantes únicos: " + estudiantes.size());
            }

            // ========== AGREGAR NOMBRES DE GRADO ==========
            for (Estudiante e : estudiantes) {
                if (e.getIdGrado() != null) {
                    nombresGrados.put(e.getIdEstudiante(), obtenerNombreGrado(e.getIdGrado()));
                }
            }

            Map<Long, Integer> alumnosPorCurso = new HashMap<>();
            for (Curso curso : cursos) {
                int count = matriculaRepository.countByGradoAndTurno(curso.getIdGrado(), curso.getTurno());
                alumnosPorCurso.put(curso.getIdCurso(), count);
            }

            model.addAttribute("docente", docente);
            model.addAttribute("cursos", cursos);
            model.addAttribute("estudiantes", estudiantes);
            model.addAttribute("cursoSeleccionado", cursoSeleccionado);
            model.addAttribute("nombreGradoActual", nombreGradoActual);
            model.addAttribute("alumnosPorCurso", alumnosPorCurso);
            model.addAttribute("nombresGrados", nombresGrados); // <-- NUEVO
            model.addAttribute("modulo", "alumnos");
            model.addAttribute("tituloPagina", "Mis Alumnos");

            System.out.println("========== FIN MIS ALUMNOS DOCENTE ==========\n\n");

            return "docente/alumnos";

        } catch (Exception e) {
            System.out.println("ERROR en misAlumnos: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/gestionar-notas")
    public String gestionarNotas(Authentication authentication,
                                 @RequestParam(required = false) Long idCurso,
                                 @RequestParam(required = false) Integer bimestre,
                                 Model model) {
        try {
            System.out.println("\n\n========== INICIO GESTIONAR NOTAS ==========");

            Docente docente = getDocenteAutenticado(authentication);
            System.out.println("Docente ID: " + docente.getIdDocente());

            List<Curso> cursos = cursoRepository.findByIdDocente(docente.getIdDocente());
            System.out.println("Total cursos asignados: " + (cursos != null ? cursos.size() : 0));

            if (cursos == null) {
                cursos = new ArrayList<>();
            }

            Curso cursoSeleccionado = null;
            List<NotaDTO> notasAlumnos = new ArrayList<>();
            String periodoActual = getPeriodoAcademicoActual();

            // Si no se especifica bimestre, usar 1 por defecto
            if (bimestre == null || bimestre < 1 || bimestre > 4) {
                bimestre = 1;
            }

            if (idCurso != null && idCurso > 0) {
                cursoSeleccionado = cursoRepository.findById(idCurso).orElse(null);
                if (cursoSeleccionado != null) {
                    int anioActual = java.time.Year.now().getValue();
                    List<Estudiante> estudiantes = estudianteRepository.findByGradoYAnio(cursoSeleccionado.getIdGrado(), anioActual);

                    for (Estudiante estudiante : estudiantes) {
                        NotaDTO nota = new NotaDTO();
                        nota.setIdEstudiante(estudiante.getIdEstudiante());
                        nota.setNombreCompleto(estudiante.getNombres() + " " + estudiante.getApellidoPaterno() + " " + estudiante.getApellidoMaterno());

                        // Cargar los 4 bimestres
                        Double[] bimestres = new Double[4];
                        for (int b = 1; b <= 4; b++) {
                            List<Nota> notasDB = notaRepository
                                    .findByEstudianteAndCursoAndBimestre(
                                            estudiante.getIdEstudiante(),
                                            idCurso,
                                            b,
                                            periodoActual);

                            if (!notasDB.isEmpty()) {
                                bimestres[b-1] = notasDB.get(0).getNota().doubleValue();
                            }
                        }

                        nota.setBimestre1(bimestres[0]);
                        nota.setBimestre2(bimestres[1]);
                        nota.setBimestre3(bimestres[2]);
                        nota.setBimestre4(bimestres[3]);

                        // Calcular promedio
                        List<Double> notasValidas = new ArrayList<>();
                        for (Double n : bimestres) {
                            if (n != null) notasValidas.add(n);
                        }

                        if (!notasValidas.isEmpty()) {
                            double promedio = notasValidas.stream()
                                    .mapToDouble(Double::doubleValue)
                                    .average()
                                    .orElse(0.0);
                            nota.setPromedioFinal(Math.round(promedio * 10.0) / 10.0);

                            if (nota.getPromedioFinal() >= 11) {
                                nota.setEstado("APROBADO");
                            } else if (nota.getPromedioFinal() >= 7) {
                                nota.setEstado("RECUPERACION");
                            } else {
                                nota.setEstado("DESAPROBADO");
                            }
                        } else {
                            nota.setPromedioFinal(null);
                            nota.setEstado("SIN NOTAS");
                        }

                        notasAlumnos.add(nota);
                    }

                    System.out.println("Notas cargadas para " + estudiantes.size() + " estudiantes");
                }
            }

            model.addAttribute("docente", docente);
            model.addAttribute("cursos", cursos);
            model.addAttribute("cursoSeleccionado", cursoSeleccionado);
            model.addAttribute("notasAlumnos", notasAlumnos);
            model.addAttribute("periodoActual", periodoActual);
            model.addAttribute("bimestreSeleccionado", bimestre);
            model.addAttribute("modulo", "notas");
            model.addAttribute("tituloPagina", "Gestionar Notas");

            System.out.println("========== FIN GESTIONAR NOTAS ==========\n\n");

            return "docente/gestionar-notas";

        } catch (Exception e) {
            System.out.println("ERROR en gestionarNotas: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    // ==================== GUARDAR NOTAS (CORREGIDO) ====================

    @PostMapping("/notas/guardar")
    public String guardarNotas(@RequestParam Long idCurso,
                               @RequestParam String periodoAcademico,
                               @RequestParam Map<String, String> notas,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            System.out.println("\n\n========== GUARDAR NOTAS ==========");
            System.out.println("Curso ID: " + idCurso);
            System.out.println("Periodo: " + periodoAcademico);

            Docente docente = getDocenteAutenticado(authentication);

            Curso curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            if (!curso.getIdDocente().equals(docente.getIdDocente())) {
                throw new RuntimeException("No tiene permisos para modificar este curso");
            }

            // Procesar cada nota por bimestre
            int notasGuardadas = 0;
            Map<Long, Map<Integer, Double>> notasPorEstudiante = new HashMap<>();

            for (Map.Entry<String, String> entry : notas.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Procesar b1_, b2_, b3_, b4_
                if ((key.startsWith("b1_") || key.startsWith("b2_") || key.startsWith("b3_") || key.startsWith("b4_"))
                        && value != null && !value.isEmpty()) {
                    try {
                        // Extraer bimestre y ID del estudiante
                        String[] parts = key.split("_");
                        int bimestre = Integer.parseInt(parts[0].substring(1)); // b1 -> 1, b2 -> 2, etc.
                        Long idEstudiante = Long.parseLong(parts[1]);
                        Double notaValor = Double.parseDouble(value);

                        if (!notasPorEstudiante.containsKey(idEstudiante)) {
                            notasPorEstudiante.put(idEstudiante, new HashMap<>());
                        }
                        notasPorEstudiante.get(idEstudiante).put(bimestre, notaValor);

                    } catch (Exception e) {
                        System.err.println("Error al procesar key: " + key + " - " + e.getMessage());
                    }
                }
            }

            // Guardar cada nota en la base de datos
            for (Map.Entry<Long, Map<Integer, Double>> entry : notasPorEstudiante.entrySet()) {
                Long idEstudiante = entry.getKey();
                Map<Integer, Double> bimestres = entry.getValue();

                Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                        .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

                for (Map.Entry<Integer, Double> bimestreEntry : bimestres.entrySet()) {
                    Integer bimestre = bimestreEntry.getKey();
                    Double notaValor = bimestreEntry.getValue();

                    // Buscar nota existente para este estudiante, curso, bimestre y periodo
                    List<Nota> notasExistentes = notaRepository
                            .findByEstudianteAndCursoAndBimestre(
                                    idEstudiante, idCurso, bimestre, periodoAcademico);

                    if (!notasExistentes.isEmpty()) {
                        // Actualizar nota existente
                        Nota nota = notasExistentes.get(0);
                        nota.setNota(BigDecimal.valueOf(notaValor));
                        nota.setFechaActualizacion(LocalDateTime.now());
                        notaRepository.save(nota);
                    } else {
                        // Crear nueva nota
                        Nota nota = new Nota();
                        nota.setEstudiante(estudiante);
                        nota.setCurso(curso);
                        nota.setBimestre(bimestre);
                        nota.setNota(BigDecimal.valueOf(notaValor));
                        nota.setPeriodoAcademico(periodoAcademico);
                        notaRepository.save(nota);
                    }
                    notasGuardadas++;
                }
            }

            System.out.println("Notas guardadas: " + notasGuardadas);
            System.out.println("========== FIN GUARDAR NOTAS ==========\n\n");

            redirectAttributes.addFlashAttribute("success", "✅ " + notasGuardadas + " nota(s) guardada(s) correctamente");

        } catch (Exception e) {
            System.err.println("ERROR en guardarNotas: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "❌ Error al guardar notas: " + e.getMessage());
        }

        return "redirect:/docente/gestionar-notas?idCurso=" + idCurso;
    }

    @GetMapping("/registrar-asistencia")
    public String registrarAsistencia(Authentication authentication,
                                      @RequestParam(required = false) Long idCurso,
                                      @RequestParam(required = false) String fecha,
                                      Model model) {
        try {
            System.out.println("\n\n========== INICIO REGISTRAR ASISTENCIA ==========");

            Docente docente = getDocenteAutenticado(authentication);
            System.out.println("Docente ID: " + docente.getIdDocente());

            List<Curso> cursos = cursoRepository.findByIdDocente(docente.getIdDocente());
            System.out.println("Total cursos asignados: " + (cursos != null ? cursos.size() : 0));

            if (cursos == null) {
                cursos = new ArrayList<>();
            }

            Curso cursoSeleccionado = null;
            List<Estudiante> estudiantes = new ArrayList<>();
            String fechaSeleccionada = fecha != null ? fecha : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            Map<Long, String> asistenciasExistentes = new HashMap<>();

            if (idCurso != null && idCurso > 0) {
                cursoSeleccionado = cursoRepository.findById(idCurso).orElse(null);
                if (cursoSeleccionado != null) {
                    int anioActual = java.time.Year.now().getValue();
                    estudiantes = estudianteRepository.findByGradoYAnio(cursoSeleccionado.getIdGrado(), anioActual);

                    for (Estudiante estudiante : estudiantes) {
                        Optional<Asistencia> asistenciaOpt = asistenciaRepository
                                .findByEstudianteIdEstudianteAndCursoIdCursoAndFecha(
                                        estudiante.getIdEstudiante(),
                                        idCurso,
                                        LocalDate.parse(fechaSeleccionada));

                        asistenciaOpt.ifPresent(a -> asistenciasExistentes.put(
                                estudiante.getIdEstudiante(),
                                a.getEstado()
                        ));
                    }

                    System.out.println("Estudiantes cargados: " + estudiantes.size());
                    System.out.println("Asistencias existentes: " + asistenciasExistentes.size());
                }
            }

            model.addAttribute("docente", docente);
            model.addAttribute("cursos", cursos);
            model.addAttribute("cursoSeleccionado", cursoSeleccionado);
            model.addAttribute("estudiantes", estudiantes);
            model.addAttribute("fechaSeleccionada", fechaSeleccionada);
            model.addAttribute("asistenciasExistentes", asistenciasExistentes);
            model.addAttribute("modulo", "asistencia");
            model.addAttribute("tituloPagina", "Registrar Asistencia");

            System.out.println("========== FIN REGISTRAR ASISTENCIA ==========\n\n");

            return "docente/registrar-asistencia";

        } catch (Exception e) {
            System.out.println("ERROR en registrarAsistencia: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    // ==================== GUARDAR ASISTENCIA (CORREGIDO) ====================

    @PostMapping("/asistencia/guardar")
    public String guardarAsistencia(@RequestParam Long idCurso,
                                    @RequestParam String fecha,
                                    @RequestParam Map<String, String> asistencias,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            System.out.println("\n\n========== GUARDAR ASISTENCIA ==========");
            System.out.println("Curso ID: " + idCurso);
            System.out.println("Fecha: " + fecha);

            Docente docente = getDocenteAutenticado(authentication);

            Curso curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            if (!curso.getIdDocente().equals(docente.getIdDocente())) {
                throw new RuntimeException("No tiene permisos para modificar este curso");
            }

            LocalDate fechaLocal = LocalDate.parse(fecha);
            int asistenciasGuardadas = 0;

            for (Map.Entry<String, String> entry : asistencias.entrySet()) {
                String key = entry.getKey();
                String estado = entry.getValue();

                if (key.startsWith("asistencia_") && estado != null && !estado.isEmpty()) {
                    try {
                        Long idEstudiante = Long.parseLong(key.substring(11));

                        Optional<Asistencia> asistenciaOpt = asistenciaRepository
                                .findByEstudianteIdEstudianteAndCursoIdCursoAndFecha(
                                        idEstudiante, idCurso, fechaLocal);

                        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

                        Asistencia asistencia;
                        if (asistenciaOpt.isPresent()) {
                            asistencia = asistenciaOpt.get();
                            asistencia.setEstado(estado);
                        } else {
                            asistencia = new Asistencia();
                            asistencia.setEstudiante(estudiante);
                            asistencia.setCurso(curso);
                            asistencia.setFecha(fechaLocal);
                            asistencia.setEstado(estado);
                            // ELIMINADO: asistencia.setDocente(docente); - No existe el campo docente en Asistencia
                            // Si quieres registrar quién registró, usa registradoPor
                            asistencia.setRegistradoPor(usuarioRepository.findByEmail(authentication.getName()).orElse(null));
                        }

                        asistenciaRepository.save(asistencia);
                        asistenciasGuardadas++;
                    } catch (Exception e) {
                        System.err.println("Error al guardar asistencia para key: " + key + " - " + e.getMessage());
                    }
                }
            }

            System.out.println("Asistencias guardadas: " + asistenciasGuardadas);
            System.out.println("========== FIN GUARDAR ASISTENCIA ==========\n\n");

            redirectAttributes.addFlashAttribute("success", "✅ " + asistenciasGuardadas + " asistencia(s) registrada(s) correctamente para el " + fecha);

        } catch (Exception e) {
            System.err.println("ERROR en guardarAsistencia: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", " Error al registrar asistencia: " + e.getMessage());
        }

        return "redirect:/docente/registrar-asistencia?idCurso=" + idCurso + "&fecha=" + fecha;
    }

    // ==================== API REST ====================

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<DocenteDTO>> listarTodos() {
        List<DocenteDTO> docentes = docenteService.listarTodos();
        return ResponseEntity.ok(docentes);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            DocenteDTO docente = docenteService.buscarPorId(id);
            return ResponseEntity.ok(docente);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> guardar(@Valid @RequestBody DocenteDTO docenteDTO) {
        try {
            DocenteDTO docenteGuardado = docenteService.guardar(docenteDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(docenteGuardado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody DocenteDTO docenteDTO) {
        try {
            docenteDTO.setIdDocente(id);
            DocenteDTO docenteActualizado = docenteService.guardar(docenteDTO);
            return ResponseEntity.ok(docenteActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            docenteService.eliminar(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Docente eliminado exitosamente");
            response.put("id", id.toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Map<String, String> crearMensajeError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}