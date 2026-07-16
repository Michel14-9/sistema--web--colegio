package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.DocenteDTO;
import com.universidad.sistema_academico.dto.NotaDTO;
import com.universidad.sistema_academico.model.*;
import com.universidad.sistema_academico.repository.*;
import com.universidad.sistema_academico.service.DocenteService;
import jakarta.servlet.http.HttpServletResponse;
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
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
    // ==================== REPORTES PARA DOCENTE (DIRECTO EN CONTROLADOR) ====================

    @GetMapping("/docentes-reportes")
    public String reportes(Authentication authentication, Model model) {
        try {
            Docente docente = getDocenteAutenticado(authentication);
            List<Curso> cursos = cursoRepository.findByIdDocente(docente.getIdDocente());

            model.addAttribute("docente", docente);
            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("modulo", "reportes");
            model.addAttribute("tituloPagina", "Reportes");

            return "docente/docentes-reportes";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

// ==================== REPORTE DE NOTAS EN PDF ====================

    @GetMapping("/reportes/notas/pdf")
    public void generarReporteNotasPDF(@RequestParam Long idCurso,
                                       @RequestParam(required = false) Integer bimestre,
                                       HttpServletResponse response,
                                       Authentication authentication) throws Exception {
        try {
            Docente docente = getDocenteAutenticado(authentication);
            Curso curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            if (!curso.getIdDocente().equals(docente.getIdDocente())) {
                throw new RuntimeException("No tiene permisos para este curso");
            }

            List<Estudiante> estudiantes = estudianteRepository.findByGradoYAnio(
                    curso.getIdGrado(),
                    Year.now().getValue()
            );

            // Configurar respuesta
            response.setContentType("application/pdf");
            String filename = "Reporte_Notas_" + curso.getNombreCurso();
            if (bimestre != null && bimestre > 0) {
                filename += "_B" + bimestre;
            }
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + filename + ".pdf");

            // Generar PDF
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // Títulos
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

            Paragraph schoolName = new Paragraph("I.E. SAN CARLOS", new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD));
            schoolName.setAlignment(Element.ALIGN_CENTER);
            document.add(schoolName);

            Paragraph title = new Paragraph("REPORTE DE NOTAS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Curso: " + curso.getNombreCurso(), subtitleFont));
            document.add(new Paragraph("Profesor: " + curso.getDocente().getNombres() + " " +
                    curso.getDocente().getApellidoPaterno(), subtitleFont));
            document.add(new Paragraph("Periodo: " + Year.now().getValue(), subtitleFont));
            if (bimestre != null && bimestre > 0) {
                document.add(new Paragraph("Bimestre: " + bimestre, subtitleFont));
            }
            document.add(new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), subtitleFont));
            document.add(new Paragraph(" "));

            // Tabla
            int columnas = bimestre != null && bimestre > 0 ? 5 : 8;
            PdfPTable table = new PdfPTable(columnas);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            String[] headers;
            if (bimestre != null && bimestre > 0) {
                headers = new String[]{"#", "Código", "Estudiante", "Nota B" + bimestre, "Estado"};
            } else {
                headers = new String[]{"#", "Código", "Estudiante", "B1", "B2", "B3", "B4", "Promedio"};
            }

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new BaseColor(200, 200, 200));
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Datos
            int count = 1;
            String periodoAcademico = String.valueOf(Year.now().getValue());

            for (Estudiante estudiante : estudiantes) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(count++), dataFont)));
                table.addCell(new PdfPCell(new Phrase(estudiante.getCodigoEstudiante() != null ? estudiante.getCodigoEstudiante() : "", dataFont)));
                table.addCell(new PdfPCell(new Phrase(estudiante.getNombres() + " " + estudiante.getApellidoPaterno(), dataFont)));

                if (bimestre != null && bimestre > 0) {
                    List<Nota> notas = notaRepository.findByEstudianteAndCursoAndBimestre(
                            estudiante.getIdEstudiante(), idCurso, bimestre, periodoAcademico);

                    if (!notas.isEmpty()) {
                        double nota = notas.get(0).getNota().doubleValue();
                        table.addCell(new PdfPCell(new Phrase(String.format("%.1f", nota), dataFont)));
                        String estado = nota >= 11 ? "APROBADO" : "DESAPROBADO";
                        PdfPCell estadoCell = new PdfPCell(new Phrase(estado, dataFont));
                        estadoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        if (nota >= 11) {
                            estadoCell.setBackgroundColor(new BaseColor(144, 238, 144));
                        } else {
                            estadoCell.setBackgroundColor(new BaseColor(255, 182, 193));
                        }
                        table.addCell(estadoCell);
                    } else {
                        table.addCell(new PdfPCell(new Phrase("-", dataFont)));
                        table.addCell(new PdfPCell(new Phrase("SIN NOTA", dataFont)));
                    }
                } else {
                    Double[] bimestres = new Double[4];
                    for (int b = 1; b <= 4; b++) {
                        List<Nota> notas = notaRepository.findByEstudianteAndCursoAndBimestre(
                                estudiante.getIdEstudiante(), idCurso, b, periodoAcademico);
                        if (!notas.isEmpty()) {
                            bimestres[b-1] = notas.get(0).getNota().doubleValue();
                        }
                    }

                    double sum = 0;
                    int countNotas = 0;
                    for (Double nota : bimestres) {
                        table.addCell(new PdfPCell(new Phrase(nota != null ? String.format("%.1f", nota) : "-", dataFont)));
                        if (nota != null) {
                            sum += nota;
                            countNotas++;
                        }
                    }

                    double promedio = countNotas > 0 ? sum / countNotas : 0;
                    PdfPCell promedioCell = new PdfPCell(new Phrase(countNotas > 0 ? String.format("%.1f", promedio) : "-", dataFont));
                    promedioCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    if (countNotas > 0 && promedio >= 11) {
                        promedioCell.setBackgroundColor(new BaseColor(144, 238, 144));
                    } else if (countNotas > 0 && promedio < 11) {
                        promedioCell.setBackgroundColor(new BaseColor(255, 182, 193));
                    }
                    table.addCell(promedioCell);
                }
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Firma del Docente: ___________________________", subtitleFont));
            document.add(new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), subtitleFont));

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al generar reporte: " + e.getMessage());
        }
    }

