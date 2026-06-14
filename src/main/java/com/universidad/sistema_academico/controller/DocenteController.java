package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.DocenteDTO;
import com.universidad.sistema_academico.model.Curso;
import com.universidad.sistema_academico.model.Docente;
import com.universidad.sistema_academico.model.Estudiante;
import com.universidad.sistema_academico.model.Usuario;
import com.universidad.sistema_academico.repository.CursoRepository;
import com.universidad.sistema_academico.repository.DocenteRepository;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import com.universidad.sistema_academico.repository.UsuarioRepository;
import com.universidad.sistema_academico.service.DocenteService;
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
    private EstudianteRepository estudianteRepository;  // ← NUEVA INYECCIÓN

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Docente docente = docenteRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Docente no encontrado"));

            List<Curso> cursos = cursoRepository.findByIdDocente(docente.getIdDocente());

            model.addAttribute("docente", docente);
            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("totalCursos", cursos != null ? cursos.size() : 0);
            model.addAttribute("totalEstudiantes", 0);

            return "docente/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/mis-cursos")
    public String misCursos(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();
            Docente docente = docenteRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Docente no encontrado"));

            List<Curso> cursos = cursoRepository.findByIdDocente(docente.getIdDocente());

            model.addAttribute("cursos", cursos != null ? cursos : List.of());
            model.addAttribute("docente", docente);

            return "docente/mis-cursos";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/curso/{id}/estudiantes")
    public String verEstudiantesPorCurso(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            String email = authentication.getName();

            Docente docente = docenteRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Docente no encontrado"));

            Curso curso = cursoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            // Verificar que el curso pertenezca al docente
            if (!curso.getIdDocente().equals(docente.getIdDocente())) {
                throw new RuntimeException("No tiene acceso a este curso");
            }

            // 🔥 NUEVO: Usar findByGradoYAnio en lugar de findByCursoId
            int anioActual = java.time.Year.now().getValue();
            List<Estudiante> estudiantes = estudianteRepository.findByGradoYAnio(curso.getIdGrado(), anioActual);

            model.addAttribute("curso", curso);
            model.addAttribute("estudiantes", estudiantes != null ? estudiantes : List.of());
            model.addAttribute("docente", docente);
            model.addAttribute("totalEstudiantes", estudiantes != null ? estudiantes.size() : 0);

            return "docente/estudiantes-por-curso";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

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

    private Map<String, String> crearMensajeError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}