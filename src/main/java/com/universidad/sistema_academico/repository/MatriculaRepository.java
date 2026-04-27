package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /**
     * Lista todas las matrículas de un estudiante.
     * @param idEstudiante ID del estudiante
     * @return lista de matrículas
     */
    List<Matricula> findByEstudianteIdEstudiante(Long idEstudiante);

    /**
     * Lista todas las matrículas de un curso.
     * @param idCurso ID del curso
     * @return lista de matrículas
     */
    List<Matricula> findByCursoIdCurso(Long idCurso);

    /**
     * Lista matrículas por estado.
     * @param estado estado de la matrícula
     * @return lista de matrículas
     */
    List<Matricula> findByEstado(String estado);

    /**
     * Verifica si un estudiante ya está matriculado en un curso.
     * @param idEstudiante ID del estudiante
     * @param idCurso ID del curso
     * @return true si existe la matrícula, false si no
     */
    boolean existsByEstudianteIdEstudianteAndCursoIdCurso(Long idEstudiante, Long idCurso);

    /**
     * Busca matrículas de un estudiante con un estado específico.
     * @param idEstudiante ID del estudiante
     * @param estado estado de la matrícula
     * @return lista de matrículas
     */
    List<Matricula> findByEstudianteIdEstudianteAndEstado(Long idEstudiante, String estado);
}