// ==================== REPORTE DE ASISTENCIA EN PDF ====================

    @GetMapping("/reportes/asistencia/pdf")
    public void generarReporteAsistenciaPDF(@RequestParam Long idCurso,
                                            @RequestParam(required = false) String fechaInicio,
                                            @RequestParam(required = false) String fechaFin,
                                            HttpServletResponse response,
                                            Authentication authentication) throws Exception {
        try {
            Docente docente = getDocenteAutenticado(authentication);
            Curso curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            if (!curso.getIdDocente().equals(docente.getIdDocente())) {
                throw new RuntimeException("No tiene permisos para este curso");
            }

            LocalDate inicio = fechaInicio != null ? LocalDate.parse(fechaInicio) : LocalDate.now().minusMonths(1);
            LocalDate fin = fechaFin != null ? LocalDate.parse(fechaFin) : LocalDate.now();

            List<Estudiante> estudiantes = estudianteRepository.findByGradoYAnio(
                    curso.getIdGrado(),
                    Year.now().getValue()
            );

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=Reporte_Asistencia_" + curso.getNombreCurso() + ".pdf");

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

            Paragraph schoolName = new Paragraph("I.E. SAN CARLOS", new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD));
            schoolName.setAlignment(Element.ALIGN_CENTER);
            document.add(schoolName);

            Paragraph title = new Paragraph("REPORTE DE ASISTENCIA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Curso: " + curso.getNombreCurso(), subtitleFont));
            document.add(new Paragraph("Profesor: " + curso.getDocente().getNombres() + " " +
                    curso.getDocente().getApellidoPaterno(), subtitleFont));
            document.add(new Paragraph("Periodo: " + inicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " al " + fin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), subtitleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            String[] headers = {"#", "Código", "Estudiante", "Presente", "Falta", "Tardanza", "% Asistencia"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new BaseColor(200, 200, 200));
                cell.setPadding(5);
                table.addCell(cell);
            }

            int count = 1;
            for (Estudiante estudiante : estudiantes) {
                // ✅ USANDO MÉTODO EXISTENTE: findByEstudianteIdEstudianteAndFechaBetween
                List<Asistencia> asistencias = asistenciaRepository
                        .findByEstudianteIdEstudianteAndFechaBetween(
                                estudiante.getIdEstudiante(), inicio, fin);

                // Filtrar por curso en memoria (o usar el método existente)
                asistencias = asistencias.stream()
                        .filter(a -> a.getCurso() != null && a.getCurso().getIdCurso().equals(idCurso))
                        .collect(Collectors.toList());

                long presentes = asistencias.stream().filter(a -> "PRESENTE".equals(a.getEstado())).count();
                long faltas = asistencias.stream().filter(a -> "FALTA".equals(a.getEstado())).count();
                long tardanzas = asistencias.stream().filter(a -> "TARDANZA".equals(a.getEstado())).count();

                long total = presentes + faltas + tardanzas;
                double porcentaje = total > 0 ? (presentes * 100.0) / total : 0;

                table.addCell(new PdfPCell(new Phrase(String.valueOf(count++), dataFont)));
                table.addCell(new PdfPCell(new Phrase(estudiante.getCodigoEstudiante() != null ? estudiante.getCodigoEstudiante() : "", dataFont)));
                table.addCell(new PdfPCell(new Phrase(estudiante.getNombres() + " " + estudiante.getApellidoPaterno(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(presentes), dataFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(faltas), dataFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(tardanzas), dataFont)));

                PdfPCell porcentajeCell = new PdfPCell(new Phrase(String.format("%.1f%%", porcentaje), dataFont));
                porcentajeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (porcentaje >= 80) {
                    porcentajeCell.setBackgroundColor(new BaseColor(144, 238, 144));
                } else if (porcentaje >= 60) {
                    porcentajeCell.setBackgroundColor(new BaseColor(255, 255, 153));
                } else {
                    porcentajeCell.setBackgroundColor(new BaseColor(255, 182, 193));
                }
                table.addCell(porcentajeCell);
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Firma del Docente: ___________________________", subtitleFont));
            document.add(new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), subtitleFont));

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al generar reporte: " + e.getMessage());
        }
    }

