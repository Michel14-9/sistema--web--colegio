/**
 * Limpia el formulario para crear un nuevo docente
 */
function limpiarFormularioDocente() {
    document.getElementById('modalTitulo').innerText = 'Nuevo Docente';
    document.getElementById('docenteForm').reset();
    document.getElementById('docenteId').value = '';
    document.getElementById('codigoDocente').value = '';
    document.getElementById('docenteForm').action = '/admin/docente/guardar';
    // Limpiar mensaje de RENIEC
    const reniecMsg = document.getElementById('reniecMensaje');
    if (reniecMsg) reniecMsg.remove();
}

/**
 * Consultar RENIEC por DNI y autocompletar datos
 */
async function consultarReniec() {
    const dniInput = document.getElementById('dni');
    const dni = dniInput.value.trim();

    let mensajeExistente = document.getElementById('reniecMensaje');
    if (mensajeExistente) mensajeExistente.remove();

    if (!dni || dni.length !== 8) {
        return;
    }

    const nombresInput = document.getElementById('nombres');
    const apellidoPaternoInput = document.getElementById('apellidoPaterno');
    const apellidoMaternoInput = document.getElementById('apellidoMaterno');

    const loadingSpan = document.createElement('small');
    loadingSpan.id = 'reniecMensaje';
    loadingSpan.className = 'text-info mt-1 d-block';
    loadingSpan.innerHTML = ' Consultando RENIEC...';
    dniInput.parentElement.appendChild(loadingSpan);

    try {
        const response = await fetch(`/api/reniec/consultar/${dni}`);
        const data = await response.json();

        if (data.success) {
            nombresInput.value = data.nombres || '';
            apellidoPaternoInput.value = data.apellidoPaterno || '';
            apellidoMaternoInput.value = data.apellidoMaterno || '';

            nombresInput.style.borderColor = '#28a745';
            apellidoPaternoInput.style.borderColor = '#28a745';
            apellidoMaternoInput.style.borderColor = '#28a745';

            loadingSpan.innerHTML = 'Datos cargados desde RENIEC';
            loadingSpan.style.color = '#28a745';

            setTimeout(() => {
                nombresInput.style.borderColor = '';
                apellidoPaternoInput.style.borderColor = '';
                apellidoMaternoInput.style.borderColor = '';
                loadingSpan.remove();
            }, 3000);
        } else {
            loadingSpan.innerHTML = ' DNI no encontrado en RENIEC';
            loadingSpan.style.color = '#dc3545';
            setTimeout(() => {
                loadingSpan.remove();
            }, 3000);
        }
    } catch (error) {
        console.error("Error consultando RENIEC:", error);
        loadingSpan.innerHTML = ' Error al consultar RENIEC';
        loadingSpan.style.color = '#dc3545';
        setTimeout(() => {
            loadingSpan.remove();
        }, 3000);
    }
}

/**
 * Carga los datos del docente en el modal para editar
 */
function editarDocente(boton) {
    const id = boton.getAttribute('data-id');
    const codigo = boton.getAttribute('data-codigo');
    const dni = boton.getAttribute('data-dni');
    const nombres = boton.getAttribute('data-nombres');
    const apellidoPaterno = boton.getAttribute('data-apellido-paterno');
    const apellidoMaterno = boton.getAttribute('data-apellido-materno');
    const email = boton.getAttribute('data-email');
    const celular = boton.getAttribute('data-celular');
    const especialidad = boton.getAttribute('data-especialidad');
    const estado = boton.getAttribute('data-estado');

    document.getElementById('modalTitulo').innerText = 'Editar Docente';
    document.getElementById('docenteId').value = id || '';
    document.getElementById('codigoDocente').value = codigo || '';
    document.getElementById('dni').value = dni || '';
    document.getElementById('nombres').value = nombres || '';
    document.getElementById('apellidoPaterno').value = apellidoPaterno || '';
    document.getElementById('apellidoMaterno').value = apellidoMaterno || '';
    document.getElementById('email').value = email || '';
    document.getElementById('celular').value = celular || '';
    document.getElementById('especialidad').value = especialidad || '';
    document.getElementById('estado').value = estado || 'ACTIVO';

    const form = document.getElementById('docenteForm');
    form.action = '/admin/docente/actualizar/' + id;
}

