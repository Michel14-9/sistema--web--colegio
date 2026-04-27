package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa la tabla "docente" en el esquema "academico".
 */
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
}