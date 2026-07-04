package com.example.demo.repository;

import com.example.demo.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {
    List<Conversacion> findByUsuarioOrderByFechaDesc(String usuario);
}