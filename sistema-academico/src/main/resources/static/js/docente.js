/**
 * Limpia el formulario para crear un nuevo docente
 */
function limpiarFormularioDocente() {
    document.getElementById('modalTitulo').innerText = 'Nuevo Docente';
    document.getElementById('docenteForm').reset();
    document.getElementById('docenteId').value = '';
    document.getElementById('codigoDocente').value = '';
    document.getElementById('docenteForm').action = '/admin/docente/guardar';

    // Limpiar campos ocultos
    document.getElementById('dniHidden').value = '';
    document.getElementById('nombresHidden').value = '';
    document.getElementById('apellidoPaternoHidden').value = '';
    document.getElementById('apellidoMaternoHidden').value = '';

    // Limpiar mensaje de RENIEC
    const reniecMsg = document.getElementById('reniecMensaje');
    if (reniecMsg) reniecMsg.remove();

    // Habilitar campos para nuevo registro
    document.getElementById('dni').disabled = false;
    document.getElementById('dni').style.backgroundColor = '';
    document.getElementById('nombres').style.backgroundColor = '#e9ecef';
    document.getElementById('apellidoPaterno').style.backgroundColor = '#e9ecef';
    document.getElementById('apellidoMaterno').style.backgroundColor = '#e9ecef';

    // Habilitar botón de búsqueda
    const btnBuscar = document.querySelector('button[onclick="consultarReniec()"]');
    if (btnBuscar) {
        btnBuscar.disabled = false;
    }

    // Enfocar en el DNI
    setTimeout(() => {
        document.getElementById('dni').focus();
    }, 300);
}

/**
 * Consultar RENIEC por DNI y autocompletar datos
 */
async function consultarReniec() {
    const dniInput = document.getElementById('dni');
    const dni = dniInput.value.trim();

    // Eliminar mensaje anterior
    let mensajeExistente = document.getElementById('reniecMensaje');
    if (mensajeExistente) mensajeExistente.remove();

    // Validar DNI
    if (!dni || dni.length !== 8) {
        mostrarNotificacion('⚠️ Ingrese un DNI válido de 8 dígitos', 'warning');
        dniInput.focus();
        dniInput.style.backgroundColor = '#f8d7da';
        return;
    }

    if (!/^\d+$/.test(dni)) {
        mostrarNotificacion('⚠️ El DNI solo debe contener números', 'warning');
        dniInput.focus();
        dniInput.style.backgroundColor = '#f8d7da';
        return;
    }

    const nombresInput = document.getElementById('nombres');
    const apellidoPaternoInput = document.getElementById('apellidoPaterno');
    const apellidoMaternoInput = document.getElementById('apellidoMaterno');

    // Mostrar loading
    const loadingSpan = document.createElement('small');
    loadingSpan.id = 'reniecMensaje';
    loadingSpan.className = 'text-info mt-1 d-block';
    loadingSpan.innerHTML = '🔄 Consultando RENIEC...';
    dniInput.parentElement.appendChild(loadingSpan);

    dniInput.disabled = true;
    dniInput.style.backgroundColor = '#fff3cd';

    try {
        const response = await fetch(`/api/reniec/consultar/${dni}`);
        const data = await response.json();

        console.log('Respuesta RENIEC:', data);

        if (data.success) {
            // Llenar campos visibles
            nombresInput.value = data.nombres || '';
            apellidoPaternoInput.value = data.apellidoPaterno || '';
            apellidoMaternoInput.value = data.apellidoMaterno || '';

            // Llenar campos ocultos (los que se enviarán al servidor)
            document.getElementById('dniHidden').value = dni;
            document.getElementById('nombresHidden').value = data.nombres || '';
            document.getElementById('apellidoPaternoHidden').value = data.apellidoPaterno || '';
            document.getElementById('apellidoMaternoHidden').value = data.apellidoMaterno || '';

            // Cambiar estilo a éxito
            dniInput.style.backgroundColor = '#d4edda';
            nombresInput.style.borderColor = '#28a745';
            apellidoPaternoInput.style.borderColor = '#28a745';
            apellidoMaternoInput.style.borderColor = '#28a745';

            loadingSpan.innerHTML = '✅ Datos cargados desde RENIEC';
            loadingSpan.style.color = '#28a745';

            mostrarNotificacion('✅ Datos encontrados correctamente', 'success');

            // Limpiar estilos después de 3 segundos
            setTimeout(() => {
                nombresInput.style.borderColor = '';
                apellidoPaternoInput.style.borderColor = '';
                apellidoMaternoInput.style.borderColor = '';
                loadingSpan.remove();
            }, 3000);

            // Enfocar en el siguiente campo
            document.getElementById('email').focus();

        } else {
            loadingSpan.innerHTML = '❌ DNI no encontrado en RENIEC';
            loadingSpan.style.color = '#dc3545';
            dniInput.style.backgroundColor = '#f8d7da';
            mostrarNotificacion('❌ ' + (data.message || 'DNI no encontrado'), 'danger');
            limpiarDatosAutocompletados();

            setTimeout(() => {
                loadingSpan.remove();
            }, 3000);
        }
    } catch (error) {
        console.error("Error consultando RENIEC:", error);
        loadingSpan.innerHTML = '❌ Error al consultar RENIEC';
        loadingSpan.style.color = '#dc3545';
        dniInput.style.backgroundColor = '#f8d7da';
        mostrarNotificacion('❌ Error al consultar RENIEC', 'danger');
        limpiarDatosAutocompletados();

        setTimeout(() => {
            loadingSpan.remove();
        }, 3000);
    } finally {
        dniInput.disabled = false;
    }
}