/**
 * Confirma y elimina un docente
 */
function confirmarEliminarDocente(boton) {
    const id = boton.getAttribute('data-id');
    const nombre = boton.getAttribute('data-nombre');

    if (confirm('¿Estás seguro de eliminar al docente "' + nombre + '"? Esta acción no se puede deshacer.')) {
        window.location.href = '/admin/docente/eliminar/' + id;
    }
}

function detectarEspecialidadAutomatica() {
    const nombreDocente = document.getElementById('filtroNombre').value;
    const especialidadSelect = document.getElementById('filtroEspecialidad');

    if (!nombreDocente || nombreDocente.trim() === '') {
        if (especialidadSelect) especialidadSelect.value = '';
        return;
    }

    const nombreUpper = nombreDocente.toUpperCase();

    const mapeoEspecialidades = [
        { palabras: ['MATEMATICA', 'MATEMÁTICAS', 'ALGEBRA', 'GEOMETRIA', 'TRIGONOMETRIA', 'CALCULO', 'ESTADISTICA', 'PROBABILIDAD', 'RAZONAMIENTO', 'LOGICA', 'SUMA', 'RESTA', 'MULTIPLICACION', 'DIVISION', 'NUMEROS'], especialidad: 'Matemáticas' },
        { palabras: ['COMUNICACION', 'COMUNICACIÓN', 'LENGUAJE', 'LITERATURA', 'GRAMATICA', 'ORTOGRAFIA', 'REDACCION', 'LECTURA', 'ESCRITURA', 'VOCALES', 'ABECEDARIO', 'SILABAS', 'PALABRAS', 'CUENTOS', 'ORATORIA', 'DEBATE'], especialidad: 'Comunicación' },
        { palabras: ['CIENCIA', 'BIOLOGIA', 'FISICA', 'QUIMICA', 'ECOLOGIA', 'MEDIO AMBIENTE', 'LABORATORIO', 'ANATOMIA', 'ZOOLOGIA', 'BOTANICA', 'ASTRONOMIA', 'GEOLOGIA', 'METODOLOGIA', 'INVESTIGACION'], especialidad: 'Ciencia y Tecnología' },
        { palabras: ['SOCIALES', 'HISTORIA', 'GEOGRAFIA', 'PERSONAL SOCIAL', 'CIVICA', 'CIUDADANIA', 'FILOSOFIA', 'PSICOLOGIA', 'SOCIOLOGIA', 'ANTROPOLOGIA', 'DERECHO', 'POLITICA', 'ECONOMIA', 'CONTABILIDAD'], especialidad: 'Ciencias Sociales' },
        { palabras: ['INGLES', 'ENGLISH'], especialidad: 'Inglés' },
        { palabras: ['ARTE', 'DIBUJO', 'PINTURA', 'MUSICA', 'TEATRO', 'DANZA', 'FOLCLOR', 'MARINERA'], especialidad: 'Arte' },
        { palabras: ['EDUCACION FISICA', 'EDUCACIÓN FÍSICA', 'DEPORTE', 'RECREACION', 'SALUD', 'VOLEIBOL', 'FULBITO', 'ATLETISMO'], especialidad: 'Educación Física' },
        { palabras: ['RELIGION', 'RELIGIÓN', 'VALORES', 'ETICA', 'MORAL', 'DOGMAS', 'CRISTIANA'], especialidad: 'Religión' },
        { palabras: ['TUTORIA', 'TUTORÍA', 'ORIENTACION', 'CONVIVENCIA'], especialidad: 'Tutoría' }
    ];

    let especialidadEncontrada = false;
    for (let item of mapeoEspecialidades) {
        for (let palabra of item.palabras) {
            if (nombreUpper.includes(palabra)) {
                if (especialidadSelect) {
                    especialidadSelect.value = item.especialidad;
                    especialidadEncontrada = true;
                    especialidadSelect.style.borderColor = '#28a745';
                    setTimeout(() => {
                        if (especialidadSelect) especialidadSelect.style.borderColor = '';
                    }, 2000);
                    console.log("Especialidad detectada:", item.especialidad);
                    aplicarFiltros();
                }
                break;
            }
        }
        if (especialidadEncontrada) break;
    }

    if (!especialidadEncontrada && especialidadSelect) {
        especialidadSelect.value = '';
    }
}

