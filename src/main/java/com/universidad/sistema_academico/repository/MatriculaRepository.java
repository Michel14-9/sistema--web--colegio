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

    Optional<Matricula> findByCodigoMatricula(String codigoMatricula);

    List<Matricula> findByEstudianteIdEstudiante(Long estudianteId);

    @Query("SELECT m FROM Matricula m WHERE m.estudiante.idEstudiante = :estudianteId ORDER BY m.anioAcademico DESC")
    List<Matricula> findHistorialByEstudianteId(@Param("estudianteId") Long estudianteId);

    @Query("SELECT m FROM Matricula m WHERE m.estudiante.idEstudiante = :estudianteId AND m.anioAcademico = :anio AND m.estado = 'ACTIVA'")
    Optional<Matricula> findMatriculaActivaByEstudianteAndAnio(@Param("estudianteId") Long estudianteId, @Param("anio") Integer anio);

    /**
     * Obtener el grado actual del estudiante (su matrícula activa)
     */
    @Query("SELECT m.idGrado FROM Matricula m WHERE m.estudiante.idEstudiante = :estudianteId AND m.estado = 'ACTIVA'")
    Optional<Integer> findGradoActualByEstudianteId(@Param("estudianteId") Long estudianteId);

    /**
     * Verificar si un estudiante tiene matrícula activa
     */
    @Query("SELECT COUNT(m) > 0 FROM Matricula m WHERE m.estudiante.idEstudiante = :estudianteId AND m.estado = 'ACTIVA'")
    boolean hasMatriculaActiva(@Param("estudianteId") Long estudianteId);

    /**
     * Contar matrículas activas de un estudiante
     */
    @Query("SELECT COUNT(m) FROM Matricula m WHERE m.estudiante.idEstudiante = :estudianteId AND m.estado = 'ACTIVA'")
    int countMatriculasActivasByEstudianteId(@Param("estudianteId") Long estudianteId);

    // ========== BÚSQUEDAS POR FILTROS ==========

    @Query("SELECT m FROM Matricula m WHERE m.anioAcademico = :anio")
    List<Matricula> findByAnioAcademico(@Param("anio") Integer anioAcademico);

    List<Matricula> findByIdGrado(Integer idGrado);

    List<Matricula> findByEstado(String estado);

    List<Matricula> findByEstudianteIdEstudianteAndEstado(Long estudianteId, String estado);

    // ========== ESTADÍSTICAS ==========

    @Query("SELECT m.idGrado, COUNT(m) FROM Matricula m WHERE m.anioAcademico = :anio AND m.estado = 'ACTIVA' GROUP BY m.idGrado ORDER BY m.idGrado")
    List<Object[]> countEstudiantesPorGrado(@Param("anio") Integer anio);

    @Query("SELECT m.seccion, COUNT(m) FROM Matricula m WHERE m.anioAcademico = :anio AND m.idGrado = :grado AND m.estado = 'ACTIVA' GROUP BY m.seccion ORDER BY m.seccion")
    List<Object[]> countEstudiantesPorSeccion(@Param("anio") Integer anio, @Param("grado") Integer grado);

    // ========== PAGINACIÓN CON FILTROS ==========

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