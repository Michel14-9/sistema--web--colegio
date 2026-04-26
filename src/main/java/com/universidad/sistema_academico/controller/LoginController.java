package com.universidad.sistema_academico.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String rol) {

        // Aquí validar credenciales con la base de datos
        // Por ahora, redirige según el rol seleccionado

        if ("admin".equals(rol)) {
            return "redirect:/admin/dashboard";
        } else if ("docente".equals(rol)) {
            return "redirect:/docente/dashboard";
        } else if ("estudiante".equals(rol)) {
            return "redirect:/estudiante/dashboard";
        }

        return "redirect:/?error=true";
    }

    // Vistas del Administrador
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/estudiantes")
    public String adminEstudiantes() {
        return "admin/estudiantes";
    }

    @GetMapping("/admin/docentes")
    public String adminDocentes() {
        return "admin/docentes";
    }

    @GetMapping("/admin/cursos")
    public String adminCursos() {
        return "admin/cursos";
    }

    @GetMapping("/admin/matriculas")
    public String adminMatriculas() {
        return "admin/matriculas";
    }

    // Vistas del Docente
    @GetMapping("/docente/dashboard")
    public String docenteDashboard() {
        return "docente/dashboard";
    }

    @GetMapping("/docente/mis-cursos")
    public String docenteMisCursos() {
        return "docente/mis-cursos";
    }

    // Vistas del Estudiante
    @GetMapping("/estudiante/dashboard")
    public String estudianteDashboard() {
        return "estudiante/dashboard";
    }

    @GetMapping("/estudiante/cursos")
    public String estudianteCursos() {
        return "estudiante/cursos-disponibles";
    }

    @GetMapping("/estudiante/matricula")
    public String estudianteMatricula() {
        return "estudiante/mi-matricula";
    }
}