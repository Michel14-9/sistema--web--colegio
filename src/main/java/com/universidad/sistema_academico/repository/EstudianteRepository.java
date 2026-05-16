package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Estudiante.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    /**
     * Busca un estudiante por su código único.
     * @param codigoEstudiante código del estudiante
     * @return Optional con el estudiante encontrado
     */
    Optional<Estudiante> findByCodigoEstudiante(String codigoEstudiante);

    /**
     * Busca un estudiante por su DNI.
     * @param dni documento nacional de identidad
     * @return Optional con el estudiante encontrado
     */
    Optional<Estudiante> findByDni(String dni);

    /**
     * Verifica si existe un estudiante con el código dado.
     * @param codigoEstudiante código del estudiante
     * @return true si existe, false si no
     */
    boolean existsByCodigoEstudiante(String codigoEstudiante);

    /**
     * Verifica si existe un estudiante con el DNI dado.
     * @param dni documento nacional de identidad
     * @return true si existe, false si no
     */
    boolean existsByDni(String dni);

    /**
     * Busca un estudiante por el ID de usuario asociado.
     * @param usuarioId ID del usuario en la tabla public.usuarios
     * @return Optional con el estudiante encontrado
     */
    Optional<Estudiante> findByUsuarioId(Long usuarioId);

    /**
     * Busca estudiantes matriculados en un curso específico
     * @param cursoId ID del curso
     * @return Lista de estudiantes matriculados en el curso
     */
    @Query("SELECT e FROM Estudiante e JOIN Matricula m ON e.idEstudiante = m.estudiante.idEstudiante WHERE m.curso.idCurso = :cursoId")
    List<Estudiante> findByCursoId(@Param("cursoId") Long cursoId);
}