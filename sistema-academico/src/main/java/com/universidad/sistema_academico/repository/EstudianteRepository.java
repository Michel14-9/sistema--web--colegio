package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Estudiante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Estudiante.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    /**
     * Busca un estudiante por su código único.
     * @param codigoEstudiante código del estudiante
     * @return Optional con el estudiante encontrado
     */
    Optional<Estudiante> findByCodigoEstudiante(String codigoEstudiante);

    /**
     * Busca un estudiante por su DNI.
     * @param dni documento nacional de identidad
     * @return Optional con el estudiante encontrado
     */
    Optional<Estudiante> findByDni(String dni);

    /**
     * Verifica si existe un estudiante con el código dado.
     * @param codigoEstudiante código del estudiante
     * @return true si existe, false si no
     */
    boolean existsByCodigoEstudiante(String codigoEstudiante);

    /**
     * Verifica si existe un estudiante con el DNI dado.
     * @param dni documento nacional de identidad
     * @return true si existe, false si no
     */
    boolean existsByDni(String dni);

    /**
     * Busca un estudiante por el ID de usuario asociado.
     * @param usuarioId ID del usuario en la tabla public.usuarios
     * @return Optional con el estudiante encontrado
     */
    Optional<Estudiante> findByUsuarioId(Long usuarioId);

    /**
     * Busca estudiantes por grado y año académico (NUEVO - reemplaza al antiguo findByCursoId)
     * @param grado grado del estudiante (1-11)
     * @param anio año académico
     * @return Lista de estudiantes matriculados en ese grado y año
     */
    @Query("SELECT e FROM Estudiante e JOIN Matricula m ON e.idEstudiante = m.estudiante.idEstudiante WHERE m.idGrado = :grado AND m.anioAcademico = :anio")
    List<Estudiante> findByGradoYAnio(@Param("grado") Integer grado, @Param("anio") Integer anio);

    // ==================== MÉTODOS PARA PAGINACIÓN Y FILTROS ====================

    /**
     * Busca estudiantes con filtros (nombre, estado, grado) y paginación
     * Usa consulta nativa con ILIKE para búsqueda case-insensitive
     */
    @Query(value = "SELECT e.* FROM academico.estudiante e " +
            "WHERE (:nombre IS NULL OR " +
            "e.nombres ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
            "e.apellido_paterno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
            "e.apellido_materno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
            "e.dni ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) AND " +
            "(:estado IS NULL OR e.estado = CAST(:estado AS VARCHAR)) AND " +
            "(:grado IS NULL OR e.id_grado = CAST(:grado AS INTEGER)) " +
            "ORDER BY e.id_estudiante DESC",
            countQuery = "SELECT COUNT(*) FROM academico.estudiante e " +
                    "WHERE (:nombre IS NULL OR " +
                    "e.nombres ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
                    "e.apellido_paterno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
                    "e.apellido_materno ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%') OR " +
                    "e.dni ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) AND " +
                    "(:estado IS NULL OR e.estado = CAST(:estado AS VARCHAR)) AND " +
                    "(:grado IS NULL OR e.id_grado = CAST(:grado AS INTEGER))",
            nativeQuery = true)
    Page<Estudiante> findWithFilters(@Param("nombre") String nombre,
                                     @Param("estado") String estado,
                                     @Param("grado") String grado,
                                     Pageable pageable);
    /**
     * Contar estudiantes por grado
     */
    @Query("SELECT COUNT(e) FROM Estudiante e WHERE e.idGrado = :idGrado")
    long countByIdGrado(@Param("idGrado") Integer idGrado);

    /**
     * Contar estudiantes por estado
     */
    long countByEstado(String estado);

    /**
     * Buscar estudiantes por estado
     */
    List<Estudiante> findByEstado(String estado);
    /**
     * Contar estudiantes por género
     */
    @Query("SELECT COUNT(e) FROM Estudiante e WHERE e.genero = :genero")
    long countByGenero(@Param("genero") String genero);

    /**
     * Contar estudiantes por grado y género
     */
    @Query("SELECT COUNT(e) FROM Estudiante e WHERE e.idGrado = :idGrado AND e.genero = :genero")
    long countByGradoAndGenero(@Param("idGrado") Integer idGrado, @Param("genero") String genero);




}