// ==================== REPORTE DE RENDIMIENTO EN EXCEL ====================

    @GetMapping("/reportes/rendimiento/excel")
    public void generarReporteRendimientoExcel(@RequestParam Long idCurso,
                                               HttpServletResponse response,
                                               Authentication authentication) throws Exception {
        try {
            Docente docente = getDocenteAutenticado(authentication);
            Curso curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            if (!curso.getIdDocente().equals(docente.getIdDocente())) {
                throw new RuntimeException("No tiene permisos para este curso");
            }

            List<Estudiante> estudiantes = estudianteRepository.findByGradoYAnio(
                    curso.getIdGrado(),
                    Year.now().getValue()
            );

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=Reporte_Rendimiento_" + curso.getNombreCurso() + ".xlsx");

            // Crear Excel
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Rendimiento");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeight(12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle aprobadoStyle = workbook.createCellStyle();
            aprobadoStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            aprobadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            aprobadoStyle.setBorderBottom(BorderStyle.THIN);
            aprobadoStyle.setBorderTop(BorderStyle.THIN);
            aprobadoStyle.setBorderLeft(BorderStyle.THIN);
            aprobadoStyle.setBorderRight(BorderStyle.THIN);

            CellStyle desaprobadoStyle = workbook.createCellStyle();
            desaprobadoStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            desaprobadoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            desaprobadoStyle.setBorderBottom(BorderStyle.THIN);
            desaprobadoStyle.setBorderTop(BorderStyle.THIN);
            desaprobadoStyle.setBorderLeft(BorderStyle.THIN);
            desaprobadoStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("I.E. SAN CARLOS - REPORTE DE RENDIMIENTO");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8));

            Row cursoRow = sheet.createRow(1);
            cursoRow.createCell(0).setCellValue("Curso: " + curso.getNombreCurso());
            cursoRow.createCell(3).setCellValue("Profesor: " + curso.getDocente().getNombres() + " " + curso.getDocente().getApellidoPaterno());

            // Headers
            Row headerRow = sheet.createRow(3);
            String[] headers = {"#", "Código", "Estudiante", "B1", "B2", "B3", "B4", "Promedio", "Estado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 4;
            int count = 1;
            String periodoAcademico = String.valueOf(Year.now().getValue());

            for (Estudiante estudiante : estudiantes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(count++);
                row.createCell(1).setCellValue(estudiante.getCodigoEstudiante() != null ? estudiante.getCodigoEstudiante() : "");
                row.createCell(2).setCellValue(estudiante.getNombres() + " " + estudiante.getApellidoPaterno());

                Double[] bimestres = new Double[4];
                for (int b = 1; b <= 4; b++) {
                    List<Nota> notas = notaRepository.findByEstudianteAndCursoAndBimestre(
                            estudiante.getIdEstudiante(), idCurso, b, periodoAcademico);
                    if (!notas.isEmpty()) {
                        bimestres[b-1] = notas.get(0).getNota().doubleValue();
                        Cell cell = row.createCell(b + 2);
                        cell.setCellValue(bimestres[b-1]);
                        cell.setCellStyle(dataStyle);
                    } else {
                        Cell cell = row.createCell(b + 2);
                        cell.setCellValue("-");
                        cell.setCellStyle(dataStyle);
                    }
                }

                // Calcular promedio
                double sum = 0;
                int countNotas = 0;
                for (Double nota : bimestres) {
                    if (nota != null) {
                        sum += nota;
                        countNotas++;
                    }
                }

                double promedio = countNotas > 0 ? sum / countNotas : 0;
                Cell promedioCell = row.createCell(7);
                promedioCell.setCellValue(countNotas > 0 ? promedio : 0);
                promedioCell.setCellStyle(dataStyle);

                // Estado
                Cell estadoCell = row.createCell(8);
                if (countNotas > 0) {
                    if (promedio >= 11) {
                        estadoCell.setCellValue("APROBADO");
                        estadoCell.setCellStyle(aprobadoStyle);
                    } else {
                        estadoCell.setCellValue("DESAPROBADO");
                        estadoCell.setCellStyle(desaprobadoStyle);
                    }
                } else {
                    estadoCell.setCellValue("SIN NOTAS");
                    estadoCell.setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al generar reporte: " + e.getMessage());
        }
    }

// ==================== BOLETÍN INDIVIDUAL EN PDF ====================

    @GetMapping("/reportes/boletin/{idEstudiante}")
    public void generarBoletinIndividual(@PathVariable Long idEstudiante,
                                         @RequestParam Long idCurso,
                                         HttpServletResponse response,
                                         Authentication authentication) throws Exception {
        try {
            Docente docente = getDocenteAutenticado(authentication);
            Curso curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            if (!curso.getIdDocente().equals(docente.getIdDocente())) {
                throw new RuntimeException("No tiene permisos para este curso");
            }

            Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=Boletin_" + estudiante.getNombres() + "_" +
                            estudiante.getApellidoPaterno() + ".pdf");

            Document document = new Document();
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

            Paragraph schoolName = new Paragraph("I.E. SAN CARLOS", new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD));
            schoolName.setAlignment(Element.ALIGN_CENTER);
            document.add(schoolName);

            Paragraph title = new Paragraph("BOLETÍN DE NOTAS - " + Year.now().getValue(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // Datos del estudiante
            document.add(new Paragraph("DATOS DEL ESTUDIANTE", sectionFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Nombre: " + estudiante.getNombres() + " " +
                    estudiante.getApellidoPaterno() + " " +
                    estudiante.getApellidoMaterno(), dataFont));
            document.add(new Paragraph("Código: " + (estudiante.getCodigoEstudiante() != null ? estudiante.getCodigoEstudiante() : ""), dataFont));
            document.add(new Paragraph("DNI: " + estudiante.getDni(), dataFont));
            document.add(new Paragraph("Grado: " + obtenerNombreGrado(estudiante.getIdGrado()), dataFont));
            document.add(new Paragraph(" "));

            // Datos del curso
            document.add(new Paragraph("DATOS DEL CURSO", sectionFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Curso: " + curso.getNombreCurso(), dataFont));
            document.add(new Paragraph("Profesor: " + curso.getDocente().getNombres() + " " +
                    curso.getDocente().getApellidoPaterno(), dataFont));
            document.add(new Paragraph(" "));

            // Tabla de notas
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            String[] headers = {"Criterio", "B1", "B2", "B3", "B4", "Promedio"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new BaseColor(200, 200, 200));
                cell.setPadding(5);
                table.addCell(cell);
            }

            String periodoAcademico = String.valueOf(Year.now().getValue());
            Double[] bimestres = new Double[4];
            for (int b = 1; b <= 4; b++) {
                List<Nota> notas = notaRepository.findByEstudianteAndCursoAndBimestre(
                        estudiante.getIdEstudiante(), idCurso, b, periodoAcademico);
                if (!notas.isEmpty()) {
                    bimestres[b-1] = notas.get(0).getNota().doubleValue();
                }
            }

            table.addCell(new PdfPCell(new Phrase("Notas", dataFont)));
            double sum = 0;
            int countNotas = 0;
            for (Double nota : bimestres) {
                PdfPCell cell = new PdfPCell(new Phrase(nota != null ? String.format("%.1f", nota) : "-", dataFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
                if (nota != null) {
                    sum += nota;
                    countNotas++;
                }
            }
            double promedio = countNotas > 0 ? sum / countNotas : 0;
            PdfPCell promedioCell = new PdfPCell(new Phrase(countNotas > 0 ? String.format("%.1f", promedio) : "-", dataFont));
            promedioCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            if (countNotas > 0 && promedio >= 11) {
                promedioCell.setBackgroundColor(new BaseColor(144, 238, 144));
            } else if (countNotas > 0 && promedio < 11) {
                promedioCell.setBackgroundColor(new BaseColor(255, 182, 193));
            }
            table.addCell(promedioCell);

            String estado = countNotas > 0 ? (promedio >= 11 ? "APROBADO" : "DESAPROBADO") : "SIN NOTAS";
            table.addCell(new PdfPCell(new Phrase("Estado", dataFont)));
            PdfPCell estadoCell = new PdfPCell(new Phrase(estado, dataFont));
            estadoCell.setColspan(5);
            estadoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            if (countNotas > 0 && promedio >= 11) {
                estadoCell.setBackgroundColor(new BaseColor(144, 238, 144));
            } else if (countNotas > 0 && promedio < 11) {
                estadoCell.setBackgroundColor(new BaseColor(255, 182, 193));
            }
            table.addCell(estadoCell);

            document.add(table);
            document.add(new Paragraph(" "));

            // Observaciones
            document.add(new Paragraph("OBSERVACIONES:", sectionFont));
            document.add(new Paragraph(" "));
            if (countNotas > 0) {
                if (promedio >= 14) {
                    document.add(new Paragraph("• Excelente rendimiento académico. ¡Felicitaciones!", dataFont));
                } else if (promedio >= 11) {
                    document.add(new Paragraph("• Buen rendimiento académico. Continúa esforzándote.", dataFont));
                } else if (promedio >= 7) {
                    document.add(new Paragraph("• Se recomienda reforzar los temas para mejorar el rendimiento.", dataFont));
                } else {
                    document.add(new Paragraph("• Se requiere apoyo adicional y refuerzo académico.", dataFont));
                }
            } else {
                document.add(new Paragraph("• Aún no se han registrado notas para este periodo.", dataFont));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Firma del Docente: ___________________________", dataFont));
            document.add(new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dataFont));

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al generar reporte: " + e.getMessage());
        }
    }

// ==================== SELECCIONAR ESTUDIANTE PARA BOLETÍN ====================

    @GetMapping("/reportes/boletin/seleccionar")
    public String seleccionarEstudianteBoletin(@RequestParam Long idCurso,
                                               Authentication authentication,
                                               Model model) {
        try {
            Docente docente = getDocenteAutenticado(authentication);
            Curso curso = cursoRepository.findById(idCurso)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            if (!curso.getIdDocente().equals(docente.getIdDocente())) {
                throw new RuntimeException("No tiene permisos para este curso");
            }

            List<Estudiante> estudiantes = estudianteRepository.findByGradoYAnio(
                    curso.getIdGrado(),
                    Year.now().getValue()
            );

            model.addAttribute("docente", docente);
            model.addAttribute("curso", curso);
            model.addAttribute("estudiantes", estudiantes);
            model.addAttribute("modulo", "reportes");
            model.addAttribute("tituloPagina", "Seleccionar Estudiante");

            return "docente/seleccionar-estudiante";

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }



}