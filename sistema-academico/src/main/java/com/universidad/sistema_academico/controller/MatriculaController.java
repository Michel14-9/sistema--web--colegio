package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.dto.DescuentoDTO;
import com.universidad.sistema_academico.model.SolicitudMatricula;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import com.universidad.sistema_academico.repository.SolicitudMatriculaRepository;
import com.universidad.sistema_academico.service.DescuentoService;
import com.universidad.sistema_academico.service.EmailService;
import com.universidad.sistema_academico.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @Autowired
    private DescuentoService descuentoService;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @GetMapping("/matricula")
    public String mostrarMatricula() {
        System.out.println("🔵🔵🔵 MOSTRANDO FORMULARIO DE MATRÍCULA 🔵🔵🔵");
        return "matricula";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioMatricula() {
        System.out.println("🔵🔵🔵 MOSTRANDO NUEVA MATRÍCULA 🔵🔵🔵");
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
            if (solicitudRepository.existsByDniAndEstado(dni, "PENDIENTE")) {
                redirectAttributes.addFlashAttribute("error", "Ya existe una solicitud pendiente con este DNI");
                return "redirect:/api/matricula/nueva";
            }

            String voucherPath = voucherService.guardarVoucher(voucher, dni);

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

            try {
                emailService.enviarConfirmacionSolicitud(
                        apoderadoEmail,
                        nombres + " " + apellidoPaterno + " " + apellidoMaterno
                );
                System.out.println("Correo de confirmación enviado a: " + apoderadoEmail);
            } catch (Exception e) {
                System.out.println("Error al enviar correo: " + e.getMessage());
            }

            redirectAttributes.addFlashAttribute("success",
                    "Solicitud de matrícula registrada correctamente. Un administrador validará su pago.");

            return "redirect:/api/matricula/matricula";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al procesar la solicitud: " + e.getMessage());
            return "redirect:/api/matricula/nueva";
        }
    }

    // ==================== FORMULARIO CON DESCUENTO ====================
    @GetMapping("/formulario")
    public String mostrarFormulario(Model model,
                                    @RequestParam(required = false) String apellidoPaterno) {
        System.out.println("🟡🟡🟡 MOSTRANDO FORMULARIO CON DESCUENTO 🟡🟡🟡");
        if (apellidoPaterno != null && !apellidoPaterno.isEmpty()) {
            System.out.println("📝 Apellido recibido en formulario: " + apellidoPaterno);
            DescuentoDTO descuento = descuentoService.calcularDescuento(apellidoPaterno);
            model.addAttribute("descuento", descuento);
            model.addAttribute("montoBase", descuentoService.getMontoBase());
            System.out.println("📊 Descuento aplicado: " + descuento.getMensaje());
        }
        return "matricula";
    }
}