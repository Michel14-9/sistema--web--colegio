package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.dto.EstudianteDTO;
import com.universidad.sistema_academico.model.Estudiante;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    // ============================================
    // MÉTODOS PARA MATRICULA CONTROLLER
    // ============================================

    /**
     * Guarda una entidad Estudiante directamente
     */
    public Estudiante saveEstudiante(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }

    /**
     * Verifica si existe un DNI
     */
    public boolean existsByDni(String dni) {
        return estudianteRepository.existsByDni(dni);
    }

    /**
     * Busca estudiante por DNI (retorna Optional)
     */
    public Optional<Estudiante> findByDni(String dni) {
        return estudianteRepository.findByDni(dni);
    }

    /**
     * Busca estudiante por ID de usuario
     */
    public Optional<Estudiante> findByUsuarioId(Long usuarioId) {
        return estudianteRepository.findByUsuarioId(usuarioId);
    }

    // ============================================
    // MÉTODOS CRUD EXISTENTES CON DTO
    // ============================================

    @Transactional(readOnly = true)
    public List<EstudianteDTO> listarTodos() {
        return estudianteRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstudianteDTO buscarPorId(Long id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + id));
        return convertirADTO(estudiante);
    }

    public EstudianteDTO guardar(EstudianteDTO estudianteDTO) {
        if (estudianteDTO.getIdEstudiante() == null) {
            if (estudianteRepository.existsByCodigoEstudiante(estudianteDTO.getCodigoEstudiante())) {
                throw new RuntimeException("Ya existe un estudiante con el código: " + estudianteDTO.getCodigoEstudiante());
            }
            if (estudianteRepository.existsByDni(estudianteDTO.getDni())) {
                throw new RuntimeException("Ya existe un estudiante con el DNI: " + estudianteDTO.getDni());
            }
        }
        Estudiante estudiante = convertirAEntidad(estudianteDTO);
        Estudiante estudianteGuardado = estudianteRepository.save(estudiante);
        return convertirADTO(estudianteGuardado);
    }

    public void eliminar(Long id) {
        if (!estudianteRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Estudiante no encontrado con ID: " + id);
        }
        estudianteRepository.deleteById(id);
    }

    // ============================================
    // MÉTODOS DE CONVERSIÓN
    // ============================================

    private EstudianteDTO convertirADTO(Estudiante estudiante) {
        EstudianteDTO dto = new EstudianteDTO();
        dto.setIdEstudiante(estudiante.getIdEstudiante());
        dto.setCodigoEstudiante(estudiante.getCodigoEstudiante());
        dto.setDni(estudiante.getDni());
        dto.setNombres(estudiante.getNombres());
        dto.setApellidoPaterno(estudiante.getApellidoPaterno());
        dto.setApellidoMaterno(estudiante.getApellidoMaterno());
        dto.setFechaNacimiento(estudiante.getFechaNacimiento());
        dto.setGenero(estudiante.getGenero());
        dto.setEmailInstitucional(estudiante.getEmailInstitucional());
        dto.setCelular(estudiante.getCelular());
        dto.setIdGrado(estudiante.getIdGrado());
        dto.setSeccion(estudiante.getSeccion());
        dto.setTurno(estudiante.getTurno());
        dto.setEstado(estudiante.getEstado());
        dto.setFechaIngreso(estudiante.getFechaIngreso());
        return dto;
    }

    private Estudiante convertirAEntidad(EstudianteDTO dto) {
        Estudiante estudiante = new Estudiante();
        estudiante.setIdEstudiante(dto.getIdEstudiante());
        estudiante.setCodigoEstudiante(dto.getCodigoEstudiante());
        estudiante.setDni(dto.getDni());
        estudiante.setNombres(dto.getNombres());
        estudiante.setApellidoPaterno(dto.getApellidoPaterno());
        estudiante.setApellidoMaterno(dto.getApellidoMaterno());
        estudiante.setFechaNacimiento(dto.getFechaNacimiento());
        estudiante.setGenero(dto.getGenero());
        estudiante.setEmailInstitucional(dto.getEmailInstitucional());
        estudiante.setCelular(dto.getCelular());
        estudiante.setIdGrado(dto.getIdGrado());
        estudiante.setSeccion(dto.getSeccion());
        estudiante.setTurno(dto.getTurno());
        estudiante.setEstado(dto.getEstado());
        estudiante.setFechaIngreso(dto.getFechaIngreso());
        return estudiante;
    }
}