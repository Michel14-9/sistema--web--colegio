package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.SolicitudMatricula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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



    Optional<SolicitudMatricula> findByDniAndEstado(String dni, String estado);

    List<SolicitudMatricula> findByEstadoAndIdGrado(String estado, Integer idGrado);

    List<SolicitudMatricula> findByApoderadoEmail(String apoderadoEmail);

    @Query("SELECT COUNT(s) FROM SolicitudMatricula s WHERE s.estado = 'PENDIENTE'")
    long countPendientes();

    @Query("SELECT s FROM SolicitudMatricula s WHERE " +
            "(:estado IS NULL OR s.estado = :estado) AND " +
            "(:grado IS NULL OR s.idGrado = :grado) " +
            "ORDER BY s.fechaSolicitud DESC")
    Page<SolicitudMatricula> findWithFilters(@Param("estado") String estado,
                                             @Param("grado") Integer grado,
                                             Pageable pageable);
}