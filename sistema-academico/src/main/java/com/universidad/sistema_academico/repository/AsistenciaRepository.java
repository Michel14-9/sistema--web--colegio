package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    // ========== CONTADORES ==========

    long countByEstudianteIdEstudianteAndEstado(@Param("estudianteId") Long estudianteId,
                                                @Param("estado") String estado);

    long countByEstudianteIdEstudianteAndCursoIdCursoAndEstado(@Param("estudianteId") Long estudianteId,
                                                               @Param("cursoId") Long cursoId,
                                                               @Param("estado") String estado);

    // ========== BÚSQUEDAS POR FECHA ==========

    List<Asistencia> findByEstudianteIdEstudianteAndFechaBetween(@Param("estudianteId") Long estudianteId,
                                                                 @Param("fechaInicio") LocalDate fechaInicio,
                                                                 @Param("fechaFin") LocalDate fechaFin);

    List<Asistencia> findByEstudianteIdEstudianteAndFecha(@Param("estudianteId") Long estudianteId,
                                                          @Param("fecha") LocalDate fecha);

    // ========== BÚSQUEDAS POR CURSO ==========

    List<Asistencia> findByEstudianteIdEstudianteAndCursoIdCurso(@Param("estudianteId") Long estudianteId,
                                                                 @Param("cursoId") Long cursoId);

    // ========== NUEVOS MÉTODOS PARA DOCENTE ==========

    /**
     * Buscar asistencia por estudiante, curso y fecha
     */
    @Query("SELECT a FROM Asistencia a WHERE a.estudiante.idEstudiante = :estudianteId AND a.curso.idCurso = :cursoId AND a.fecha = :fecha")
    Optional<Asistencia> findByEstudianteIdEstudianteAndCursoIdCursoAndFecha(@Param("estudianteId") Long estudianteId,
                                                                             @Param("cursoId") Long cursoId,
                                                                             @Param("fecha") LocalDate fecha);

    /**
     * Buscar asistencias de un curso en una fecha específica
     */
    @Query("SELECT a FROM Asistencia a WHERE a.curso.idCurso = :cursoId AND a.fecha = :fecha")
    List<Asistencia> findByCursoIdCursoAndFecha(@Param("cursoId") Long cursoId,
                                                @Param("fecha") LocalDate fecha);

    /**
     * Contar total de clases (días distintos) de un curso
     */
    @Query("SELECT COUNT(DISTINCT a.fecha) FROM Asistencia a WHERE a.estudiante.idEstudiante = :estudianteId AND a.curso.idCurso = :cursoId")
    Long countTotalClasesByCurso(@Param("estudianteId") Long estudianteId,
                                 @Param("cursoId") Long cursoId);

    /**
     * Calcular porcentaje de asistencia de un estudiante en un curso
     */
    @Query("SELECT (COUNT(CASE WHEN a.estado = 'PRESENTE' THEN 1 END) * 100.0 / COUNT(*)) " +
            "FROM Asistencia a WHERE a.estudiante.idEstudiante = :estudianteId AND a.curso.idCurso = :cursoId")
    Double getPorcentajeAsistenciaByCurso(@Param("estudianteId") Long estudianteId,
                                          @Param("cursoId") Long cursoId);

    /**
     * Contar asistencias de un curso en una fecha específica por estado
     */
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.curso.idCurso = :cursoId AND a.fecha = :fecha AND a.estado = :estado")
    long countByCursoIdAndFechaAndEstado(@Param("cursoId") Long cursoId,
                                         @Param("fecha") LocalDate fecha,
                                         @Param("estado") String estado);

    /**
     * Buscar todas las asistencias de un curso
     */
    List<Asistencia> findByCursoIdCurso(Long cursoId);


}