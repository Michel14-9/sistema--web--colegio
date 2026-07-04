package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversaciones")
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;

    @Column(columnDefinition = "TEXT")
    private String pregunta;

    @Column(columnDefinition = "TEXT")
    private String respuesta;

    private LocalDateTime fecha;

    // Constructor vacío (necesario para JPA)
    public Conversacion() {}

    // Constructor con parámetros
    public Conversacion(String usuario, String pregunta, String respuesta) {
        this.usuario = usuario;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.fecha = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}