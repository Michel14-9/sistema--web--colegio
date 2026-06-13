/**
 * Normalizar texto: convierte a mayúsculas y elimina tildes
 */
function normalizarTexto(texto) {
    if (!texto) return '';
    return texto.toUpperCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[^A-Z0-9\s]/g, '');
}

/**
 * Extraer la primera palabra del nombre del curso para autocompletar
 */
function extraerPrimeraPalabra(nombreCurso) {
    if (!nombreCurso) return '';
    const palabras = nombreCurso.trim().split(/\s+/);
    return palabras[0] || '';
}

/**
 * Lista de palabras clave para autocompletado dinámico (se filtran mientras escribe)
 */
const listaAutocompletado = [
    // MATEMÁTICAS
    'Álgebra', 'Geometría', 'Trigonometría', 'Cálculo', 'Aritmética', 'Estadística',
    'Probabilidad', 'Razonamiento Matemático', 'Matemática', 'Aritmetica', 'Algebra',
    // COMUNICACIÓN
    'Lectura', 'Escritura', 'Literatura', 'Redacción', 'Oratoria', 'Debate', 'Gramática',
    'Ortografía', 'Lenguaje', 'Comunicación',
    // CIENCIAS
    'Física', 'Química', 'Biología', 'Ecología', 'Genética', 'Anatomía', 'Botánica',
    'Zoología', 'Ciencia', 'Tecnología', 'Laboratorio',
    // SOCIALES
    'Historia', 'Geografía', 'Economía', 'Filosofía', 'Psicología', 'Sociología',
    'Antropología', 'Ciudadanía', 'Personal Social', 'Cívica', 'Formación Ciudadana',
    // ARTE
    'Arte', 'Dibujo', 'Pintura', 'Música', 'Teatro', 'Danza', 'Folclor', 'Marinera',
    // EDUCACIÓN FÍSICA
    'Deporte', 'Voleibol', 'Fulbito', 'Atletismo', 'Recreación', 'Salud', 'Educación Física',
    // INGLÉS
    'Inglés', 'English',
    // RELIGIÓN Y TUTORÍA
    'Religión', 'Valores', 'Tutoría', 'Convivencia', 'Orientación',
    // PALABRAS CLAVE PRIMARIA
    'Suma', 'Resta', 'Multiplicación', 'División', 'Números', 'Conteo', 'Figuras', 'Patrones',
    'Vocales', 'Abecedario', 'Sílabas', 'Palabras', 'Cuentos', 'Dibujo', 'Colorear', 'Manualidades',
    'Animales', 'Plantas', 'Cuerpo Humano', 'Familia', 'Escuela', 'Comunidad', 'Perú', 'Ica'
];

/**
 * Lista de cursos completos sugeridos
 */
const listaCursosCompletos = [
    'Álgebra Lineal', 'Geometría Analítica', 'Trigonometría', 'Cálculo Diferencial',
    'Física Moderna', 'Química Orgánica', 'Biología Celular', 'Historia del Perú',
    'Geografía de Ica', 'Economía', 'Filosofía', 'Psicología', 'Literatura Peruana',
    'Matemática - Sumas y Restas', 'Comunicación - Lectura y Escritura',
    'Ciencia y Ambiente - Los Animales', 'Personal Social - Mi Familia',
    'Arte - Dibujo y Colorear', 'Educación Física - Juegos Recreativos',
    'Inglés Básico - Vocabulario', 'Tutoría - Convivencia Escolar'
];

/**
 * Mostrar sugerencias dinámicas mientras escribe
 */
