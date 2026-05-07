package com.universidad.sistema_academico.controller;

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
import com.universidad.sistema_academico.service.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    // ==================== VISTAS MVC ====================

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Estudiante estudiante = estudianteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            List<Curso> cursos = cursoRepository.findAll();

            model.addAttribute("estudiante", estudiante);
            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("totalCursos", cursos != null ? cursos.size() : 0);

            return "estudiante/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/cursos")
    public String misCursos(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Estudiante estudiante = estudianteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            List<Curso> cursos = cursoRepository.findAll();

            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("estudiante", estudiante);

            return "estudiante/mis-cursos";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/cursos-disponibles")
    public String cursosDisponibles(Model model) {
        List<Curso> cursos = cursoRepository.findAll();
        model.addAttribute("cursosDisponibles", cursos != null ? cursos : List.of());
        return "estudiante/cursos-disponibles";
    }

    @GetMapping("/docentes-disponibles")
    public String docentes(Model model) {
        List<Docente> docentes = docenteRepository.findAll();
        model.addAttribute("docentes", docentes != null ? docentes : List.of());
        return "estudiante/docentes-disponibles";
    }

    @GetMapping("/matricula")
    public String miMatricula(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Estudiante estudiante = estudianteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

            List<Matricula> matriculas = matriculaRepository.findByEstudianteIdEstudiante(estudiante.getIdEstudiante());

            model.addAttribute("matriculas", matriculas != null ? matriculas : List.of());
            model.addAttribute("estudiante", estudiante);

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