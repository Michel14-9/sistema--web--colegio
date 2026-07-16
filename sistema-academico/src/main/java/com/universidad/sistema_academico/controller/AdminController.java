package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.model.*;
import com.universidad.sistema_academico.repository.*;
import com.universidad.sistema_academico.service.SolicitudMatriculaService;
import com.universidad.sistema_academico.service.UsuarioService;

import jakarta.annotation.PostConstruct;

// ==================== EXCEL (Apache POI) ====================
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;


// ==================== PDF (iText 7) ====================
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;

// ==================== SPRING ====================
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
// ==================== SERVICIOS ====================
import com.universidad.sistema_academico.service.EmailService;

// ==================== JAVA ====================
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private NotaRepository notaRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EstudianteRepository estudianteRepository;
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${voucher.upload.directory:uploads/vouchers}")
    private String uploadDirectory;

    // ==================== ACTUALIZAR MATRÍCULAS VENCIDAS AL INICIO ====================

    @PostConstruct
    public void actualizarMatriculasVencidas() {
        int anioActual = java.time.Year.now().getValue();
        int anioAnterior = anioActual - 1;

        try {
            List<Matricula> matriculasVencidas = matriculaRepository.findByAnioAcademicoAndEstado(anioAnterior, "ACTIVO");

            for (Matricula m : matriculasVencidas) {
                m.setEstado("INACTIVO");
                matriculaRepository.save(m);
                System.out.println(" Matrícula ID " + m.getIdMatricula() +
                        " (Estudiante: " + m.getEstudiante().getNombres() +
                        ") cambiada a INACTIVO (año " + anioAnterior + ")");
            }

            if (!matriculasVencidas.isEmpty()) {
                System.out.println(" Matrículas vencidas actualizadas: " + matriculasVencidas.size());
            }
        } catch (Exception e) {
            System.err.println(" Error al actualizar matrículas vencidas: " + e.getMessage());
        }
    }

    // ==================== MÉTODO PARA REGISTRAR ACTIVIDADES ====================

    private void registrarActividad(String usuario, String accion, String entidad, String detalle) {
        Actividad actividad = new Actividad();
        actividad.setUsuario(usuario);
        actividad.setAccion(accion);
        actividad.setEntidad(entidad);
        actividad.setDetalle(detalle);
        actividad.setFecha(LocalDateTime.now());
        actividadRepository.save(actividad);
    }

    // ==================== MÉTODO PARA ACTUALIZAR CUPOS ====================

    private void actualizarAlumnosActualesPorGradoYTurno(Integer idGrado, String turno) {
        System.out.println("=== ACTUALIZANDO ALUMNOS ACTUALES ===");
        System.out.println("Grado: " + idGrado);
        System.out.println("Turno: " + turno);

        long cantidadEstudiantes = matriculaRepository.countByEstadoAndIdGradoAndTurno("ACTIVA", idGrado, turno);
        System.out.println("Estudiantes activos en este grado/turno: " + cantidadEstudiantes);

        List<Curso> cursos = cursoRepository.findAll().stream()
                .filter(curso -> curso.getIdGrado() != null &&
                        curso.getIdGrado().equals(idGrado) &&
                        curso.getTurno() != null &&
                        curso.getTurno().equalsIgnoreCase(turno) &&
                        !curso.isEliminado())
                .collect(Collectors.toList());

        System.out.println("Cursos a actualizar: " + cursos.size());

        for (Curso curso : cursos) {
            curso.setAlumnosActuales((int) cantidadEstudiantes);
            cursoRepository.save(curso);
            System.out.println("  - Curso: " + curso.getNombreCurso() +
                    " | Alumnos actuales: " + cantidadEstudiantes);
        }
        System.out.println("=== FIN ACTUALIZACIÓN ===\n");
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalEstudiantes = estudianteRepository.count();
        long totalDocentes = docenteRepository.count();
        long totalCursos = cursoRepository.count();
        long totalMatriculas = matriculaRepository.count();
        List<Actividad> ultimasActividades = actividadRepository.findTop10ByOrderByFechaDesc();

        model.addAttribute("totalEstudiantes", totalEstudiantes);
        model.addAttribute("totalDocentes", totalDocentes);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("totalMatriculas", totalMatriculas);
        model.addAttribute("ultimasActividades", ultimasActividades);

        return "admin/dashboard";
    }

    // ==================== CRUD ESTUDIANTES ====================

    @GetMapping("/estudiantes")
    public String listarEstudiantes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String filtroNombre,
            @RequestParam(required = false) String filtroEstado,
            @RequestParam(required = false) String filtroGrado,
            Model model) {

        if (filtroNombre != null && filtroNombre.isEmpty()) filtroNombre = null;
        if (filtroEstado != null && filtroEstado.isEmpty()) filtroEstado = null;
        if (filtroGrado != null && filtroGrado.isEmpty()) filtroGrado = null;

        Pageable pageable = PageRequest.of(page, 10);

        Page<Estudiante> estudiantesPage = estudianteRepository.findWithFilters(
                filtroNombre, filtroEstado, filtroGrado, pageable);

        model.addAttribute("estudiantes", estudiantesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", estudiantesPage.getTotalPages());
        model.addAttribute("totalItems", estudiantesPage.getTotalElements());
        model.addAttribute("filtroNombre", filtroNombre);
        model.addAttribute("filtroEstado", filtroEstado);
        model.addAttribute("filtroGrado", filtroGrado);

        return "admin/estudiantes";
    }

    @GetMapping("/estudiante/nuevo")
    public String mostrarFormularioNuevoEstudiante(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "admin/estudiante-form";
    }

    @PostMapping("/estudiante/guardar")
    public String guardarEstudiante(@ModelAttribute Estudiante estudiante, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        String detalles = "Se registró estudiante: " + estudiante.getNombres() + " " + estudiante.getApellidoPaterno() + " " + estudiante.getApellidoMaterno();

        estudianteRepository.save(estudiante);
        registrarActividad(usuario, "CREAR", "Estudiante", detalles);
        return "redirect:/admin/estudiantes";
    }

    @GetMapping("/estudiante/editar/{id}")
    public String mostrarFormularioEditarEstudiante(@PathVariable Long id, Model model) {
        Optional<Estudiante> estudiante = estudianteRepository.findById(id);
        if (estudiante.isPresent()) {
            model.addAttribute("estudiante", estudiante.get());
            return "admin/estudiante-form";
        }
        return "redirect:/admin/estudiantes";
    }

    @GetMapping("/estudiante/eliminar/{id}")
    public String eliminarEstudiante(@PathVariable Long id, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        Optional<Estudiante> estudiante = estudianteRepository.findById(id);

        if (estudiante.isPresent()) {
            String detalles = "Se eliminó estudiante: " + estudiante.get().getNombres() + " " + estudiante.get().getApellidoPaterno();
            estudianteRepository.deleteById(id);
            registrarActividad(usuario, "ELIMINAR", "Estudiante", detalles);
        }
        return "redirect:/admin/estudiantes";
    }

    // ==================== CRUD DOCENTES ====================

    @GetMapping("/docentes")
    public String listarDocentes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String filtroNombre,
            @RequestParam(required = false) String filtroEspecialidad,
            @RequestParam(required = false) String filtroEstado,
            Model model) {

        System.out.println("=== DOCENTES PAGINACIÓN ===");
        System.out.println("Página: " + page);
        System.out.println("filtroNombre: " + filtroNombre);
        System.out.println("filtroEspecialidad: " + filtroEspecialidad);
        System.out.println("filtroEstado: " + filtroEstado);

        if (filtroNombre != null && filtroNombre.isEmpty()) filtroNombre = null;
        if (filtroEspecialidad != null && filtroEspecialidad.isEmpty()) filtroEspecialidad = null;
        if (filtroEstado != null && filtroEstado.isEmpty()) filtroEstado = null;

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, 10);

        org.springframework.data.domain.Page<Docente> docentesPage = docenteRepository.findWithFilters(
                filtroNombre, filtroEspecialidad, filtroEstado, pageable);

        System.out.println("Total elementos: " + docentesPage.getTotalElements());
        System.out.println("Total páginas: " + docentesPage.getTotalPages());

        List<Docente> docentesFiltrados = docentesPage.getContent().stream()
                .filter(d -> d != null)
                .collect(Collectors.toList());

        List<String> especialidades = getEspecialidadesList();

        model.addAttribute("docentes", docentesFiltrados);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", docentesPage.getTotalPages());
        model.addAttribute("totalItems", docentesFiltrados.size());
        model.addAttribute("hasPrevious", docentesPage.hasPrevious());
        model.addAttribute("hasNext", docentesPage.hasNext());
        model.addAttribute("filtroNombre", filtroNombre);
        model.addAttribute("filtroEspecialidad", filtroEspecialidad);
        model.addAttribute("filtroEstado", filtroEstado);
        model.addAttribute("especialidades", especialidades);

        return "admin/docentes";
    }

    @GetMapping("/docente/nuevo")
    public String mostrarFormularioNuevoDocente(Model model) {
        model.addAttribute("docente", new Docente());
        model.addAttribute("especialidades", getEspecialidadesList());
        return "redirect:/admin/docentes";
    }

    @PostMapping("/docente/guardar")
    public String guardarDocente(@ModelAttribute Docente docente,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        String usuario = auth != null ? auth.getName() : "sistema";

        try {
            System.out.println("=== DATOS RECIBIDOS DEL FORMULARIO ===");
            System.out.println("DNI: " + docente.getDni());
            System.out.println("Nombres: " + docente.getNombres());
            System.out.println("Apellido Paterno: " + docente.getApellidoPaterno());
            System.out.println("Apellido Materno: " + docente.getApellidoMaterno());
            System.out.println("Email Personal: " + docente.getEmail());
            System.out.println("Especialidad: " + docente.getEspecialidad());
            System.out.println("Celular: " + docente.getCelular());

            String emailPersonal = docente.getEmail();

            // Validaciones...
            if (docente.getDni() == null || docente.getDni().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "✗ El DNI es obligatorio");
                return "redirect:/admin/docentes";
            }

            if (docente.getNombres() == null || docente.getNombres().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "✗ Los nombres son obligatorios");
                return "redirect:/admin/docentes";
            }

            if (docente.getApellidoPaterno() == null || docente.getApellidoPaterno().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "✗ El apellido paterno es obligatorio");
                return "redirect:/admin/docentes";
            }

            if (docente.getApellidoMaterno() == null || docente.getApellidoMaterno().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "✗ El apellido materno es obligatorio");
                return "redirect:/admin/docentes";
            }

            if (docente.getEspecialidad() == null || docente.getEspecialidad().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "✗ La especialidad es obligatoria");
                return "redirect:/admin/docentes";
            }

            if (docenteRepository.existsByDni(docente.getDni())) {
                redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el DNI: " + docente.getDni());
                return "redirect:/admin/docentes";
            }

            if (emailPersonal != null && !emailPersonal.trim().isEmpty()) {
                if (docenteRepository.existsByEmail(emailPersonal)) {
                    redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el email: " + emailPersonal);
                    return "redirect:/admin/docentes";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "✗ El email personal es obligatorio");
                return "redirect:/admin/docentes";
            }

            if (docente.getCelular() != null && !docente.getCelular().trim().isEmpty()) {
                if (docente.getCelular().length() != 9 || !docente.getCelular().matches("\\d+")) {
                    redirectAttributes.addFlashAttribute("error", "✗ El celular debe tener 9 dígitos numéricos");
                    return "redirect:/admin/docentes";
                }
            }

            String emailInstitucional = generarEmailDocente(
                    docente.getNombres().trim(),
                    docente.getApellidoPaterno().trim(),
                    docente.getApellidoMaterno().trim()
            );
            System.out.println("Email institucional generado: " + emailInstitucional);

            if (docenteRepository.existsByEmail(emailInstitucional)) {
                redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el email institucional: " + emailInstitucional);
                return "redirect:/admin/docentes";
            }

            docente.setEmail(emailInstitucional);
            docente.setEspecialidad(docente.getEspecialidad().toUpperCase().trim());
            docente.setEstado("ACTIVO");

            if (docente.getCodigoDocente() == null || docente.getCodigoDocente().trim().isEmpty()) {
                docente.generarCodigoAutomatico();
                System.out.println("Código generado: " + docente.getCodigoDocente());
            }

            String passwordTemporal = generarPasswordTemporal();
            System.out.println("Contraseña temporal: " + passwordTemporal);

            Usuario usuarioDocente = new Usuario();
            usuarioDocente.setUsername(emailInstitucional);
            usuarioDocente.setPassword(passwordEncoder.encode(passwordTemporal));
            usuarioDocente.setNombre(docente.getNombres().trim());
            usuarioDocente.setApellido(docente.getApellidoPaterno().trim() + " " + docente.getApellidoMaterno().trim());
            usuarioDocente.setEmail(emailInstitucional);
            usuarioDocente.setDocumento(docente.getDni());
            usuarioDocente.setTelefono(docente.getCelular());
            usuarioDocente.setRol("DOCENTE");
            usuarioDocente.setActivo(true);
            usuarioDocente.setFechaRegistro(LocalDateTime.now());

            Usuario usuarioGuardado = usuarioService.save(usuarioDocente);
            System.out.println(" Usuario creado con ID: " + usuarioGuardado.getId());

            docente.setUsuario(usuarioGuardado);

            Docente docenteGuardado = docenteRepository.save(docente);
            System.out.println(" Docente guardado con ID: " + docenteGuardado.getIdDocente());

            try {
                emailService.enviarCredencialesDocente(
                        emailPersonal,
                        emailInstitucional,
                        passwordTemporal,
                        docente.getNombres()
                );
                System.out.println(" Credenciales enviadas al email personal: " + emailPersonal);
            } catch (Exception e) {
                System.err.println(" Error al enviar email a " + emailPersonal + ": " + e.getMessage());
            }

            registrarActividad(usuario, "CREAR", "Docente",
                    "Se registró docente: " + docente.getNombres() +
                            " - DNI: " + docente.getDni() +
                            " - Email personal: " + emailPersonal +
                            " - Email institucional: " + emailInstitucional +
                            " - Especialidad: " + docente.getEspecialidad());

            redirectAttributes.addFlashAttribute("success",
                    " Docente '" + docente.getNombres() + " " + docente.getApellidoPaterno() +
                            "' creado correctamente.\n" +
                            "📧 Email institucional: " + emailInstitucional + "\n" +
                            "📨 Se enviaron las credenciales a su correo personal: " + emailPersonal + "\n" +
                            "🔑 Contraseña temporal: " + passwordTemporal);

        } catch (Exception e) {
            System.err.println("=== ERROR AL GUARDAR DOCENTE ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "✗ Error al guardar: " + e.getMessage());
        }

        return "redirect:/admin/docentes";
    }

    private String generarEmailDocente(String nombres, String apellidoPaterno, String apellidoMaterno) {
        String base = (nombres.substring(0, 1) + apellidoPaterno + apellidoMaterno)
                .toLowerCase()
                .replaceAll("á", "a")
                .replaceAll("é", "e")
                .replaceAll("í", "i")
                .replaceAll("ó", "o")
                .replaceAll("ú", "u")
                .replaceAll("ñ", "n")
                .replaceAll("[^a-z0-9]", "");

        String email = base + "@docente.iesancarlos.edu.pe";
        int contador = 1;
        while (usuarioService.existsByUsername(email) || docenteRepository.existsByEmail(email)) {
            email = base + contador + "@docente.iesancarlos.edu.pe";
            contador++;
        }
        return email;
    }

    private String generarPasswordTemporal() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return password.toString();
    }

    @GetMapping("/docente/editar/{id}")
    public String mostrarFormularioEditarDocente(@PathVariable Long id, Model model) {
        Optional<Docente> docente = docenteRepository.findById(id);
        if (docente.isPresent()) {
            model.addAttribute("docente", docente.get());
            model.addAttribute("especialidades", getEspecialidadesList());
            return "admin/docente-form";
        }
        return "redirect:/admin/docentes";
    }

    @PostMapping("/docente/actualizar/{id}")
    public String actualizarDocente(@PathVariable Long id,
                                    @ModelAttribute Docente docente,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        String usuario = auth != null ? auth.getName() : "sistema";

        try {
            Optional<Docente> docenteExistenteOpt = docenteRepository.findById(id);
            if (!docenteExistenteOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "✗ Docente no encontrado");
                return "redirect:/admin/docentes";
            }

            Docente docenteOriginal = docenteExistenteOpt.get();

            String nuevoDni = docente.getDni();
            String dniActual = docenteOriginal.getDni();

            if (nuevoDni != null && !nuevoDni.equals(dniActual)) {
                if (nuevoDni.length() != 8 || !nuevoDni.matches("\\d+")) {
                    redirectAttributes.addFlashAttribute("error", "✗ El DNI debe tener 8 dígitos numéricos");
                    return "redirect:/admin/docentes";
                }
                if (docenteRepository.existsByDniAndIdDocenteNot(nuevoDni, id)) {
                    redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el DNI: " + nuevoDni);
                    return "redirect:/admin/docentes";
                }
                docenteOriginal.setDni(nuevoDni);
            }

            String nuevoEmail = docente.getEmail();
            String emailActual = docenteOriginal.getEmail();

            if (nuevoEmail != null && !nuevoEmail.isEmpty() && !nuevoEmail.equals(emailActual)) {
                if (docenteRepository.existsByEmailAndIdDocenteNot(nuevoEmail, id)) {
                    redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el email: " + nuevoEmail);
                    return "redirect:/admin/docentes";
                }
                docenteOriginal.setEmail(nuevoEmail);
            }

            String nuevoCelular = docente.getCelular();
            if (nuevoCelular != null && !nuevoCelular.isEmpty()) {
                if (nuevoCelular.length() != 9 || !nuevoCelular.matches("\\d+")) {
                    redirectAttributes.addFlashAttribute("error", "✗ El celular debe tener 9 dígitos numéricos");
                    return "redirect:/admin/docentes";
                }
                docenteOriginal.setCelular(nuevoCelular);
            } else {
                docenteOriginal.setCelular(null);
            }

            if (docente.getEspecialidad() == null || docente.getEspecialidad().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "✗ La especialidad es obligatoria");
                return "redirect:/admin/docentes";
            }
            docenteOriginal.setEspecialidad(docente.getEspecialidad().toUpperCase().trim());

            docenteOriginal.setNombres(docente.getNombres());
            docenteOriginal.setApellidoPaterno(docente.getApellidoPaterno());
            docenteOriginal.setApellidoMaterno(docente.getApellidoMaterno());
            docenteOriginal.setEstado(docente.getEstado());

            if (docenteOriginal.getUsuario() != null) {
                Usuario usuarioDocente = docenteOriginal.getUsuario();
                usuarioDocente.setNombre(docenteOriginal.getNombres());
                usuarioDocente.setApellido(docenteOriginal.getApellidoPaterno() + " " + docenteOriginal.getApellidoMaterno());
                usuarioDocente.setDocumento(docenteOriginal.getDni());
                usuarioDocente.setTelefono(docenteOriginal.getCelular());

                if (nuevoEmail != null && !nuevoEmail.isEmpty() && !nuevoEmail.equals(usuarioDocente.getEmail())) {
                    usuarioDocente.setEmail(nuevoEmail);
                    usuarioDocente.setUsername(nuevoEmail);
                }

                if (docente.getUsuario() != null &&
                        docente.getUsuario().getPassword() != null &&
                        !docente.getUsuario().getPassword().isEmpty() &&
                        !docente.getUsuario().getPassword().startsWith("$2a$")) {
                    usuarioDocente.setPassword(passwordEncoder.encode(docente.getUsuario().getPassword()));
                }

                usuarioService.save(usuarioDocente);
            }

            docenteRepository.save(docenteOriginal);

            registrarActividad(usuario, "EDITAR", "Docente",
                    "Se actualizó docente: " + docenteOriginal.getNombres() +
                            " - DNI: " + docenteOriginal.getDni() +
                            " - Teléfono: " + docenteOriginal.getCelular() +
                            " - Especialidad: " + docenteOriginal.getEspecialidad());

            redirectAttributes.addFlashAttribute("success",
                    " Docente '" + docenteOriginal.getNombres() + " " + docenteOriginal.getApellidoPaterno() +
                            "' actualizado correctamente");

        } catch (Exception e) {
            System.err.println("=== ERROR AL ACTUALIZAR DOCENTE ===");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "✗ Error al actualizar: " + e.getMessage());
        }

        return "redirect:/admin/docentes";
    }

    @GetMapping("/docente/eliminar/{id}")
    public String eliminarDocente(@PathVariable Long id,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        String usuario = auth != null ? auth.getName() : "sistema";

        Optional<Docente> docente = docenteRepository.findById(id);
        if (docente.isPresent()) {
            String nombreDocente = docente.get().getNombres();

            List<Curso> cursosAsignados = cursoRepository.findByIdDocenteAndEstado(id, "ACTIVO");
            if (!cursosAsignados.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        " No se puede eliminar al docente '" + nombreDocente +
                                "' porque tiene " + cursosAsignados.size() + " curso(s) asignado(s)");
                return "redirect:/admin/docentes";
            }

            Docente docenteParaEliminar = docente.get();
            docenteParaEliminar.setEliminado(true);
            docenteParaEliminar.setEstado("INACTIVO");
            docenteRepository.save(docenteParaEliminar);

            registrarActividad(usuario, "ELIMINAR", "Docente", "Se eliminó docente: " + nombreDocente);
            redirectAttributes.addFlashAttribute("success", " Docente '" + nombreDocente + "' eliminado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error: Docente no encontrado");
        }

        return "redirect:/admin/docentes";
    }

    private List<String> getEspecialidadesList() {
        return java.util.Arrays.asList(
                "MATEMÁTICAS",
                "COMUNICACIÓN",
                "CIENCIA Y TECNOLOGÍA",
                "CIENCIAS SOCIALES",
                "INGLÉS",
                "ARTE",
                "EDUCACIÓN FÍSICA",
                "RELIGIÓN",
                "TUTORÍA"
        );
    }

    // ==================== CRUD CURSOS ====================

    @GetMapping("/cursos")
    public String listarCursos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer filtroGrado,
            @RequestParam(required = false) String filtroTurno,
            @RequestParam(required = false) String filtroArea,
            @RequestParam(required = false) String filtroEstado,
            @RequestParam(required = false) String filtroNombre,
            Model model) {

        if (filtroGrado != null && filtroGrado == 0) filtroGrado = null;
        if (filtroTurno != null && filtroTurno.isEmpty()) filtroTurno = null;
        if (filtroArea != null && filtroArea.isEmpty()) filtroArea = null;
        if (filtroEstado != null && filtroEstado.isEmpty()) filtroEstado = null;
        if (filtroNombre != null && filtroNombre.isEmpty()) filtroNombre = null;

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, 10);

        org.springframework.data.domain.Page<Curso> cursosPage = cursoRepository.findWithFilters(
                filtroGrado,
                filtroTurno,
                filtroArea,
                filtroEstado,
                filtroNombre,
                pageable
        );

        List<Curso> todosCursos = cursoRepository.findAllWithDocente();
        long totalCursos = todosCursos.size();
        long cursosActivos = todosCursos.stream().filter(c -> "ACTIVO".equals(c.getEstado())).count();
        long cursosInactivos = todosCursos.stream().filter(c -> "INACTIVO".equals(c.getEstado())).count();
        long cursosSinDocente = todosCursos.stream().filter(c -> c.getIdDocente() == null || c.getIdDocente() == 0).count();
        int totalCupos = todosCursos.stream().mapToInt(c -> c.getCapacidadMaxima() != null ? c.getCapacidadMaxima() : 36).sum();

        // Crear mapa de nombres de grado
        Map<Integer, String> nombresGrados = new HashMap<>();
        for (int i = 1; i <= 11; i++) {
            nombresGrados.put(i, obtenerNombreGrado(i));
        }

        model.addAttribute("cursos", cursosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", cursosPage.getTotalPages());
        model.addAttribute("totalItems", cursosPage.getTotalElements());
        model.addAttribute("hasPrevious", cursosPage.hasPrevious());
        model.addAttribute("hasNext", cursosPage.hasNext());
        model.addAttribute("filtroGrado", filtroGrado);
        model.addAttribute("filtroTurno", filtroTurno);
        model.addAttribute("filtroArea", filtroArea);
        model.addAttribute("filtroEstado", filtroEstado);
        model.addAttribute("filtroNombre", filtroNombre);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("cursosActivos", cursosActivos);
        model.addAttribute("cursosInactivos", cursosInactivos);
        model.addAttribute("cursosSinDocente", cursosSinDocente);
        model.addAttribute("totalCupos", totalCupos);
        model.addAttribute("docentes", docenteRepository.findAll());
        model.addAttribute("nombresGrados", nombresGrados); // Agregar esto

        return "admin/cursos";
    }

    @GetMapping("/curso/nuevo")
    public String mostrarFormularioNuevoCurso(Model model) {
        model.addAttribute("curso", new Curso());
        model.addAttribute("docentes", docenteRepository.findAll());
        return "admin/cursos";
    }

    @PostMapping("/curso/guardar")
    public String guardarCurso(@ModelAttribute Curso curso,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        String usuario = auth != null ? auth.getName() : "sistema";

        try {
            boolean duplicado = cursoRepository.existsByNombreCursoAndIdGradoAndSeccionAndTurno(
                    curso.getNombreCurso(),
                    curso.getIdGrado(),
                    curso.getSeccion(),
                    curso.getTurno()
            );

            if (duplicado) {
                redirectAttributes.addFlashAttribute("error",
                        "✗ Ya existe un curso con el mismo nombre, grado, sección y turno");
                return "redirect:/admin/cursos";
            }

            int cantidadCursos = cursoRepository.countByIdGradoAndAreaAndTurno(
                    curso.getIdGrado(),
                    curso.getArea(),
                    curso.getTurno()
            );

            if (cantidadCursos >= 4) {
                redirectAttributes.addFlashAttribute("error",
                        "✗ Máximo 4 cursos permitidos por grado, área y turno (secciones A, B, C, D)");
                return "redirect:/admin/cursos";
            }

            if (curso.getIdDocente() != null && curso.getIdDocente() > 0) {
                Integer horasActuales = cursoRepository.sumHorasSemanalesByDocente(curso.getIdDocente());
                if (horasActuales == null) horasActuales = 0;

                int nuevasHoras = curso.getHorasSemanales() != null ? curso.getHorasSemanales() : 0;
                int totalHoras = horasActuales + nuevasHoras;

                if (totalHoras > 30) {
                    redirectAttributes.addFlashAttribute("error",
                            "✗ El docente excede las 30 horas semanales permitidas. " +
                                    "Horas actuales: " + horasActuales + ", Nuevas horas: " + nuevasHoras);
                    return "redirect:/admin/cursos";
                }
            }

            if (curso.getIdDocente() != null && curso.getIdDocente() > 0 && curso.getHorario() != null && !curso.getHorario().isEmpty()) {
                boolean tieneConflicto = cursoRepository.existsByDocenteAndHorarioAndIdCursoNot(
                        curso.getIdDocente(),
                        curso.getHorario(),
                        0L
                );

                if (tieneConflicto) {
                    redirectAttributes.addFlashAttribute("error",
                            "✗ El docente ya tiene un curso asignado en el horario: " + curso.getHorario());
                    return "redirect:/admin/cursos";
                }
            }

            if (curso.getCodigoCurso() == null || curso.getCodigoCurso().isEmpty()) {
                curso.generarCodigoAutomatico();
            }

            long cantidadEstudiantes = matriculaRepository.countByEstadoAndIdGradoAndTurno(
                    "ACTIVA",
                    curso.getIdGrado(),
                    curso.getTurno()
            );
            curso.setAlumnosActuales((int) cantidadEstudiantes);
            System.out.println(" Alumnos actuales calculados para el curso: " + cantidadEstudiantes);

            if (curso.getCapacidadMaxima() == null) {
                curso.setCapacidadMaxima(36);
            }

            cursoRepository.save(curso);
            registrarActividad(usuario, "CREAR", "Curso", "Se registró curso: " + curso.getNombreCurso() +
                    " - Grado: " + curso.getIdGrado() +
                    " - Turno: " + curso.getTurno() +
                    " - Alumnos actuales: " + curso.getAlumnosActuales());
            redirectAttributes.addFlashAttribute("success", "✅ Curso '" + curso.getNombreCurso() +
                    "' creado correctamente con " + curso.getAlumnosActuales() + " alumnos matriculados.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "✗ Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/cursos";
    }

    @GetMapping("/curso/editar/{id}")
    public String mostrarFormularioEditarCurso(@PathVariable Long id, Model model) {
        Optional<Curso> curso = cursoRepository.findById(id);
        if (curso.isPresent()) {
            model.addAttribute("curso", curso.get());
            model.addAttribute("docentes", docenteRepository.findAll());
            return "admin/curso-form";
        }
        return "redirect:/admin/cursos";
    }

    @PostMapping("/curso/actualizar/{id}")
    public String actualizarCurso(@PathVariable Long id,
                                  @ModelAttribute Curso curso,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        String usuario = auth != null ? auth.getName() : "sistema";

        try {
            Optional<Curso> cursoExistenteOpt = cursoRepository.findById(id);
            if (!cursoExistenteOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "✗ Curso no encontrado");
                return "redirect:/admin/cursos";
            }

            Curso cursoOriginal = cursoExistenteOpt.get();
            curso.setIdCurso(id);

            boolean duplicado = cursoRepository.existsByNombreCursoAndIdGradoAndSeccionAndTurno(
                    curso.getNombreCurso(),
                    curso.getIdGrado(),
                    curso.getSeccion(),
                    curso.getTurno()
            );

            if (duplicado && !cursoOriginal.getNombreCurso().equals(curso.getNombreCurso())) {
                redirectAttributes.addFlashAttribute("error",
                        "✗ Ya existe un curso con el mismo nombre, grado, sección y turno");
                return "redirect:/admin/cursos";
            }

            if (curso.getIdDocente() != null && curso.getIdDocente() > 0) {
                Integer horasActuales = cursoRepository.sumHorasSemanalesByDocente(curso.getIdDocente());
                if (horasActuales == null) horasActuales = 0;

                if (cursoOriginal.getIdDocente() != null && cursoOriginal.getIdDocente().equals(curso.getIdDocente())) {
                    horasActuales -= (cursoOriginal.getHorasSemanales() != null ? cursoOriginal.getHorasSemanales() : 0);
                }

                int nuevasHoras = curso.getHorasSemanales() != null ? curso.getHorasSemanales() : 0;
                int totalHoras = horasActuales + nuevasHoras;

                if (totalHoras > 30) {
                    redirectAttributes.addFlashAttribute("error",
                            "✗ El docente excede las 30 horas semanales permitidas. " +
                                    "Horas actuales: " + horasActuales + ", Nuevas horas: " + nuevasHoras);
                    return "redirect:/admin/cursos";
                }
            }

            if (curso.getIdDocente() != null && curso.getIdDocente() > 0 && curso.getHorario() != null && !curso.getHorario().isEmpty()) {
                boolean tieneConflicto = cursoRepository.existsByDocenteAndHorarioAndIdCursoNot(
                        curso.getIdDocente(),
                        curso.getHorario(),
                        id
                );

                if (tieneConflicto) {
                    redirectAttributes.addFlashAttribute("error",
                            "✗ El docente ya tiene un curso asignado en el horario: " + curso.getHorario());
                    return "redirect:/admin/cursos";
                }
            }

            long cantidadEstudiantes = matriculaRepository.countByEstadoAndIdGradoAndTurno(
                    "ACTIVA",
                    curso.getIdGrado(),
                    curso.getTurno()
            );
            curso.setAlumnosActuales((int) cantidadEstudiantes);
            System.out.println("✅ Alumnos actuales actualizados: " + cantidadEstudiantes);

            if (curso.getCapacidadMaxima() == null) {
                curso.setCapacidadMaxima(36);
            }

            cursoRepository.save(curso);
            registrarActividad(usuario, "EDITAR", "Curso", "Se actualizó curso: " + curso.getNombreCurso() +
                    " - Alumnos actuales: " + curso.getAlumnosActuales());
            redirectAttributes.addFlashAttribute("success", "✅ Curso '" + curso.getNombreCurso() +
                    "' actualizado correctamente con " + curso.getAlumnosActuales() + " alumnos matriculados.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "✗ Error al actualizar: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/cursos";
    }

    @GetMapping("/curso/eliminar/{id}")
    public String eliminarCurso(@PathVariable Long id,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        String usuario = auth != null ? auth.getName() : "sistema";

        Optional<Curso> curso = cursoRepository.findById(id);
        if (curso.isPresent()) {
            String nombreCurso = curso.get().getNombreCurso();

            Curso cursoParaEliminar = curso.get();
            cursoParaEliminar.setEliminado(true);
            cursoRepository.save(cursoParaEliminar);

            registrarActividad(usuario, "ELIMINAR", "Curso", "Se eliminó curso: " + nombreCurso);
            redirectAttributes.addFlashAttribute("success", " Curso '" + nombreCurso + "' eliminado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", " Error: Curso no encontrado");
        }

        return "redirect:/admin/cursos";
    }

    // ==================== VALIDACIÓN EN TIEMPO REAL ====================

    @GetMapping("/curso/validar-horario")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> validarHorario(
            @RequestParam Long idDocente,
            @RequestParam String horario,
            @RequestParam(required = false) Long idCurso) {

        Map<String, Boolean> response = new HashMap<>();
        Long cursoId = (idCurso != null) ? idCurso : 0L;

        boolean tieneConflicto = cursoRepository.existsByDocenteAndHorarioAndIdCursoNot(
                idDocente,
                horario,
                cursoId
        );

        response.put("conflicto", tieneConflicto);
        return ResponseEntity.ok(response);
    }

    // ==================== FILTRAR DOCENTES POR ESPECIALIDAD ====================

    @GetMapping("/docentes/por-especialidad")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getDocentesByEspecialidad(
            @RequestParam(required = false) String especialidad) {

        List<Docente> docentes;
        if (especialidad != null && !especialidad.isEmpty()) {
            docentes = docenteRepository.findByEspecialidadAndEstadoAndEliminadoFalse(especialidad);
        } else {
            docentes = docenteRepository.findAllActive();
        }

        List<Map<String, Object>> response = docentes.stream()
                .filter(d -> "ACTIVO".equals(d.getEstado()))
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", d.getIdDocente());
                    map.put("nombre", d.getNombres() + " " + d.getApellidoPaterno() + " " + d.getApellidoMaterno());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ==================== CRUD MATRÍCULAS ====================

    @GetMapping("/matriculas")
    public String listarMatriculas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer grado,
            Model model) {

        List<Matricula> matriculas;

        // Aplicar filtros
        if (estado != null && !estado.isEmpty() && grado != null) {
            matriculas = matriculaRepository.findByEstadoAndIdGrado(estado, grado);
        } else if (estado != null && !estado.isEmpty()) {
            matriculas = matriculaRepository.findByEstado(estado);
        } else if (grado != null) {
            matriculas = matriculaRepository.findByIdGrado(grado);
        } else {
            matriculas = matriculaRepository.findAll();
        }

        // Agregar nombres de grado
        Map<Long, String> nombresGrados = new HashMap<>();
        for (Matricula m : matriculas) {
            if (m.getIdGrado() != null) {
                nombresGrados.put(m.getIdMatricula(), obtenerNombreGrado(m.getIdGrado()));
            }
        }

        // Calcular estadísticas
        long totalPendientes = matriculaRepository.findByEstado("PENDIENTE").size();
        long totalActivas = matriculaRepository.findByEstado("ACTIVA").size();
        long totalInactivas = matriculaRepository.findByEstado("INACTIVA").size();

        model.addAttribute("matriculas", matriculas);
        model.addAttribute("nombresGrados", nombresGrados);
        model.addAttribute("totalPendientes", totalPendientes);
        model.addAttribute("totalActivas", totalActivas);
        model.addAttribute("totalInactivas", totalInactivas);
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("gradoFiltro", grado);
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        model.addAttribute("cursos", cursoRepository.findAll());

        return "admin/matriculas";
    }

    // ==================== ACTUALIZAR MATRÍCULAS VENCIDAS MANUAL ====================

    @GetMapping("/actualizar-matriculas")
    public String actualizarMatriculasVencidasManual(RedirectAttributes redirectAttributes) {
        int anioActual = java.time.Year.now().getValue();
        int anioAnterior = anioActual - 1;

        try {
            List<Matricula> matriculasVencidas = matriculaRepository.findByAnioAcademicoAndEstado(anioAnterior, "ACTIVO");

            for (Matricula m : matriculasVencidas) {
                m.setEstado("INACTIVO");
                matriculaRepository.save(m);
            }

            if (!matriculasVencidas.isEmpty()) {
                redirectAttributes.addFlashAttribute("success",
                        "✅ " + matriculasVencidas.size() + " matrículas del año " + anioAnterior + " fueron desactivadas");
            } else {
                redirectAttributes.addFlashAttribute("info",
                        " No hay matrículas activas del año " + anioAnterior);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", " Error al actualizar: " + e.getMessage());
        }

        return "redirect:/admin/matriculas";
    }

    // ==================== GESTIÓN DE SOLICITUDES DE MATRÍCULA ====================

    @Autowired
    private SolicitudMatriculaService solicitudMatriculaService;

    @GetMapping("/solicitudes")
    public String listarSolicitudes(Model model) {
        List<SolicitudMatricula> pendientes = solicitudMatriculaService.listarPendientes();
        List<SolicitudMatricula> aprobadas = solicitudMatriculaService.listarAprobadas();
        List<SolicitudMatricula> rechazadas = solicitudMatriculaService.listarRechazadas();

        // Crear mapa de nombres de grado para todas las solicitudes
        Map<Integer, String> nombresGrados = new HashMap<>();
        for (int i = 1; i <= 11; i++) {
            nombresGrados.put(i, obtenerNombreGrado(i));
        }

        model.addAttribute("pendientes", pendientes);
        model.addAttribute("aprobadas", aprobadas);
        model.addAttribute("rechazadas", rechazadas);
        model.addAttribute("nombresGrados", nombresGrados); // ✅ Agregar esto

        return "admin/solicitudes";
    }

    @GetMapping("/solicitud/{id}")
    public String verDetalleSolicitud(@PathVariable Long id, Model model) {
        SolicitudMatricula solicitud = solicitudMatriculaService.buscarPorId(id);

        String voucherFilename = "";
        if (solicitud.getVoucherPath() != null && !solicitud.getVoucherPath().isEmpty()) {
            String fullPath = solicitud.getVoucherPath();
            String cleanPath = fullPath.replace("\\", "/");
            voucherFilename = cleanPath.substring(cleanPath.lastIndexOf("/") + 1);
        }


        String nombreGrado = obtenerNombreGrado(solicitud.getIdGrado());

        model.addAttribute("solicitud", solicitud);
        model.addAttribute("voucherFilename", voucherFilename);
        model.addAttribute("nombreGrado", nombreGrado);

        return "admin/solicitud-detalle";
    }

    @PostMapping("/solicitud/aprobar/{id}")
    public String aprobarSolicitud(@PathVariable Long id, Authentication auth) {
        String email = auth != null ? auth.getName() : "admin";

        Usuario admin = usuarioService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado con email: " + email));

        SolicitudMatricula solicitud = solicitudMatriculaService.aprobarSolicitud(id, admin.getId());

        Optional<Matricula> matriculaOpt = matriculaRepository
                .findMatriculaActivaByEstudianteId(solicitud.getEstudiante().getIdEstudiante());

        if (matriculaOpt.isPresent()) {
            Matricula matricula = matriculaOpt.get();
            actualizarAlumnosActualesPorGradoYTurno(matricula.getIdGrado(), matricula.getTurno());
        }

        registrarActividad(email, "APROBAR", "SolicitudMatricula",
                "Se aprobó la solicitud de matrícula ID: " + id);

        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/solicitud/rechazar/{id}")
    public String rechazarSolicitud(@PathVariable Long id,
                                    @RequestParam String motivo,
                                    Authentication auth) {
        String email = auth != null ? auth.getName() : "admin";

        Usuario admin = usuarioService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado con email: " + email));

        solicitudMatriculaService.rechazarSolicitud(id, admin.getId(), motivo);

        registrarActividad(email, "RECHAZAR", "SolicitudMatricula",
                "Se rechazó la solicitud de matrícula ID: " + id + " - Motivo: " + motivo);

        return "redirect:/admin/solicitudes";
    }

    // ==================== VER VOUCHER ====================

    @GetMapping("/voucher/{filename}")
    @ResponseBody
    public ResponseEntity<byte[]> verVoucher(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDirectory, filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = determineContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(fileBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String determineContentType(String filename) {
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }

    // ==================== FILTRAR DOCENTES POR ESPECIALIDAD SEGÚN NOMBRE DEL CURSO ====================

    @GetMapping("/docentes/por-nombre-curso")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDocentesByNombreCurso(@RequestParam String nombreCurso) {
        Map<String, Object> response = new HashMap<>();

        String especialidad = detectarEspecialidadDesdeNombreCurso(nombreCurso);
        response.put("especialidadDetectada", especialidad);

        List<Docente> docentes;
        if (especialidad != null && !especialidad.isEmpty()) {
            docentes = docenteRepository.findByEspecialidadAndEstadoAndEliminadoFalse(especialidad);
        } else {
            docentes = docenteRepository.findAllActive();
        }

        List<Map<String, Object>> docentesList = docentes.stream()
                .filter(d -> "ACTIVO".equals(d.getEstado()))
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", d.getIdDocente());
                    map.put("nombre", d.getNombres() + " " + d.getApellidoPaterno() + " " + d.getApellidoMaterno());
                    map.put("especialidad", d.getEspecialidad());
                    return map;
                })
                .collect(Collectors.toList());

        response.put("docentes", docentesList);
        response.put("total", docentesList.size());

        return ResponseEntity.ok(response);
    }

    private String detectarEspecialidadDesdeNombreCurso(String nombreCurso) {
        if (nombreCurso == null || nombreCurso.trim().isEmpty()) {
            return null;
        }

        String nombreNorm = normalizarTexto(nombreCurso);

        Map<String, String> mapaEspecialidades = new HashMap<>();
        mapaEspecialidades.put("MATEMATICA", "MATEMÁTICAS");
        mapaEspecialidades.put("MATEMATICAS", "MATEMÁTICAS");
        mapaEspecialidades.put("ALGEBRA", "MATEMÁTICAS");
        mapaEspecialidades.put("GEOMETRIA", "MATEMÁTICAS");
        mapaEspecialidades.put("TRIGONOMETRIA", "MATEMÁTICAS");
        mapaEspecialidades.put("CALCULO", "MATEMÁTICAS");
        mapaEspecialidades.put("ARITMETICA", "MATEMÁTICAS");
        mapaEspecialidades.put("ESTADISTICA", "MATEMÁTICAS");
        mapaEspecialidades.put("PROBABILIDAD", "MATEMÁTICAS");
        mapaEspecialidades.put("RAZONAMIENTO", "MATEMÁTICAS");
        mapaEspecialidades.put("SUMA", "MATEMÁTICAS");
        mapaEspecialidades.put("RESTA", "MATEMÁTICAS");
        mapaEspecialidades.put("MULTIPLICACION", "MATEMÁTICAS");
        mapaEspecialidades.put("DIVISION", "MATEMÁTICAS");
        mapaEspecialidades.put("NUMEROS", "MATEMÁTICAS");

        mapaEspecialidades.put("COMUNICACION", "COMUNICACIÓN");
        mapaEspecialidades.put("LENGUAJE", "COMUNICACIÓN");
        mapaEspecialidades.put("LITERATURA", "COMUNICACIÓN");
        mapaEspecialidades.put("GRAMATICA", "COMUNICACIÓN");
        mapaEspecialidades.put("ORTOGRAFIA", "COMUNICACIÓN");
        mapaEspecialidades.put("REDACCION", "COMUNICACIÓN");
        mapaEspecialidades.put("LECTURA", "COMUNICACIÓN");
        mapaEspecialidades.put("ESCRITURA", "COMUNICACIÓN");
        mapaEspecialidades.put("VOCALES", "COMUNICACIÓN");
        mapaEspecialidades.put("ORATORIA", "COMUNICACIÓN");

        mapaEspecialidades.put("CIENCIA", "CIENCIA Y TECNOLOGÍA");
        mapaEspecialidades.put("BIOLOGIA", "CIENCIA Y TECNOLOGÍA");
        mapaEspecialidades.put("FISICA", "CIENCIA Y TECNOLOGÍA");
        mapaEspecialidades.put("QUIMICA", "CIENCIA Y TECNOLOGÍA");
        mapaEspecialidades.put("ECOLOGIA", "CIENCIA Y TECNOLOGÍA");
        mapaEspecialidades.put("LABORATORIO", "CIENCIA Y TECNOLOGÍA");
        mapaEspecialidades.put("ANATOMIA", "CIENCIA Y TECNOLOGÍA");
        mapaEspecialidades.put("ZOOLOGIA", "CIENCIA Y TECNOLOGÍA");
        mapaEspecialidades.put("BOTANICA", "CIENCIA Y TECNOLOGÍA");
        mapaEspecialidades.put("ASTRONOMIA", "CIENCIA Y TECNOLOGÍA");

        mapaEspecialidades.put("HISTORIA", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("GEOGRAFIA", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("PERSONAL SOCIAL", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("CIVICA", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("CIUDADANIA", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("FILOSOFIA", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("PSICOLOGIA", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("SOCIOLOGIA", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("ANTROPOLOGIA", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("ECONOMIA", "CIENCIAS SOCIALES");
        mapaEspecialidades.put("CONTABILIDAD", "CIENCIAS SOCIALES");

        mapaEspecialidades.put("INGLES", "INGLÉS");
        mapaEspecialidades.put("ENGLISH", "INGLÉS");

        mapaEspecialidades.put("ARTE", "ARTE");
        mapaEspecialidades.put("DIBUJO", "ARTE");
        mapaEspecialidades.put("PINTURA", "ARTE");
        mapaEspecialidades.put("MUSICA", "ARTE");
        mapaEspecialidades.put("TEATRO", "ARTE");
        mapaEspecialidades.put("DANZA", "ARTE");
        mapaEspecialidades.put("FOLCLOR", "ARTE");
        mapaEspecialidades.put("MARINERA", "ARTE");

        mapaEspecialidades.put("EDUCACION FISICA", "EDUCACIÓN FÍSICA");
        mapaEspecialidades.put("DEPORTE", "EDUCACIÓN FÍSICA");
        mapaEspecialidades.put("RECREACION", "EDUCACIÓN FÍSICA");
        mapaEspecialidades.put("SALUD", "EDUCACIÓN FÍSICA");
        mapaEspecialidades.put("VOLEIBOL", "EDUCACIÓN FÍSICA");
        mapaEspecialidades.put("FULBITO", "EDUCACIÓN FÍSICA");
        mapaEspecialidades.put("ATLETISMO", "EDUCACIÓN FÍSICA");

        mapaEspecialidades.put("RELIGION", "RELIGIÓN");
        mapaEspecialidades.put("VALORES", "RELIGIÓN");
        mapaEspecialidades.put("ETICA", "RELIGIÓN");

        mapaEspecialidades.put("TUTORIA", "TUTORÍA");
        mapaEspecialidades.put("ORIENTACION", "TUTORÍA");
        mapaEspecialidades.put("CONVIVENCIA", "TUTORÍA");

        for (Map.Entry<String, String> entry : mapaEspecialidades.entrySet()) {
            if (nombreNorm.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.toUpperCase(), Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replaceAll("[^A-Z0-9\\s]", "");
    }

    // ==================== GESTIÓN DE ESTUDIANTES (NUEVOS MÉTODOS) ====================

    @GetMapping("/estudiante/ver/{id}")
    public String verDetalleEstudiante(@PathVariable Long id, Model model) {
        Optional<Estudiante> estudianteOpt = estudianteRepository.findById(id);
        if (estudianteOpt.isPresent()) {
            Estudiante estudiante = estudianteOpt.get();
            model.addAttribute("estudiante", estudiante);

            List<Matricula> matriculas = matriculaRepository.findHistorialByEstudianteId(id);
            model.addAttribute("matriculas", matriculas);

            long matriculasActivas = matriculas.stream()
                    .filter(m -> "ACTIVA".equals(m.getEstado()))
                    .count();
            model.addAttribute("matriculasActivas", matriculasActivas);

            return "admin/estudiante-detalle";
        }
        return "redirect:/admin/estudiantes";
    }

    @GetMapping("/estudiante/cambiar-estado/{id}")
    public String cambiarEstadoEstudiante(@PathVariable Long id,
                                          @RequestParam String estado,
                                          RedirectAttributes redirectAttributes) {
        Optional<Estudiante> estudianteOpt = estudianteRepository.findById(id);
        if (estudianteOpt.isPresent()) {
            Estudiante estudiante = estudianteOpt.get();

            Optional<Matricula> matriculaOpt = matriculaRepository
                    .findMatriculaActivaByEstudianteId(estudiante.getIdEstudiante());

            estudiante.setEstado(estado);
            estudianteRepository.save(estudiante);

            if ("INACTIVO".equals(estado) && matriculaOpt.isPresent()) {
                Matricula matricula = matriculaOpt.get();
                actualizarAlumnosActualesPorGradoYTurno(matricula.getIdGrado(), matricula.getTurno());
            }

            String mensaje = "Estudiante '" + estudiante.getNombres() + "' " +
                    (estado.equals("ACTIVO") ? "habilitado" : "deshabilitado") +
                    " correctamente";
            redirectAttributes.addFlashAttribute("success", mensaje);
        } else {
            redirectAttributes.addFlashAttribute("error", "Estudiante no encontrado");
        }
        return "redirect:/admin/estudiantes";
    }

    private String obtenerNombreGrado(Integer idGrado) {
        if (idGrado == null) return "Sin asignar";

        return switch (idGrado) {
            case 1 -> "Primero de Primaria";
            case 2 -> "Segundo de Primaria";
            case 3 -> "Tercero de Primaria";
            case 4 -> "Cuarto de Primaria";
            case 5 -> "Quinto de Primaria";
            case 6 -> "Sexto de Primaria";
            case 7 -> "Primero de Secundaria";
            case 8 -> "Segundo de Secundaria";
            case 9 -> "Tercero de Secundaria";
            case 10 -> "Cuarto de Secundaria";
            case 11 -> "Quinto de Secundaria";
            default -> "Grado " + idGrado;
        };
    }

    @PostMapping("/estudiante/actualizar/{id}")
    public String actualizarEstudiante(@PathVariable Long id,
                                       @ModelAttribute Estudiante estudiante,
                                       RedirectAttributes redirectAttributes) {
        try {
            Optional<Estudiante> existente = estudianteRepository.findById(id);
            if (!existente.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Estudiante no encontrado");
                return "redirect:/admin/estudiantes";
            }

            estudiante.setIdEstudiante(id);
            estudianteRepository.save(estudiante);
            redirectAttributes.addFlashAttribute("success", "Estudiante actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/admin/estudiantes";
    }

    // ==================== REPORTES AVANZADOS ====================

    @GetMapping("/reportes")
    public String reportes(Model model) {
        // Estadísticas generales
        long totalEstudiantes = estudianteRepository.count();
        long totalDocentes = docenteRepository.count();
        long totalCursos = cursoRepository.count();
        long totalMatriculas = matriculaRepository.count();

        // Matrículas por estado
        long pendientes = matriculaRepository.findByEstado("PENDIENTE").size();
        long activas = matriculaRepository.findByEstado("ACTIVA").size();
        long inactivas = matriculaRepository.findByEstado("INACTIVO").size();

        // Matrículas por grado (1-11)
        Map<Integer, Long> matriculasPorGrado = new LinkedHashMap<>();
        for (int i = 1; i <= 11; i++) {
            long count = matriculaRepository.countByIdGrado(i);
            matriculasPorGrado.put(i, count);
        }

        // Cursos por estado
        long cursosActivos = cursoRepository.countByEstado("ACTIVO");
        long cursosInactivos = cursoRepository.countByEstado("INACTIVO");

        // Docentes por especialidad
        Map<String, Long> docentesPorEspecialidad = new LinkedHashMap<>();
        List<String> especialidades = getEspecialidadesList();
        for (String esp : especialidades) {
            long count = docenteRepository.countByEspecialidad(esp);
            docentesPorEspecialidad.put(esp, count);
        }

        // Estudiantes por género
        long masculinos = estudianteRepository.countByGenero("M");
        long femeninos = estudianteRepository.countByGenero("F");

        // Matrículas por turno
        long manana = matriculaRepository.countByTurno("MAÑANA");
        long tarde = matriculaRepository.countByTurno("TARDE");

        List<Curso> todosCursos = cursoRepository.findAll();
        model.addAttribute("cursos", todosCursos);

        model.addAttribute("totalEstudiantes", totalEstudiantes);
        model.addAttribute("totalDocentes", totalDocentes);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("totalMatriculas", totalMatriculas);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("activas", activas);
        model.addAttribute("inactivas", inactivas);
        model.addAttribute("matriculasPorGrado", matriculasPorGrado);
        model.addAttribute("cursosActivos", cursosActivos);
        model.addAttribute("cursosInactivos", cursosInactivos);
        model.addAttribute("docentesPorEspecialidad", docentesPorEspecialidad);
        model.addAttribute("masculinos", masculinos);
        model.addAttribute("femeninos", femeninos);
        model.addAttribute("manana", manana);
        model.addAttribute("tarde", tarde);
        model.addAttribute("modulo", "reportes");

        return "admin/reportes";
    }

// ==================== ENDPOINTS PARA GRÁFICOS ====================

    @GetMapping("/reportes/estudiantes-por-grado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteEstudiantesPorGrado() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> data = new LinkedHashMap<>();

        for (int i = 1; i <= 11; i++) {
            String nombreGrado = obtenerNombreGrado(i);
            long count = estudianteRepository.countByIdGrado(i);
            data.put(nombreGrado, count);
        }

        response.put("labels", data.keySet());
        response.put("values", data.values());
        response.put("total", data.values().stream().mapToLong(Long::longValue).sum());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reportes/matriculas-por-anio")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteMatriculasPorAnio() {
        Map<String, Object> response = new HashMap<>();
        Map<Integer, Long> data = new LinkedHashMap<>();

        int anioActual = java.time.Year.now().getValue();
        for (int i = anioActual - 4; i <= anioActual; i++) {
            long count = matriculaRepository.countByAnioAcademico(String.valueOf(i));
            data.put(i, count);
        }

        response.put("labels", data.keySet());
        response.put("values", data.values());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reportes/matriculas-por-grado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteMatriculasPorGrado() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> data = new LinkedHashMap<>();

        for (int i = 1; i <= 11; i++) {
            String nombreGrado = obtenerNombreGrado(i);
            long count = matriculaRepository.countByIdGrado(i);
            data.put(nombreGrado, count);
        }

        response.put("labels", data.keySet());
        response.put("values", data.values());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reportes/cursos-por-grado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteCursosPorGrado() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> data = new LinkedHashMap<>();

        for (int i = 1; i <= 11; i++) {
            String nombreGrado = obtenerNombreGrado(i);
            long count = cursoRepository.countByIdGrado(i);
            data.put(nombreGrado, count);
        }

        response.put("labels", data.keySet());
        response.put("values", data.values());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reportes/docentes-por-especialidad")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteDocentesPorEspecialidad() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> data = new LinkedHashMap<>();

        List<String> especialidades = getEspecialidadesList();
        for (String esp : especialidades) {
            long count = docenteRepository.countByEspecialidad(esp);
            data.put(esp, count);
        }

        response.put("labels", data.keySet());
        response.put("values", data.values());
        response.put("total", data.values().stream().mapToLong(Long::longValue).sum());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reportes/estudiantes-por-genero")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteEstudiantesPorGenero() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> data = new LinkedHashMap<>();

        data.put("Masculino", estudianteRepository.countByGenero("M"));
        data.put("Femenino", estudianteRepository.countByGenero("F"));

        response.put("data", data);
        response.put("total", data.values().stream().mapToLong(Long::longValue).sum());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reportes/matriculas-por-turno")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteMatriculasPorTurno() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> data = new LinkedHashMap<>();

        data.put("Mañana", matriculaRepository.countByTurno("MAÑANA"));
        data.put("Tarde", matriculaRepository.countByTurno("TARDE"));

        response.put("data", data);

        return ResponseEntity.ok(response);
    }

// ==================== REPORTE DE ASISTENCIA ====================

    @GetMapping("/reportes/asistencia-curso/{idCurso}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteAsistenciaCurso(@PathVariable Long idCurso) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> data = new LinkedHashMap<>();

        List<Asistencia> asistencias = asistenciaRepository.findByCursoIdCurso(idCurso);

        long presente = asistencias.stream().filter(a -> "PRESENTE".equals(a.getEstado())).count();
        long ausente = asistencias.stream().filter(a -> "AUSENTE".equals(a.getEstado())).count();
        long tardanza = asistencias.stream().filter(a -> "TARDANZA".equals(a.getEstado())).count();
        long justificado = asistencias.stream().filter(a -> "JUSTIFICADO".equals(a.getEstado())).count();

        data.put("Presente", presente);
        data.put("Ausente", ausente);
        data.put("Tardanza", tardanza);
        data.put("Justificado", justificado);

        response.put("data", data);
        response.put("total", asistencias.size());

        return ResponseEntity.ok(response);
    }

// ==================== REPORTE DE NOTAS ====================

    @GetMapping("/reportes/notas-curso/{idCurso}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reporteNotasCurso(@PathVariable Long idCurso) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Long> data = new LinkedHashMap<>();

        String periodoActual = String.valueOf(java.time.Year.now().getValue());

        List<Nota> notas = notaRepository.findByCursoAndPeriodoAcademico(idCurso, periodoActual);

        long aprobados = notas.stream().filter(n -> n.getNota().doubleValue() >= 11).count();
        long recuperacion = notas.stream().filter(n -> n.getNota().doubleValue() >= 7 && n.getNota().doubleValue() < 11).count();
        long desaprobados = notas.stream().filter(n -> n.getNota().doubleValue() < 7).count();

        data.put("Aprobados", aprobados);
        data.put("Recuperación", recuperacion);
        data.put("Desaprobados", desaprobados);

        response.put("data", data);
        response.put("total", notas.size());

        return ResponseEntity.ok(response);
    }

// ==================== EXPORTAR A EXCEL ====================

    @GetMapping("/reportes/exportar-excel/{tipo}")
    @ResponseBody
    public ResponseEntity<byte[]> exportarExcel(@PathVariable String tipo) {
        try {
            // Crear libro de trabajo
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Reporte " + tipo);

            // Crear estilos
            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeight(12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowNum = 0;

            switch (tipo) {
                case "estudiantes":
                    rowNum = exportarEstudiantesExcel(sheet, headerStyle, dataStyle);
                    break;
                case "docentes":
                    rowNum = exportarDocentesExcel(sheet, headerStyle, dataStyle);
                    break;
                case "cursos":
                    rowNum = exportarCursosExcel(sheet, headerStyle, dataStyle);
                    break;
                case "matriculas":
                    rowNum = exportarMatriculasExcel(sheet, headerStyle, dataStyle);
                    break;
                default:
                    rowNum = exportarEstudiantesExcel(sheet, headerStyle, dataStyle);
            }

            // Autoajustar columnas
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            // Crear respuesta
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "reporte_" + tipo + ".xlsx");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private int exportarEstudiantesExcel(XSSFSheet sheet, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = 0;

        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE ESTUDIANTES");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        // Encabezados
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Código", "DNI", "Nombres", "Apellidos", "Grado"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        List<Estudiante> estudiantes = estudianteRepository.findAll();
        for (Estudiante e : estudiantes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(e.getIdEstudiante());
            row.createCell(1).setCellValue(e.getCodigoEstudiante());
            row.createCell(2).setCellValue(e.getDni());
            row.createCell(3).setCellValue(e.getNombres());
            row.createCell(4).setCellValue(e.getApellidoPaterno() + " " + e.getApellidoMaterno());
            row.createCell(5).setCellValue(obtenerNombreGrado(e.getIdGrado()));

            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        return rowNum;
    }

    private int exportarDocentesExcel(XSSFSheet sheet, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE DOCENTES");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Código", "DNI", "Nombres", "Apellidos", "Email", "Especialidad"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Docente> docentes = docenteRepository.findAll();
        for (Docente d : docentes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(d.getIdDocente());
            row.createCell(1).setCellValue(d.getCodigoDocente());
            row.createCell(2).setCellValue(d.getDni());
            row.createCell(3).setCellValue(d.getNombres());
            row.createCell(4).setCellValue(d.getApellidoPaterno() + " " + d.getApellidoMaterno());
            row.createCell(5).setCellValue(d.getEmail());
            row.createCell(6).setCellValue(d.getEspecialidad());

            for (int i = 0; i < 7; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        return rowNum;
    }

    private int exportarCursosExcel(XSSFSheet sheet, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE CURSOS");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Código", "Nombre", "Grado", "Sección", "Turno", "Área", "Docente"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Curso> cursos = cursoRepository.findAllWithDocente();
        for (Curso c : cursos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(c.getIdCurso());
            row.createCell(1).setCellValue(c.getCodigoCurso());
            row.createCell(2).setCellValue(c.getNombreCurso());
            row.createCell(3).setCellValue(obtenerNombreGrado(c.getIdGrado()));
            row.createCell(4).setCellValue(c.getSeccion());
            row.createCell(5).setCellValue(c.getTurno());
            row.createCell(6).setCellValue(c.getArea());
            row.createCell(7).setCellValue(c.getDocente() != null ?
                    c.getDocente().getNombres() + " " + c.getDocente().getApellidoPaterno() : "Sin asignar");

            for (int i = 0; i < 8; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        return rowNum;
    }

    private int exportarMatriculasExcel(XSSFSheet sheet, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE MATRÍCULAS");
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Código", "Estudiante", "Grado", "Sección", "Turno", "Estado"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Matricula> matriculas = matriculaRepository.findAll();
        for (Matricula m : matriculas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(m.getIdMatricula());
            row.createCell(1).setCellValue(m.getCodigoMatricula());
            row.createCell(2).setCellValue(m.getEstudiante() != null ?
                    m.getEstudiante().getNombres() + " " + m.getEstudiante().getApellidoPaterno() : "N/A");
            row.createCell(3).setCellValue(obtenerNombreGrado(m.getIdGrado()));
            row.createCell(4).setCellValue(m.getSeccion());
            row.createCell(5).setCellValue(m.getTurno());
            row.createCell(6).setCellValue(m.getEstado());

            for (int i = 0; i < 7; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        return rowNum;
    }

// ==================== EXPORTAR A PDF (iText) ====================

    @GetMapping("/reportes/exportar-pdf/{tipo}")
    @ResponseBody
    public ResponseEntity<byte[]> exportarPDF(@PathVariable String tipo) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Agregar título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("I.E. San Carlos - Reporte " + tipo, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Agregar fecha
            Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            Paragraph date = new Paragraph("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(new Paragraph(" "));

            // Tabla según tipo
            PdfPTable table;
            switch (tipo) {
                case "estudiantes":
                    table = crearTablaEstudiantesPDF();
                    break;
                case "docentes":
                    table = crearTablaDocentesPDF();
                    break;
                case "cursos":
                    table = crearTablaCursosPDF();
                    break;
                case "matriculas":
                    table = crearTablaMatriculasPDF();
                    break;
                default:
                    table = crearTablaEstudiantesPDF();
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "reporte_" + tipo + ".pdf");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private PdfPTable crearTablaEstudiantesPDF() {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        // Encabezados
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        String[] headers = {"ID", "Código", "DNI", "Nombres", "Apellidos", "Grado"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Datos
        Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        List<Estudiante> estudiantes = estudianteRepository.findAll();
        for (Estudiante e : estudiantes) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(e.getIdEstudiante() != null ? e.getIdEstudiante() : ""), dataFont)));
            table.addCell(new PdfPCell(new Phrase(e.getCodigoEstudiante() != null ? e.getCodigoEstudiante() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(e.getDni() != null ? e.getDni() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(e.getNombres() != null ? e.getNombres() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(
                    (e.getApellidoPaterno() != null ? e.getApellidoPaterno() : "") + " " +
                            (e.getApellidoMaterno() != null ? e.getApellidoMaterno() : ""),
                    dataFont)));
            table.addCell(new PdfPCell(new Phrase(obtenerNombreGrado(e.getIdGrado()), dataFont)));
        }

        return table;
    }

    private PdfPTable crearTablaDocentesPDF() {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        String[] headers = {"ID", "Código", "DNI", "Nombres", "Apellidos", "Especialidad"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        List<Docente> docentes = docenteRepository.findAll();
        for (Docente d : docentes) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(d.getIdDocente() != null ? d.getIdDocente() : ""), dataFont)));
            table.addCell(new PdfPCell(new Phrase(d.getCodigoDocente() != null ? d.getCodigoDocente() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(d.getDni() != null ? d.getDni() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(d.getNombres() != null ? d.getNombres() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(
                    (d.getApellidoPaterno() != null ? d.getApellidoPaterno() : "") + " " +
                            (d.getApellidoMaterno() != null ? d.getApellidoMaterno() : ""),
                    dataFont)));
            table.addCell(new PdfPCell(new Phrase(d.getEspecialidad() != null ? d.getEspecialidad() : "", dataFont)));
        }

        return table;
    }

    private PdfPTable crearTablaCursosPDF() {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        String[] headers = {"ID", "Nombre", "Grado", "Sección", "Turno", "Área"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        List<Curso> cursos = cursoRepository.findAllWithDocente();
        for (Curso c : cursos) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(c.getIdCurso() != null ? c.getIdCurso() : ""), dataFont)));
            table.addCell(new PdfPCell(new Phrase(c.getNombreCurso() != null ? c.getNombreCurso() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(obtenerNombreGrado(c.getIdGrado()), dataFont)));
            table.addCell(new PdfPCell(new Phrase(c.getSeccion() != null ? c.getSeccion() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(c.getTurno() != null ? c.getTurno() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(c.getArea() != null ? c.getArea() : "", dataFont)));
        }

        return table;
    }

    private PdfPTable crearTablaMatriculasPDF() {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        String[] headers = {"ID", "Código", "Estudiante", "Grado", "Sección", "Estado"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        List<Matricula> matriculas = matriculaRepository.findAll();
        for (Matricula m : matriculas) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(m.getIdMatricula() != null ? m.getIdMatricula() : ""), dataFont)));
            table.addCell(new PdfPCell(new Phrase(m.getCodigoMatricula() != null ? m.getCodigoMatricula() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(
                    m.getEstudiante() != null ?
                            (m.getEstudiante().getNombres() != null ? m.getEstudiante().getNombres() : "") + " " +
                                    (m.getEstudiante().getApellidoPaterno() != null ? m.getEstudiante().getApellidoPaterno() : "")
                            : "N/A",
                    dataFont)));
            table.addCell(new PdfPCell(new Phrase(obtenerNombreGrado(m.getIdGrado()), dataFont)));
            table.addCell(new PdfPCell(new Phrase(m.getSeccion() != null ? m.getSeccion() : "", dataFont)));
            table.addCell(new PdfPCell(new Phrase(m.getEstado() != null ? m.getEstado() : "", dataFont)));
        }

        return table;
    }
    }