function mostrarSugerenciasDinamicas() {
    const input = document.getElementById('nombreCurso');
    const valor = input.value.toLowerCase();

    // Crear o obtener el datalist
    let datalist = document.getElementById('sugerenciasDinamicas');
    if (!datalist) {
        datalist = document.createElement('datalist');
        datalist.id = 'sugerenciasDinamicas';
        input.setAttribute('list', 'sugerenciasDinamicas');
        document.body.appendChild(datalist);
    }

    // Limpiar opciones anteriores
    datalist.innerHTML = '';

    if (valor.length < 1) return;

    // 1. Buscar palabras clave que coincidan (empiecen con lo que escribe)
    const sugerenciasPalabras = listaAutocompletado.filter(palabra =>
        palabra.toLowerCase().startsWith(valor) ||
        palabra.toLowerCase().includes(valor)
    );

    // 2. Buscar cursos completos que coincidan
    const sugerenciasCursos = listaCursosCompletos.filter(curso =>
        curso.toLowerCase().includes(valor)
    );

    // Agregar palabras clave al datalist
    sugerenciasPalabras.slice(0, 8).forEach(sugerencia => {
        const option = document.createElement('option');
        option.value = sugerencia;
        datalist.appendChild(option);
    });

    // Agregar cursos completos al datalist
    sugerenciasCursos.slice(0, 5).forEach(curso => {
        const option = document.createElement('option');
        option.value = curso;
        datalist.appendChild(option);
    });

    // Mostrar contador de sugerencias (opcional)
    let contadorSpan = document.getElementById('contadorSugerencias');
    if (!contadorSpan) {
        contadorSpan = document.createElement('small');
        contadorSpan.id = 'contadorSugerencias';
        contadorSpan.className = 'text-muted mt-1 d-block';
        input.parentElement.appendChild(contadorSpan);
    }

    const totalSugerencias = sugerenciasPalabras.length + sugerenciasCursos.length;
    if (totalSugerencias > 0) {
        contadorSpan.innerHTML = `💡 ${totalSugerencias} sugerencia${totalSugerencias !== 1 ? 's' : ''} disponibles. Escribe y selecciona.`;
        contadorSpan.style.color = '#6c757d';
    } else {
        contadorSpan.innerHTML = `💡 No hay sugerencias para "${valor}". Escribe el nombre completo del curso.`;
    }
}

/**
 * Palabras clave para detectar nivel (Primaria vs Secundaria) - PERÚ ICA
 */
const palabrasPrimaria = [
    // MATEMÁTICA
    'SUMA', 'RESTA', 'MULTIPLICACION', 'DIVISION', 'NUMEROS', 'CONTEO', 'FIGURAS', 'PATRONES',
    // COMUNICACIÓN
    'LECTURA', 'ESCRITURA', 'CALIGRAFIA', 'VOCALES', 'ABECEDARIO', 'SILABAS', 'PALABRAS', 'CUENTOS',
    'DIBUJO', 'COLOREAR', 'MANUALIDADES',
    // CIENCIA Y AMBIENTE
    'ANIMALES', 'PLANTAS', 'CUERPO HUMANO', 'SALUD', 'HIGIENE',
    // PERSONAL SOCIAL
    'FAMILIA', 'ESCUELA', 'COMUNIDAD', 'PERU', 'ICA', 'SIMBOLOS PATRIOS'
];

