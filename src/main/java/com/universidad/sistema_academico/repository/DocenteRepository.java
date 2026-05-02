package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    Optional<Docente> findByCodigoDocente(String codigoDocente);

    Optional<Docente> findByDni(String dni);


    Optional<Docente> findByEmail(String email);

    boolean existsByCodigoDocente(String codigoDocente);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    java.util.List<Docente> findByEstado(String estado);
}