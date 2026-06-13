package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Curso.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    /**
     * Busca un curso por su código único.
     * @param codigoCurso código del curso
     * @return Optional con el curso encontrado
     */
    Optional<Curso> findByCodigoCurso(String codigoCurso);

    /**
     * Busca cursos por grado.
     * @param idGrado identificador del grado
     * @return lista de cursos del grado
     */
    List<Curso> findByIdGrado(Integer idGrado);

    /**
     * Busca cursos asignados a un docente.
     * @param idDocente identificador del docente
     * @return lista de cursos del docente
     */
    List<Curso> findByIdDocente(Long idDocente);

    /**
     * Verifica si existe un curso con el código dado.
     * @param codigoCurso código del curso
     * @return true si existe, false si no
     */
    boolean existsByCodigoCurso(String codigoCurso);
    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.docente")
    List<Curso> findAllWithDocente();
}
