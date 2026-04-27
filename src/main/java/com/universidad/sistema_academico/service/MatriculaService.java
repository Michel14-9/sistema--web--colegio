package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.dto.MatriculaDTO;
import com.universidad.sistema_academico.model.Curso;
import com.universidad.sistema_academico.model.Estudiante;
import com.universidad.sistema_academico.model.Matricula;
import com.universidad.sistema_academico.repository.CursoRepository;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import com.universidad.sistema_academico.repository.MatriculaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository;

    public MatriculaService(MatriculaRepository matriculaRepository,
                            EstudianteRepository estudianteRepository,
                            CursoRepository cursoRepository) {
        this.matriculaRepository = matriculaRepository;
        this.estudianteRepository = estudianteRepository;
        this.cursoRepository = cursoRepository;
    }

    @Transactional(readOnly = true)
    public List<MatriculaDTO> listarTodos() {
        return matriculaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MatriculaDTO buscarPorId(Long id) {
        Matricula m = matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));
        return convertirADTO(m);
    }

    public MatriculaDTO guardar(MatriculaDTO dto) {
        if (matriculaRepository.existsByEstudianteIdEstudianteAndCursoIdCurso(
                dto.getIdEstudiante(), dto.getIdCurso())) {
            throw new RuntimeException("El estudiante ya está matriculado en este curso");
        }

        Estudiante estudiante = estudianteRepository.findById(dto.getIdEstudiante())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        Curso curso = cursoRepository.findById(dto.getIdCurso())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        Matricula m = new Matricula();
        m.setIdMatricula(dto.getIdMatricula());
        m.setEstudiante(estudiante);
        m.setCurso(curso);
        m.setFechaMatricula(dto.getFechaMatricula() != null ? dto.getFechaMatricula() : LocalDate.now());
        m.setEstado(dto.getEstado());

        return convertirADTO(matriculaRepository.save(m));
    }

    public void eliminar(Long id) {
        matriculaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MatriculaDTO> listarPorEstudiante(Long idEstudiante) {
        return matriculaRepository.findByEstudianteIdEstudiante(idEstudiante)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private MatriculaDTO convertirADTO(Matricula m) {
        return new MatriculaDTO(
                m.getIdMatricula(),
                m.getEstudiante().getIdEstudiante(),
                m.getCurso().getIdCurso(),
                m.getFechaMatricula(),
                m.getEstado()
        );
    }
}