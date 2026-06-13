package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "curso", schema = "academico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Long idCurso;

    @Column(name = "codigo_curso", unique = true, nullable = false, length = 20)
    private String codigoCurso;

    @Column(name = "nombre_curso", nullable = false, length = 150)
    private String nombreCurso;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "horas_semanales")
    private Integer horasSemanales;

    @Column(name = "id_grado")
    private Integer idGrado;

    @Column(name = "id_docente")
    private Long idDocente;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "estado", length = 20)
    private String estado;

    // ========== NUEVOS CAMPOS ==========

    @Column(name = "seccion", length = 5)
    private String seccion;  // A, B, C, D

    @Column(name = "turno", length = 10)
    private String turno;    // MAÑANA, TARDE

    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;  // default 36

    @Column(name = "alumnos_actuales")
    private Integer alumnosActuales;  // alumnos matriculados

    @Column(name = "horario", length = 50)
    private String horario;  // Ej: "LUNES 7-9", "MARTES 14-16"

    // ========== RELACIONES ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", referencedColumnName = "id_docente", insertable = false, updatable = false)
    private Docente docente;

    @PrePersist
    public void generarCodigoAutomatico() {
        if (codigoCurso == null || codigoCurso.isEmpty()) {
            String siglas = obtenerSiglas(nombreCurso);
            String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
            this.codigoCurso = siglas + "-" + timestamp;
        }

        // Valores por defecto para los nuevos campos
        if (capacidadMaxima == null) {
            capacidadMaxima = 36;
        }
        if (alumnosActuales == null) {
            alumnosActuales = 0;
        }
    }

    private String obtenerSiglas(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return "CUR";
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
        return siglas.length() >= 2 ? siglas.toString() : "CUR";
    }

    // ========== MÉTODOS HELPER ==========

    public int getCuposDisponibles() {
        return (capacidadMaxima != null ? capacidadMaxima : 36) - (alumnosActuales != null ? alumnosActuales : 0);
    }

    public boolean hayCupo() {
        return getCuposDisponibles() > 0;
    }

    public double getPorcentajeOcupacion() {
        int capacidad = capacidadMaxima != null ? capacidadMaxima : 36;
        int actuales = alumnosActuales != null ? alumnosActuales : 0;
        if (capacidad == 0) return 0;
        return (actuales * 100.0) / capacidad;
    }
}