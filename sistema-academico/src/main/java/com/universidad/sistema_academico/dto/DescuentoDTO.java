package com.universidad.sistema_academico.dto;

import java.math.BigDecimal;

public class DescuentoDTO {
    private int numeroHermanos;
    private BigDecimal porcentajeDescuento;
    private BigDecimal montoDescuento;
    private BigDecimal montoFinal;
    private boolean aplicaDescuento;
    private String mensaje;

    // Constructor vacío
    public DescuentoDTO() {}

    // Constructor con parámetros
    public DescuentoDTO(int numeroHermanos, BigDecimal porcentajeDescuento,
                        BigDecimal montoDescuento, BigDecimal montoFinal,
                        boolean aplicaDescuento, String mensaje) {
        this.numeroHermanos = numeroHermanos;
        this.porcentajeDescuento = porcentajeDescuento;
        this.montoDescuento = montoDescuento;
        this.montoFinal = montoFinal;
        this.aplicaDescuento = aplicaDescuento;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public int getNumeroHermanos() { return numeroHermanos; }
    public void setNumeroHermanos(int numeroHermanos) { this.numeroHermanos = numeroHermanos; }

    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
    public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }

    public BigDecimal getMontoDescuento() { return montoDescuento; }
    public void setMontoDescuento(BigDecimal montoDescuento) { this.montoDescuento = montoDescuento; }

    public BigDecimal getMontoFinal() { return montoFinal; }
    public void setMontoFinal(BigDecimal montoFinal) { this.montoFinal = montoFinal; }

    public boolean isAplicaDescuento() { return aplicaDescuento; }
    public void setAplicaDescuento(boolean aplicaDescuento) { this.aplicaDescuento = aplicaDescuento; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}