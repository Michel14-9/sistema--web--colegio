package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.model.Estudiante;
import com.universidad.sistema_academico.model.Usuario;
import com.universidad.sistema_academico.service.EmailService;
import com.universidad.sistema_academico.service.EstudianteService;
import com.universidad.sistema_academico.service.UsuarioService;
import com.universidad.sistema_academico.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/api/matricula")
public class MatriculaController {

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VoucherService voucherService;


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
            @RequestParam String apoderadoNombres,
            @RequestParam String apoderadoDni,
            @RequestParam String apoderadoTelefono,
            @RequestParam String apoderadoEmail,
            @RequestParam String direccion,
            @RequestParam("voucher") MultipartFile voucher,
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Validar que el DNI no esté registrado
            if (estudianteService.existsByDni(dni)) {
                redirectAttributes.addFlashAttribute("error", "El DNI ya está registrado");
                return "redirect:/api/matricula/nueva";
            }

            // 2. Validar y guardar el voucher
            String voucherPath = voucherService.guardarVoucher(voucher, dni);

            // 3. Validar voucher
            boolean voucherValido = voucherService.validarVoucher(voucherPath);
            if (!voucherValido) {
                redirectAttributes.addFlashAttribute("error", "El voucher no es válido o el monto es incorrecto");
                return "redirect:/api/matricula/nueva";
            }

            // 4. Generar email institucional
            String emailInstitucional = generarEmail(nombres, apellidoPaterno, apellidoMaterno);

            // 5. Generar username y contraseña temporal
            String username = generarUsername(nombres, apellidoPaterno, apellidoMaterno);
            String passwordTemporal = generarPasswordTemporal();

            // 6. Crear usuario
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setEmail(emailInstitucional);
            usuario.setPassword(passwordEncoder.encode(passwordTemporal));
            usuario.setRol("ESTUDIANTE");
            usuario.setActivo(true);
            usuarioService.save(usuario);

            // 7. Crear estudiante
            Estudiante estudiante = new Estudiante();
            estudiante.setUsuario(usuario);
            estudiante.setCodigoEstudiante(generarCodigoEstudiante());
            estudiante.setDni(dni);
            estudiante.setNombres(nombres);
            estudiante.setApellidoPaterno(apellidoPaterno);
            estudiante.setApellidoMaterno(apellidoMaterno);
            estudiante.setFechaNacimiento(fechaNacimiento);
            estudiante.setGenero(genero);
            estudiante.setEmailInstitucional(emailInstitucional);
            estudiante.setCelular(celular);
            estudiante.setIdGrado(idGrado);
            estudiante.setSeccion(seccion);
            estudiante.setTurno(turno);
            estudiante.setEstado("ACTIVO");
            estudiante.setFechaIngreso(LocalDate.now());

            estudianteService.saveEstudiante(estudiante);

            // 8. Enviar email con credenciales
            emailService.enviarCredenciales(apoderadoEmail, emailInstitucional, username, passwordTemporal);

            redirectAttributes.addFlashAttribute("success",
                    "¡Matrícula exitosa! Se han enviado las credenciales al correo del apoderado.\n" +
                            "Usuario: " + username + "\n" +
                            "Email institucional: " + emailInstitucional);

            return "redirect:/login";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al procesar la matrícula: " + e.getMessage());
            return "redirect:/api/matricula/nueva";
        }
    }

    private String generarEmail(String nombres, String apellidoPaterno, String apellidoMaterno) {
        String base = (nombres.substring(0, 1) + apellidoPaterno + apellidoMaterno)
                .toLowerCase()
                .replaceAll("á", "a")
                .replaceAll("é", "e")
                .replaceAll("í", "i")
                .replaceAll("ó", "o")
                .replaceAll("ú", "u")
                .replaceAll("ñ", "n");
        return base + "@estudiante.iesancarlos.edu.pe";
    }

    private String generarUsername(String nombres, String apellidoPaterno, String apellidoMaterno) {
        String base = (nombres.substring(0, 1) + apellidoPaterno + apellidoMaterno)
                .toLowerCase()
                .replaceAll("á", "a")
                .replaceAll("é", "e")
                .replaceAll("í", "i")
                .replaceAll("ó", "o")
                .replaceAll("ú", "u")
                .replaceAll("ñ", "n")
                .replaceAll("[^a-z0-9]", "");

        String username = base;
        int contador = 1;
        while (usuarioService.existsByUsername(username)) {
            username = base + contador;
            contador++;
        }
        return username;
    }

    private String generarPasswordTemporal() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String generarCodigoEstudiante() {
        return "EST-" + LocalDate.now().getYear() + "-" + System.currentTimeMillis();
    }
}