/**
 * Limpia los datos autocompletados
 */
function limpiarDatosAutocompletados() {
    document.getElementById('nombres').value = '';
    document.getElementById('apellidoPaterno').value = '';
    document.getElementById('apellidoMaterno').value = '';
    document.getElementById('dniHidden').value = '';
    document.getElementById('nombresHidden').value = '';
    document.getElementById('apellidoPaternoHidden').value = '';
    document.getElementById('apellidoMaternoHidden').value = '';
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

    // Llenar campos visibles y ocultos
    document.getElementById('dni').value = dni || '';
    document.getElementById('dni').disabled = true;
    document.getElementById('dni').style.backgroundColor = '#e9ecef';
    document.getElementById('dniHidden').value = dni || '';

    document.getElementById('nombres').value = nombres || '';
    document.getElementById('nombresHidden').value = nombres || '';

    document.getElementById('apellidoPaterno').value = apellidoPaterno || '';
    document.getElementById('apellidoPaternoHidden').value = apellidoPaterno || '';

    document.getElementById('apellidoMaterno').value = apellidoMaterno || '';
    document.getElementById('apellidoMaternoHidden').value = apellidoMaterno || '';

    document.getElementById('email').value = email || '';
    document.getElementById('celular').value = celular || '';
    document.getElementById('especialidad').value = especialidad || '';
    document.getElementById('estado').value = estado || 'ACTIVO';

    // Deshabilitar botón de búsqueda en modo edición
    const btnBuscar = document.querySelector('button[onclick="consultarReniec()"]');
    if (btnBuscar) {
        btnBuscar.disabled = true;
    }

    const form = document.getElementById('docenteForm');
    form.action = '/admin/docente/actualizar/' + id;
}

/**
 * Confirma y elimina un docente
 */
function confirmarEliminarDocente(boton) {
    const id = boton.getAttribute('data-id');
    const nombre = boton.getAttribute('data-nombre');

    if (confirm('⚠️ ¿Estás seguro de eliminar al docente "' + nombre + '"?\n\nEsta acción no se puede deshacer.')) {
        window.location.href = '/admin/docente/eliminar/' + id;
    }
}

/**
 * Muestra una notificación temporal
 */
