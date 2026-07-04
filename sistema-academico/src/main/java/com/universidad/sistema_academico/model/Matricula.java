package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla "matricula" en el esquema "academico".
 * Ahora representa una MATRÍCULA ANUAL (el estudiante se matricula a un grado, no a cursos individuales)
 */
@Entity
@Table(name = "matricula", schema = "academico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_matricula")
    private Long idMatricula;

    /**
     * Código único de la matrícula (ej: MAT-2025-001)
     */
    @Column(name = "codigo_matricula", unique = true, length = 30)
    private String codigoMatricula;

    /**
     * Relación muchos a uno: muchas matrículas pueden pertenecer a un estudiante.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", referencedColumnName = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    /**
     * Año académico de la matrícula (2025, 2026, etc.)
     */
    @Column(name = "anio_academico", nullable = false)
    private Integer anioAcademico;

    /**
     * Grado al que se matricula (1-11)
     */
    @Column(name = "id_grado", nullable = false)
    private Integer idGrado;

    /**
     * Sección (A, B, C, D)
     */
    @Column(name = "seccion", length = 1)
    private String seccion;

    /**
     * Turno (MAÑANA, TARDE)
     */
    @Column(name = "turno", length = 10)
    private String turno;

    /**
     * Fecha en que se realizó la matrícula
     */
    @Column(name = "fecha_matricula")
    private LocalDate fechaMatricula;

    /**
     * Estado de la matrícula: ACTIVA, FINALIZADA, ANULADA
     */
    @Column(name = "estado", length = 20)
    private String estado;

    /**
     * Administrador que aprobó la matrícula
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por", referencedColumnName = "id")
    private Usuario aprobadoPor;

    /**
     * Fecha y hora de aprobación
     */
    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    /**
     * Observaciones o notas sobre la matrícula
     */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    /**
     * Generar código automático antes de persistir
     */
    @PrePersist
    public void generarCodigo() {
        if (codigoMatricula == null || codigoMatricula.isEmpty()) {
            int anio = (anioAcademico != null) ? anioAcademico : java.time.Year.now().getValue();
            this.codigoMatricula = "MAT-" + anio + "-" + System.currentTimeMillis();
        }
        if (fechaMatricula == null) {
            this.fechaMatricula = LocalDate.now();
        }
        if (estado == null) {
            this.estado = "ACTIVA";
        }
    }
}