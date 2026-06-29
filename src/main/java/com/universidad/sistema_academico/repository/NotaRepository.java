// NotaRepository.java
package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {

    // CORREGIDO: usar idEstudiante en lugar de id
    List<Nota> findByEstudianteIdEstudianteAndPeriodoAcademico(Long estudianteId, String periodoAcademico);

    // CORREGIDO: usar idEstudiante en lugar de id
    List<Nota> findByEstudianteIdEstudianteAndCursoIdCursoAndPeriodoAcademico(Long estudianteId, Long cursoId, String periodoAcademico);

    // CORREGIDO: usar idEstudiante en lugar de id
    @Query("SELECT AVG(n.nota) FROM Nota n WHERE n.estudiante.idEstudiante = :estudianteId AND n.curso.idCurso = :cursoId AND n.periodoAcademico = :periodo")
    Optional<Double> getPromedioByCurso(@Param("estudianteId") Long estudianteId,
                                        @Param("cursoId") Long cursoId,
                                        @Param("periodo") String periodo);

    // CORREGIDO: usar idEstudiante en lugar de id
    @Query("SELECT AVG(n.nota) FROM Nota n WHERE n.estudiante.idEstudiante = :estudianteId AND n.periodoAcademico = :periodo")
    Optional<Double> getPromedioGeneral(@Param("estudianteId") Long estudianteId,
                                        @Param("periodo") String periodo);
}