package com.universidad.sistema_academico.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la entidad Matricula.
 * Se utiliza para transferir datos de matrícula entre el cliente y el servidor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaDTO {

    private Long idMatricula;

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long idEstudiante;

    @NotNull(message = "El ID del curso es obligatorio")
    private Long idCurso;

    private LocalDate fechaMatricula;

    @Size(max = 20, message = "El estado no debe exceder 20 caracteres")
    private String estado;
}