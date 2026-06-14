package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "docente", schema = "academico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_docente")
    private Long idDocente;

    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    @Column(name = "codigo_docente", unique = true, nullable = false, length = 20)
    private String codigoDocente;

    @Column(name = "dni", unique = true, nullable = false, length = 8)
    private String dni;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    @Column(name = "especialidad", nullable = false, length = 100)
    private String especialidad;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "celular", length = 15)
    private String celular;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "eliminado")
    private boolean eliminado = false;

    @PrePersist
    @PreUpdate
    public void normalizarCampos() {
        // Normalizar especialidad a mayúsculas
        if (especialidad != null && !especialidad.isEmpty()) {
            especialidad = especialidad.toUpperCase().trim();
        }

        // Normalizar email a minúsculas
        if (email != null && !email.isEmpty()) {
            email = email.toLowerCase().trim();
        }

        // Generar código automático si está vacío
        if (codigoDocente == null || codigoDocente.isEmpty()) {
            generarCodigoAutomatico();
        }

        // Estado por defecto
        if (estado == null) {
            estado = "ACTIVO";
        }
    }

    public void generarCodigoAutomatico() {
        if (codigoDocente == null || codigoDocente.isEmpty()) {
            String siglas = obtenerSiglas(nombres);
            String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
            this.codigoDocente = siglas + "-" + timestamp;
        }
    }

    private String obtenerSiglas(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return "DOC";
        }
        String[] palabras = nombre.trim().split("\\s+");
        StringBuilder siglas = new StringBuilder();
        for (String palabra : palabras) {
            if (palabra.length() > 0) {
                siglas.append(Character.toUpperCase(palabra.charAt(0)));
            }
        }
        if (siglas.length() < 2 && nombre.length() >= 3) {
            return nombre.substring(0, 3).toUpperCase();
        }
        return siglas.length() >= 2 ? siglas.toString() : "DOC";
    }
}