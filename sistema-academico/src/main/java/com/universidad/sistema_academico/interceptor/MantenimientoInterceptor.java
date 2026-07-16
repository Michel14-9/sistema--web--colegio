package com.universidad.sistema_academico.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.InputStream;
import java.util.Properties;

@Component
public class MantenimientoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("🚀🚀🚀 INTERCEPTOR EJECUTÁNDOSE 🚀🚀🚀");

        boolean modoMantenimiento = verificarModoMantenimiento();

        System.out.println("🔍 Modo mantenimiento: " + modoMantenimiento);

        if (modoMantenimiento) {
            String uri = request.getRequestURI();
            System.out.println("🔍 URI solicitada: " + uri);

            if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/img/") ||
                    uri.startsWith("/webjars/") || uri.startsWith("/favicon.ico")) {
                return true;
            }

            if (uri.equals("/mantenimiento") || uri.equals("/login") || uri.equals("/logout") ||
                    uri.equals("/api/verificar-mantenimiento")) {
                System.out.println("✅ Ruta permitida: " + uri);
                return true;
            }

            System.out.println("🔴 Redirigiendo a mantenimiento: " + uri);
            response.sendRedirect("/mantenimiento");
            return false;
        }

        System.out.println("✅ Sistema operativo normal");
        return true;
    }

    private boolean verificarModoMantenimiento() {
        try {
            ClassPathResource resource = new ClassPathResource("config.properties");
            System.out.println("📄 Buscando archivo en classpath: config.properties");

            try (InputStream input = resource.getInputStream()) {
                Properties prop = new Properties();
                prop.load(input);
                String valor = prop.getProperty("sistema.modo-mantenimiento", "false");
                // 🔥 IMPORTANTE: Limpiar espacios en blanco
                valor = valor.trim();
                System.out.println("📄 Valor leído: sistema.modo-mantenimiento='" + valor + "'");
                boolean resultado = Boolean.parseBoolean(valor);
                System.out.println("📄 Parseado a: " + resultado);
                return resultado;
            }
        } catch (Exception e) {
            System.out.println("❌ Error al leer config.properties: " + e.getMessage());
            return false;
        }
    }
}