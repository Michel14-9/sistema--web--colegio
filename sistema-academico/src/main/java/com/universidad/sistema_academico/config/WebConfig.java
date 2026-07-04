package com.universidad.sistema_academico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web general del proyecto.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registra vistas simples sin necesidad de métodos extras en controladores.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/login-view", "/");
    }

    /**
     * Configuración explícita de recursos estáticos.
     * Aunque Spring Boot ya lo hace automáticamente, esto deja la estructura más clara.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}
