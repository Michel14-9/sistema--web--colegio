package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.MatriculaDTO;
import com.universidad.sistema_academico.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar operaciones CRUD de Matrículas.
 * Base URL: /api/matriculas
 */
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }


    /**
     * GET /api/matriculas
     * Lista todas las matrículas.
     */
    @GetMapping
    public ResponseEntity<List<MatriculaDTO>> listarTodos() {
        List<MatriculaDTO> matriculas = matriculaService.listarTodos();
        return ResponseEntity.ok(matriculas);
    }

    /**
     * GET /api/matriculas/{id}
     * Busca una matrícula por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            MatriculaDTO matricula = matriculaService.buscarPorId(id);
            return ResponseEntity.ok(matricula);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * POST /api/matriculas
     * Crea una nueva matrícula.
     */
    @PostMapping
    public ResponseEntity<?> guardar(@Valid @RequestBody MatriculaDTO matriculaDTO) {
        try {
            MatriculaDTO matriculaGuardada = matriculaService.guardar(matriculaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(matriculaGuardada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * PUT /api/matriculas/{id}
     * Actualiza una matrícula existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody MatriculaDTO matriculaDTO) {
        try {
            matriculaDTO.setIdMatricula(id);
            MatriculaDTO matriculaActualizada = matriculaService.guardar(matriculaDTO);
            return ResponseEntity.ok(matriculaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * DELETE /api/matriculas/{id}
     * Elimina una matrícula por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            matriculaService.eliminar(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Matrícula eliminada exitosamente");
            response.put("id", id.toString());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }


    /**
     * GET /api/matriculas/estudiante/{idEstudiante}
     * Lista las matrículas de un estudiante.
     */
    @GetMapping("/estudiante/{idEstudiante}")
    public ResponseEntity<?> listarPorEstudiante(@PathVariable Long idEstudiante) {
        try {
            List<MatriculaDTO> matriculas = matriculaService.listarPorEstudiante(idEstudiante);
            return ResponseEntity.ok(matriculas);
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