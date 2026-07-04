package com.universidad.sistema_academico.dto;

import lombok.Data;

@Data
public class NotaDTO {
    private Long idNota;
    private Long idEstudiante;
    private String nombreCompleto;
    private Long idCurso;
    private String nombreCurso;
    private String codigoCurso;
    private Double bimestre1;
    private Double bimestre2;
    private Double bimestre3;
    private Double bimestre4;
    private Double promedioFinal;
    private String estado; // APROBADO, RECUPERACIÓN, DESAPROBADO, SIN NOTAS
}