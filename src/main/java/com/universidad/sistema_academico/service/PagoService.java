package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.dto.PagoDTO;
import com.universidad.sistema_academico.model.*;
import com.universidad.sistema_academico.repository.MatriculaRepository;
import com.universidad.sistema_academico.repository.PagoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final MatriculaRepository matriculaRepository;

    public List<PagoDTO> listarTodos() {
        return pagoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PagoDTO> listarPorMatricula(Long matriculaId) {
        return pagoRepository.findByMatricula_Id(matriculaId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PagoDTO registrar(PagoDTO dto) {

        Matricula matricula = matriculaRepository.findById(dto.getMatriculaId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Matrícula no encontrada: " + dto.getMatriculaId()));

        BigDecimal monto = dto.getMonto() != null ? dto.getMonto() : BigDecimal.ZERO;
        BigDecimal descuento = dto.getDescuento() != null ? dto.getDescuento() : BigDecimal.ZERO;

        BigDecimal subtotal = monto.multiply(BigDecimal.valueOf(dto.getCantidad()));
        BigDecimal total = subtotal.subtract(descuento);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total no puede ser negativo");
        }

        Pago pago = Pago.builder()
                .numeroRecibo(generarNumeroRecibo())
                .matricula(matricula)
                .concepto(dto.getConcepto())
                .descripcion(dto.getDescripcion())
                .cantidad(dto.getCantidad())
                .monto(monto)
                .descuento(descuento)
                .total(total)
                .metodoPago(dto.getMetodoPago())
                .numeroOperacion(dto.getNumeroOperacion())
                .estado(EstadoPago.PAGADO)
                .fechaPago(LocalDateTime.now())
                .build();

        pago = pagoRepository.save(pago);

        return toDTO(pago);
    }

    public PagoDTO buscarPorId(Long id) {
        return pagoRepository.findWithDetailsById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado: " + id));
    }

    @Transactional
    public void anular(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado: " + id));

        if (pago.getEstado() == EstadoPago.ANULADO) {
            throw new IllegalStateException("El pago ya está anulado");
        }

        pago.setEstado(EstadoPago.ANULADO);
        pagoRepository.save(pago);
    }

    private String generarNumeroRecibo() {
        String anio = String.valueOf(LocalDate.now().getYear());
        long correlativo = pagoRepository.countByNumeroReciboStartingWith("REC-" + anio) + 1;
        return String.format("REC-%s-%04d", anio, correlativo);
    }

    private PagoDTO toDTO(Pago p) {

        Estudiante est = p.getMatricula().getEstudiante();

        BigDecimal subtotal = p.getMonto()
                .multiply(BigDecimal.valueOf(p.getCantidad()));

        String nombreCompleto = est.getApellidoPaterno() + " "
                + est.getApellidoMaterno() + ", "
                + est.getNombres();

        String gradoSeccion = est.getIdGrado()
                + " – Sección " + est.getSeccion();

        return PagoDTO.builder()
                .id(p.getId())
                .numeroRecibo(p.getNumeroRecibo())
                .matriculaId(p.getMatricula().getIdMatricula())

                .nombreEstudiante(nombreCompleto)
                .codigoEstudiante(est.getCodigoEstudiante())
                .gradoSeccion(gradoSeccion)
                .nombreApoderado(null) // ⚠️ solo si no existe en entidad
                .institucion("I.E. San Martín de Porres")

                .concepto(p.getConcepto())
                .descripcion(p.getDescripcion())
                .cantidad(p.getCantidad())
                .monto(p.getMonto())
                .descuento(p.getDescuento())
                .subtotal(subtotal)
                .total(p.getTotal())
                .metodoPago(p.getMetodoPago())
                .numeroOperacion(p.getNumeroOperacion())
                .estado(p.getEstado())
                .fechaPago(p.getFechaPago())
                .build();
    }
}