package com.universidad.sistema_academico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) para la entidad Estudiante.
 * Se utiliza para transferir datos entre el cliente y el servidor
 * sin exponer directamente la entidad JPA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteDTO {

    private Long idEstudiante;

    @NotBlank(message = "El código del estudiante es obligatorio")
    @Size(max = 20, message = "El código no debe exceder 20 caracteres")
    private String codigoEstudiante;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 caracteres")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no deben exceder 100 caracteres")
    private String nombres;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 100, message = "El apellido paterno no debe exceder 100 caracteres")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(max = 100, message = "El apellido materno no debe exceder 100 caracteres")
    private String apellidoMaterno;

    private LocalDate fechaNacimiento;

    @Size(max = 1, message = "El género debe ser un solo carácter (M/F)")
    private String genero;

    @Email(message = "El email institucional no es válido")
    @Size(max = 150, message = "El email no debe exceder 150 caracteres")
    private String emailInstitucional;

    @Size(max = 15, message = "El celular no debe exceder 15 caracteres")
    private String celular;

    private Integer idGrado;

    @Size(max = 5, message = "La sección no debe exceder 5 caracteres")
    private String seccion;

    @Size(max = 10, message = "El turno no debe exceder 10 caracteres")
    private String turno;

    @Size(max = 20, message = "El estado no debe exceder 20 caracteres")
    private String estado;

    private LocalDate fechaIngreso;
}
