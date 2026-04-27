package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.universidad.sistema_academico.model.Curso;
import com.universidad.sistema_academico.model.Estudiante;

import java.time.LocalDate;

/**
 * Entidad que representa la tabla "matricula" en el esquema "academico".
 */
@Entity
@Table(name = "matricula", schema = "academico")
@NoArgsConstructor
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_matricula")
    private Long idMatricula;

    /**
     * Relación muchos a uno:
     * muchas matrículas pueden pertenecer a un estudiante.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", referencedColumnName = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    /**
     * Relación muchos a uno:
     * muchas matrículas pueden pertenecer a un curso.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", referencedColumnName = "id_curso", nullable = false)
    private Curso curso;

    @Column(name = "fecha_matricula")
    private LocalDate fechaMatricula;

    @Column(name = "estado", length = 20)
    private String estado;
}