package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.SolicitudMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudMatriculaRepository extends JpaRepository<SolicitudMatricula, Long> {

    List<SolicitudMatricula> findByEstado(String estado);

    List<SolicitudMatricula> findByEstadoOrderByFechaSolicitudDesc(String estado);

    boolean existsByDniAndEstado(String dni, String estado);




    Optional<SolicitudMatricula> findByDni(String dni);


    long countByEstado(String estado);


    List<SolicitudMatricula> findAllByOrderByFechaSolicitudDesc();
}