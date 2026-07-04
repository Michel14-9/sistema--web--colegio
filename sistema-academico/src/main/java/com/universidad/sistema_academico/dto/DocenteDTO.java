package com.universidad.sistema_academico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la entidad Docente.
 * Se utiliza para transferir datos entre el cliente y el servidor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocenteDTO {

    private Long idDocente;

    @NotBlank(message = "El código del docente es obligatorio")
    @Size(max = 20, message = "El código no debe exceder 20 caracteres")
    private String codigoDocente;

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

    @NotBlank(message = "La especialidad es obligatoria")
    @Size(max = 100, message = "La especialidad no debe exceder 100 caracteres")
    private String especialidad;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no debe exceder 150 caracteres")
    private String email;

    @Size(max = 15, message = "El celular no debe exceder 15 caracteres")
    private String celular;

    @Size(max = 20, message = "El estado no debe exceder 20 caracteres")
    private String estado;
}