const palabrasSecundaria = [
    // MATEMÁTICA (Perú)
    'ALGEBRA', 'TRIGONOMETRIA', 'GEOMETRIA', 'CALCULO', 'ARITMETICA', 'RAZONAMIENTO',
    'ESTADISTICA', 'PROBABILIDAD', 'LOGICA', 'ECUACIONES', 'FUNCIONES', 'MATRICES',
    // COMUNICACIÓN (Perú)
    'LITERATURA', 'REDACCION', 'ORATORIA', 'DEBATE', 'LINGUISTICA', 'SEMANTICA',
    // CIENCIA Y TECNOLOGÍA (Perú)
    'FISICA', 'QUIMICA', 'BIOLOGIA', 'GENETICA', 'ECOLOGIA', 'CELULA', 'ADN', 'ENERGIA', 'MOVIMIENTO',
    'ANATOMIA', 'ZOOLOGIA', 'BOTANICA', 'ASTRONOMIA', 'GEOLOGIA', 'INVESTIGACION', 'METODOLOGIA',
    // CIENCIAS SOCIALES (Perú)
    'HISTORIA', 'GEOGRAFIA', 'ECONOMIA', 'CONTABILIDAD', 'FILOSOFIA',
    'PSICOLOGIA', 'SOCIOLOGIA', 'ANTROPOLOGIA', 'DERECHO', 'POLITICA',
    'CIUDADANIA', 'CIVICA', 'FORMACION CIUDADANA', 'PERSONAL SOCIAL',
    'DESARROLLO PERSONAL', 'TUTORIA', 'ORIENTACION',
    // INGLÉS (Perú)
    'INGLES', 'ENGLISH',
    // ARTE (Perú)
    'ARTE', 'DIBUJO', 'PINTURA', 'MUSICA', 'TEATRO', 'DANZA', 'FOLCLOR', 'MARINERA',
    // EDUCACIÓN FÍSICA (Perú)
    'EDUCACION FISICA', 'DEPORTE', 'RECREACION', 'SALUD', 'VOLEIBOL', 'FULBITO', 'ATLETISMO'
];

/**
 * Detectar nivel (Primaria/Secundaria) según el nombre del curso
 */
function detectarNivelPorNombre(nombreCurso) {
    const nombreNormalizado = normalizarTexto(nombreCurso);
    const primeraPalabra = extraerPrimeraPalabra(nombreNormalizado);

    // Primero buscar en la primera palabra
    for (let palabra of palabrasSecundaria) {
        if (primeraPalabra === palabra || primeraPalabra.includes(palabra) || palabra.includes(primeraPalabra)) {
            return 'SECUNDARIA';
        }
    }
    for (let palabra of palabrasPrimaria) {
        if (primeraPalabra === palabra || primeraPalabra.includes(palabra) || palabra.includes(primeraPalabra)) {
            return 'PRIMARIA';
        }
    }

    // Si no se encuentra en la primera palabra, buscar en todo el texto
    for (let palabra of palabrasSecundaria) {
        if (nombreNormalizado.includes(palabra)) {
            return 'SECUNDARIA';
        }
    }
    for (let palabra of palabrasPrimaria) {
        if (nombreNormalizado.includes(palabra)) {
            return 'PRIMARIA';
        }
    }

    return null;
}

/**
 * Mostrar sugerencia de primera palabra
 */
function mostrarSugerenciaPrimeraPalabra(nombreCurso) {
    const primeraPalabra = extraerPrimeraPalabra(nombreCurso);
    if (!primeraPalabra) return;

    const nombreInput = document.getElementById('nombreCurso');
    let sugerenciaSpan = document.getElementById('sugerenciaPrimeraPalabra');

    if (!sugerenciaSpan) {
        sugerenciaSpan = document.createElement('small');
        sugerenciaSpan.id = 'sugerenciaPrimeraPalabra';
        sugerenciaSpan.className = 'mt-1 d-block';
        nombreInput.parentElement.appendChild(sugerenciaSpan);
    }

    // Buscar coincidencias en las listas
    const esSecundaria = palabrasSecundaria.some(p =>
        primeraPalabra === p || primeraPalabra.includes(p) || p.includes(primeraPalabra)
    );
    const esPrimaria = palabrasPrimaria.some(p =>
        primeraPalabra === p || primeraPalabra.includes(p) || p.includes(primeraPalabra)
    );

    if (esSecundaria) {
        sugerenciaSpan.innerHTML = ` <strong>"${primeraPalabra}"</strong> sugiere nivel <strong>SECUNDARIA</strong>`;
        sugerenciaSpan.style.color = '#fd7e14';
    } else if (esPrimaria) {
        sugerenciaSpan.innerHTML = ` <strong>"${primeraPalabra}"</strong> sugiere nivel <strong>PRIMARIA</strong>`;
        sugerenciaSpan.style.color = '#17a2b8';
    } else {
        sugerenciaSpan.innerHTML = ` Escribe una palabra clave como: ÁLGEBRA, FÍSICA (Secundaria) o LECTURA, SUMA (Primaria)`;
        sugerenciaSpan.style.color = '#6c757d';
    }
}

