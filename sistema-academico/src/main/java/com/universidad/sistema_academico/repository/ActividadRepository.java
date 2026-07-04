package com.universidad.sistema_academico.repository;

import com.universidad.sistema_academico.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findTop10ByOrderByFechaDesc();
}