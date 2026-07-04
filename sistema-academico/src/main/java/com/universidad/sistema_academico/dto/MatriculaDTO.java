package com.universidad.sistema_academico.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para la entidad Matricula.
 * Representa una MATRÍCULA ANUAL (el estudiante se matricula a un grado, no a cursos individuales)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatriculaDTO {

    private Long idMatricula;

    private String codigoMatricula;

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long idEstudiante;

    private String nombreEstudiante;  // Para mostrar en listados
    private String dniEstudiante;      // Para mostrar en listados

    @NotNull(message = "El año académico es obligatorio")
    private Integer anioAcademico;

    @NotNull(message = "El grado es obligatorio")
    private Integer idGrado;

    private String nombreGrado;  // Para mostrar (ej: "1° Primaria")

    @Size(max = 1, message = "La sección debe ser A, B, C o D")
    private String seccion;

    @Size(max = 10, message = "El turno debe ser MAÑANA o TARDE")
    private String turno;

    private LocalDate fechaMatricula;

    @Size(max = 20, message = "El estado no debe exceder 20 caracteres")
    private String estado;  // ACTIVA, FINALIZADA, ANULADA

    private Long aprobadoPor;
    private String nombreAdministrador;
    private LocalDateTime fechaAprobacion;
    private String observaciones;

    /**
     * Constructor para crear una nueva matrícula desde solicitud
     */
    public MatriculaDTO(Long idEstudiante, Integer anioAcademico, Integer idGrado,
                        String seccion, String turno) {
        this.idEstudiante = idEstudiante;
        this.anioAcademico = anioAcademico;
        this.idGrado = idGrado;
        this.seccion = seccion;
        this.turno = turno;
        this.fechaMatricula = LocalDate.now();
        this.estado = "ACTIVA";
    }

    /**
     * Método helper para obtener nombre del grado
     */
    public String getNombreGradoFormateado() {
        if (idGrado == null) return "No especificado";
        String[] grados = {
                "1° Primaria", "2° Primaria", "3° Primaria", "4° Primaria", "5° Primaria", "6° Primaria",
                "1° Secundaria", "2° Secundaria", "3° Secundaria", "4° Secundaria", "5° Secundaria"
        };
        return grados[idGrado - 1];
    }

    /**
     * Método helper para obtener grado + sección
     */
    public String getGradoYSeccion() {
        String grado = getNombreGradoFormateado();
        String seccionStr = (seccion != null && !seccion.isEmpty()) ? seccion : "";
        return grado + (seccionStr.isEmpty() ? "" : " \"" + seccionStr + "\"");
    }
}