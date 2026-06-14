package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.model.Estudiante;
import com.universidad.sistema_academico.model.Matricula;
import com.universidad.sistema_academico.model.Usuario;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import com.universidad.sistema_academico.repository.MatriculaRepository;
import com.universidad.sistema_academico.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;

    public MatriculaService(MatriculaRepository matriculaRepository,
                            EstudianteRepository estudianteRepository,
                            UsuarioRepository usuarioRepository) {
        this.matriculaRepository = matriculaRepository;
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crear una nueva matrícula anual para un estudiante
     */
    public Matricula crearMatricula(Long estudianteId, Integer anioAcademico, Integer idGrado,
                                    String seccion, String turno, Long administradorId) {

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        Usuario administrador = usuarioRepository.findById(administradorId)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        // Verificar si ya tiene matrícula activa este año
        if (matriculaRepository.findMatriculaActivaByEstudianteAndAnio(estudianteId, anioAcademico).isPresent()) {
            throw new RuntimeException("El estudiante ya tiene una matrícula activa para el año " + anioAcademico);
        }

        Matricula matricula = new Matricula();
        matricula.setEstudiante(estudiante);
        matricula.setAnioAcademico(anioAcademico);
        matricula.setIdGrado(idGrado);
        matricula.setSeccion(seccion);
        matricula.setTurno(turno);
        matricula.setFechaMatricula(LocalDate.now());
        matricula.setEstado("ACTIVA");
        matricula.setAprobadoPor(administrador);
        matricula.setFechaAprobacion(LocalDateTime.now());

        // El código se genera automáticamente en @PrePersist

        return matriculaRepository.save(matricula);
    }

    /**
     * Actualizar grado/sección/turno de una matrícula (ej: promoción de año)
     */
    public Matricula actualizarMatricula(Long matriculaId, Integer idGrado, String seccion, String turno) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));

        matricula.setIdGrado(idGrado);
        matricula.setSeccion(seccion);
        matricula.setTurno(turno);

        return matriculaRepository.save(matricula);
    }

    /**
     * Finalizar una matrícula (cuando el estudiante completa el año)
     */
    public Matricula finalizarMatricula(Long matriculaId) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));

        matricula.setEstado("FINALIZADA");
        return matriculaRepository.save(matricula);
    }

    /**
     * Anular una matrícula
     */
    public Matricula anularMatricula(Long matriculaId, String motivo) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));

        matricula.setEstado("ANULADA");
        matricula.setObservaciones(motivo);
        return matriculaRepository.save(matricula);
    }

    /**
     * Listar todas las matrículas
     */
    @Transactional(readOnly = true)
    public List<Matricula> listarTodos() {
        return matriculaRepository.findAll();
    }

    /**
     * Buscar matrícula por ID
     */
    @Transactional(readOnly = true)
    public Matricula buscarPorId(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));
    }

    /**
     * Listar matrículas de un estudiante (historial)
     */
    @Transactional(readOnly = true)
    public List<Matricula> listarPorEstudiante(Long estudianteId) {
        return matriculaRepository.findHistorialByEstudianteId(estudianteId);
    }

    /**
     * Obtener matrícula activa del estudiante en el año actual
     */
    @Transactional(readOnly = true)
    public Matricula getMatriculaActiva(Long estudianteId, Integer anio) {
        return matriculaRepository.findMatriculaActivaByEstudianteAndAnio(estudianteId, anio)
                .orElse(null);
    }

    /**
     * Contar estudiantes por grado en un año
     */
    @Transactional(readOnly = true)
    public List<Object[]> contarEstudiantesPorGrado(Integer anio) {
        return matriculaRepository.countEstudiantesPorGrado(anio);
    }

    /**
     * Verificar si un estudiante tiene matrícula activa
     */
    @Transactional(readOnly = true)
    public boolean tieneMatriculaActiva(Long estudianteId, Integer anio) {
        return matriculaRepository.findMatriculaActivaByEstudianteAndAnio(estudianteId, anio).isPresent();
    }
}