/**
 * Filtrar grados según el nivel detectado
 */
function filtrarGradosPorNivel(nivel) {
    const gradoSelect = document.getElementById('idGrado');
    if (!gradoSelect) return;

    const opciones = gradoSelect.querySelectorAll('option');
    let primerValido = null;

    opciones.forEach(option => {
        if (!option.value) return;

        const valor = parseInt(option.value);

        if (nivel === 'PRIMARIA') {
            if (valor >= 1 && valor <= 6) {
                option.style.display = '';
                if (!primerValido) primerValido = option.value;
            } else {
                option.style.display = 'none';
            }
        } else if (nivel === 'SECUNDARIA') {
            if (valor >= 7 && valor <= 11) {
                option.style.display = '';
                if (!primerValido) primerValido = option.value;
            } else {
                option.style.display = 'none';
            }
        } else {
            option.style.display = '';
        }
    });

    if (primerValido) {
        gradoSelect.value = primerValido;
    } else {
        gradoSelect.value = '';
    }

    // Mostrar mensaje informativo
    let mensajeNivel = document.getElementById('mensajeNivel');
    if (!mensajeNivel) {
        const gradoGroup = gradoSelect.closest('.mb-3');
        if (gradoGroup) {
            mensajeNivel = document.createElement('small');
            mensajeNivel.id = 'mensajeNivel';
            mensajeNivel.className = 'text-info mt-1 d-block';
            gradoGroup.appendChild(mensajeNivel);
        }
    }

    if (mensajeNivel) {
        if (nivel === 'PRIMARIA') {
            mensajeNivel.innerHTML = ' Nivel detectado: <strong>Primaria</strong> (grados 1° a 6°)';
        } else if (nivel === 'SECUNDARIA') {
            mensajeNivel.innerHTML = ' Nivel detectado: <strong>Secundaria</strong> (grados 1° a 5°)';
        } else {
            mensajeNivel.innerHTML = ' Escribe el nombre del curso para detectar automáticamente el nivel';
        }
    }
}

/**
 * Validar si el horario está disponible para el docente seleccionado (tiempo real)
 */
async function validarHorarioTiempoReal() {
    const idDocente = document.getElementById('idDocente').value;
    const horario = document.getElementById('horario').value;
    const cursoId = document.getElementById('cursoId').value || '0';

    let mensajeHorario = document.getElementById('mensajeHorario');

    if (!idDocente || !horario || horario === "") {
        if (mensajeHorario) {
            mensajeHorario.remove();
        }
        return;
    }

    try {
        const response = await fetch(`/admin/curso/validar-horario?idDocente=${idDocente}&horario=${encodeURIComponent(horario)}&idCurso=${cursoId}`);
        const data = await response.json();

        if (!mensajeHorario) {
            const horarioGroup = document.getElementById('horario').closest('.mb-3');
            mensajeHorario = document.createElement('div');
            mensajeHorario.id = 'mensajeHorario';
            mensajeHorario.className = 'mt-2 p-2 rounded';
            horarioGroup.appendChild(mensajeHorario);
        }

        if (data.conflicto) {
            mensajeHorario.innerHTML = ' <strong>¡HORARIO OCUPADO!</strong> Este docente YA tiene un curso en este horario';
            mensajeHorario.style.backgroundColor = '#f8d7da';
            mensajeHorario.style.color = '#721c24';
            mensajeHorario.style.border = '1px solid #f5c6cb';
            document.getElementById('horario').style.borderColor = '#dc3545';
            document.getElementById('horario').style.backgroundColor = '#fff8f8';
        } else {
            mensajeHorario.innerHTML = ' <strong>Horario disponible</strong> para este docente';
            mensajeHorario.style.backgroundColor = '#d4edda';
            mensajeHorario.style.color = '#155724';
            mensajeHorario.style.border = '1px solid #c3e6cb';
            document.getElementById('horario').style.borderColor = '#28a745';
            document.getElementById('horario').style.backgroundColor = '';
        }
    } catch (error) {
        console.error("Error validando horario:", error);
    }
}

