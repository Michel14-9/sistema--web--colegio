package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Matricula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    // ========== BÚSQUEDAS BÁSICAS ==========

    /**
     * Buscar matrícula por código único
     */
    Optional<Matricula> findByCodigoMatricula(String codigoMatricula);

    /**
     * Buscar todas las matrículas de un estudiante (historial completo)
     */
    List<Matricula> findByEstudianteIdEstudiante(Long estudianteId);

    /**
     * Buscar matrículas de un estudiante ordenadas por año descendente
     */
    @Query("SELECT m FROM Matricula m WHERE m.estudiante.idEstudiante = :estudianteId ORDER BY m.anioAcademico DESC")
    List<Matricula> findHistorialByEstudianteId(@Param("estudianteId") Long estudianteId);

    /**
     * Buscar matrícula ACTIVA del estudiante en el año actual
     */
    @Query("SELECT m FROM Matricula m WHERE m.estudiante.idEstudiante = :estudianteId AND m.anioAcademico = :anio AND m.estado = 'ACTIVA'")
    Optional<Matricula> findMatriculaActivaByEstudianteAndAnio(@Param("estudianteId") Long estudianteId, @Param("anio") Integer anio);

    // ========== BÚSQUEDAS POR FILTROS ==========

    /**
     * Buscar matrículas por año académico (CORREGIDO con @Query)
     */
    @Query("SELECT m FROM Matricula m WHERE m.anioAcademico = :anio")
    List<Matricula> findByAnioAcademico(@Param("anio") Integer anioAcademico);

    /**
     * Buscar matrículas por grado
     */
    List<Matricula> findByIdGrado(Integer idGrado);

    /**
     * Buscar matrículas por estado
     */
    List<Matricula> findByEstado(String estado);

    /**
     * Buscar matrículas activas de un estudiante
     */
    List<Matricula> findByEstudianteIdEstudianteAndEstado(Long estudianteId, String estado);

    // ========== ESTADÍSTICAS ==========

    /**
     * Contar estudiantes por grado en un año específico
     */
    @Query("SELECT m.idGrado, COUNT(m) FROM Matricula m WHERE m.anioAcademico = :anio AND m.estado = 'ACTIVA' GROUP BY m.idGrado ORDER BY m.idGrado")
    List<Object[]> countEstudiantesPorGrado(@Param("anio") Integer anio);

    /**
     * Contar estudiantes por sección en un grado y año específico
     */
    @Query("SELECT m.seccion, COUNT(m) FROM Matricula m WHERE m.anioAcademico = :anio AND m.idGrado = :grado AND m.estado = 'ACTIVA' GROUP BY m.seccion ORDER BY m.seccion")
    List<Object[]> countEstudiantesPorSeccion(@Param("anio") Integer anio, @Param("grado") Integer grado);

    // ========== PAGINACIÓN CON FILTROS ==========

    /**
     * Buscar matrículas con filtros y paginación (vista de administrador)
     */
    @Query("SELECT m FROM Matricula m WHERE " +
            "(:anio IS NULL OR m.anioAcademico = :anio) AND " +
            "(:grado IS NULL OR m.idGrado = :grado) AND " +
            "(:seccion IS NULL OR m.seccion = :seccion) AND " +
            "(:turno IS NULL OR m.turno = :turno) AND " +
            "(:estado IS NULL OR m.estado = :estado) " +
            "ORDER BY m.anioAcademico DESC, m.idMatricula DESC")
    Page<Matricula> findWithFilters(@Param("anio") Integer anio,
                                    @Param("grado") Integer grado,
                                    @Param("seccion") String seccion,
                                    @Param("turno") String turno,
                                    @Param("estado") String estado,
                                    Pageable pageable);
}