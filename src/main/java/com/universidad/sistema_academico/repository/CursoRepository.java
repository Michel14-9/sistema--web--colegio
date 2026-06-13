package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.docente WHERE c.eliminado = false OR c.eliminado IS NULL")
    List<Curso> findAllWithDocente();

    // ========== PAGINACIÓN Y FILTROS ==========

    /**
     * Paginación de cursos con docente
     */
    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.docente WHERE c.eliminado = false OR c.eliminado IS NULL")
    Page<Curso> findAllWithDocentePaged(Pageable pageable);

    @Query(value = "SELECT c.* FROM academico.curso c " +
            "LEFT JOIN academico.docente d ON d.id_docente = c.id_docente " +
            "WHERE (:grado IS NULL OR c.id_grado = CAST(:grado AS INTEGER)) " +
            "AND (:turno IS NULL OR c.turno = CAST(:turno AS VARCHAR)) " +
            "AND (:area IS NULL OR c.area = CAST(:area AS VARCHAR)) " +
            "AND (:estado IS NULL OR c.estado = CAST(:estado AS VARCHAR)) " +
            "AND (:nombre IS NULL OR c.nombre_curso ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) " +
            "AND (c.eliminado = false OR c.eliminado IS NULL) " +
            "ORDER BY c.id_curso DESC",
            countQuery = "SELECT COUNT(*) FROM academico.curso c " +
                    "WHERE (:grado IS NULL OR c.id_grado = CAST(:grado AS INTEGER)) " +
                    "AND (:turno IS NULL OR c.turno = CAST(:turno AS VARCHAR)) " +
                    "AND (:area IS NULL OR c.area = CAST(:area AS VARCHAR)) " +
                    "AND (:estado IS NULL OR c.estado = CAST(:estado AS VARCHAR)) " +
                    "AND (:nombre IS NULL OR c.nombre_curso ILIKE CONCAT('%', CAST(:nombre AS VARCHAR), '%')) " +
                    "AND (c.eliminado = false OR c.eliminado IS NULL)",
            nativeQuery = true)
    Page<Curso> findWithFilters(@Param("grado") Integer grado,
                                @Param("turno") String turno,
                                @Param("area") String area,
                                @Param("estado") String estado,
                                @Param("nombre") String nombre,
                                Pageable pageable);

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