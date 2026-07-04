package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {

    // ========== BÚSQUEDAS BÁSICAS ==========

    List<Nota> findByEstudianteIdEstudiante(Long estudianteId);

    List<Nota> findByCursoIdCurso(Long cursoId);

    @Query("SELECT n FROM Nota n WHERE n.estudiante.idEstudiante = :estudianteId AND n.periodoAcademico = :periodo")
    List<Nota> findByEstudianteAndPeriodoAcademico(@Param("estudianteId") Long estudianteId,
                                                   @Param("periodo") String periodo);

    // ==========  MÉTODOS PARA DOCENTE ==========

    /**
     * Buscar notas por estudiante, curso y periodo académico
     */
    @Query("SELECT n FROM Nota n WHERE n.estudiante.idEstudiante = :estudianteId AND n.curso.idCurso = :cursoId AND n.periodoAcademico = :periodo")
    List<Nota> findByEstudianteIdEstudianteAndCursoIdCursoAndPeriodoAcademico(@Param("estudianteId") Long estudianteId,
                                                                              @Param("cursoId") Long cursoId,
                                                                              @Param("periodo") String periodo);

    /**
     * Buscar notas de todos los estudiantes de un curso en un periodo
     */
    @Query("SELECT n FROM Nota n WHERE n.curso.idCurso = :cursoId AND n.periodoAcademico = :periodo")
    List<Nota> findByCursoAndPeriodoAcademico(@Param("cursoId") Long cursoId,
                                              @Param("periodo") String periodo);

    /**
     * Buscar notas por estudiante, curso y bimestre
     */
    @Query("SELECT n FROM Nota n WHERE n.estudiante.idEstudiante = :estudianteId AND n.curso.idCurso = :cursoId AND n.bimestre = :bimestre AND n.periodoAcademico = :periodo")
    List<Nota> findByEstudianteAndCursoAndBimestre(@Param("estudianteId") Long estudianteId,
                                                   @Param("cursoId") Long cursoId,
                                                   @Param("bimestre") Integer bimestre,
                                                   @Param("periodo") String periodo);

    /**
     * Obtener promedio de un estudiante en un curso
     */
    @Query("SELECT AVG(n.nota) FROM Nota n WHERE n.estudiante.idEstudiante = :estudianteId AND n.curso.idCurso = :cursoId AND n.periodoAcademico = :periodo")
    Double getPromedioByEstudianteAndCurso(@Param("estudianteId") Long estudianteId,
                                           @Param("cursoId") Long cursoId,
                                           @Param("periodo") String periodo);
}