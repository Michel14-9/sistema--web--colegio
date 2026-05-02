package com.universidad.sistema_academico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "curso", schema = "academico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Long idCurso;

    @Column(name = "codigo_curso", unique = true, nullable = false, length = 20)
    private String codigoCurso;

    @Column(name = "nombre_curso", nullable = false, length = 150)
    private String nombreCurso;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "horas_semanales")
    private Integer horasSemanales;

    @Column(name = "id_grado")
    private Integer idGrado;

    @Column(name = "id_docente")
    private Long idDocente;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "estado", length = 20)
    private String estado;


    @PrePersist
    public void generarCodigoAutomatico() {
        if (codigoCurso == null || codigoCurso.isEmpty()) {
            // Generar código basado en el nombre + timestamp
            String siglas = obtenerSiglas(nombreCurso);
            String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
            this.codigoCurso = siglas + "-" + timestamp;
        }
    }

    /**
     * Método auxiliar para obtener las siglas del nombre del curso.
     * Ejemplo: "Matemáticas Avanzadas" -> "MA"
     */
    private String obtenerSiglas(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return "CUR";
        }

        String[] palabras = nombre.trim().split("\\s+");
        StringBuilder siglas = new StringBuilder();

        for (String palabra : palabras) {
            if (palabra.length() > 0) {
                siglas.append(Character.toUpperCase(palabra.charAt(0)));
            }
        }

        // Si las siglas son muy cortas, usar primeras 3 letras del nombre
        if (siglas.length() < 2 && nombre.length() >= 3) {
            return nombre.substring(0, 3).toUpperCase();
        }

        return siglas.length() >= 2 ? siglas.toString() : "CUR";
    }
}