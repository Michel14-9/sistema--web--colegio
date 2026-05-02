package com.universidad.sistema_academico.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // ✅ Maneja GET /login - muestra el formulario
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    // ✅ Redirige la raíz a /login
    @GetMapping("/")
    public String redirigirALogin() {
        return "redirect:/login";
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