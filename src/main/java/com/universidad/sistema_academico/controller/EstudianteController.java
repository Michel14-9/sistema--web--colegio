package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.EstudianteDTO;
import com.universidad.sistema_academico.service.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar operaciones CRUD de Estudiantes.
 * Base URL: /api/estudiantes
 */
@RestController
@RequestMapping("/api/estudiantes")
@CrossOrigin(origins = "*") // Permitir peticiones desde cualquier origen (ajustar en producción)
public class EstudianteController {

    private final EstudianteService estudianteService;

    // Inyección de dependencias por constructor
    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // ========================
    // ENDPOINTS CRUD
    // ========================

    /**
     * GET /api/estudiantes
     * Lista todos los estudiantes.
     */
    @GetMapping
    public ResponseEntity<List<EstudianteDTO>> listarTodos() {
        List<EstudianteDTO> estudiantes = estudianteService.listarTodos();
        return ResponseEntity.ok(estudiantes);
    }

    /**
     * GET /api/estudiantes/{id}
     * Busca un estudiante por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            EstudianteDTO estudiante = estudianteService.buscarPorId(id);
            return ResponseEntity.ok(estudiante);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * POST /api/estudiantes
     * Crea un nuevo estudiante.
     */
    @PostMapping
    public ResponseEntity<?> guardar(@Valid @RequestBody EstudianteDTO estudianteDTO) {
        try {
            EstudianteDTO estudianteGuardado = estudianteService.guardar(estudianteDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(estudianteGuardado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * PUT /api/estudiantes/{id}
     * Actualiza un estudiante existente.
     */
    @PutMapping("/{id}")
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

    /**
     * DELETE /api/estudiantes/{id}
     * Elimina un estudiante por su ID.
     */
    @DeleteMapping("/{id}")
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
