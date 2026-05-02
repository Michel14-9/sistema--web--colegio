/**
 * Limpia el formulario para crear un nuevo curso
 */
function limpiarFormularioCurso() {
    document.getElementById('modalTitulo').innerText = 'Nuevo Curso';
    document.getElementById('cursoForm').reset();
    document.getElementById('cursoId').value = '';
    document.getElementById('codigoCurso').value = '';
    document.getElementById('cursoForm').action = '/admin/curso/guardar';
}

/**
 * Carga los datos del curso en el modal para editar
 */
function editarCurso(id, codigo, nombre, descripcion, horas, area, grado, idDocente, estado) {
    document.getElementById('modalTitulo').innerText = 'Editar Curso';
    document.getElementById('cursoId').value = id;
    document.getElementById('codigoCurso').value = codigo;
    document.getElementById('nombreCurso').value = nombre;
    document.getElementById('descripcion').value = descripcion || '';
    document.getElementById('horasSemanales').value = horas || '';
    document.getElementById('area').value = area || '';
    document.getElementById('idGrado').value = grado || '';
    document.getElementById('idDocente').value = idDocente || '';
    document.getElementById('estado').value = estado || 'ACTIVO';
    document.getElementById('cursoForm').action = '/admin/curso/actualizar/' + id;
}

/**
 * Confirma y elimina un curso
 */
function confirmarEliminarCurso(id, nombre) {
    if (confirm('¿Estás seguro de eliminar el curso "' + nombre + '"?')) {
        window.location.href = '/admin/curso/eliminar/' + id;
    }
}

/**
 * Validar formulario antes de enviar
 */
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('cursoForm');
    if (form) {
        form.addEventListener('submit', function(event) {
            const nombreCurso = document.getElementById('nombreCurso').value;
            if (!nombreCurso || nombreCurso.trim() === '') {
                event.preventDefault();
                alert('El nombre del curso es obligatorio');
                return false;
            }
        });
    }
});