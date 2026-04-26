package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad que representa la tabla "estudiante" en el esquema "academico".
 * Mapea todos los campos de la tabla de la base de datos PostgreSQL.
 */
@Entity
@Table(name = "estudiante", schema = "academico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Long idEstudiante;

    @Column(name = "codigo_estudiante", unique = true, nullable = false, length = 20)
    private String codigoEstudiante;

    @Column(name = "dni", unique = true, nullable = false, length = 8)
    private String dni;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 1)
    private String genero;

    @Column(name = "email_institucional", length = 150)
    private String emailInstitucional;

    @Column(name = "celular", length = 15)
    private String celular;

    @Column(name = "id_grado")
    private Integer idGrado;

    @Column(name = "seccion", length = 5)
    private String seccion;

    @Column(name = "turno", length = 10)
    private String turno;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;
}
