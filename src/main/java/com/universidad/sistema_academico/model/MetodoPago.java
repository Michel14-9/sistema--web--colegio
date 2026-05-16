package com.universidad.sistema_academico.model;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia bancaria"),
    TARJETA("Tarjeta de débito/crédito"),
    YAPE("Yape"),
    PLIN("Plin");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}