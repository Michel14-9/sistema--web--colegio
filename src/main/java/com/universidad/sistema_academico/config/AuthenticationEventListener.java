package com.universidad.sistema_academico.config;

import com.universidad.sistema_academico.service.ActividadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    @Autowired
    private ActividadService actividadService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        actividadService.registrarActividad(username, "LOGIN", "Sistema", "Inicio de sesión exitoso");
    }
}