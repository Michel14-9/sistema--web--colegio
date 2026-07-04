package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.model.Matricula;
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

    /**
     * Envía credenciales al apoderado después de aprobar la matrícula
     */
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
                📧 Email institucional: %s
                
                Puede acceder al sistema en: http://localhost:8080/login
                
                Recomendamos cambiar la contraseña en su primer inicio de sesión.
                
                Atentamente,
                I.E. San Carlos
                """,
                username, passwordTemporal, emailEstudiante
        ));

        try {
            mailSender.send(message);
            System.out.println(" Email de credenciales enviado a: " + emailApoderado);
        } catch (Exception e) {
            System.err.println("Error al enviar email de credenciales: " + e.getMessage());
        }
    }

    /**
     * Notifica al administrador que hay una nueva solicitud de matrícula
     */
    public void enviarNotificacionAdmin(String emailAdmin, String estudianteNombres, String estudianteApellidos, String dni) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(emailAdmin);
        message.setSubject("I.E. San Carlos - Nueva Solicitud de Matrícula");
        message.setText(String.format(
                """
                Estimado administrador,
                
                Se ha recibido una nueva solicitud de matrícula.
                
                DATOS DEL ESTUDIANTE:
                📧 Nombres: %s
                📧 Apellidos: %s
                📧 DNI: %s
                
                Ingrese al panel de administración para revisar y aprobar/rechazar la solicitud:
                http://localhost:8080/admin/solicitudes
                
                Atentamente,
                Sistema de Gestión Académica
                I.E. San Carlos
                """,
                estudianteNombres, estudianteApellidos, dni
        ));

        try {
            mailSender.send(message);
            System.out.println(" Notificación enviada al administrador");
        } catch (Exception e) {
            System.err.println(" Error al enviar notificación al administrador: " + e.getMessage());
        }
    }

    /**
     * Envía confirmación al apoderado de que la solicitud fue recibida
     */
    public void enviarConfirmacionSolicitud(String emailApoderado, String estudianteNombres) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(emailApoderado);
        message.setSubject("I.E. San Carlos - Solicitud de Matrícula Recibida");
        message.setText(String.format(
                """
                Estimado apoderado,
                
                Hemos recibido su solicitud de matrícula para %s.
                
                ESTADO: En revisión
                
                Su solicitud está siendo evaluada por nuestro equipo administrativo.
                Recibirá un correo electrónico cuando sea aprobada o rechazada.
                
                El proceso puede tomar de 24 a 48 horas hábiles.
                
                Atentamente,
                I.E. San Carlos
                """,
                estudianteNombres
        ));

        try {
            mailSender.send(message);
            System.out.println(" Confirmación de solicitud enviada a: " + emailApoderado);
        } catch (Exception e) {
            System.err.println(" Error al enviar confirmación: " + e.getMessage());
        }
    }

    /**
     * Envía notificación de rechazo al apoderado
     */
    public void enviarRechazo(String emailApoderado, String motivo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(emailApoderado);
        message.setSubject("I.E. San Carlos - Estado de su solicitud de matrícula");
        message.setText(String.format(
                """
                Estimado apoderado,
                
                Su solicitud de matrícula ha sido REVISADA.
                
                ESTADO: RECHAZADA
                
                Motivo: %s
                
                Para consultas adicionales, comuníquese con la administración del colegio.
                
                Atentamente,
                I.E. San Carlos
                """,
                motivo
        ));

        try {
            mailSender.send(message);
            System.out.println(" Email de rechazo enviado a: " + emailApoderado);
        } catch (Exception e) {
            System.err.println(" Error al enviar email de rechazo: " + e.getMessage());
        }
    }

    /**
     * Envía credenciales al docente cuando es registrado
     */
    public void enviarCredencialesDocente(String emailDocente, String username, String passwordTemporal, String nombres) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(emailDocente);
        message.setSubject("I.E. San Carlos - Credenciales de Acceso para Docente");
        message.setText(String.format(
                """
                Estimado docente %s,
                
                Ha sido registrado exitosamente en el Sistema de Gestión Académica de la I.E. San Carlos.
                
                CREDENCIALES DE ACCESO:
                📧 Usuario (email): %s
                🔑 Contraseña temporal: %s
                
                Puede acceder al sistema en: http://localhost:8080/login
                
                Recomendamos cambiar su contraseña en el primer inicio de sesión.
                
                Atentamente,
                I.E. San Carlos
                """,
                nombres, username, passwordTemporal
        ));

        try {
            mailSender.send(message);
            System.out.println(" Credenciales enviadas al docente: " + emailDocente);
        } catch (Exception e) {
            System.err.println(" Error al enviar email al docente: " + e.getMessage());
        }
    }



    /**
     * Envía credenciales con información completa de la matrícula (versión mejorada)
     */
    public void enviarCredencialesConMatricula(String emailApoderado, String emailEstudiante,
                                               String username, String passwordTemporal,
                                               Matricula matricula) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(emailApoderado);
        message.setSubject("I.E. San Carlos - Matrícula Aprobada y Credenciales de Acceso");

        String gradoStr = obtenerNombreGrado(matricula.getIdGrado());

        message.setText(String.format(
                """
                Estimado apoderado,
                
                ¡Felicidades! Su solicitud de matrícula ha sido APROBADA.
                
                ========================================
                📋 DETALLES DE LA MATRÍCULA
                ========================================
                🎓 Estudiante: %s %s %s
                📧 Email institucional: %s
                🆔 Código de Matrícula: %s
                📅 Año Académico: %d
                📚 Grado: %s
                🔤 Sección: %s
                🕐 Turno: %s
                
                ========================================
                🔐 CREDENCIALES DE ACCESO
                ========================================
                👤 Usuario: %s
                🔑 Contraseña temporal: %s
                
                Puede acceder al sistema en: http://localhost:8080/login
                
                ⚠️ Importante: Recomendamos cambiar la contraseña en el primer inicio de sesión.
                
                Atentamente,
                I.E. San Carlos
                """,
                matricula.getEstudiante().getNombres(),
                matricula.getEstudiante().getApellidoPaterno(),
                matricula.getEstudiante().getApellidoMaterno(),
                emailEstudiante,
                matricula.getCodigoMatricula(),
                matricula.getAnioAcademico(),
                gradoStr,
                matricula.getSeccion() != null ? matricula.getSeccion() : "No asignada",
                matricula.getTurno() != null ? matricula.getTurno() : "No asignado",
                username,
                passwordTemporal
        ));

        try {
            mailSender.send(message);
            System.out.println(" Email de credenciales con matrícula enviado a: " + emailApoderado);
        } catch (Exception e) {
            System.err.println(" Error al enviar email de credenciales: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para obtener nombre del grado
     */
    private String obtenerNombreGrado(Integer idGrado) {
        if (idGrado == null) return "No especificado";
        String[] grados = {
                "1° Primaria", "2° Primaria", "3° Primaria", "4° Primaria", "5° Primaria", "6° Primaria",
                "1° Secundaria", "2° Secundaria", "3° Secundaria", "4° Secundaria", "5° Secundaria"
        };
        return grados[idGrado - 1];
    }
}