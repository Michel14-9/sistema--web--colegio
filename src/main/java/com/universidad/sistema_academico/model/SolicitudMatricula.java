package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla "solicitud_matricula" en el esquema "academico".
 * Almacena las solicitudes de matrícula pendientes de aprobación por el administrador.
 */
@Entity
@Table(name = "solicitud_matricula", schema = "academico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    // ========== DATOS DEL ESTUDIANTE ==========

    @Column(name = "dni", nullable = false, length = 8)
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

    @Column(name = "celular", length = 15)
    private String celular;

    // ========== DATOS ACADÉMICOS ==========

    @Column(name = "id_grado")
    private Integer idGrado;

    @Column(name = "seccion", length = 5)
    private String seccion;

    @Column(name = "turno", length = 10)
    private String turno;

    // ========== DATOS DEL APODERADO (COMPLETOS) ==========

    @Column(name = "apoderado_dni", length = 8, nullable = false)
    private String apoderadoDni;

    @Column(name = "apoderado_nombres", length = 100, nullable = false)
    private String apoderadoNombres;

    @Column(name = "apoderado_apellido_paterno", length = 100, nullable = false)
    private String apoderadoApellidoPaterno;

    @Column(name = "apoderado_apellido_materno", length = 100, nullable = false)
    private String apoderadoApellidoMaterno;

    @Column(name = "apoderado_telefono", length = 15, nullable = false)
    private String apoderadoTelefono;

    @Column(name = "apoderado_email", length = 150, nullable = false)
    private String apoderadoEmail;

    @Column(name = "direccion", columnDefinition = "TEXT", nullable = false)
    private String direccion;

    // ========== VOUCHER ==========

    @Column(name = "voucher_path", length = 255)
    private String voucherPath;

    // ========== ESTADO Y FECHAS ==========

    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "PENDIENTE";

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ========== RELACIONES ==========

    @Column(name = "administrador_id")
    private Long administradorId;

    @OneToOne
    @JoinColumn(name = "id_estudiante", referencedColumnName = "id_estudiante")
    private Estudiante estudiante;

    // Método helper para obtener nombre completo del apoderado
    public String getApoderadoNombreCompleto() {
        return apoderadoNombres + " " + apoderadoApellidoPaterno + " " + apoderadoApellidoMaterno;
    }
}