/**
 * Limpia el formulario para crear un nuevo docente
 */
function limpiarFormularioDocente() {
    document.getElementById('modalTitulo').innerText = 'Nuevo Docente';
    document.getElementById('docenteForm').reset();
    document.getElementById('docenteId').value = '';
    document.getElementById('codigoDocente').value = '';
    document.getElementById('docenteForm').action = '/admin/docente/guardar';
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

/**
 * Aplicar filtros de búsqueda
 */
function aplicarFiltros() {
    const nombre = document.getElementById('filtroNombre').value;
    const especialidad = document.getElementById('filtroEspecialidad').value;
    const estado = document.getElementById('filtroEstado').value;

    let url = '/admin/docentes?page=0';
    if (nombre && nombre !== '') url += '&filtroNombre=' + encodeURIComponent(nombre);
    if (especialidad && especialidad !== '') url += '&filtroEspecialidad=' + especialidad;
    if (estado && estado !== '') url += '&filtroEstado=' + estado;

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
 * Inicializar eventos de filtros
 */
function inicializarFiltrosDocentes() {
    const btnBuscar = document.getElementById('btnBuscar');
    const btnLimpiar = document.getElementById('btnLimpiar');
    const filtroNombre = document.getElementById('filtroNombre');
    const filtroEspecialidad = document.getElementById('filtroEspecialidad');
    const filtroEstado = document.getElementById('filtroEstado');

    if (btnBuscar) {
        btnBuscar.addEventListener('click', aplicarFiltros);
    }

    if (btnLimpiar) {
        btnLimpiar.addEventListener('click', limpiarFiltros);
    }

    if (filtroNombre) {
        filtroNombre.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                aplicarFiltros();
            }
        });
    }

    if (filtroEspecialidad) {
        filtroEspecialidad.addEventListener('change', aplicarFiltros);
    }

    if (filtroEstado) {
        filtroEstado.addEventListener('change', aplicarFiltros);
    }

    console.log("Filtros de docentes inicializados");
}

/**
 * Auto-cerrar alertas después de 5 segundos
 */
document.addEventListener('DOMContentLoaded', function() {
    // Auto-cerrar alertas
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);

    // Inicializar filtros
    inicializarFiltrosDocentes();

    // Validar formulario antes de enviar
    const form = document.getElementById('docenteForm');
    if (form) {
        form.addEventListener('submit', function(event) {
            const dni = document.getElementById('dni').value;
            const nombres = document.getElementById('nombres').value;
            const apellidoPaterno = document.getElementById('apellidoPaterno').value;
            const apellidoMaterno = document.getElementById('apellidoMaterno').value;
            const especialidad = document.getElementById('especialidad').value;

            if (!dni || dni.length !== 8) {
                event.preventDefault();
                alert('❌ El DNI debe tener 8 dígitos');
                return false;
            }

            if (!nombres || nombres.trim() === '') {
                event.preventDefault();
                alert('❌ Los nombres son obligatorios');
                return false;
            }

            if (!apellidoPaterno || apellidoPaterno.trim() === '') {
                event.preventDefault();
                alert('❌ El apellido paterno es obligatorio');
                return false;
            }

            if (!apellidoMaterno || apellidoMaterno.trim() === '') {
                event.preventDefault();
                alert('❌ El apellido materno es obligatorio');
                return false;
            }

            if (!especialidad) {
                event.preventDefault();
                alert('❌ Debe seleccionar una especialidad');
                return false;
            }

            return true;
        });
    }
});