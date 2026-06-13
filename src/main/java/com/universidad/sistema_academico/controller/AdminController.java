package com.universidad.sistema_academico.controller;

import com.universidad.sistema_academico.model.*;
import com.universidad.sistema_academico.repository.ActividadRepository;
import com.universidad.sistema_academico.repository.CursoRepository;
import com.universidad.sistema_academico.repository.DocenteRepository;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import com.universidad.sistema_academico.repository.MatriculaRepository;
import com.universidad.sistema_academico.service.SolicitudMatriculaService;
import com.universidad.sistema_academico.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Value("${voucher.upload.directory:uploads/vouchers}")
    private String uploadDirectory;

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
    public String listarEstudiantes(Model model) {
        model.addAttribute("estudiantes", estudianteRepository.findAll());
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

    @PostMapping("/estudiante/actualizar/{id}")
    public String actualizarEstudiante(@PathVariable Long id, @ModelAttribute Estudiante estudiante, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        String detalles = "Se actualizó estudiante: " + estudiante.getNombres() + " " + estudiante.getApellidoPaterno();

        estudiante.setIdEstudiante(id);
        estudianteRepository.save(estudiante);
        registrarActividad(usuario, "EDITAR", "Estudiante", detalles);
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
    public String listarDocentes(Model model) {
        model.addAttribute("docentes", docenteRepository.findAll());
        return "admin/docentes";
    }

    @GetMapping("/docente/nuevo")
    public String mostrarFormularioNuevoDocente(Model model) {
        model.addAttribute("docente", new Docente());
        return "admin/docente-form";
    }

    @PostMapping("/docente/guardar")
    public String guardarDocente(@ModelAttribute Docente docente, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        String detalles = "Se registró docente: " + docente.getNombres() + " " + docente.getApellidoPaterno() + " " + docente.getApellidoMaterno();

        docenteRepository.save(docente);
        registrarActividad(usuario, "CREAR", "Docente", detalles);
        return "redirect:/admin/docentes";
    }

    @GetMapping("/docente/editar/{id}")
    public String mostrarFormularioEditarDocente(@PathVariable Long id, Model model) {
        Optional<Docente> docente = docenteRepository.findById(id);
        if (docente.isPresent()) {
            model.addAttribute("docente", docente.get());
            return "admin/docente-form";
        }
        return "redirect:/admin/docentes";
    }

    @PostMapping("/docente/actualizar/{id}")
    public String actualizarDocente(@PathVariable Long id, @ModelAttribute Docente docente, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        String detalles = "Se actualizó docente: " + docente.getNombres() + " " + docente.getApellidoPaterno();

        docente.setIdDocente(id);
        docenteRepository.save(docente);
        registrarActividad(usuario, "EDITAR", "Docente", detalles);
        return "redirect:/admin/docentes";
    }

    @GetMapping("/docente/eliminar/{id}")
    public String eliminarDocente(@PathVariable Long id, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        Optional<Docente> docente = docenteRepository.findById(id);

        if (docente.isPresent()) {
            String detalles = "Se eliminó docente: " + docente.get().getNombres() + " " + docente.get().getApellidoPaterno();
            docenteRepository.deleteById(id);
            registrarActividad(usuario, "ELIMINAR", "Docente", detalles);
        }
        return "redirect:/admin/docentes";
    }

    // ==================== CRUD CURSOS ====================

    @GetMapping("/cursos")
    public String listarCursos(Model model) {
        // Usar el método que carga los cursos con sus docentes
        List<Curso> cursos = cursoRepository.findAllWithDocente();

        model.addAttribute("cursos", cursos);
        model.addAttribute("docentes", docenteRepository.findAll());
        return "admin/cursos";
    }

    @GetMapping("/curso/nuevo")
    public String mostrarFormularioNuevoCurso(Model model) {
        model.addAttribute("curso", new Curso());
        model.addAttribute("docentes", docenteRepository.findAll());
        return "admin/curso-form";
    }

    @PostMapping("/curso/guardar")
    public String guardarCurso(@ModelAttribute Curso curso, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        String detalles = "Se registró curso: " + curso.getNombreCurso() + " (Código: " + curso.getCodigoCurso() + ")";

        cursoRepository.save(curso);
        registrarActividad(usuario, "CREAR", "Curso", detalles);
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
    public String actualizarCurso(@PathVariable Long id, @ModelAttribute Curso curso, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        String detalles = "Se actualizó curso: " + curso.getNombreCurso();

        curso.setIdCurso(id);
        cursoRepository.save(curso);
        registrarActividad(usuario, "EDITAR", "Curso", detalles);
        return "redirect:/admin/cursos";
    }

    @GetMapping("/curso/eliminar/{id}")
    public String eliminarCurso(@PathVariable Long id, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        Optional<Curso> curso = cursoRepository.findById(id);

        if (curso.isPresent()) {
            String detalles = "Se eliminó curso: " + curso.get().getNombreCurso();
            cursoRepository.deleteById(id);
            registrarActividad(usuario, "ELIMINAR", "Curso", detalles);
        }
        return "redirect:/admin/cursos";
    }

    // ==================== CRUD MATRÍCULAS ====================

    @GetMapping("/matriculas")
    public String listarMatriculas(Model model) {
        model.addAttribute("matriculas", matriculaRepository.findAll());
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        model.addAttribute("cursos", cursoRepository.findAll());
        return "admin/matriculas";
    }

    @GetMapping("/matricula/nuevo")
    public String mostrarFormularioNuevaMatricula(Model model) {
        model.addAttribute("matricula", new Matricula());
        model.addAttribute("estudiantes", estudianteRepository.findAll());
        model.addAttribute("cursos", cursoRepository.findAll());
        return "admin/matricula-form";
    }

    @PostMapping("/matricula/guardar")
    public String guardarMatricula(@ModelAttribute Matricula matricula, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        String detalles = "Se registró nueva matrícula para estudiante ID: " + matricula.getEstudiante().getIdEstudiante();

        matriculaRepository.save(matricula);
        registrarActividad(usuario, "CREAR", "Matrícula", detalles);
        return "redirect:/admin/matriculas";
    }

    @GetMapping("/matricula/editar/{id}")
    public String mostrarFormularioEditarMatricula(@PathVariable Long id, Model model) {
        Optional<Matricula> matricula = matriculaRepository.findById(id);
        if (matricula.isPresent()) {
            model.addAttribute("matricula", matricula.get());
            model.addAttribute("estudiantes", estudianteRepository.findAll());
            model.addAttribute("cursos", cursoRepository.findAll());
            return "admin/matricula-form";
        }
        return "redirect:/admin/matriculas";
    }

    @PostMapping("/matricula/actualizar/{id}")
    public String actualizarMatricula(@PathVariable Long id, @ModelAttribute Matricula matricula, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        String detalles = "Se actualizó matrícula ID: " + id;

        matricula.setIdMatricula(id);
        matriculaRepository.save(matricula);
        registrarActividad(usuario, "EDITAR", "Matrícula", detalles);
        return "redirect:/admin/matriculas";
    }

    @GetMapping("/matricula/eliminar/{id}")
    public String eliminarMatricula(@PathVariable Long id, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";
        String detalles = "Se eliminó matrícula ID: " + id;

        matriculaRepository.deleteById(id);
        registrarActividad(usuario, "ELIMINAR", "Matrícula", detalles);
        return "redirect:/admin/matriculas";
    }

    // ==================== GESTIÓN DE SOLICITUDES DE MATRÍCULA ====================

    @Autowired
    private SolicitudMatriculaService solicitudMatriculaService;

    @GetMapping("/solicitudes")
    public String listarSolicitudes(Model model) {
        model.addAttribute("pendientes", solicitudMatriculaService.listarPendientes());
        model.addAttribute("aprobadas", solicitudMatriculaService.listarAprobadas());
        model.addAttribute("rechazadas", solicitudMatriculaService.listarRechazadas());
        return "admin/solicitudes";
    }

    @GetMapping("/solicitud/{id}")
    public String verDetalleSolicitud(@PathVariable Long id, Model model) {
        SolicitudMatricula solicitud = solicitudMatriculaService.buscarPorId(id);

        // Extraer solo el nombre del archivo del voucher
        String voucherFilename = "";
        if (solicitud.getVoucherPath() != null && !solicitud.getVoucherPath().isEmpty()) {
            String fullPath = solicitud.getVoucherPath();
            // Limpiar la ruta: reemplazar \ por / y luego extraer el nombre
            String cleanPath = fullPath.replace("\\", "/");
            voucherFilename = cleanPath.substring(cleanPath.lastIndexOf("/") + 1);

            System.out.println("=== VOUCHER DEBUG ===");
            System.out.println("Path original: " + fullPath);
            System.out.println("Path limpio: " + cleanPath);
            System.out.println("Nombre archivo: " + voucherFilename);
        }

        model.addAttribute("solicitud", solicitud);
        model.addAttribute("voucherFilename", voucherFilename);
        return "admin/solicitud-detalle";
    }

    @PostMapping("/solicitud/aprobar/{id}")
    public String aprobarSolicitud(@PathVariable Long id, Authentication auth) {
        String email = auth != null ? auth.getName() : "admin";

        // Buscar por EMAIL, no por username
        Usuario admin = usuarioService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado con email: " + email));

        solicitudMatriculaService.aprobarSolicitud(id, admin.getId());

        registrarActividad(email, "APROBAR", "SolicitudMatricula",
                "Se aprobó la solicitud de matrícula ID: " + id);

        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/solicitud/rechazar/{id}")
    public String rechazarSolicitud(@PathVariable Long id,
                                    @RequestParam String motivo,
                                    Authentication auth) {
        String email = auth != null ? auth.getName() : "admin";

        // Buscar por EMAIL
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
}