/**
 * Detectar área automática según el nombre del curso
 */
function detectarAreaAutomatica() {
    const nombreCurso = document.getElementById('nombreCurso').value;
    const areaSelect = document.getElementById('area');
    const nombreNormalizado = normalizarTexto(nombreCurso);

    // Mostrar sugerencia de primera palabra
    if (nombreCurso && nombreCurso.trim() !== '') {
        mostrarSugerenciaPrimeraPalabra(nombreCurso);
    }

    if (!nombreCurso || nombreCurso.trim() === '') {
        if (areaSelect) areaSelect.value = '';
        filtrarGradosPorNivel(null);
        return;
    }

    const mapeoAreas = [
        { palabras: ['MATEMATICA', 'MATEMÁTICAS', 'ARITMETICA', 'ALGEBRA', 'GEOMETRIA', 'TRIGONOMETRIA', 'CALCULO', 'ESTADISTICA', 'PROBABILIDAD', 'RAZONAMIENTO', 'LOGICA', 'SUMA', 'RESTA', 'MULTIPLICACION', 'DIVISION', 'NUMEROS'], area: 'MATEMÁTICAS' },
        { palabras: ['COMUNICACION', 'COMUNICACIÓN', 'LENGUAJE', 'LITERATURA', 'GRAMATICA', 'ORTOGRAFIA', 'REDACCION', 'LECTURA', 'ESCRITURA', 'VOCALES', 'ABECEDARIO', 'SILABAS', 'PALABRAS', 'CUENTOS', 'ORATORIA', 'DEBATE', 'LINGUISTICA', 'SEMANTICA'], area: 'COMUNICACIÓN' },
        { palabras: ['CIENCIA', 'BIOLOGIA', 'FISICA', 'QUIMICA', 'ECOLOGIA', 'MEDIO AMBIENTE', 'LABORATORIO', 'ANATOMIA', 'ZOOLOGIA', 'BOTANICA', 'ASTRONOMIA', 'GEOLOGIA', 'METODOLOGIA', 'INVESTIGACION', 'ENERGIA', 'MOVIMIENTO', 'CELULA', 'ADN', 'GENETICA'], area: 'CIENCIA Y TECNOLOGÍA' },
        { palabras: ['SOCIALES', 'HISTORIA', 'GEOGRAFIA', 'PERSONAL SOCIAL', 'CIVICA', 'CIUDADANIA', 'FILOSOFIA', 'FILOSOFÍA', 'PSICOLOGIA', 'PSICOLOGÍA', 'SOCIOLOGIA', 'SOCIOLOGÍA', 'ANTROPOLOGIA', 'ANTROPOLOGÍA', 'DERECHO', 'POLITICA', 'ECONOMIA', 'ECONOMÍA', 'CONTABILIDAD', 'FORMACION CIUDADANA', 'DESARROLLO PERSONAL'], area: 'CIENCIAS SOCIALES' },
        { palabras: ['INGLES', 'ENGLISH'], area: 'INGLÉS' },
        { palabras: ['ARTE', 'DIBUJO', 'PINTURA', 'MUSICA', 'TEATRO', 'DANZA', 'FOLCLOR', 'MARINERA'], area: 'ARTE' },
        { palabras: ['EDUCACION FISICA', 'EDUCACIÓN FÍSICA', 'DEPORTE', 'RECREACION', 'SALUD', 'VOLEIBOL', 'FULBITO', 'ATLETISMO'], area: 'EDUCACIÓN FÍSICA' },
        { palabras: ['RELIGION', 'RELIGIÓN', 'VALORES', 'ETICA', 'MORAL', 'DOGMAS', 'CRISTIANA'], area: 'RELIGIÓN' },
        { palabras: ['TUTORIA', 'TUTORÍA', 'ORIENTACION', 'CONVIVENCIA'], area: 'TUTORÍA' }
    ];

    let areaEncontrada = false;
    for (let item of mapeoAreas) {
        for (let palabra of item.palabras) {
            if (nombreNormalizado.includes(palabra)) {
                if (areaSelect) areaSelect.value = item.area;
                areaEncontrada = true;
                if (areaSelect) {
                    areaSelect.style.borderColor = '#28a745';
                    setTimeout(() => {
                        if (areaSelect) areaSelect.style.borderColor = '';
                    }, 2000);
                }
                break;
            }
        }
        if (areaEncontrada) break;
    }

    if (!areaEncontrada && areaSelect) {
        areaSelect.value = '';
    }

    const nivel = detectarNivelPorNombre(nombreCurso);
    filtrarGradosPorNivel(nivel);
    generarHorarioAutomatico();
}

