package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.DescuentoDTO;
import com.universidad.sistema_academico.service.DescuentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matricula")
public class MatriculaRestController {

    @Autowired
    private DescuentoService descuentoService;

    @GetMapping("/verificar-descuento")
    public DescuentoDTO verificarDescuento(@RequestParam String apellidoPaterno) {
        System.out.println("========================================");
        System.out.println("🔴🔴🔴 ENDPOINT VERIFICAR DESCUENTO EJECUTADO 🔴🔴🔴");
        System.out.println("📝 Apellido recibido: " + apellidoPaterno);
        System.out.println("========================================");

        DescuentoDTO descuento = descuentoService.calcularDescuento(apellidoPaterno);

        System.out.println("📊 Resultado:");
        System.out.println("   - Aplica descuento: " + descuento.isAplicaDescuento());
        System.out.println("   - Número de hermanos: " + descuento.getNumeroHermanos());
        System.out.println("   - Porcentaje: " + descuento.getPorcentajeDescuento() + "%");
        System.out.println("   - Monto final: S/ " + descuento.getMontoFinal());
        System.out.println("========================================");

        return descuento;
    }
}