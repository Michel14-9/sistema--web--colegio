package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.dto.EstudianteDTO;
import com.universidad.sistema_academico.model.Estudiante;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que contiene la lógica de negocio para la entidad Estudiante.
 * Realiza conversión entre Entity y DTO.
 */
@Service
@Transactional
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    // Inyección de dependencias por constructor (buena práctica, no requiere @Autowired)
    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    // ========================
    // MÉTODOS CRUD
    // ========================

    /**
     * Lista todos los estudiantes.
     * @return lista de EstudianteDTO
     */
    @Transactional(readOnly = true)
    public List<EstudianteDTO> listarTodos() {
        return estudianteRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un estudiante por su ID.
     * @param id identificador del estudiante
     * @return EstudianteDTO encontrado
     * @throws RuntimeException si no se encuentra el estudiante
     */
    @Transactional(readOnly = true)
    public EstudianteDTO buscarPorId(Long id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Estudiante no encontrado con ID: " + id));
        return convertirADTO(estudiante);
    }

    /**
     * Guarda un nuevo estudiante o actualiza uno existente.
     * @param estudianteDTO datos del estudiante
     * @return EstudianteDTO guardado con su ID generado
     */
    public EstudianteDTO guardar(EstudianteDTO estudianteDTO) {
        // Validar unicidad de código y DNI al crear un nuevo estudiante
        if (estudianteDTO.getIdEstudiante() == null) {
            if (estudianteRepository.existsByCodigoEstudiante(estudianteDTO.getCodigoEstudiante())) {
                throw new RuntimeException(
                        "Ya existe un estudiante con el código: " + estudianteDTO.getCodigoEstudiante());
            }
            if (estudianteRepository.existsByDni(estudianteDTO.getDni())) {
                throw new RuntimeException(
                        "Ya existe un estudiante con el DNI: " + estudianteDTO.getDni());
            }
        }

        Estudiante estudiante = convertirAEntidad(estudianteDTO);
        Estudiante estudianteGuardado = estudianteRepository.save(estudiante);
        return convertirADTO(estudianteGuardado);
    }

    /**
     * Elimina un estudiante por su ID.
     * @param id identificador del estudiante a eliminar
     * @throws RuntimeException si no se encuentra el estudiante
     */
    public void eliminar(Long id) {
        if (!estudianteRepository.existsById(id)) {
            throw new RuntimeException(
                    "No se puede eliminar. Estudiante no encontrado con ID: " + id);
        }
        estudianteRepository.deleteById(id);
    }

    // ========================
    // MÉTODOS DE CONVERSIÓN
    // ========================

    /**
     * Convierte una entidad Estudiante a EstudianteDTO.
     * @param estudiante entidad a convertir
     * @return DTO resultante
     */
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

    /**
     * Convierte un EstudianteDTO a entidad Estudiante.
     * @param dto DTO a convertir
     * @return entidad resultante
     */
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
