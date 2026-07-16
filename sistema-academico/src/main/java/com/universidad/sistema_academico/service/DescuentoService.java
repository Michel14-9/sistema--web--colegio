package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.dto.DescuentoDTO;
import com.universidad.sistema_academico.model.Estudiante;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DescuentoService {

    private static final BigDecimal MONTO_BASE = new BigDecimal("200.00");
    private static final BigDecimal DESCUENTO_X_HERMANO = new BigDecimal("0.10");
    private static final BigDecimal DESCUENTO_MAXIMO = new BigDecimal("0.30");

    @Autowired
    private EstudianteRepository estudianteRepository;

    public DescuentoDTO calcularDescuento(Estudiante estudiante) {
        return calcularDescuento(estudiante.getApellidoPaterno());
    }

    public DescuentoDTO calcularDescuento(String apellidoPaterno) {
        System.out.println("🟣🟣🟣 DESCUENTO SERVICE EJECUTADO 🟣🟣🟣");
        System.out.println("📝 Apellido paterno recibido: '" + apellidoPaterno + "'");

        if (apellidoPaterno == null || apellidoPaterno.trim().isEmpty()) {
            System.out.println("❌ Apellido vacío o nulo");
            return new DescuentoDTO(0, BigDecimal.ZERO, BigDecimal.ZERO, MONTO_BASE, false, "No se puede verificar hermanos sin apellido");
        }

        // Limpiar y convertir a mayúsculas
        String apellidoLimpio = apellidoPaterno.trim().toUpperCase();
        System.out.println("📝 Apellido limpio: '" + apellidoLimpio + "'");

        // Buscar estudiantes con el mismo apellido paterno
        System.out.println("🔍 Buscando estudiantes con apellido: '" + apellidoLimpio + "'");
        List<Estudiante> hermanos = estudianteRepository.findByApellidoPaterno(apellidoLimpio);

        System.out.println("📊 Estudiantes encontrados: " + hermanos.size());
        if (!hermanos.isEmpty()) {
            for (Estudiante e : hermanos) {
                System.out.println("   - " + e.getNombres() + " " + e.getApellidoPaterno() + " (ID: " + e.getIdEstudiante() + ")");
            }
        }

        int numeroHermanos = hermanos.size();
        System.out.println("👨‍👩‍👧‍👦 Número de hermanos: " + numeroHermanos);

        BigDecimal porcentajeDescuento = calcularPorcentajeDescuento(numeroHermanos);
        System.out.println("📊 Porcentaje de descuento: " + porcentajeDescuento.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP) + "%");

        BigDecimal montoDescuento = MONTO_BASE.multiply(porcentajeDescuento).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoFinal = MONTO_BASE.subtract(montoDescuento).setScale(2, RoundingMode.HALF_UP);

        System.out.println("💰 Monto base: S/ " + MONTO_BASE);
        System.out.println("💰 Descuento: S/ " + montoDescuento);
        System.out.println("💰 Monto final: S/ " + montoFinal);

        boolean aplicaDescuento = porcentajeDescuento.compareTo(BigDecimal.ZERO) > 0;
        String mensaje = generarMensaje(numeroHermanos, porcentajeDescuento);

        System.out.println("📝 Mensaje: " + mensaje);
        System.out.println("🟣🟣🟣 FIN DESCUENTO SERVICE 🟣🟣🟣");

        return new DescuentoDTO(
                numeroHermanos,
                porcentajeDescuento.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP),
                montoDescuento,
                montoFinal,
                aplicaDescuento,
                mensaje
        );
    }

    private BigDecimal calcularPorcentajeDescuento(int numeroHermanos) {
        if (numeroHermanos <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal descuento = new BigDecimal(numeroHermanos).multiply(DESCUENTO_X_HERMANO);

        if (descuento.compareTo(DESCUENTO_MAXIMO) > 0) {
            return DESCUENTO_MAXIMO;
        }

        return descuento;
    }

    private String generarMensaje(int numeroHermanos, BigDecimal porcentajeDescuento) {
        if (numeroHermanos <= 0) {
            return "No se detectaron hermanos en la institución.";
        }

        if (porcentajeDescuento.compareTo(new BigDecimal("30")) >= 0) {
            return "✅ " + numeroHermanos + " hermanos en la institución. ¡Descuento máximo del 30% aplicado!";
        }

        return "✅ " + numeroHermanos + " hermano(s) en la institución. Descuento del " +
                porcentajeDescuento.setScale(0, RoundingMode.HALF_UP) + "% aplicado.";
    }

    public BigDecimal getMontoBase() {
        return MONTO_BASE;
    }
}