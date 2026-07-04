package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.dto.CursoDTO;
import com.universidad.sistema_academico.model.Curso;
import com.universidad.sistema_academico.repository.CursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que contiene la lógica de negocio para la entidad Curso.
 * Realiza conversión entre Entity y DTO.
 */
@Service
@Transactional
public class CursoService {

    private final CursoRepository cursoRepository;

    // Inyección de dependencias por constructor (buena práctica, no requiere @Autowired)
    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    // ========================
    // MÉTODOS CRUD
    // ========================

    /**
     * Lista todos los cursos.
     * @return lista de CursoDTO
     */
    @Transactional(readOnly = true)
    public List<CursoDTO> listarTodos() {
        return cursoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un curso por su ID.
     * @param id identificador del curso
     * @return CursoDTO encontrado
     * @throws RuntimeException si no se encuentra el curso
     */
    @Transactional(readOnly = true)
    public CursoDTO buscarPorId(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Curso no encontrado con ID: " + id));
        return convertirADTO(curso);
    }

    /**
     * Guarda un nuevo curso o actualiza uno existente.
     * @param cursoDTO datos del curso
     * @return CursoDTO guardado con su ID generado
     */
    public CursoDTO guardar(CursoDTO cursoDTO) {
        // Validar unicidad de código al crear un nuevo curso
        if (cursoDTO.getIdCurso() == null) {
            if (cursoRepository.existsByCodigoCurso(cursoDTO.getCodigoCurso())) {
                throw new RuntimeException(
                        "Ya existe un curso con el código: " + cursoDTO.getCodigoCurso());
            }
        }

        Curso curso = convertirAEntidad(cursoDTO);
        Curso cursoGuardado = cursoRepository.save(curso);
        return convertirADTO(cursoGuardado);
    }

    /**
     * Elimina un curso por su ID.
     * @param id identificador del curso a eliminar
     * @throws RuntimeException si no se encuentra el curso
     */
    public void eliminar(Long id) {
        if (!cursoRepository.existsById(id)) {
            throw new RuntimeException(
                    "No se puede eliminar. Curso no encontrado con ID: " + id);
        }
        cursoRepository.deleteById(id);
    }

    // ========================
    // MÉTODOS DE CONVERSIÓN
    // ========================

    /**
     * Convierte una entidad Curso a CursoDTO.
     * @param curso entidad a convertir
     * @return DTO resultante
     */
    private CursoDTO convertirADTO(Curso curso) {
        CursoDTO dto = new CursoDTO();
        dto.setIdCurso(curso.getIdCurso());
        dto.setCodigoCurso(curso.getCodigoCurso());
        dto.setNombreCurso(curso.getNombreCurso());
        dto.setDescripcion(curso.getDescripcion());
        dto.setHorasSemanales(curso.getHorasSemanales());
        dto.setIdGrado(curso.getIdGrado());
        dto.setIdDocente(curso.getIdDocente());
        dto.setArea(curso.getArea());
        dto.setEstado(curso.getEstado());
        return dto;
    }

    /**
     * Convierte un CursoDTO a entidad Curso.
     * @param dto DTO a convertir
     * @return entidad resultante
     */
    private Curso convertirAEntidad(CursoDTO dto) {
        Curso curso = new Curso();
        curso.setIdCurso(dto.getIdCurso());
        curso.setCodigoCurso(dto.getCodigoCurso());
        curso.setNombreCurso(dto.getNombreCurso());
        curso.setDescripcion(dto.getDescripcion());
        curso.setHorasSemanales(dto.getHorasSemanales());
        curso.setIdGrado(dto.getIdGrado());
        curso.setIdDocente(dto.getIdDocente());
        curso.setArea(dto.getArea());
        curso.setEstado(dto.getEstado());
        return curso;
    }
}