function mostrarNotificacion(mensaje, tipo) {
    // Eliminar notificaciones anteriores
    const notificacionesAnteriores = document.querySelectorAll('.notificacion-flotante');
    notificacionesAnteriores.forEach(el => el.remove());

    const alerta = document.createElement('div');
    alerta.className = 'notificacion-flotante alert alert-' + tipo + ' alert-dismissible fade show position-fixed top-0 end-0 m-3';
    alerta.style.zIndex = '9999';
    alerta.style.maxWidth = '450px';
    alerta.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    alerta.style.animation = 'slideInRight 0.5s ease';

    let icono = '';
    switch(tipo) {
        case 'success': icono = 'fa-check-circle'; break;
        case 'danger': icono = 'fa-exclamation-circle'; break;
        case 'warning': icono = 'fa-exclamation-triangle'; break;
        case 'info': icono = 'fa-info-circle'; break;
        default: icono = 'fa-info-circle';
    }

    const titulo = tipo === 'success' ? '¡Éxito!' :
                   tipo === 'danger' ? '¡Error!' :
                   tipo === 'warning' ? '¡Atención!' : 'Información';

    alerta.innerHTML = `
        <div class="d-flex align-items-center">
            <div class="me-2"><i class="fas ${icono} fa-2x"></i></div>
            <div><strong>${titulo}</strong><br>${mensaje}</div>
            <button type="button" class="btn-close ms-auto" onclick="this.parentElement.parentElement.remove()"></button>
        </div>
    `;
    document.body.appendChild(alerta);

    // Auto-eliminar después de 5 segundos
    setTimeout(() => {
        if (alerta) {
            alerta.remove();
        }
    }, 5000);
}

/**
 * Detecta especialidad automáticamente según el nombre ingresado
 */
