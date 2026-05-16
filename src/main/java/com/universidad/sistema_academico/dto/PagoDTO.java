package com.universidad.sistema_academico.dto;

import com.universidad.sistema_academico.model.ConceptoPago;
import com.universidad.sistema_academico.model.EstadoPago;
import com.universidad.sistema_academico.model.MetodoPago;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDTO {

    private Long id;
    private String numeroRecibo;

    @NotNull(message = "Debe seleccionar una matrícula")
    private Long matriculaId;

    private String nombreEstudiante;
    private String codigoEstudiante;
    private String gradoSeccion;

    private String nombreApoderado;

    private String institucion;

    @NotNull(message = "El concepto es obligatorio")
    private ConceptoPago concepto;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "Máximo 200 caracteres")
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer cantidad;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    private BigDecimal descuento;

    private BigDecimal subtotal;
    private BigDecimal total;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    private String numeroOperacion;

    private EstadoPago estado;
    private LocalDateTime fechaPago;
}