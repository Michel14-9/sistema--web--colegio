package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.EstadoPago;
import com.universidad.sistema_academico.model.Pago;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByMatricula_Id(Long matriculaId);

    Optional<Pago> findByNumeroRecibo(String numeroRecibo);

    List<Pago> findByEstado(EstadoPago estado);

    List<Pago> findByMatricula_Estudiante_IdEstudiante(Long estudianteId);

    @EntityGraph(attributePaths = {"matricula", "matricula.estudiante"})
    List<Pago> findAll();

    @EntityGraph(attributePaths = {"matricula", "matricula.estudiante"})
    Optional<Pago> findWithDetailsById(Long id);

    // Pagos entre fechas
    List<Pago> findByFechaPagoBetween(LocalDateTime inicio, LocalDateTime fin);

    // Pagos por estado y fecha
    List<Pago> findByEstadoAndFechaPagoBetween(
            EstadoPago estado,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // Pagos de un estudiante por fecha
    List<Pago> findByMatricula_Estudiante_IdEstudianteAndFechaPagoBetween(
            Long estudianteId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    long countByNumeroReciboStartingWith(String prefix);

}