/**
 * Aplicar filtros de búsqueda (con depuración)
 */
function aplicarFiltros() {
    const nombre = document.getElementById('filtroNombre').value;
    const especialidad = document.getElementById('filtroEspecialidad').value;
    const estado = document.getElementById('filtroEstado').value;

    console.log("=== APLICANDO FILTROS ===");
    console.log("Nombre:", nombre);
    console.log("Especialidad:", especialidad);
    console.log("Estado:", estado);

    let url = '/admin/docentes?page=0';
    if (nombre && nombre !== '') url += '&filtroNombre=' + encodeURIComponent(nombre);
    if (especialidad && especialidad !== '') url += '&filtroEspecialidad=' + especialidad;
    if (estado && estado !== '') url += '&filtroEstado=' + estado;

    console.log("URL generada:", url);
    window.location.href = url;
}

/**
 * Limpiar todos los filtros
 */
function limpiarFiltros() {
    document.getElementById('filtroNombre').value = '';
    document.getElementById('filtroEspecialidad').value = '';
    document.getElementById('filtroEstado').value = '';
    aplicarFiltros();
}

/**
 * Inicializar eventos de filtros con detección automática
 */
function inicializarFiltrosDocentes() {
    const btnBuscar = document.getElementById('btnBuscar');
    const btnLimpiar = document.getElementById('btnLimpiar');
    const filtroNombre = document.getElementById('filtroNombre');
    const filtroEspecialidad = document.getElementById('filtroEspecialidad');
    const filtroEstado = document.getElementById('filtroEstado');

    if (filtroNombre) {
        filtroNombre.addEventListener('input', function() {
            detectarEspecialidadAutomatica();
        });
        filtroNombre.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                aplicarFiltros();
            }
        });
    }

    if (btnBuscar) {
        btnBuscar.addEventListener('click', aplicarFiltros);
    }

    if (btnLimpiar) {
        btnLimpiar.addEventListener('click', limpiarFiltros);
    }

    if (filtroEspecialidad) {
        filtroEspecialidad.addEventListener('change', aplicarFiltros);
    }

    if (filtroEstado) {
        filtroEstado.addEventListener('change', aplicarFiltros);
    }

    console.log("Filtros de docentes inicializados con detección automática");
}

/**
 * Auto-cerrar alertas después de 5 segundos
 */
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);

    inicializarFiltrosDocentes();

    const dniInput = document.getElementById('dni');
    if (dniInput) {
        dniInput.addEventListener('blur', consultarReniec);
        dniInput.addEventListener('input', function(e) {
            if (e.target.value.length === 8) {
                consultarReniec();
            }
        });
    }

    const form = document.getElementById('docenteForm');
    if (form) {
        form.addEventListener('submit', function(event) {
            const dni = document.getElementById('dni').value;
            const nombres = document.getElementById('nombres').value;
            const apellidoPaterno = document.getElementById('apellidoPaterno').value;
            const apellidoMaterno = document.getElementById('apellidoMaterno').value;
            const especialidad = document.getElementById('especialidad').value;
            const celular = document.getElementById('celular').value;

            if (!dni || dni.length !== 8) {
                event.preventDefault();
                alert(' El DNI debe tener 8 dígitos');
                return false;
            }

            if (!nombres || nombres.trim() === '') {
                event.preventDefault();
                alert(' Los nombres son obligatorios');
                return false;
            }

            if (!apellidoPaterno || apellidoPaterno.trim() === '') {
                event.preventDefault();
                alert(' El apellido paterno es obligatorio');
                return false;
            }

            if (!apellidoMaterno || apellidoMaterno.trim() === '') {
                event.preventDefault();
                alert(' El apellido materno es obligatorio');
                return false;
            }

            if (!especialidad) {
                event.preventDefault();
                alert(' Debe seleccionar una especialidad');
                return false;
            }

           // Validación de celular (exactamente 9 dígitos y solo números)
           if (celular && (celular.length !== 9 || !/^\d+$/.test(celular))) {
               event.preventDefault();
               alert(' El número de celular debe tener exactamente 9 dígitos numéricos');
               return false;
           }

            return true;
        });
    }
});