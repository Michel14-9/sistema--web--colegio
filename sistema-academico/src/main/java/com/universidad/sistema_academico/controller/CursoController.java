package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.CursoDTO;
import com.universidad.sistema_academico.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar operaciones CRUD de Cursos.
 * Base URL: /api/cursos
 */
@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*") // Permitir peticiones desde cualquier origen (ajustar en producción)
public class CursoController {

    private final CursoService cursoService;

    // Inyección de dependencias por constructor
    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    // ========================
    // ENDPOINTS CRUD
    // ========================

    /**
     * GET /api/cursos
     * Lista todos los cursos.
     */
    @GetMapping
    public ResponseEntity<List<CursoDTO>> listarTodos() {
        List<CursoDTO> cursos = cursoService.listarTodos();
        return ResponseEntity.ok(cursos);
    }

    /**
     * GET /api/cursos/{id}
     * Busca un curso por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            CursoDTO curso = cursoService.buscarPorId(id);
            return ResponseEntity.ok(curso);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * POST /api/cursos
     * Crea un nuevo curso.
     */
    @PostMapping
    public ResponseEntity<?> guardar(@Valid @RequestBody CursoDTO cursoDTO) {
        try {
            CursoDTO cursoGuardado = cursoService.guardar(cursoDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(cursoGuardado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * PUT /api/cursos/{id}
     * Actualiza un curso existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody CursoDTO cursoDTO) {
        try {
            cursoDTO.setIdCurso(id);
            CursoDTO cursoActualizado = cursoService.guardar(cursoDTO);
            return ResponseEntity.ok(cursoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * DELETE /api/cursos/{id}
     * Elimina un curso por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            cursoService.eliminar(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Curso eliminado exitosamente");
            response.put("id", id.toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    // ========================
    // MÉTODOS AUXILIARES
    // ========================

    /**
     * Crea un mapa con el mensaje de error para respuestas JSON consistentes.
     */
    private Map<String, String> crearMensajeError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}
