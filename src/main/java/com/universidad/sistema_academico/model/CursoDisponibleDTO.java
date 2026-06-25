package com.universidad.sistema_academico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoDisponibleDTO {
    private Long idCurso;
    private String codigoCurso;
    private String nombreCurso;
    private String descripcion;
    private Integer horasSemanales;
    private Integer idGrado;
    private Long idDocente;
    private String area;
    private String estado;
    private String seccion;
    private String turno;
    private Integer capacidadMaxima;
    private Integer alumnosActuales;
    private String horario;
    private String docenteNombre;
    private boolean inscrito;
    private int cuposDisponibles;
    private double porcentajeOcupacion;
    private boolean hayCupo;
}