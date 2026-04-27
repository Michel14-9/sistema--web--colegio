package com.universidad.sistema_academico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración general de beans de la aplicación.
 */
@Configuration
public class AppConfig {

    /**
     * Bean para encriptar contraseñas.
     * Se utilizará cuando implementemos autenticación real.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}