/**
 * Generar horario automático según Grado, Sección y Turno
 */
async function generarHorarioAutomatico() {
    const grado = document.getElementById('idGrado').value;
    const seccion = document.getElementById('seccion').value;
    const turno = document.getElementById('turno').value;
    const horarioSelect = document.getElementById('horario');

    if (!grado || !seccion || !turno || !horarioSelect) {
        return;
    }

    const gradoNum = parseInt(grado);
    const esPrimaria = gradoNum <= 6;
    const esManana = turno === 'MAÑANA';
    const area = document.getElementById('area').value;

    let horario = '';

    if (esPrimaria) {
        if (esManana) {
            horario = 'LUNES a VIERNES 8:00 - 12:00';
        } else {
            horario = 'LUNES a VIERNES 13:00 - 17:00';
        }
    } else {
        const dias = ['LUNES', 'MARTES', 'MIÉRCOLES', 'JUEVES', 'VIERNES'];
        const indiceSeccion = ['A', 'B', 'C', 'D'].indexOf(seccion);
        const dia = dias[indiceSeccion % dias.length];

        const horariosPorArea = {
            'MATEMÁTICAS': { MAÑANA: '7-9', TARDE: '13-15' },
            'COMUNICACIÓN': { MAÑANA: '9-11', TARDE: '15-17' },
            'CIENCIA Y TECNOLOGÍA': { MAÑANA: '11-13', TARDE: '13-15' },
            'CIENCIAS SOCIALES': { MAÑANA: '7-9', TARDE: '15-17' },
            'INGLÉS': { MAÑANA: '9-11', TARDE: '13-15' },
            'ARTE': { MAÑANA: '11-13', TARDE: '15-17' },
            'EDUCACIÓN FÍSICA': { MAÑANA: '7-9', TARDE: '13-15' },
            'RELIGIÓN': { MAÑANA: '9-11', TARDE: '15-17' },
            'TUTORÍA': { MAÑANA: '11-13', TARDE: '13-15' }
        };

        const horarioBase = horariosPorArea[area] || horariosPorArea['MATEMÁTICAS'];
        const hora = esManana ? horarioBase.MAÑANA : horarioBase.TARDE;
        horario = `${dia} ${hora}`;
    }

    let horarioEncontrado = false;
    for (let i = 0; i < horarioSelect.options.length; i++) {
        if (horarioSelect.options[i].value === horario) {
            horarioSelect.value = horario;
            horarioEncontrado = true;
            break;
        }
    }

    if (!horarioEncontrado) {
        horarioSelect.value = "";
        let mensajeHorarioGen = document.getElementById('mensajeHorarioGenerado');
        if (!mensajeHorarioGen) {
            const horarioGroup = document.getElementById('horario').closest('.mb-3');
            mensajeHorarioGen = document.createElement('small');
            mensajeHorarioGen.id = 'mensajeHorarioGenerado';
            mensajeHorarioGen.className = 'text-info mt-1 d-block';
            horarioGroup.appendChild(mensajeHorarioGen);
        }
        mensajeHorarioGen.innerHTML = ` Horario sugerido: ${horario}. Selecciona esta opción o elige otra.`;
        setTimeout(() => {
            if (mensajeHorarioGen) mensajeHorarioGen.innerHTML = '';
        }, 3000);
    }

    if (horarioEncontrado) {
        horarioSelect.style.borderColor = '#28a745';
        setTimeout(() => {
            if (horarioSelect) horarioSelect.style.borderColor = '';
        }, 2000);
    }

    await validarHorarioTiempoReal();
}

