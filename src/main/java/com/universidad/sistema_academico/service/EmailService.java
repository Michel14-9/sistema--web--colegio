package com.universidad.sistema_academico.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void enviarCredenciales(String emailApoderado, String emailEstudiante, String username, String passwordTemporal) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(emailApoderado);
        message.setSubject("I.E. San Carlos - Credenciales de Acceso para su hijo(a)");
        message.setText(String.format(
                """
                Estimado apoderado,
                
                Su hijo(a) ha sido matriculado exitosamente en la I.E. San Carlos.
                
                CREDENCIALES DE ACCESO AL SISTEMA:
                📧 Usuario: %s
                🔑 Contraseña temporal: %s
                
                Puede acceder al sistema en: http://localhost:8080/login
                
                Recomendamos cambiar la contraseña en su primer inicio de sesión.
                
                Atentamente,
                I.E. San Carlos
                """,
                username, passwordTemporal
        ));

        try {
            mailSender.send(message);
            System.out.println("Email enviado a: " + emailApoderado);
        } catch (Exception e) {
            System.err.println("Error al enviar email: " + e.getMessage());
        }
    }
}