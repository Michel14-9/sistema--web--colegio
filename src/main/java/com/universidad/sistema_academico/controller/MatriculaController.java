package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.model.SolicitudMatricula;
import com.universidad.sistema_academico.repository.SolicitudMatriculaRepository;
import com.universidad.sistema_academico.service.EmailService;
import com.universidad.sistema_academico.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/api/matricula")
public class MatriculaController {

    @Autowired
    private SolicitudMatriculaRepository solicitudRepository;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/matricula")
    public String mostrarMatricula() {
        return "matricula";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioMatricula() {
        return "matricula";
    }

    @PostMapping("/registrar")
    public String registrarMatricula(
            @RequestParam String dni,
            @RequestParam String nombres,
            @RequestParam String apellidoPaterno,
            @RequestParam String apellidoMaterno,
            @RequestParam LocalDate fechaNacimiento,
            @RequestParam String genero,
            @RequestParam String celular,
            @RequestParam Integer idGrado,
            @RequestParam String seccion,
            @RequestParam String turno,
            @RequestParam String apoderadoDni,
            @RequestParam String apoderadoNombres,
            @RequestParam String apoderadoApellidoPaterno,
            @RequestParam String apoderadoApellidoMaterno,
            @RequestParam String apoderadoTelefono,
            @RequestParam String apoderadoEmail,
            @RequestParam String direccion,
            @RequestParam("voucher") MultipartFile voucher,
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Validar que no exista una solicitud previa con este DNI
            if (solicitudRepository.existsByDniAndEstado(dni, "PENDIENTE")) {
                redirectAttributes.addFlashAttribute("error", "Ya existe una solicitud pendiente con este DNI");
                return "redirect:/api/matricula/nueva";
            }

            // 2. Guardar el voucher
            String voucherPath = voucherService.guardarVoucher(voucher, dni);

            // 3. Crear SOLICITUD de matrícula (NO crear usuario aún)
            SolicitudMatricula solicitud = new SolicitudMatricula();
            solicitud.setDni(dni);
            solicitud.setNombres(nombres.toUpperCase());
            solicitud.setApellidoPaterno(apellidoPaterno.toUpperCase());
            solicitud.setApellidoMaterno(apellidoMaterno.toUpperCase());
            solicitud.setFechaNacimiento(fechaNacimiento);
            solicitud.setGenero(genero);
            solicitud.setCelular(celular);
            solicitud.setIdGrado(idGrado);
            solicitud.setSeccion(seccion);
            solicitud.setTurno(turno);
            solicitud.setApoderadoDni(apoderadoDni);
            solicitud.setApoderadoNombres(apoderadoNombres.toUpperCase());
            solicitud.setApoderadoApellidoPaterno(apoderadoApellidoPaterno.toUpperCase());
            solicitud.setApoderadoApellidoMaterno(apoderadoApellidoMaterno.toUpperCase());
            solicitud.setApoderadoTelefono(apoderadoTelefono);
            solicitud.setApoderadoEmail(apoderadoEmail);
            solicitud.setDireccion(direccion.toUpperCase());
            solicitud.setVoucherPath(voucherPath);
            solicitud.setEstado("PENDIENTE");
            solicitud.setFechaSolicitud(LocalDateTime.now());

            solicitudRepository.save(solicitud);

            // 4. Enviar correo de confirmación al apoderado
            try {
                emailService.enviarConfirmacionSolicitud(
                        apoderadoEmail,
                        nombres + " " + apellidoPaterno + " " + apellidoMaterno
                );
                System.out.println(" Correo de confirmación enviado a: " + apoderadoEmail);
            } catch (Exception e) {
                System.out.println(" Error al enviar correo de confirmación: " + e.getMessage());
            }

            System.out.println("========================================");
            System.out.println(" NUEVA SOLICITUD DE MATRÍCULA");
            System.out.println("DNI: " + dni);
            System.out.println("Estudiante: " + nombres + " " + apellidoPaterno);
            System.out.println("Apoderado email: " + apoderadoEmail);
            System.out.println("Estado: PENDIENTE DE APROBACIÓN");
            System.out.println("========================================");

            redirectAttributes.addFlashAttribute("success",
                    " Solicitud de matrícula registrada correctamente. " +
                            "Un administrador validará su pago y recibirá sus credenciales por correo electrónico.");

            return "redirect:/api/matricula/matricula";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al procesar la solicitud: " + e.getMessage());
            return "redirect:/api/matricula/nueva";
        }
    }
}