function detectarEspecialidadAutomatica() {
    const nombreDocente = document.getElementById('filtroNombre').value;
    const especialidadSelect = document.getElementById('filtroEspecialidad');

    if (!nombreDocente || nombreDocente.trim() === '') {
        if (especialidadSelect) especialidadSelect.value = '';
        return;
    }

    const nombreUpper = nombreDocente.toUpperCase();

    const mapeoEspecialidades = [
        { palabras: ['MATEMATICA', 'MATEMÁTICAS', 'ALGEBRA', 'GEOMETRIA', 'TRIGONOMETRIA', 'CALCULO', 'ESTADISTICA', 'PROBABILIDAD', 'RAZONAMIENTO', 'LOGICA', 'SUMA', 'RESTA', 'MULTIPLICACION', 'DIVISION', 'NUMEROS'], especialidad: 'MATEMÁTICAS' },
        { palabras: ['COMUNICACION', 'COMUNICACIÓN', 'LENGUAJE', 'LITERATURA', 'GRAMATICA', 'ORTOGRAFIA', 'REDACCION', 'LECTURA', 'ESCRITURA', 'VOCALES', 'ABECEDARIO', 'SILABAS', 'PALABRAS', 'CUENTOS', 'ORATORIA', 'DEBATE'], especialidad: 'COMUNICACIÓN' },
        { palabras: ['CIENCIA', 'BIOLOGIA', 'FISICA', 'QUIMICA', 'ECOLOGIA', 'MEDIO AMBIENTE', 'LABORATORIO', 'ANATOMIA', 'ZOOLOGIA', 'BOTANICA', 'ASTRONOMIA', 'GEOLOGIA', 'METODOLOGIA', 'INVESTIGACION'], especialidad: 'CIENCIA Y TECNOLOGÍA' },
        { palabras: ['SOCIALES', 'HISTORIA', 'GEOGRAFIA', 'PERSONAL SOCIAL', 'CIVICA', 'CIUDADANIA', 'FILOSOFIA', 'PSICOLOGIA', 'SOCIOLOGIA', 'ANTROPOLOGIA', 'DERECHO', 'POLITICA', 'ECONOMIA', 'CONTABILIDAD'], especialidad: 'CIENCIAS SOCIALES' },
        { palabras: ['INGLES', 'ENGLISH'], especialidad: 'INGLÉS' },
        { palabras: ['ARTE', 'DIBUJO', 'PINTURA', 'MUSICA', 'TEATRO', 'DANZA', 'FOLCLOR', 'MARINERA'], especialidad: 'ARTE' },
        { palabras: ['EDUCACION FISICA', 'EDUCACIÓN FÍSICA', 'DEPORTE', 'RECREACION', 'SALUD', 'VOLEIBOL', 'FULBITO', 'ATLETISMO'], especialidad: 'EDUCACIÓN FÍSICA' },
        { palabras: ['RELIGION', 'RELIGIÓN', 'VALORES', 'ETICA', 'MORAL', 'DOGMAS', 'CRISTIANA'], especialidad: 'RELIGIÓN' },
        { palabras: ['TUTORIA', 'TUTORÍA', 'ORIENTACION', 'CONVIVENCIA'], especialidad: 'TUTORÍA' }
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
 * Aplicar filtros de búsqueda
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
                e.preventDefault();
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

// ==================== ESTILOS CSS PARA NOTIFICACIONES ====================
// Agregar estilos para las notificaciones flotantes
const estiloNotificacion = document.createElement('style');
estiloNotificacion.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    .notificacion-flotante {
        animation: slideInRight 0.5s ease;
    }
`;
document.head.appendChild(estiloNotificacion);

// ==================== INICIALIZACIÓN ====================
document.addEventListener('DOMContentLoaded', function() {
    // Auto-cerrar alertas después de 5 segundos
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);

    // Inicializar filtros
    inicializarFiltrosDocentes();

    // Eventos para el formulario de docente
    const dniInput = document.getElementById('dni');
    if (dniInput) {
        dniInput.addEventListener('blur', consultarReniec);
        dniInput.addEventListener('input', function(e) {
            if (e.target.value.length === 8) {
                consultarReniec();
            }
        });
        // Permitir búsqueda con Enter
        dniInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                consultarReniec();
            }
        });
    }

    // Validación del formulario antes de enviar
    const form = document.getElementById('docenteForm');
    if (form) {
        form.addEventListener('submit', function(event) {
            const dni = document.getElementById('dniHidden').value;
            const nombres = document.getElementById('nombresHidden').value;
            const apellidoPaterno = document.getElementById('apellidoPaternoHidden').value;
            const apellidoMaterno = document.getElementById('apellidoMaternoHidden').value;
            const especialidad = document.getElementById('especialidad').value;
            const celular = document.getElementById('celular').value;

            // Validar DNI (usar campo oculto)
            if (!dni || dni.length !== 8) {
                event.preventDefault();
                mostrarNotificacion(' El DNI debe tener 8 dígitos', 'warning');
                document.getElementById('dni').focus();
                document.getElementById('dni').style.backgroundColor = '#f8d7da';
                return false;
            }

            // Validar nombres
            if (!nombres || nombres.trim() === '') {
                event.preventDefault();
                mostrarNotificacion(' Los nombres son obligatorios', 'warning');
                document.getElementById('dni').focus();
                return false;
            }

            // Validar apellido paterno
            if (!apellidoPaterno || apellidoPaterno.trim() === '') {
                event.preventDefault();
                mostrarNotificacion(' El apellido paterno es obligatorio', 'warning');
                document.getElementById('dni').focus();
                return false;
            }

            // Validar apellido materno
            if (!apellidoMaterno || apellidoMaterno.trim() === '') {
                event.preventDefault();
                mostrarNotificacion(' El apellido materno es obligatorio', 'warning');
                document.getElementById('dni').focus();
                return false;
            }

            // Validar especialidad
            if (!especialidad || especialidad === '') {
                event.preventDefault();
                mostrarNotificacion(' Debe seleccionar una especialidad', 'warning');
                document.getElementById('especialidad').focus();
                return false;
            }

            // Validar celular (exactamente 9 dígitos y solo números)
            if (celular && (celular.length !== 9 || !/^\d+$/.test(celular))) {
                event.preventDefault();
                mostrarNotificacion(' El celular debe tener exactamente 9 dígitos numéricos', 'warning');
                document.getElementById('celular').focus();
                return false;
            }

            return true;
        });
    }
});

// ==================== EXPORTAR FUNCIONES PARA USO GLOBAL ====================
window.consultarReniec = consultarReniec;
window.limpiarFormularioDocente = limpiarFormularioDocente;
window.editarDocente = editarDocente;
window.confirmarEliminarDocente = confirmarEliminarDocente;
window.mostrarNotificacion = mostrarNotificacion;
window.aplicarFiltros = aplicarFiltros;
window.limpiarFiltros = limpiarFiltros;
window.detectarEspecialidadAutomatica = detectarEspecialidadAutomatica;