/**
 * Limpia el formulario para crear un nuevo curso
 */
function limpiarFormularioCurso() {
    document.getElementById('modalTitulo').innerText = 'Nuevo Curso';
    document.getElementById('cursoForm').reset();
    document.getElementById('cursoId').value = '';
    document.getElementById('codigoCurso').value = '';
    document.getElementById('alumnosActuales').value = '0';
    document.getElementById('capacidadMaxima').value = '36';
    document.getElementById('cursoForm').action = '/admin/curso/guardar';

    const mensajeNivel = document.getElementById('mensajeNivel');
    if (mensajeNivel) {
        mensajeNivel.innerHTML = ' Escribe el nombre del curso para detectar automáticamente el nivel';
    }

    const mensajeHorario = document.getElementById('mensajeHorario');
    if (mensajeHorario) {
        mensajeHorario.remove();
    }

    const sugerencia = document.getElementById('sugerenciaPrimeraPalabra');
    if (sugerencia) {
        sugerencia.remove();
    }

    const contador = document.getElementById('contadorSugerencias');
    if (contador) {
        contador.remove();
    }
}

/**
 * Carga los datos del curso en el modal para editar
 */
function editarCurso(boton) {
    const id = boton.getAttribute('data-id');
    const codigo = boton.getAttribute('data-codigo');
    const nombre = boton.getAttribute('data-nombre');
    const descripcion = boton.getAttribute('data-descripcion');
    const horas = boton.getAttribute('data-horas');
    const area = boton.getAttribute('data-area');
    const grado = boton.getAttribute('data-grado');
    const idDocente = boton.getAttribute('data-docente');
    const estado = boton.getAttribute('data-estado');
    const seccion = boton.getAttribute('data-seccion');
    const turno = boton.getAttribute('data-turno');
    const capacidad = boton.getAttribute('data-capacidad');
    const horario = boton.getAttribute('data-horario');

    document.getElementById('modalTitulo').innerText = 'Editar Curso';
    document.getElementById('cursoId').value = id || '';
    document.getElementById('codigoCurso').value = codigo || '';
    document.getElementById('nombreCurso').value = nombre || '';
    document.getElementById('descripcion').value = descripcion || '';
    document.getElementById('horasSemanales').value = horas || '';
    document.getElementById('area').value = area || '';
    document.getElementById('idGrado').value = grado || '';
    document.getElementById('idDocente').value = idDocente || '';
    document.getElementById('estado').value = estado || 'ACTIVO';
    document.getElementById('seccion').value = seccion || '';
    document.getElementById('turno').value = turno || '';
    document.getElementById('capacidadMaxima').value = capacidad || '36';
    document.getElementById('alumnosActuales').value = '0';
    document.getElementById('horario').value = horario || '';

    const form = document.getElementById('cursoForm');
    form.action = '/admin/curso/actualizar/' + id;

    setTimeout(() => {
        validarHorarioTiempoReal();
    }, 100);
}

/**
 * Confirma y elimina un curso
 */
function confirmarEliminarCurso(boton) {
    const id = boton.getAttribute('data-id');
    const nombre = boton.getAttribute('data-nombre');

    if (confirm('¿Estás seguro de eliminar el curso "' + nombre + '"? Esta acción no se puede deshacer.')) {
        window.location.href = '/admin/curso/eliminar/' + id;
    }
}

