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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // ==================== CRUD DOCENTES MEJORADO ====================

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

        // Limpiar filtros vacíos
        if (filtroNombre != null && filtroNombre.isEmpty()) filtroNombre = null;
        if (filtroEspecialidad != null && filtroEspecialidad.isEmpty()) filtroEspecialidad = null;
        if (filtroEstado != null && filtroEstado.isEmpty()) filtroEstado = null;

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, 10);

        org.springframework.data.domain.Page<Docente> docentesPage = docenteRepository.findWithFilters(
                filtroNombre, filtroEspecialidad, filtroEstado, pageable);

        System.out.println("Total elementos: " + docentesPage.getTotalElements());
        System.out.println("Total páginas: " + docentesPage.getTotalPages());

        // Obtener lista de especialidades únicas para el filtro
        List<String> especialidades = docenteRepository.findAllActive().stream()
                .map(Docente::getEspecialidad)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("docentes", docentesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", docentesPage.getTotalPages());
        model.addAttribute("totalItems", docentesPage.getTotalElements());
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

            // 2. Validar Email único
            if (docente.getEmail() != null && !docente.getEmail().isEmpty() &&
                    docenteRepository.existsByEmail(docente.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "✗ Ya existe un docente con el email: " + docente.getEmail());
                return "redirect:/admin/docentes";
            }

            // Generar código automático si está vacío
            if (docente.getCodigoDocente() == null || docente.getCodigoDocente().isEmpty()) {
                docente.generarCodigoAutomatico();
            }

            if (docente.getEstado() == null) docente.setEstado("ACTIVO");

            docenteRepository.save(docente);
            registrarActividad(usuario, "CREAR", "Docente", "Se registró docente: " + docente.getNombres() + " " + docente.getApellidoPaterno());
            redirectAttributes.addFlashAttribute("success", "✅ Docente '" + docente.getNombres() + "' creado correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "✗ Error al guardar: " + e.getMessage());
        }

        return "redirect:/admin/docentes";
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

            // Validar que no tenga cursos asignados activos
            List<Curso> cursosAsignados = cursoRepository.findByIdDocenteAndEstado(id, "ACTIVO");
            if (!cursosAsignados.isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "❌ No se puede eliminar al docente '" + nombreDocente +
                                "' porque tiene " + cursosAsignados.size() + " curso(s) asignado(s)");
                return "redirect:/admin/docentes";
            }

            // Soft Delete
            Docente docenteParaEliminar = docente.get();
            docenteParaEliminar.setEliminado(true);
            docenteParaEliminar.setEstado("INACTIVO");
            docenteRepository.save(docenteParaEliminar);

            registrarActividad(usuario, "ELIMINAR", "Docente", "Se eliminó docente: " + nombreDocente);
            redirectAttributes.addFlashAttribute("success", "✅ Docente '" + nombreDocente + "' eliminado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ Error: Docente no encontrado");
        }

        return "redirect:/admin/docentes";
    }

    // Método auxiliar para obtener lista de especialidades
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

        // ========== DEPURACIÓN: Mostrar parámetros recibidos ==========
        System.out.println("=== PAGINACIÓN DEBUG ===");
        System.out.println("Página solicitada: " + page);
        System.out.println("filtroGrado recibido: " + filtroGrado);
        System.out.println("filtroTurno recibido: " + filtroTurno);
        System.out.println("filtroArea recibido: " + filtroArea);
        System.out.println("filtroEstado recibido: " + filtroEstado);
        System.out.println("filtroNombre recibido: " + filtroNombre);

        // Limpiar filtros vacíos
        if (filtroGrado != null && filtroGrado == 0) filtroGrado = null;
        if (filtroTurno != null && filtroTurno.isEmpty()) filtroTurno = null;
        if (filtroArea != null && filtroArea.isEmpty()) filtroArea = null;
        if (filtroEstado != null && filtroEstado.isEmpty()) filtroEstado = null;
        if (filtroNombre != null && filtroNombre.isEmpty()) filtroNombre = null;

        System.out.println("=== FILTROS DESPUÉS DE LIMPIAR ===");
        System.out.println("filtroGrado: " + filtroGrado);
        System.out.println("filtroTurno: " + filtroTurno);
        System.out.println("filtroArea: " + filtroArea);
        System.out.println("filtroEstado: " + filtroEstado);
        System.out.println("filtroNombre: " + filtroNombre);

        // Configurar paginación (10 cursos por página)
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, 10);

        // Aplicar filtros con paginación
        org.springframework.data.domain.Page<Curso> cursosPage = cursoRepository.findWithFilters(
                filtroGrado,
                filtroTurno,
                filtroArea,
                filtroEstado,
                filtroNombre,
                pageable
        );

        // ========== DEPURACIÓN: Mostrar resultados ==========
        System.out.println("=== RESULTADOS DE LA CONSULTA ===");
        System.out.println("Total de elementos en BD: " + cursosPage.getTotalElements());
        System.out.println("Total de páginas: " + cursosPage.getTotalPages());
        System.out.println("Elementos en página actual: " + cursosPage.getNumberOfElements());
        System.out.println("Contenido size: " + cursosPage.getContent().size());
        System.out.println("¿Tiene página anterior? " + cursosPage.hasPrevious());
        System.out.println("¿Tiene página siguiente? " + cursosPage.hasNext());
        System.out.println("=====================================");

        // Estadísticas para el dashboard de cursos
        List<Curso> todosCursos = cursoRepository.findAllWithDocente();
        long totalCursos = todosCursos.size();
        long cursosActivos = todosCursos.stream().filter(c -> "ACTIVO".equals(c.getEstado())).count();
        long cursosInactivos = todosCursos.stream().filter(c -> "INACTIVO".equals(c.getEstado())).count();
        long cursosSinDocente = todosCursos.stream().filter(c -> c.getIdDocente() == null || c.getIdDocente() == 0).count();
        int totalCupos = todosCursos.stream().mapToInt(c -> c.getCapacidadMaxima() != null ? c.getCapacidadMaxima() : 36).sum();

        // Agregar atributos al modelo
        model.addAttribute("cursos", cursosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", cursosPage.getTotalPages());
        model.addAttribute("totalItems", cursosPage.getTotalElements());
        model.addAttribute("hasPrevious", cursosPage.hasPrevious());
        model.addAttribute("hasNext", cursosPage.hasNext());

        // Mantener filtros en la vista
        model.addAttribute("filtroGrado", filtroGrado);
        model.addAttribute("filtroTurno", filtroTurno);
        model.addAttribute("filtroArea", filtroArea);
        model.addAttribute("filtroEstado", filtroEstado);
        model.addAttribute("filtroNombre", filtroNombre);

        // Estadísticas
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
            // ========== VALIDACIONES ==========

            // 1. Validar curso duplicado (mismo nombre, grado, sección, turno)
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

            // 2. Validar límite de 4 cursos por grado/área/turno
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

            // 3. Validar horas del docente (no exceder 30 semanales)
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

            // 4. Validar cruce de horarios del docente
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

            // Generar código si está vacío
            if (curso.getCodigoCurso() == null || curso.getCodigoCurso().isEmpty()) {
                curso.generarCodigoAutomatico();
            }

            // Establecer valores por defecto
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

            // ========== VALIDACIONES ==========

            // 1. Validar curso duplicado (excluyendo el curso actual)
            boolean duplicado = cursoRepository.existsByNombreCursoAndIdGradoAndSeccionAndTurno(
                    curso.getNombreCurso(),
                    curso.getIdGrado(),
                    curso.getSeccion(),
                    curso.getTurno()
            );

            // Si hay duplicado y no es el mismo curso
            if (duplicado && !cursoOriginal.getNombreCurso().equals(curso.getNombreCurso())) {
                redirectAttributes.addFlashAttribute("error",
                        "✗ Ya existe un curso con el mismo nombre, grado, sección y turno");
                return "redirect:/admin/cursos";
            }

            // 2. Validar horas del docente
            if (curso.getIdDocente() != null && curso.getIdDocente() > 0) {
                Integer horasActuales = cursoRepository.sumHorasSemanalesByDocente(curso.getIdDocente());
                if (horasActuales == null) horasActuales = 0;

                // Restar horas del curso original si es el mismo docente
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

            // 3. Validar cruce de horarios del docente (excluyendo el curso actual)
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

            // Soft Delete: solo marcar como eliminado
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

    /**
     * Validar si hay cruce de horario en tiempo real (AJAX)
     */
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