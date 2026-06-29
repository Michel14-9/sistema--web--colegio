// AsistenciaRepository.java
package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    long countByEstudianteIdEstudianteAndEstado(@Param("estudianteId") Long estudianteId,
                                                @Param("estado") String estado);

    long countByEstudianteIdEstudianteAndCursoIdCursoAndEstado(@Param("estudianteId") Long estudianteId,
                                                               @Param("cursoId") Long cursoId,
                                                               @Param("estado") String estado);

    List<Asistencia> findByEstudianteIdEstudianteAndFechaBetween(@Param("estudianteId") Long estudianteId,
                                                                 @Param("fechaInicio") LocalDate fechaInicio,
                                                                 @Param("fechaFin") LocalDate fechaFin);

    List<Asistencia> findByEstudianteIdEstudianteAndCursoIdCurso(@Param("estudianteId") Long estudianteId,
                                                                 @Param("cursoId") Long cursoId);

    // CORREGIDO: Cambiar de long a Long y usar CAST
    @Query(value = "SELECT CAST(COUNT(DISTINCT a.fecha) AS BIGINT) FROM academico.asistencia a WHERE a.id_estudiante = CAST(:estudianteId AS BIGINT) AND a.id_curso = CAST(:cursoId AS BIGINT)", nativeQuery = true)
    Long countTotalClasesByCurso(@Param("estudianteId") Long estudianteId,
                                 @Param("cursoId") Long cursoId);

    // Alternativa con JPQL y CAST
    // @Query("SELECT CAST(COUNT(DISTINCT a.fecha) AS long) FROM Asistencia a WHERE a.estudiante.idEstudiante = :estudianteId AND a.curso.idCurso = :cursoId")
    // Long countTotalClasesByCurso(@Param("estudianteId") Long estudianteId,
    //                              @Param("cursoId") Long cursoId);

    List<Asistencia> findByEstudianteIdEstudianteAndFecha(@Param("estudianteId") Long estudianteId,
                                                          @Param("fecha") LocalDate fecha);

    @Query("SELECT (COUNT(CASE WHEN a.estado = 'PRESENTE' THEN 1 END) * 100.0 / COUNT(*)) " +
            "FROM Asistencia a WHERE a.estudiante.idEstudiante = :estudianteId AND a.curso.idCurso = :cursoId")
    Double getPorcentajeAsistenciaByCurso(@Param("estudianteId") Long estudianteId,
                                          @Param("cursoId") Long cursoId);
}