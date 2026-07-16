package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.model.Usuario;
import com.universidad.sistema_academico.repository.UsuarioRepository;
import com.universidad.sistema_academico.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PasswordController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/olvide-contrasena")
    public String mostrarFormularioOlvideContrasena() {
        return "olvide-contrasena";
    }

    @PostMapping("/olvide-contrasena")
    public String procesarOlvideContrasena(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
            usuarioRepository.save(usuario);

            String resetLink = "http://localhost:8080/resetear-contrasena?token=" + token;

            try {
                emailService.enviarCorreoResetPassword(
                        usuario.getEmail(),
                        usuario.getNombre(),
                        resetLink
                );
                redirectAttributes.addFlashAttribute("success",
                        "Se ha enviado un enlace de recuperación a tu correo electrónico.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error",
                        "Error al enviar el correo: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("success",
                    "Si el correo está registrado, recibirás un enlace de recuperación.");
        }

        return "redirect:/olvide-contrasena";
    }

    @GetMapping("/resetear-contrasena")
    public String mostrarFormularioResetearContrasena(
            @RequestParam String token,
            Model model) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("error", "El enlace de recuperación no es válido o ha expirado.");
            return "resetear-contrasena";
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getResetTokenExpiry() == null ||
                usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "El enlace de recuperación ha expirado.");
            return "resetear-contrasena";
        }

        model.addAttribute("token", token);
        model.addAttribute("email", usuario.getEmail());
        return "resetear-contrasena";
    }

    @PostMapping("/resetear-contrasena")
    public String procesarResetearContrasena(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/resetear-contrasena?token=" + token;
        }

        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/resetear-contrasena?token=" + token;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token);

        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El enlace de recuperación no es válido.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getResetTokenExpiry() == null ||
                usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "El enlace de recuperación ha expirado.");
            return "redirect:/login";
        }

        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("success",
                "Tu contraseña ha sido actualizada correctamente. Ahora puedes iniciar sesión.");

        return "redirect:/login";
    }
}