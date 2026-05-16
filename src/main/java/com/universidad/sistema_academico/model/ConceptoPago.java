package com.universidad.sistema_academico.model;

public enum ConceptoPago {
    MATRICULA("Matrícula"),
    PENSION("Pensión mensual"),
    MORA("Mora por retraso"),
    OTRO("Otro concepto");

    private final String descripcion;

    ConceptoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}