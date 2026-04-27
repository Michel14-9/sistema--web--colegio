package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    /**
     * @param codigoDocente código del docente
     * @return
     */
    Optional<Docente> findByCodigoDocente(String codigoDocente);

    /**
     * @param dni documento nacional de identidad
     * @return
     */
    Optional<Docente> findByDni(String dni);

    /**
     * @param codigoDocente código del docente
     * @return 
     */
    boolean existsByCodigoDocente(String codigoDocente);

    /**
     * @param dni documento nacional de identidad
     * @return
     */
    boolean existsByDni(String dni);

    /**
     * @param email correo del docente
     * @return
     */
    boolean existsByEmail(String email);

    /**
     * @param estado estado del docente
     * @return 
     */
    java.util.List<Docente> findByEstado(String estado);
}