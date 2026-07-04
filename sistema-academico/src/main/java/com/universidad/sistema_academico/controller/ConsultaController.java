package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.service.ConsultaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/consultar")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    @PostMapping
    public Map<String, String> consultar(@RequestBody Map<String, String> request) {
        String pregunta = request.get("pregunta");
        System.out.println("🔍 MONOLITO - Recibida pregunta: " + pregunta);

        String respuesta = consultaService.buscarRespuesta(pregunta);

        System.out.println("✅ MONOLITO - Respuesta generada: " + respuesta);

        Map<String, String> response = new HashMap<>();
        response.put("respuesta", respuesta);
        return response;
    }
}