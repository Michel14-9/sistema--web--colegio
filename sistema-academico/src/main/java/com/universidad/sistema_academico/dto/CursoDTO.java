package com.universidad.sistema_academico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * DTO (Data Transfer Object) para la entidad Curso.
 * Se utiliza para transferir datos entre el cliente y el servidor
 * sin exponer directamente la entidad JPA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoDTO {

    private Long idCurso;

    @NotBlank(message = "El código del curso es obligatorio")
    @Size(max = 20, message = "El código no debe exceder 20 caracteres")
    private String codigoCurso;

    @NotBlank(message = "El nombre del curso es obligatorio")
    @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
    private String nombreCurso;

    private String descripcion;

    @Min(value = 1, message = "Las horas semanales deben ser al menos 1")
    @Max(value = 40, message = "Las horas semanales no deben exceder 40")
    private Integer horasSemanales;

    private Integer idGrado;

    private Long idDocente;

    @Size(max = 100, message = "El área no debe exceder 100 caracteres")
    private String area;

    @Size(max = 20, message = "El estado no debe exceder 20 caracteres")
    private String estado;
}
