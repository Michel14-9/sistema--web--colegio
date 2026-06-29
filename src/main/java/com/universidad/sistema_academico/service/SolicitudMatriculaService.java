package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.model.*;
import com.universidad.sistema_academico.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SolicitudMatriculaService {

    @Autowired
    private SolicitudMatriculaRepository solicitudRepository;

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Guarda una nueva solicitud de matrícula
     */
    public SolicitudMatricula guardar(SolicitudMatricula solicitud) {
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaSolicitud(LocalDateTime.now());
        return solicitudRepository.save(solicitud);
    }

    /**
     * Lista todas las solicitudes pendientes
     */
    public List<SolicitudMatricula> listarPendientes() {
        return solicitudRepository.findByEstadoOrderByFechaSolicitudDesc("PENDIENTE");
    }

    /**
     * Lista todas las solicitudes aprobadas
     */
    public List<SolicitudMatricula> listarAprobadas() {
        return solicitudRepository.findByEstadoOrderByFechaSolicitudDesc("APROBADO");
    }

    /**
     * Lista todas las solicitudes rechazadas
     */
    public List<SolicitudMatricula> listarRechazadas() {
        return solicitudRepository.findByEstadoOrderByFechaSolicitudDesc("RECHAZADO");
    }

    /**
     * Aprueba una solicitud, crea el usuario, estudiante y la MATRÍCULA ANUAL
     * AHORA RETORNA LA SOLICITUD APROBADA
     */
    @Transactional
    public SolicitudMatricula aprobarSolicitud(Long idSolicitud, Long administradorId) {
        SolicitudMatricula solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Validar que esté pendiente
        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException("La solicitud ya fue procesada");
        }

        Usuario administrador = usuarioRepository.findById(administradorId)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado con ID: " + administradorId));

        // Generar credenciales
        String username = generarUsername(solicitud.getNombres(), solicitud.getApellidoPaterno());
        String emailInstitucional = generarEmail(solicitud.getNombres(), solicitud.getApellidoPaterno(), solicitud.getApellidoMaterno());
        String passwordTemporal = generarPasswordTemporal();

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(emailInstitucional);
        usuario.setPassword(passwordEncoder.encode(passwordTemporal));
        usuario.setNombre(solicitud.getNombres());
        usuario.setApellido(solicitud.getApellidoPaterno() + " " + solicitud.getApellidoMaterno());
        usuario.setRol("ESTUDIANTE");
        usuario.setDocumento(solicitud.getDni());
        usuario.setTelefono(solicitud.getCelular());
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuarioService.save(usuario);

        // Crear estudiante
        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setCodigoEstudiante(generarCodigoEstudiante());
        estudiante.setDni(solicitud.getDni());
        estudiante.setNombres(solicitud.getNombres());
        estudiante.setApellidoPaterno(solicitud.getApellidoPaterno());
        estudiante.setApellidoMaterno(solicitud.getApellidoMaterno());
        estudiante.setFechaNacimiento(solicitud.getFechaNacimiento());
        estudiante.setGenero(solicitud.getGenero());
        estudiante.setEmailInstitucional(emailInstitucional);
        estudiante.setCelular(solicitud.getCelular());
        estudiante.setIdGrado(solicitud.getIdGrado());
        estudiante.setSeccion(solicitud.getSeccion());
        estudiante.setTurno(solicitud.getTurno());
        estudiante.setEstado("ACTIVO");
        estudiante.setFechaIngreso(LocalDate.now());
        estudiante = estudianteService.saveEstudiante(estudiante);

        // ========== CREAR LA MATRÍCULA ANUAL ==========
        Matricula matricula = new Matricula();
        matricula.setEstudiante(estudiante);
        matricula.setAnioAcademico(LocalDate.now().getYear());
        matricula.setIdGrado(solicitud.getIdGrado());
        matricula.setSeccion(solicitud.getSeccion());
        matricula.setTurno(solicitud.getTurno());
        matricula.setFechaMatricula(LocalDate.now());
        matricula.setEstado("ACTIVA");
        matricula.setAprobadoPor(administrador);
        matricula.setFechaAprobacion(LocalDateTime.now());
        matricula.setObservaciones("Matrícula generada desde solicitud #" + idSolicitud);

        matricula = matriculaRepository.save(matricula);

        // Actualizar solicitud
        solicitud.setEstado("APROBADO");
        solicitud.setFechaProcesamiento(LocalDateTime.now());
        solicitud.setAdministradorId(administradorId);
        solicitud.setEstudiante(estudiante);
        SolicitudMatricula solicitudActualizada = solicitudRepository.save(solicitud);

        // Enviar email con credenciales y datos de matrícula
        emailService.enviarCredencialesConMatricula(
                solicitud.getApoderadoEmail(),
                emailInstitucional,
                username,
                passwordTemporal,
                matricula
        );

        // RETORNAR LA SOLICITUD ACTUALIZADA
        return solicitudActualizada;
    }

    /**
     * Rechaza una solicitud
     */
    @Transactional
    public void rechazarSolicitud(Long idSolicitud, Long administradorId, String motivo) {
        SolicitudMatricula solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new RuntimeException("La solicitud ya fue procesada");
        }

        solicitud.setEstado("RECHAZADO");
        solicitud.setFechaProcesamiento(LocalDateTime.now());
        solicitud.setAdministradorId(administradorId);
        solicitud.setObservaciones(motivo);
        solicitudRepository.save(solicitud);

        // Enviar email de rechazo
        emailService.enviarRechazo(solicitud.getApoderadoEmail(), motivo);
    }

    /**
     * Buscar solicitud por ID
     */
    public SolicitudMatricula buscarPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud de matrícula no encontrada con ID: " + id));
    }

    /**
     * Verificar si existe solicitud pendiente por DNI
     */
    public boolean existeSolicitudPendientePorDni(String dni) {
        return solicitudRepository.existsByDniAndEstado(dni, "PENDIENTE");
    }

    // ========== MÉTODOS PRIVADOS ==========

    private String generarUsername(String nombres, String apellidoPaterno) {
        String base = (nombres.substring(0, 1) + apellidoPaterno)
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

    private String generarPasswordTemporal() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String generarCodigoEstudiante() {
        return "EST" + LocalDate.now().getYear() + String.format("%06d", (int)(Math.random() * 1000000));
    }
}