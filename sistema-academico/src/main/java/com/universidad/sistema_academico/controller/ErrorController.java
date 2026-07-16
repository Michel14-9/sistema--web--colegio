package com.universidad.sistema_academico.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    // ==================== MANEJO GENERAL DE ERRORES ====================
    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Obtener los atributos de error
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object error = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = 0;
        if (status != null) {
            try {
                statusCode = Integer.parseInt(status.toString());
            } catch (NumberFormatException e) {
                statusCode = 500;
            }
        }

        // Mensajes personalizados en español
        String titulo = "";
        String mensaje = "";
        String descripcion = "";
        String icono = "";

        switch (statusCode) {
            case 404:
                titulo = "Página no encontrada";
                mensaje = "Lo sentimos, la página que buscas no existe.";
                descripcion = "La URL que intentaste acceder no está disponible. Verifica que la dirección sea correcta.";
                icono = "fa-solid fa-map-signs";
                break;
            case 403:
                titulo = "Acceso Denegado";
                mensaje = "No tienes permisos para acceder a esta página.";
                descripcion = "Si crees que esto es un error, contacta con el administrador del sistema.";
                icono = "fa-solid fa-ban";
                break;
            case 500:
                titulo = "Error interno del servidor";
                mensaje = "Algo salió mal en el servidor.";
                descripcion = "Estamos trabajando para solucionar este problema. Por favor, intenta más tarde.";
                icono = "fa-solid fa-server";
                break;
            default:
                titulo = "Error";
                mensaje = "Ha ocurrido un error inesperado.";
                descripcion = "Por favor, intenta nuevamente o contacta con soporte.";
                icono = "fa-solid fa-exclamation-triangle";
                break;
        }

        model.addAttribute("status", statusCode);
        model.addAttribute("error", titulo);
        model.addAttribute("message", mensaje);
        model.addAttribute("descripcion", descripcion);
        model.addAttribute("icono", icono);

        // Limpiar el path para que se vea más limpio
        String pathStr = path != null ? path.toString() : "/";
        if (pathStr.contains("/error")) {
            pathStr = "/" + request.getParameter("path");
        }
        model.addAttribute("path", pathStr);

        // Redirigir según el código de error
        switch (statusCode) {
            case 404:
                return "error/404";
            case 403:
                return "error/403";
            case 500:
                return "error/500";
            default:
                return "error/error-generico";
        }
    }

    // ==================== 404 - PÁGINA NO ENCONTRADA ====================
    @GetMapping("/404")
    public String error404(Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("error", "Página no encontrada");
        model.addAttribute("message", "Lo sentimos, la página que buscas no existe.");
        model.addAttribute("descripcion", "La URL que intentaste acceder no está disponible. Verifica que la dirección sea correcta.");
        model.addAttribute("icono", "fa-solid fa-map-signs");
        model.addAttribute("path", "La página solicitada no existe");
        return "error/404";
    }

    // ==================== 403 - ACCESO DENEGADO ====================
    @GetMapping("/403")
    public String error403(Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("error", "Acceso Denegado");
        model.addAttribute("message", "No tienes permisos para acceder a esta página.");
        model.addAttribute("descripcion", "Si crees que esto es un error, contacta con el administrador del sistema.");
        model.addAttribute("icono", "fa-solid fa-ban");
        model.addAttribute("path", "Acceso restringido");
        return "error/403";
    }

    // ==================== 500 - ERROR INTERNO ====================
    @GetMapping("/500")
    public String error500(Model model) {
        model.addAttribute("status", 500);
        model.addAttribute("error", "Error interno del servidor");
        model.addAttribute("message", "Algo salió mal en el servidor.");
        model.addAttribute("descripcion", "Estamos trabajando para solucionar este problema. Por favor, intenta más tarde.");
        model.addAttribute("icono", "fa-solid fa-server");
        model.addAttribute("path", "Error interno");
        return "error/500";
    }
}