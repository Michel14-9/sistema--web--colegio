package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.DocenteDTO;
import com.universidad.sistema_academico.service.DocenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar operaciones CRUD de Docentes.
 * Base URL: /api/docentes
 */
@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    /**
     * GET /api/docentes
     * Lista todos los docentes.
     */
    @GetMapping
    public ResponseEntity<List<DocenteDTO>> listarTodos() {
        List<DocenteDTO> docentes = docenteService.listarTodos();
        return ResponseEntity.ok(docentes);
    }

    /**
     * GET /api/docentes/{id}
     * Busca un docente por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            DocenteDTO docente = docenteService.buscarPorId(id);
            return ResponseEntity.ok(docente);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * POST /api/docentes
     * Crea un nuevo docente.
     */
    @PostMapping
    public ResponseEntity<?> guardar(@Valid @RequestBody DocenteDTO docenteDTO) {
        try {
            DocenteDTO docenteGuardado = docenteService.guardar(docenteDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(docenteGuardado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearMensajeError(e.getMessage()));
        }
    }

    /**
     * PUT /api/docentes/{id}
     * Actualiza un docente existente.
     */
    @PutMapping("/{id}")
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

    /**
     * DELETE /api/docentes/{id}
     * Elimina un docente por su ID.
     */
    @DeleteMapping("/{id}")
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