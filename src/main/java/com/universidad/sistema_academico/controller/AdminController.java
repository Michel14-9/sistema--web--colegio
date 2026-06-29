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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.text.Normalizer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.universidad.sistema_academico.service.EmailService;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private EmailService emailService;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        return "admin/docente-form";
    }

    @PostMapping("/docente/guardar")
    public String guardarDocente(@ModelAttribute Docente docente,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        String usuario = auth != null ? auth.getName() : "sistema";

        try {
            // 1. Validar DNI único
            if (docenteRepository.existsByDni(docente.getDni())) {
                redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el DNI: " + docente.getDni());
                return "redirect:/admin/docentes";
            }

            // 2. Guardar el email personal ANTES de sobrescribirlo
            String emailPersonal = docente.getEmail(); // <-- Este es el email que el admin ingresó en el formulario

            // 3. Validar que el email personal no esté vacío
            if (emailPersonal == null || emailPersonal.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "✗ El email del docente es obligatorio");
                return "redirect:/admin/docentes";
            }

            // 4. Validar que el email personal no exista ya en la base de datos
            if (docenteRepository.existsByEmail(emailPersonal)) {
                redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el email: " + emailPersonal);
                return "redirect:/admin/docentes";
            }

            // 5. Generar email institucional automáticamente
            String emailInstitucional = generarEmailDocente(docente.getNombres(),
                    docente.getApellidoPaterno(),
                    docente.getApellidoMaterno());

            // 6. Validar que el email institucional no exista
            if (docenteRepository.existsByEmail(emailInstitucional)) {
                redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el email institucional: " + emailInstitucional);
                return "redirect:/admin/docentes";
            }

            // 7. Validar celular (9 dígitos)
            if (docente.getCelular() != null && !docente.getCelular().isEmpty() &&
                    docente.getCelular().length() != 9) {
                redirectAttributes.addFlashAttribute("error", "✗ El número de celular debe tener 9 dígitos");
                return "redirect:/admin/docentes";
            }

            // 8. ASIGNAR EMAIL INSTITUCIONAL (sobrescribe el email personal)
            docente.setEmail(emailInstitucional);

            // Generar código automático si está vacío
            if (docente.getCodigoDocente() == null || docente.getCodigoDocente().isEmpty()) {
                docente.generarCodigoAutomatico();
            }

            if (docente.getEstado() == null) docente.setEstado("ACTIVO");

            // Guardar docente
            docenteRepository.save(docente);

            // ========== CREAR USUARIO PARA EL DOCENTE ==========
            String passwordTemporal = generarPasswordTemporal();

            Usuario usuarioDocente = new Usuario();
            usuarioDocente.setUsername(emailInstitucional);  // Username = email institucional
            usuarioDocente.setPassword(passwordEncoder.encode(passwordTemporal));
            usuarioDocente.setNombre(docente.getNombres());
            usuarioDocente.setApellido(docente.getApellidoPaterno() + " " + docente.getApellidoMaterno());
            usuarioDocente.setEmail(emailInstitucional);      // Email = email institucional
            usuarioDocente.setRol("DOCENTE");
            usuarioDocente.setActivo(true);
            usuarioDocente.setFechaRegistro(LocalDateTime.now());

            usuarioService.save(usuarioDocente);

            // Asociar el usuario al docente
            docente.setUsuario(usuarioDocente);
            docenteRepository.save(docente);

            // ========== ENVIAR CREDENCIALES POR EMAIL ==========
            // Enviar al EMAIL PERSONAL del docente (el que el admin ingresó)
            emailService.enviarCredencialesDocente(
                    emailPersonal,              // Email personal (destino)
                    emailInstitucional,         // Username (email institucional)
                    passwordTemporal,           // Contraseña temporal
                    docente.getNombres()        // Nombre del docente
            );

            registrarActividad(usuario, "CREAR", "Docente", "Se registró docente: " + docente.getNombres() +
                    " - Email institucional: " + emailInstitucional +
                    " - Email personal: " + emailPersonal);

            redirectAttributes.addFlashAttribute("success", "✅ Docente '" + docente.getNombres() +
                    "' creado correctamente. Email institucional: " + emailInstitucional +
                    ". Se enviaron credenciales a su correo personal.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "✗ Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/docentes";
    }

    /**
     * Generar email institucional para docente
     */
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

        // Si el email base ya existe, agregar un número
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
            docente.setIdDocente(id);

            // 1. Validar DNI único (excluyendo el actual)
            if (!docente.getDni().equals(docenteOriginal.getDni()) &&
                    docenteRepository.existsByDni(docente.getDni())) {
                redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el DNI: " + docente.getDni());
                return "redirect:/admin/docentes";
            }

            // 2. Validar Email único (excluyendo el actual)
            if (docente.getEmail() != null && !docente.getEmail().isEmpty() &&
                    !docente.getEmail().equals(docenteOriginal.getEmail()) &&
                    docenteRepository.existsByEmail(docente.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el email: " + docente.getEmail());
                return "redirect:/admin/docentes";
            }

            // 3. Si se actualizó el usuario asociado al docente
            if (docente.getUsuario() != null && docente.getUsuario().getId() != null) {
                Optional<Usuario> usuarioOpt = usuarioService.findById(docente.getUsuario().getId());
                if (usuarioOpt.isPresent()) {
                    Usuario usuarioDocente = usuarioOpt.get();
                    usuarioDocente.setNombre(docente.getNombres());
                    usuarioDocente.setApellido(docente.getApellidoPaterno() + " " + docente.getApellidoMaterno());
                    usuarioDocente.setEmail(docente.getEmail());
                    usuarioDocente.setUsername(docente.getEmail());

                    // Si se envió una nueva contraseña, hashearla
                    if (docente.getUsuario().getPassword() != null &&
                            !docente.getUsuario().getPassword().isEmpty() &&
                            !docente.getUsuario().getPassword().startsWith("$2a$")) {
                        usuarioDocente.setPassword(passwordEncoder.encode(docente.getUsuario().getPassword()));
                    }

                    usuarioService.save(usuarioDocente);
                    docente.setUsuario(usuarioDocente);
                }
            }

            docenteRepository.save(docente);
            registrarActividad(usuario, "EDITAR", "Docente", "Se actualizó docente: " + docente.getNombres());
            redirectAttributes.addFlashAttribute("success", "✅ Docente '" + docente.getNombres() + "' actualizado correctamente");

        } catch (Exception e) {
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

        return "admin/cursos";
    }

    @GetMapping("/curso/nuevo")
    public String mostrarFormularioNuevoCurso(Model model) {
        model.addAttribute("curso", new Curso());
        model.addAttribute("docentes", docenteRepository.findAll());
        return "admin/curso-form";
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

            if (curso.getCapacidadMaxima() == null) curso.setCapacidadMaxima(36);
            if (curso.getAlumnosActuales() == null) curso.setAlumnosActuales(0);

            cursoRepository.save(curso);
            registrarActividad(usuario, "CREAR", "Curso", "Se registró curso: " + curso.getNombreCurso());
            redirectAttributes.addFlashAttribute("success", "✅ Curso '" + curso.getNombreCurso() + "' creado correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "✗ Error al guardar: " + e.getMessage());
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

            cursoRepository.save(curso);
            registrarActividad(usuario, "EDITAR", "Curso", "Se actualizó curso: " + curso.getNombreCurso());
            redirectAttributes.addFlashAttribute("success", "✅ Curso '" + curso.getNombreCurso() + "' actualizado correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "✗ Error al actualizar: " + e.getMessage());
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

        matriculaRepository.save(matricula);
        actualizarAlumnosActualesPorGradoYTurno(matricula.getIdGrado(), matricula.getTurno());

        String detalles = "Se registró nueva matrícula para estudiante ID: " +
                matricula.getEstudiante().getIdEstudiante() +
                " - Grado: " + matricula.getIdGrado() +
                " - Turno: " + matricula.getTurno();

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

        matricula.setIdMatricula(id);
        matriculaRepository.save(matricula);
        actualizarAlumnosActualesPorGradoYTurno(matricula.getIdGrado(), matricula.getTurno());

        String detalles = "Se actualizó matrícula ID: " + id +
                " - Grado: " + matricula.getIdGrado() +
                " - Turno: " + matricula.getTurno();

        registrarActividad(usuario, "EDITAR", "Matrícula", detalles);
        return "redirect:/admin/matriculas";
    }

    @GetMapping("/matricula/eliminar/{id}")
    public String eliminarMatricula(@PathVariable Long id, Authentication auth) {
        String usuario = auth != null ? auth.getName() : "sistema";

        Optional<Matricula> matriculaOpt = matriculaRepository.findById(id);
        if (matriculaOpt.isPresent()) {
            Matricula matricula = matriculaOpt.get();
            Integer idGrado = matricula.getIdGrado();
            String turno = matricula.getTurno();

            matriculaRepository.deleteById(id);
            actualizarAlumnosActualesPorGradoYTurno(idGrado, turno);

            String detalles = "Se eliminó matrícula ID: " + id;
            registrarActividad(usuario, "ELIMINAR", "Matrícula", detalles);
        }

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

        String voucherFilename = "";
        if (solicitud.getVoucherPath() != null && !solicitud.getVoucherPath().isEmpty()) {
            String fullPath = solicitud.getVoucherPath();
            String cleanPath = fullPath.replace("\\", "/");
            voucherFilename = cleanPath.substring(cleanPath.lastIndexOf("/") + 1);
        }

        model.addAttribute("solicitud", solicitud);
        model.addAttribute("voucherFilename", voucherFilename);
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
}