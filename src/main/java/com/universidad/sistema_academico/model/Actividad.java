package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "actividades", schema = "academico")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;        // Quién hizo la acción
    private String accion;         // Qué acción (CREAR, EDITAR, ELIMINAR, LOGIN)
    private String entidad;        // Sobre qué entidad (Estudiante, Docente, Curso)
    private String detalle;        // Detalle específico
    private String ip;             // Dirección IP (opcional)
    private LocalDateTime fecha;

    // Constructor por defecto
    public Actividad() {}

    // Constructor con campos principales
    public Actividad(String usuario, String accion, String entidad, String detalle) {
        this.usuario = usuario;
        this.accion = accion;
        this.entidad = entidad;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}