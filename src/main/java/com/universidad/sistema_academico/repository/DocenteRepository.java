package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Docente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    Optional<Docente> findByCodigoDocente(String codigoDocente);

    Optional<Docente> findByDni(String dni);

    Optional<Docente> findByEmail(String email);

    boolean existsByCodigoDocente(String codigoDocente);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    List<Docente> findByEstado(String estado);

    // ========== NUEVOS MÉTODOS PARA VALIDAR UNICIDAD EXCLUYENDO UN ID ==========

    /**
     * Verifica si existe un docente con un DNI específico, excluyendo un ID
     */
    @Query("SELECT COUNT(d) > 0 FROM Docente d WHERE d.dni = :dni AND d.idDocente != :idDocente AND (d.eliminado = false OR d.eliminado IS NULL)")
    boolean existsByDniAndIdDocenteNot(@Param("dni") String dni, @Param("idDocente") Long idDocente);

    /**
     * Verifica si existe un docente con un email específico, excluyendo un ID
     */
    @Query("SELECT COUNT(d) > 0 FROM Docente d WHERE d.email = :email AND d.idDocente != :idDocente AND (d.eliminado = false OR d.eliminado IS NULL)")
    boolean existsByEmailAndIdDocenteNot(@Param("email") String email, @Param("idDocente") Long idDocente);

    /**
     * Verifica si existe un docente con un código específico, excluyendo un ID
     */
    @Query("SELECT COUNT(d) > 0 FROM Docente d WHERE d.codigoDocente = :codigoDocente AND d.idDocente != :idDocente AND (d.eliminado = false OR d.eliminado IS NULL)")
    boolean existsByCodigoDocenteAndIdDocenteNot(@Param("codigoDocente") String codigoDocente, @Param("idDocente") Long idDocente);

    // ========== BÚSQUEDA POR ESPECIALIDAD ==========

    /**
     * Buscar docentes por especialidad, estado ACTIVO y no eliminados
     */
    @Query("SELECT d FROM Docente d WHERE d.especialidad = :especialidad AND d.estado = 'ACTIVO' AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findByEspecialidadAndEstadoAndEliminadoFalse(@Param("especialidad") String especialidad);

    // ========== DOCENTES ACTIVOS ==========

    @Query("SELECT d FROM Docente d WHERE (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findAllActive();

    @Query("SELECT d FROM Docente d WHERE (d.eliminado = false OR d.eliminado IS NULL)")
    Page<Docente> findAllActivePaged(Pageable pageable);

    // ========== PAGINACIÓN Y FILTROS ==========

    @Query(value = "SELECT d.* FROM academico.docente d " +
            "WHERE (:nombre IS NULL OR d.nombres ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
            "d.apellido_paterno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
            "d.apellido_materno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) AND " +
            "(:especialidad IS NULL OR d.especialidad = CAST(:especialidad AS VARCHAR)) AND " +
            "(:estado IS NULL OR d.estado = CAST(:estado AS VARCHAR)) AND " +
            "(d.eliminado = false OR d.eliminado IS NULL) " +
            "ORDER BY d.id_docente DESC",
            countQuery = "SELECT COUNT(*) FROM academico.docente d " +
                    "WHERE (:nombre IS NULL OR d.nombres ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
                    "d.apellido_paterno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
                    "d.apellido_materno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) AND " +
                    "(:especialidad IS NULL OR d.especialidad = CAST(:especialidad AS VARCHAR)) AND " +
                    "(:estado IS NULL OR d.estado = CAST(:estado AS VARCHAR)) AND " +
                    "(d.eliminado = false OR d.eliminado IS NULL)",
            nativeQuery = true)
    Page<Docente> findWithFilters(@Param("nombre") String nombre,
                                  @Param("especialidad") String especialidad,
                                  @Param("estado") String estado,
                                  Pageable pageable);

    // ========== MÉTODOS ADICIONALES ÚTILES ==========

    /**
     * Buscar docentes por especialidad (todos los estados)
     */
    @Query("SELECT d FROM Docente d WHERE d.especialidad = :especialidad AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findByEspecialidad(@Param("especialidad") String especialidad);

    /**
     * Buscar docentes por nombre (contiene)
     */
    @Query("SELECT d FROM Docente d WHERE (d.nombres ILIKE CONCAT('%', :nombre, '%') OR " +
            "d.apellidoPaterno ILIKE CONCAT('%', :nombre, '%') OR " +
            "d.apellidoMaterno ILIKE CONCAT('%', :nombre, '%')) AND " +
            "(d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findByNombreContaining(@Param("nombre") String nombre);

    /**
     * Buscar docentes por estado y no eliminados
     */
    @Query("SELECT d FROM Docente d WHERE d.estado = :estado AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findByEstadoAndEliminadoFalse(@Param("estado") String estado);

    /**
     * Contar docentes por especialidad
     */
    @Query("SELECT COUNT(d) FROM Docente d WHERE d.especialidad = :especialidad AND (d.eliminado = false OR d.eliminado IS NULL)")
    long countByEspecialidad(@Param("especialidad") String especialidad);

    /**
     * Contar docentes activos
     */
    @Query("SELECT COUNT(d) FROM Docente d WHERE d.estado = 'ACTIVO' AND (d.eliminado = false OR d.eliminado IS NULL)")
    long countActive();

    /**
     * Contar docentes inactivos
     */
    @Query("SELECT COUNT(d) FROM Docente d WHERE d.estado = 'INACTIVO' AND (d.eliminado = false OR d.eliminado IS NULL)")
    long countInactive();

    /**
     * Buscar docentes con cursos asignados
     */
    @Query("SELECT DISTINCT d FROM Docente d JOIN Curso c ON c.idDocente = d.idDocente WHERE c.estado = 'ACTIVO' AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findDocentesWithCursos();

    /**
     * Buscar docentes sin cursos asignados
     */
    @Query("SELECT d FROM Docente d WHERE d.idDocente NOT IN (SELECT c.idDocente FROM Curso c WHERE c.estado = 'ACTIVO') AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findDocentesWithoutCursos();

    /**
     * Buscar docentes por especialidad y estado
     */
    @Query("SELECT d FROM Docente d WHERE d.especialidad = :especialidad AND d.estado = :estado AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findByEspecialidadAndEstado(@Param("especialidad") String especialidad, @Param("estado") String estado);

    /**
     * Buscar docentes con DNI que contenga un valor
     */
    @Query("SELECT d FROM Docente d WHERE d.dni LIKE CONCAT('%', :dni, '%') AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findByDniContaining(@Param("dni") String dni);

    /**
     * Buscar docentes por email institucional (dominio)
     */
    @Query("SELECT d FROM Docente d WHERE d.email LIKE CONCAT('%@docente.iesancarlos.edu.pe') AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findDocentesInstitucionales();

    /**
     * Buscar docentes con email personal (no institucional)
     */
    @Query("SELECT d FROM Docente d WHERE d.email NOT LIKE CONCAT('%@docente.iesancarlos.edu.pe') AND d.email IS NOT NULL AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findDocentesWithPersonalEmail();

    /**
     * Buscar docentes que no tienen usuario asociado
     */
    @Query("SELECT d FROM Docente d WHERE d.usuario IS NULL AND (d.eliminado = false OR d.eliminado IS NULL)")
    List<Docente> findDocentesWithoutUser();

    /**
     * Buscar docente por ID de usuario
     */
    @Query("SELECT d FROM Docente d WHERE d.usuario.id = :usuarioId AND (d.eliminado = false OR d.eliminado IS NULL)")
    Optional<Docente> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}