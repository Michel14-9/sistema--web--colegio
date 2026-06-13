package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    Optional<Curso> findByCodigoCurso(String codigoCurso);

    List<Curso> findByIdGrado(Integer idGrado);

    List<Curso> findByIdDocente(Long idDocente);

    boolean existsByCodigoCurso(String codigoCurso);

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.docente")
    List<Curso> findAllWithDocente();

    // ========== NUEVOS MÉTODOS PARA VALIDACIONES ==========

    /**
     * Verifica si ya existe un curso duplicado (mismo nombre, grado, sección y turno)
     */
    boolean existsByNombreCursoAndIdGradoAndSeccionAndTurno(
            String nombreCurso, Integer idGrado, String seccion, String turno);

    /**
     * Cuenta cuántos cursos existen por grado, área y turno (para control de secciones)
     */
    int countByIdGradoAndAreaAndTurno(Integer idGrado, String area, String turno);

    /**
     * Busca cursos por grado y turno
     */
    List<Curso> findByIdGradoAndTurno(Integer idGrado, String turno);

    /**
     * Busca cursos activos por docente
     */
    List<Curso> findByIdDocenteAndEstado(Long idDocente, String estado);

    /**
     * Calcula el total de horas semanales asignadas a un docente
     */
    @Query("SELECT SUM(c.horasSemanales) FROM Curso c WHERE c.idDocente = :idDocente AND c.estado = 'ACTIVO'")
    Integer sumHorasSemanalesByDocente(@Param("idDocente") Long idDocente);

    /**
     * Verifica si un docente ya tiene un curso en el mismo horario
     */
    @Query("SELECT COUNT(c) > 0 FROM Curso c WHERE c.idDocente = :idDocente AND c.horario = :horario AND c.idCurso != :idCurso")
    boolean existsByDocenteAndHorarioAndIdCursoNot(@Param("idDocente") Long idDocente,
                                                   @Param("horario") String horario,
                                                   @Param("idCurso") Long idCurso);
}