/**
 * Inicializa los valores por defecto y genera horario automáticamente al abrir el modal
 */
function inicializarFormularioCurso() {
    const grado = document.getElementById('idGrado').value;
    const seccion = document.getElementById('seccion').value;
    const turno = document.getElementById('turno').value;

    if (grado && seccion && turno) {
        generarHorarioAutomatico();
    }
}

/**
 * Auto-cerrar alertas y configurar event listeners
 */
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);

    const nombreInput = document.getElementById('nombreCurso');
    if (nombreInput) {
        nombreInput.addEventListener('input', function(e) {
            detectarAreaAutomatica();
            mostrarSugerenciasDinamicas();
        });
        nombreInput.addEventListener('paste', function() {
            setTimeout(() => {
                detectarAreaAutomatica();
                mostrarSugerenciasDinamicas();
            }, 100);
        });
    }

    const gradoSelect = document.getElementById('idGrado');
    const seccionSelect = document.getElementById('seccion');
    const turnoSelect = document.getElementById('turno');
    const areaSelect = document.getElementById('area');
    const idDocenteSelect = document.getElementById('idDocente');
    const horarioSelect = document.getElementById('horario');

    if (gradoSelect) gradoSelect.addEventListener('change', generarHorarioAutomatico);
    if (seccionSelect) seccionSelect.addEventListener('change', generarHorarioAutomatico);
    if (turnoSelect) turnoSelect.addEventListener('change', generarHorarioAutomatico);
    if (areaSelect) areaSelect.addEventListener('change', generarHorarioAutomatico);

    if (idDocenteSelect) {
        idDocenteSelect.addEventListener('change', validarHorarioTiempoReal);
    }
    if (horarioSelect) {
        horarioSelect.addEventListener('change', validarHorarioTiempoReal);
    }

    const cursoModal = document.getElementById('cursoModal');
    if (cursoModal) {
        cursoModal.addEventListener('shown.bs.modal', function() {
            setTimeout(() => {
                inicializarFormularioCurso();
            }, 100);
        });
    }

    const form = document.getElementById('cursoForm');
    if (form) {
        form.addEventListener('submit', async function(event) {
            const nombreCurso = document.getElementById('nombreCurso').value;
            const seccion = document.getElementById('seccion').value;
            const turno = document.getElementById('turno').value;
            const horas = document.getElementById('horasSemanales').value;
            const area = document.getElementById('area').value;
            const grado = document.getElementById('idGrado').value;
            const idDocente = document.getElementById('idDocente').value;
            const horario = document.getElementById('horario').value;

            if (!nombreCurso || nombreCurso.trim() === '') {
                event.preventDefault();
                alert(' El nombre del curso es obligatorio');
                return false;
            }

            if (!seccion) {
                event.preventDefault();
                alert(' Debe seleccionar una sección (A, B, C o D)');
                return false;
            }

            if (!turno) {
                event.preventDefault();
                alert(' Debe seleccionar un turno (MAÑANA o TARDE)');
                return false;
            }

            if (!horas) {
                event.preventDefault();
                alert(' Debe seleccionar las horas semanales');
                return false;
            }

            if (!area) {
                event.preventDefault();
                alert(' Debe seleccionar el área del curso');
                return false;
            }

            if (!grado) {
                event.preventDefault();
                alert(' Debe seleccionar el grado');
                return false;
            }

            if (idDocente && horario) {
                const cursoId = document.getElementById('cursoId').value || '0';
                const response = await fetch(`/admin/curso/validar-horario?idDocente=${idDocente}&horario=${encodeURIComponent(horario)}&idCurso=${cursoId}`);
                const data = await response.json();

                if (data.conflicto) {
                    event.preventDefault();
                    alert('No se puede guardar: El docente YA tiene un curso en el horario ' + horario);
                    return false;
                }
            }

            return true;
        });
    }
});