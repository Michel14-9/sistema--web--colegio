package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa la tabla "curso" en el esquema "academico".
 * Mapea todos los campos de la tabla de la base de datos PostgreSQL.
 */
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
}
