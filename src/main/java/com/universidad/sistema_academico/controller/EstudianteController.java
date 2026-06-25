package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.CursoDisponibleDTO;
import com.universidad.sistema_academico.dto.EstudianteDTO;
import com.universidad.sistema_academico.model.Curso;
import com.universidad.sistema_academico.model.Docente;
import com.universidad.sistema_academico.model.Estudiante;
import com.universidad.sistema_academico.model.Matricula;
import com.universidad.sistema_academico.model.Usuario;
import com.universidad.sistema_academico.repository.CursoRepository;
import com.universidad.sistema_academico.repository.DocenteRepository;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import com.universidad.sistema_academico.repository.MatriculaRepository;
import com.universidad.sistema_academico.repository.UsuarioRepository;
import jakarta.validation.Valid;
import com.universidad.sistema_academico.service.EstudianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // ==================== MÉTODO AUXILIAR ====================

    /**
     * Método para obtener el estudiante autenticado
     */
    private Estudiante getEstudianteAutenticado(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return estudianteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
    }

    /**
     * Método para convertir Curso a CursoDisponibleDTO
     */
    private CursoDisponibleDTO convertirACursoDisponibleDTO(Curso curso, boolean inscrito) {
        CursoDisponibleDTO dto = new CursoDisponibleDTO();
        dto.setIdCurso(curso.getIdCurso());
        dto.setCodigoCurso(curso.getCodigoCurso());
        dto.setNombreCurso(curso.getNombreCurso());
        dto.setDescripcion(curso.getDescripcion());
        dto.setHorasSemanales(curso.getHorasSemanales());
        dto.setIdGrado(curso.getIdGrado());
        dto.setIdDocente(curso.getIdDocente());
        dto.setArea(curso.getArea());
        dto.setEstado(curso.getEstado());
        dto.setSeccion(curso.getSeccion());
        dto.setTurno(curso.getTurno());
        dto.setCapacidadMaxima(curso.getCapacidadMaxima());
        dto.setAlumnosActuales(curso.getAlumnosActuales());
        dto.setHorario(curso.getHorario());
        dto.setInscrito(inscrito);
        dto.setCuposDisponibles(curso.getCuposDisponibles());
        dto.setPorcentajeOcupacion(curso.getPorcentajeOcupacion());
        dto.setHayCupo(curso.hayCupo());

        // Agregar nombre del docente si existe
        if (curso.getDocente() != null) {
            dto.setDocenteNombre(curso.getDocente().getNombres() + " " + curso.getDocente().getApellidoPaterno());
        } else {
            dto.setDocenteNombre("Sin asignar");
        }

        return dto;
    }

    // ==================== VISTAS MVC ====================

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        try {
            // 1. Obtener el estudiante autenticado
            Estudiante estudiante = getEstudianteAutenticado(authentication);

            // 2. Obtener el grado actual del estudiante desde su matrícula activa
            Optional<Integer> gradoActualOpt = matriculaRepository.findGradoActualByEstudianteId(estudiante.getIdEstudiante());
            Integer gradoActual = gradoActualOpt.orElse(null);

            // 3. Verificar si tiene matrícula activa
            boolean tieneMatriculaActiva = matriculaRepository.hasMatriculaActiva(estudiante.getIdEstudiante());

            // 4. Obtener cursos del grado del estudiante (si tiene matrícula activa)
            List<Curso> cursos = new ArrayList<>();
            if (tieneMatriculaActiva && gradoActual != null) {
                cursos = cursoRepository.findAll().stream()
                        .filter(curso -> curso.getIdGrado() != null && curso.getIdGrado().equals(gradoActual))
                        .collect(Collectors.toList());
            }

            // 5. Calcular estadísticas
            int totalCursos = cursos.size();
            long cursosActivos = cursos.stream()
                    .filter(c -> "ACTIVO".equals(c.getEstado()) || "ACTIVA".equals(c.getEstado()))
                    .count();

            // 6. Agregar todo al modelo
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("totalCursos", totalCursos);
            model.addAttribute("cursosActivos", cursosActivos);
            model.addAttribute("gradoActual", gradoActual);
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
            // 1. Obtener el estudiante autenticado
            Estudiante estudiante = getEstudianteAutenticado(authentication);

            // 2. Obtener el grado actual del estudiante desde su matrícula activa
            Optional<Integer> gradoActualOpt = matriculaRepository.findGradoActualByEstudianteId(estudiante.getIdEstudiante());
            Integer gradoActual = gradoActualOpt.orElse(null);

            // 3. Verificar si tiene matrícula activa
            boolean tieneMatriculaActiva = matriculaRepository.hasMatriculaActiva(estudiante.getIdEstudiante());

            // 4. Obtener cursos del grado del estudiante (si tiene matrícula activa)
            List<Curso> cursos = new ArrayList<>();
            if (tieneMatriculaActiva && gradoActual != null) {
                cursos = cursoRepository.findAll().stream()
                        .filter(curso -> curso.getIdGrado() != null && curso.getIdGrado().equals(gradoActual))
                        .collect(Collectors.toList());
            }

            // 5. Aplicar filtros si existen
            if (buscar != null && !buscar.isEmpty()) {
                cursos = cursos.stream()
                        .filter(c -> c.getNombreCurso().toLowerCase().contains(buscar.toLowerCase()) ||
                                c.getCodigoCurso().toLowerCase().contains(buscar.toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (area != null && !area.isEmpty()) {
                cursos = cursos.stream()
                        .filter(c -> c.getArea() != null &&
                                c.getArea().toLowerCase().contains(area.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // 6. Agregar todo al modelo
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("totalCursosInscritos", cursos.size());
            model.addAttribute("gradoActual", gradoActual);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("modulo", "mis-cursos");
            model.addAttribute("tituloPagina", "Mis Cursos");

            return "estudiante/mis-cursos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/cursos-disponibles")
    public String cursosDisponibles(Authentication authentication, Model model) {
        try {
            // 1. Obtener el estudiante autenticado
            Estudiante estudiante = getEstudianteAutenticado(authentication);

            // 2. Obtener el grado actual del estudiante desde su matrícula activa
            Optional<Integer> gradoActualOpt = matriculaRepository.findGradoActualByEstudianteId(estudiante.getIdEstudiante());
            Integer gradoActual = gradoActualOpt.orElse(null);

            // 3. Obtener todos los cursos
            List<Curso> cursos = cursoRepository.findAll();

            // 4. Filtrar cursos por el grado del estudiante (si tiene grado)
            List<Curso> cursosFiltrados = cursos;
            if (gradoActual != null) {
                cursosFiltrados = cursos.stream()
                        .filter(curso -> curso.getIdGrado() != null && curso.getIdGrado().equals(gradoActual))
                        .collect(Collectors.toList());
            }

            // 5. Verificar si el estudiante tiene matrícula activa
            boolean tieneMatriculaActiva = matriculaRepository.hasMatriculaActiva(estudiante.getIdEstudiante());

            // 6. Convertir a DTOs (sin inscripción individual, solo disponibilidad)
            List<CursoDisponibleDTO> cursosDisponiblesDTO = new ArrayList<>();
            for (Curso curso : cursosFiltrados) {
                // Como la matrícula es anual, no hay inscripción individual a cursos
                CursoDisponibleDTO dto = convertirACursoDisponibleDTO(curso, false);
                cursosDisponiblesDTO.add(dto);
            }

            // 7. Calcular cursos con cupo
            long cursosConCupo = cursosDisponiblesDTO.stream()
                    .filter(CursoDisponibleDTO::isHayCupo)
                    .count();

            // 8. Contar matrículas activas del estudiante
            int misMatriculas = matriculaRepository.countMatriculasActivasByEstudianteId(estudiante.getIdEstudiante());

            // 9. Agregar todo al modelo
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("cursosDisponibles", cursosDisponiblesDTO);
            model.addAttribute("cursosConCupo", cursosConCupo);
            model.addAttribute("misInscripciones", misMatriculas);
            model.addAttribute("gradoActual", gradoActual);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("modulo", "cursos-disponibles");
            model.addAttribute("tituloPagina", "Cursos Disponibles");

            return "estudiante/cursos-disponibles";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/docentes-disponibles")
    public String docentesDisponibles(Authentication authentication,
                                      @RequestParam(required = false) String especialidad,
                                      @RequestParam(required = false) String estado,
                                      Model model) {
        try {
            // 1. Obtener el estudiante autenticado
            Estudiante estudiante = getEstudianteAutenticado(authentication);

            // 2. Obtener el grado actual del estudiante desde su matrícula activa
            Optional<Integer> gradoActualOpt = matriculaRepository.findGradoActualByEstudianteId(estudiante.getIdEstudiante());
            Integer gradoActual = gradoActualOpt.orElse(null);

            // 3. Verificar si tiene matrícula activa
            boolean tieneMatriculaActiva = matriculaRepository.hasMatriculaActiva(estudiante.getIdEstudiante());

            // 4. Obtener todos los docentes
            List<Docente> docentes = docenteRepository.findAll();

            // 5. Aplicar filtros si existen
            if (especialidad != null && !especialidad.isEmpty()) {
                docentes = docentes.stream()
                        .filter(d -> d.getEspecialidad() != null &&
                                d.getEspecialidad().toLowerCase().contains(especialidad.toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (estado != null && !estado.isEmpty()) {
                docentes = docentes.stream()
                        .filter(d -> estado.equals(d.getEstado()))
                        .collect(Collectors.toList());
            }

            // 6. Agregar todo al modelo
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("docentes", docentes != null ? docentes : List.of());
            model.addAttribute("gradoActual", gradoActual);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("modulo", "docentes-disponibles");
            model.addAttribute("tituloPagina", "Docentes Disponibles");

            return "estudiante/docentes-disponibles";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/matricula")
    public String miMatricula(Authentication authentication, Model model) {
        try {
            // 1. Obtener el estudiante autenticado
            Estudiante estudiante = getEstudianteAutenticado(authentication);

            // 2. Obtener el historial de matrículas del estudiante
            List<Matricula> matriculas = matriculaRepository.findHistorialByEstudianteId(estudiante.getIdEstudiante());

            // 3. Obtener la matrícula activa actual
            Optional<Matricula> matriculaActivaOpt = matriculas.stream()
                    .filter(m -> "ACTIVA".equals(m.getEstado()))
                    .findFirst();

            Matricula matriculaActiva = matriculaActivaOpt.orElse(null);
            boolean tieneMatriculaActiva = matriculaActiva != null;

            // 4. Agregar todo al modelo
            model.addAttribute("estudiante", estudiante);
            model.addAttribute("matriculas", matriculas != null ? matriculas : List.of());
            model.addAttribute("matriculaActiva", matriculaActiva);
            model.addAttribute("tieneMatriculaActiva", tieneMatriculaActiva);
            model.addAttribute("modulo", "mi-matricula");
            model.addAttribute("tituloPagina", "Mi Matrícula");

            return "estudiante/mi-matricula";
        } catch (Exception e) {
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