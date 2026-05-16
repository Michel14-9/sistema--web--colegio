package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.PagoDTO;
import com.universidad.sistema_academico.model.ConceptoPago;
import com.universidad.sistema_academico.model.MetodoPago;
import com.universidad.sistema_academico.service.MatriculaService;
import com.universidad.sistema_academico.service.PagoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final MatriculaService matriculaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pagos", pagoService.listarTodos());
        model.addAttribute("titulo", "Gestión de Pagos");
        return "pagos/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model,
                                  @RequestParam(required = false) Long matriculaId) {

        PagoDTO dto = PagoDTO.builder()
                .matriculaId(matriculaId)
                .cantidad(1)
                .monto(BigDecimal.ZERO)
                .descuento(BigDecimal.ZERO)
                .build();

        cargarCombos(model, dto);
        return "pagos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("pago") PagoDTO dto,
                          BindingResult result,
                          Model model,
                          RedirectAttributes flash) {

        if (dto.getDescuento() == null) {
            dto.setDescuento(BigDecimal.ZERO);
        }

        if (result.hasErrors()) {
            cargarCombos(model, dto);
            return "pagos/form";
        }

        PagoDTO guardado = pagoService.registrar(dto);

        flash.addFlashAttribute("exito",
                "Pago registrado correctamente. Recibo: " + guardado.getNumeroRecibo());

        return "redirect:/pagos/recibo/" + guardado.getId();
    }

    @GetMapping("/recibo/{id}")
    public String verRecibo(@PathVariable Long id, Model model) {

        PagoDTO pago = pagoService.buscarPorId(id);
        model.addAttribute("pago", pago);

        return "pagos/recibo";
    }

    @PostMapping("/anular/{id}")
    public String anular(@PathVariable Long id,
                         RedirectAttributes flash) {

        pagoService.anular(id);

        flash.addFlashAttribute("aviso", "Pago anulado correctamente");
        return "redirect:/pagos";
    }

    private void cargarCombos(Model model, PagoDTO dto) {

        model.addAttribute("pago", dto);

        model.addAttribute("conceptos", ConceptoPago.values());
        model.addAttribute("metodos", MetodoPago.values());

        model.addAttribute("matriculas", matriculaService.listarTodos());

        model.addAttribute("titulo",
                dto.getId() == null ? "Registrar Pago" : "Editar Pago");
    }
}