// Nota.java
package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notas", schema = "academico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Long idNota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @Column(name = "bimestre", nullable = false)
    private Integer bimestre;

    @Column(name = "nota", nullable = false, precision = 4, scale = 2)
    private BigDecimal nota;  // Cambiar de Double a BigDecimal

    @Column(name = "periodo_academico", nullable = false, length = 20)
    private String periodoAcademico;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "observacion")
    private String observacion;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}