package com.universidad.sistema_academico.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MantenimientoController {

    @Value("${sistema.modo-mantenimiento:false}")
    private boolean modoMantenimiento;

    @GetMapping("/mantenimiento")
    public String mantenimiento(Model model) {
        model.addAttribute("modoMantenimiento", modoMantenimiento);
        return "mantenimiento";
    }

    @GetMapping("/api/verificar-mantenimiento")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarMantenimiento() {
        Map<String, Object> response = new HashMap<>();
        response.put("modoMantenimiento", modoMantenimiento);
        return ResponseEntity.ok(response);
    }
}