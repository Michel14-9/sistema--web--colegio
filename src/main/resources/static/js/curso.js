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

function extraerPrimeraPalabra(nombreCurso) {
    if (!nombreCurso) return '';
    const palabras = nombreCurso.trim().split(/\s+/);
    return palabras[0] || '';
}

const listaAutocompletado = [
    'Álgebra', 'Geometría', 'Trigonometría', 'Cálculo', 'Aritmética', 'Estadística',
    'Probabilidad', 'Razonamiento Matemático', 'Matemática', 'Aritmetica', 'Algebra',
    'Lectura', 'Escritura', 'Literatura', 'Redacción', 'Oratoria', 'Debate', 'Gramática',
    'Ortografía', 'Lenguaje', 'Comunicación',
    'Física', 'Química', 'Biología', 'Ecología', 'Genética', 'Anatomía', 'Botánica',
    'Zoología', 'Ciencia', 'Tecnología', 'Laboratorio',
    'Historia', 'Geografía', 'Economía', 'Filosofía', 'Psicología', 'Sociología',
    'Antropología', 'Ciudadanía', 'Personal Social', 'Cívica', 'Formación Ciudadana',
    'Arte', 'Dibujo', 'Pintura', 'Música', 'Teatro', 'Danza', 'Folclor', 'Marinera',
    'Deporte', 'Voleibol', 'Fulbito', 'Atletismo', 'Recreación', 'Salud', 'Educación Física',
    'Inglés', 'English',
    'Religión', 'Valores', 'Tutoría', 'Convivencia', 'Orientación',
    'Suma', 'Resta', 'Multiplicación', 'División', 'Números', 'Conteo', 'Figuras', 'Patrones',
    'Vocales', 'Abecedario', 'Sílabas', 'Palabras', 'Cuentos', 'Colorear', 'Manualidades',
    'Animales', 'Plantas', 'Cuerpo Humano', 'Familia', 'Escuela', 'Comunidad', 'Perú', 'Ica'
];

const listaCursosCompletos = [
    'Álgebra Lineal', 'Geometría Analítica', 'Trigonometría', 'Cálculo Diferencial',
    'Física Moderna', 'Química Orgánica', 'Biología Celular', 'Historia del Perú',
    'Geografía de Ica', 'Economía', 'Filosofía', 'Psicología', 'Literatura Peruana',
    'Matemática - Sumas y Restas', 'Comunicación - Lectura y Escritura',
    'Ciencia y Ambiente - Los Animales', 'Personal Social - Mi Familia',
    'Arte - Dibujo y Colorear', 'Educación Física - Juegos Recreativos',
    'Inglés Básico - Vocabulario', 'Tutoría - Convivencia Escolar'
];

function mostrarSugerenciasDinamicas() {
    const input = document.getElementById('nombreCurso');
    const valor = input.value.toLowerCase();

    let datalist = document.getElementById('sugerenciasDinamicas');
    if (!datalist) {
        datalist = document.createElement('datalist');
        datalist.id = 'sugerenciasDinamicas';
        input.setAttribute('list', 'sugerenciasDinamicas');
        document.body.appendChild(datalist);
    }

    datalist.innerHTML = '';
    if (valor.length < 1) return;

    const sugerenciasPalabras = listaAutocompletado.filter(p =>
        p.toLowerCase().startsWith(valor) || p.toLowerCase().includes(valor)
    );
    const sugerenciasCursos = listaCursosCompletos.filter(c =>
        c.toLowerCase().includes(valor)
    );

    sugerenciasPalabras.slice(0, 8).forEach(s => {
        const opt = document.createElement('option'); opt.value = s; datalist.appendChild(opt);
    });
    sugerenciasCursos.slice(0, 5).forEach(c => {
        const opt = document.createElement('option'); opt.value = c; datalist.appendChild(opt);
    });

    let contadorSpan = document.getElementById('contadorSugerencias');
    if (!contadorSpan) {
        contadorSpan = document.createElement('small');
        contadorSpan.id = 'contadorSugerencias';
        contadorSpan.className = 'text-muted mt-1 d-block';
        input.parentElement.appendChild(contadorSpan);
    }
    const total = sugerenciasPalabras.length + sugerenciasCursos.length;
    contadorSpan.innerHTML = total > 0
        ? ` 💡 ${total} sugerencia${total !== 1 ? 's' : ''} disponibles. Escribe y selecciona.`
        : ` 💡 No hay sugerencias para "${valor}". Escribe el nombre completo del curso.`;
    contadorSpan.style.color = '#6c757d';
}

const palabrasPrimaria = [
    'SUMA', 'RESTA', 'MULTIPLICACION', 'DIVISION', 'NUMEROS', 'CONTEO', 'FIGURAS', 'PATRONES',
    'LECTURA', 'ESCRITURA', 'CALIGRAFIA', 'VOCALES', 'ABECEDARIO', 'SILABAS', 'PALABRAS', 'CUENTOS',
    'DIBUJO', 'COLOREAR', 'MANUALIDADES',
    'ANIMALES', 'PLANTAS', 'CUERPO HUMANO', 'SALUD', 'HIGIENE',
    'FAMILIA', 'ESCUELA', 'COMUNIDAD', 'PERU', 'ICA', 'SIMBOLOS PATRIOS'
];

const palabrasSecundaria = [
    'ALGEBRA', 'TRIGONOMETRIA', 'GEOMETRIA', 'CALCULO', 'ARITMETICA', 'RAZONAMIENTO',
    'ESTADISTICA', 'PROBABILIDAD', 'LOGICA', 'ECUACIONES', 'FUNCIONES', 'MATRICES',
    'LITERATURA', 'REDACCION', 'ORATORIA', 'DEBATE', 'LINGUISTICA', 'SEMANTICA',
    'FISICA', 'QUIMICA', 'BIOLOGIA', 'GENETICA', 'ECOLOGIA', 'CELULA', 'ADN', 'ENERGIA', 'MOVIMIENTO',
    'ANATOMIA', 'ZOOLOGIA', 'BOTANICA', 'ASTRONOMIA', 'GEOLOGIA', 'INVESTIGACION', 'METODOLOGIA',
    'HISTORIA', 'GEOGRAFIA', 'ECONOMIA', 'CONTABILIDAD', 'FILOSOFIA',
    'PSICOLOGIA', 'SOCIOLOGIA', 'ANTROPOLOGIA', 'DERECHO', 'POLITICA',
    'CIUDADANIA', 'CIVICA', 'FORMACION CIUDADANA', 'PERSONAL SOCIAL',
    'DESARROLLO PERSONAL', 'TUTORIA', 'ORIENTACION',
    'INGLES', 'ENGLISH',
    'ARTE', 'DIBUJO', 'PINTURA', 'MUSICA', 'TEATRO', 'DANZA', 'FOLCLOR', 'MARINERA',
    'EDUCACION FISICA', 'DEPORTE', 'RECREACION', 'VOLEIBOL', 'FULBITO', 'ATLETISMO'
];

const mapeoAreas = [
    { palabras: ['MATEMATICA', 'MATEMATICAS', 'ARITMETICA', 'ALGEBRA', 'GEOMETRIA', 'TRIGONOMETRIA', 'CALCULO', 'ESTADISTICA', 'PROBABILIDAD', 'RAZONAMIENTO', 'LOGICA', 'SUMA', 'RESTA', 'MULTIPLICACION', 'DIVISION', 'NUMEROS'], area: 'MATEMÁTICAS' },
    { palabras: ['COMUNICACION', 'COMUNICACION', 'LENGUAJE', 'LITERATURA', 'GRAMATICA', 'ORTOGRAFIA', 'REDACCION', 'LECTURA', 'ESCRITURA', 'VOCALES', 'ABECEDARIO', 'SILABAS', 'PALABRAS', 'CUENTOS', 'ORATORIA', 'DEBATE', 'LINGUISTICA', 'SEMANTICA'], area: 'COMUNICACIÓN' },
    { palabras: ['CIENCIA', 'BIOLOGIA', 'FISICA', 'QUIMICA', 'ECOLOGIA', 'MEDIO AMBIENTE', 'LABORATORIO', 'ANATOMIA', 'ZOOLOGIA', 'BOTANICA', 'ASTRONOMIA', 'GEOLOGIA', 'METODOLOGIA', 'INVESTIGACION', 'ENERGIA', 'MOVIMIENTO', 'CELULA', 'ADN', 'GENETICA'], area: 'CIENCIA Y TECNOLOGÍA' },
    { palabras: ['SOCIALES', 'HISTORIA', 'GEOGRAFIA', 'PERSONAL SOCIAL', 'CIVICA', 'CIUDADANIA', 'FILOSOFIA', 'PSICOLOGIA', 'SOCIOLOGIA', 'ANTROPOLOGIA', 'DERECHO', 'POLITICA', 'ECONOMIA', 'CONTABILIDAD', 'FORMACION CIUDADANA', 'DESARROLLO PERSONAL'], area: 'CIENCIAS SOCIALES' },
    { palabras: ['INGLES', 'ENGLISH'], area: 'INGLÉS' },
    { palabras: ['ARTE', 'DIBUJO', 'PINTURA', 'MUSICA', 'TEATRO', 'DANZA', 'FOLCLOR', 'MARINERA'], area: 'ARTE' },
    { palabras: ['EDUCACION FISICA', 'DEPORTE', 'RECREACION', 'SALUD', 'VOLEIBOL', 'FULBITO', 'ATLETISMO'], area: 'EDUCACIÓN FÍSICA' },
    { palabras: ['RELIGION', 'VALORES', 'ETICA', 'MORAL', 'DOGMAS', 'CRISTIANA'], area: 'RELIGIÓN' },
    { palabras: ['TUTORIA', 'ORIENTACION', 'CONVIVENCIA'], area: 'TUTORÍA' }
];

function detectarAreaDesdeNombre(nombreCurso) {
    const nombreNorm = normalizarTexto(nombreCurso);
    for (let item of mapeoAreas) {
        for (let palabra of item.palabras) {
            if (nombreNorm.includes(palabra)) return item.area;
        }
    }
    return '';
}

function detectarNivelPorNombre(nombreCurso) {
    const nombreNorm = normalizarTexto(nombreCurso);
    const primeraPalabra = extraerPrimeraPalabra(nombreNorm);

    for (let p of palabrasSecundaria) {
        if (primeraPalabra === p || primeraPalabra.includes(p) || p.includes(primeraPalabra)) return 'SECUNDARIA';
    }
    for (let p of palabrasPrimaria) {
        if (primeraPalabra === p || primeraPalabra.includes(p) || p.includes(primeraPalabra)) return 'PRIMARIA';
    }
    for (let p of palabrasSecundaria) {
        if (nombreNorm.includes(p)) return 'SECUNDARIA';
    }
    for (let p of palabrasPrimaria) {
        if (nombreNorm.includes(p)) return 'PRIMARIA';
    }
    return null;
}

function mostrarSugerenciaPrimeraPalabra(nombreCurso) {
    const primeraPalabra = extraerPrimeraPalabra(normalizarTexto(nombreCurso));
    if (!primeraPalabra) return;

    const nombreInput = document.getElementById('nombreCurso');
    let sugerenciaSpan = document.getElementById('sugerenciaPrimeraPalabra');
    if (!sugerenciaSpan) {
        sugerenciaSpan = document.createElement('small');
        sugerenciaSpan.id = 'sugerenciaPrimeraPalabra';
        sugerenciaSpan.className = 'mt-1 d-block';
        nombreInput.parentElement.appendChild(sugerenciaSpan);
    }

    const esSecundaria = palabrasSecundaria.some(p =>
        primeraPalabra === p || primeraPalabra.includes(p) || p.includes(primeraPalabra)
    );
    const esPrimaria = palabrasPrimaria.some(p =>
        primeraPalabra === p || primeraPalabra.includes(p) || p.includes(primeraPalabra)
    );

    if (esSecundaria) {
        sugerenciaSpan.innerHTML = `  <strong>"${primeraPalabra}"</strong> sugiere nivel <strong>SECUNDARIA</strong>`;
        sugerenciaSpan.style.color = '#fd7e14';
    } else if (esPrimaria) {
        sugerenciaSpan.innerHTML = `  <strong>"${primeraPalabra}"</strong> sugiere nivel <strong>PRIMARIA</strong>`;
        sugerenciaSpan.style.color = '#17a2b8';
    } else {
        sugerenciaSpan.innerHTML = `  Escribe una palabra clave como: ÁLGEBRA, FÍSICA (Secundaria) o LECTURA, SUMA (Primaria)`;
        sugerenciaSpan.style.color = '#6c757d';
    }
}

function filtrarGradosPorNivel(nivel) {
    const gradoSelect = document.getElementById('idGrado');
    if (!gradoSelect) return;

    const opciones = gradoSelect.querySelectorAll('option');
    let primerValido = null;

    opciones.forEach(option => {
        if (!option.value) return;
        const valor = parseInt(option.value);
        if (nivel === 'PRIMARIA') {
            option.style.display = (valor >= 1 && valor <= 6) ? '' : 'none';
            if (valor >= 1 && valor <= 6 && !primerValido) primerValido = option.value;
        } else if (nivel === 'SECUNDARIA') {
            option.style.display = (valor >= 7 && valor <= 11) ? '' : 'none';
            if (valor >= 7 && valor <= 11 && !primerValido) primerValido = option.value;
        } else {
            option.style.display = '';
        }
    });

    if (primerValido) gradoSelect.value = primerValido;
    else gradoSelect.value = '';

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
        if (nivel === 'PRIMARIA') mensajeNivel.innerHTML = '  Nivel detectado: <strong>Primaria</strong> (grados 1° a 6°)';
        else if (nivel === 'SECUNDARIA') mensajeNivel.innerHTML = '  Nivel detectado: <strong>Secundaria</strong> (grados 1° a 5°)';
        else mensajeNivel.innerHTML = '  Escribe el nombre del curso para detectar automáticamente el nivel';
    }
}

// ==================== FILTROS FRONTEND EN TIEMPO REAL ====================

/**
 * Lee los valores actuales de todos los filtros
 */
function obtenerValoresFiltros() {
    return {
        nombre: normalizarTexto(document.getElementById('filtroNombre')?.value || ''),
        grado:  (document.getElementById('filtroGrado')?.value  || '').trim(),
        turno:  (document.getElementById('filtroTurno')?.value  || '').trim(),
        estado: (document.getElementById('filtroEstado')?.value || '').trim(),
        area:   normalizarTexto(document.getElementById('filtroArea')?.value || '')
    };
}

/**
 * Filtra las filas de la tabla en el frontend sin recargar la página.
 * Cada <tr> de datos debe tener data-* con los valores de sus celdas.
 */
function filtrarTablaFrontend() {
    const f = obtenerValoresFiltros();
    const filas = document.querySelectorAll('#tablaCursos tbody tr[data-nombre]');
    let visibles = 0;

    filas.forEach(fila => {
        const nombre  = normalizarTexto(fila.dataset.nombre  || '');
        const grado   = (fila.dataset.grado   || '').trim();
        const turno   = normalizarTexto(fila.dataset.turno   || '');
        const estado  = normalizarTexto(fila.dataset.estado  || '');
        const area    = normalizarTexto(fila.dataset.area    || '');

        const coincide =
            (!f.nombre || nombre.includes(f.nombre))  &&
            (!f.grado  || grado === f.grado)           &&
            (!f.turno  || turno.includes(normalizarTexto(f.turno)))  &&
            (!f.estado || estado.includes(f.estado))   &&
            (!f.area   || area.includes(f.area));

        fila.style.display = coincide ? '' : 'none';
        if (coincide) visibles++;
    });

    // Fila vacía si no hay resultados
    let filaVacia = document.getElementById('filaVaciaFiltro');
    if (visibles === 0) {
        if (!filaVacia) {
            filaVacia = document.createElement('tr');
            filaVacia.id = 'filaVaciaFiltro';
            filaVacia.innerHTML = `
                <td colspan="12" class="text-center py-4 text-muted">
                    <i class="fas fa-search fa-2x mb-2 d-block"></i>
                    No se encontraron cursos con los filtros aplicados
                </td>`;
            document.querySelector('#tablaCursos tbody').appendChild(filaVacia);
        }
        filaVacia.style.display = '';
    } else if (filaVacia) {
        filaVacia.style.display = 'none';
    }

    // Contador de resultados
    actualizarContadorResultados(visibles, filas.length);

    // Actualizar área detectada e indicador de nivel
    const nombreRaw = document.getElementById('filtroNombre')?.value || '';
    actualizarAreaYNivelFiltro(nombreRaw);
}

function actualizarContadorResultados(visibles, total) {
    let cont = document.getElementById('contadorResultadosFiltro');
    if (!cont) {
        cont = document.createElement('small');
        cont.id = 'contadorResultadosFiltro';
        cont.className = 'text-muted mt-1 d-block';
        document.getElementById('filtroNombre')?.parentElement?.appendChild(cont);
    }
    if (visibles === total) {
        cont.innerHTML = '';
    } else {
        cont.innerHTML = ` Mostrando <strong>${visibles}</strong> de <strong>${total}</strong> cursos`;
        cont.style.color = visibles === 0 ? '#dc3545' : '#6c757d';
    }
}

/**
 * Detecta área y nivel a partir del nombre escrito en el filtro,
 * actualiza el campo área y el badge de nivel, y filtra el select de grado.
 */
function actualizarAreaYNivelFiltro(nombreCurso) {
    // --- Área ---
    const areaInput = document.getElementById('filtroArea');
    const area = detectarAreaDesdeNombre(nombreCurso);
    if (areaInput) {
        areaInput.value = area;
        areaInput.style.borderColor = area ? '#28a745' : '';
        areaInput.style.backgroundColor = area ? '#f0fff4' : '#e9ecef';
    }

    // --- Nivel ---
    const nivel = detectarNivelPorNombre(nombreCurso);
    const filtroGrado = document.getElementById('filtroGrado');

    if (filtroGrado) {
        Array.from(filtroGrado.options).forEach(opt => {
            if (!opt.value) { opt.style.display = ''; return; }
            const v = parseInt(opt.value);
            if (nivel === 'PRIMARIA')    opt.style.display = (v >= 1 && v <= 6)  ? '' : 'none';
            else if (nivel === 'SECUNDARIA') opt.style.display = (v >= 7 && v <= 11) ? '' : 'none';
            else opt.style.display = '';
        });

        // Si el grado seleccionado queda oculto, resetear a "Todos"
        const gradoActual = parseInt(filtroGrado.value);
        if (nivel === 'PRIMARIA'    && gradoActual > 6)  filtroGrado.value = '';
        if (nivel === 'SECUNDARIA'  && gradoActual <= 6) filtroGrado.value = '';
    }

    // --- Badge de nivel ---
    let badge = document.getElementById('badgeNivelFiltro');
    const contenedor = document.getElementById('filtroNombre')?.parentElement;
    if (!badge && contenedor) {
        badge = document.createElement('small');
        badge.id = 'badgeNivelFiltro';
        badge.className = 'mt-1 d-block';
        contenedor.appendChild(badge);
    }
    if (badge) {
        if (!nombreCurso.trim()) {
            badge.innerHTML = '';
        } else if (nivel === 'PRIMARIA') {
            badge.innerHTML = ' Nivel detectado: <strong>PRIMARIA</strong>';
            badge.style.color = '#17a2b8';
        } else if (nivel === 'SECUNDARIA') {
            badge.innerHTML = ' Nivel detectado: <strong>SECUNDARIA</strong>';
            badge.style.color = '#fd7e14';
        } else {
            badge.innerHTML = ' Escribe una palabra clave del curso';
            badge.style.color = '#6c757d';
        }
    }
}

function inicializarFiltros() {
    // Escuchar cambios en todos los filtros → filtrar en frontend
    const filtroNombre = document.getElementById('filtroNombre');
    if (filtroNombre) {
        filtroNombre.addEventListener('input', filtrarTablaFrontend);
    }

    ['filtroGrado', 'filtroTurno', 'filtroEstado'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('change', filtrarTablaFrontend);
    });

    // Botón buscar: también filtra en frontend (ya no recarga)
    const btnBuscar = document.getElementById('btnBuscar');
    if (btnBuscar) {
        btnBuscar.addEventListener('click', filtrarTablaFrontend);
        btnBuscar.title = 'Los resultados se filtran automáticamente';
    }

    // Botón limpiar filtros
    const btnLimpiar = document.getElementById('btnLimpiarFiltros');
    if (btnLimpiar) {
        btnLimpiar.addEventListener('click', limpiarFiltros);
    }
}

function limpiarFiltros() {
    ['filtroNombre', 'filtroArea'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = '';
    });
    ['filtroGrado', 'filtroTurno', 'filtroEstado'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = '';
    });

    // Restaurar opciones del select grado
    const filtroGrado = document.getElementById('filtroGrado');
    if (filtroGrado) Array.from(filtroGrado.options).forEach(opt => opt.style.display = '');

    const areaInput = document.getElementById('filtroArea');
    if (areaInput) { areaInput.style.borderColor = ''; areaInput.style.backgroundColor = '#e9ecef'; }

    filtrarTablaFrontend();
}

// ==================== MODAL: ÁREA Y NIVEL AUTOMÁTICOS ====================

function detectarAreaAutomatica() {
    const nombreCurso = document.getElementById('nombreCurso').value;
    const areaInput = document.getElementById('area');

    if (nombreCurso && nombreCurso.trim() !== '') {
        mostrarSugerenciaPrimeraPalabra(nombreCurso);
    }

    if (!nombreCurso || nombreCurso.trim() === '') {
        if (areaInput) areaInput.value = '';
        filtrarGradosPorNivel(null);
        return;
    }

    const area = detectarAreaDesdeNombre(nombreCurso);
    if (areaInput) {
        areaInput.value = area;
        if (area) {
            areaInput.style.borderColor = '#28a745';
            setTimeout(() => { areaInput.style.borderColor = ''; }, 2000);
        }
    }

    const nivel = detectarNivelPorNombre(nombreCurso);
    filtrarGradosPorNivel(nivel);
    generarHorarioAutomatico();
}

// ==================== HORARIO Y VALIDACIÓN ====================

async function validarHorarioTiempoReal() {
    const idDocente = document.getElementById('idDocente').value;
    const horario = document.getElementById('horario').value;
    const cursoId = document.getElementById('cursoId').value || '0';

    let mensajeHorario = document.getElementById('mensajeHorario');

    if (!idDocente || !horario) {
        if (mensajeHorario) mensajeHorario.remove();
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
            mensajeHorario.innerHTML = '  <strong>¡HORARIO OCUPADO!</strong> Este docente YA tiene un curso en este horario';
            mensajeHorario.style.cssText = 'background-color:#f8d7da;color:#721c24;border:1px solid #f5c6cb';
            document.getElementById('horario').style.borderColor = '#dc3545';
            document.getElementById('horario').style.backgroundColor = '#fff8f8';
        } else {
            mensajeHorario.innerHTML = '  <strong>Horario disponible</strong> para este docente';
            mensajeHorario.style.cssText = 'background-color:#d4edda;color:#155724;border:1px solid #c3e6cb';
            document.getElementById('horario').style.borderColor = '#28a745';
            document.getElementById('horario').style.backgroundColor = '';
        }
    } catch (error) {
        console.error("Error validando horario:", error);
    }
}

async function generarHorarioAutomatico() {
    const grado = document.getElementById('idGrado').value;
    const seccion = document.getElementById('seccion').value;
    const turno = document.getElementById('turno').value;
    const horarioSelect = document.getElementById('horario');

    if (!grado || !seccion || !turno || !horarioSelect) return;

    const gradoNum = parseInt(grado);
    const esPrimaria = gradoNum <= 6;
    const esManana = turno === 'MAÑANA';
    const area = document.getElementById('area').value;

    let horario = '';

    if (esPrimaria) {
        horario = esManana ? 'LUNES a VIERNES 8:00 - 12:00' : 'LUNES a VIERNES 13:00 - 17:00';
    } else {
        const dias = ['LUNES', 'MARTES', 'MIÉRCOLES', 'JUEVES', 'VIERNES'];
        const dia = dias[['A','B','C','D'].indexOf(seccion) % dias.length];
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
        const base = horariosPorArea[area] || horariosPorArea['MATEMÁTICAS'];
        const hora = esManana ? base.MAÑANA : base.TARDE;
        horario = `${dia} ${hora}`;
    }

    let encontrado = false;
    for (let i = 0; i < horarioSelect.options.length; i++) {
        if (horarioSelect.options[i].value === horario) {
            horarioSelect.value = horario;
            encontrado = true;
            break;
        }
    }

    if (!encontrado) {
        horarioSelect.value = '';
        let msg = document.getElementById('mensajeHorarioGenerado');
        if (!msg) {
            msg = document.createElement('small');
            msg.id = 'mensajeHorarioGenerado';
            msg.className = 'text-info mt-1 d-block';
            document.getElementById('horario').closest('.mb-3').appendChild(msg);
        }
        msg.innerHTML = ` 💡 Horario sugerido: ${horario}. Selecciona esta opción o elige otra.`;
        setTimeout(() => { if (msg) msg.innerHTML = ''; }, 3000);
    } else {
        horarioSelect.style.borderColor = '#28a745';
        setTimeout(() => { horarioSelect.style.borderColor = ''; }, 2000);
    }

    await validarHorarioTiempoReal();
}

// ==================== CRUD MODAL ====================

function limpiarFormularioCurso() {
    document.getElementById('modalTitulo').innerText = 'Nuevo Curso';
    document.getElementById('cursoForm').reset();
    document.getElementById('cursoId').value = '';
    document.getElementById('codigoCurso').value = '';
    document.getElementById('alumnosActuales').value = '0';
    document.getElementById('capacidadMaxima').value = '36';
    document.getElementById('cursoForm').action = '/admin/curso/guardar';

    const mensajeNivel = document.getElementById('mensajeNivel');
    if (mensajeNivel) mensajeNivel.innerHTML = '  Escribe el nombre del curso para detectar automáticamente el nivel';

    ['mensajeHorario', 'sugerenciaPrimeraPalabra', 'contadorSugerencias'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.remove();
    });
}

function editarCurso(boton) {
    document.getElementById('modalTitulo').innerText = 'Editar Curso';
    document.getElementById('cursoId').value = boton.getAttribute('data-id') || '';
    document.getElementById('codigoCurso').value = boton.getAttribute('data-codigo') || '';
    document.getElementById('nombreCurso').value = boton.getAttribute('data-nombre') || '';
    document.getElementById('descripcion').value = boton.getAttribute('data-descripcion') || '';
    document.getElementById('horasSemanales').value = boton.getAttribute('data-horas') || '';
    document.getElementById('area').value = boton.getAttribute('data-area') || '';
    document.getElementById('idGrado').value = boton.getAttribute('data-grado') || '';
    document.getElementById('idDocente').value = boton.getAttribute('data-docente') || '';
    document.getElementById('estado').value = boton.getAttribute('data-estado') || 'ACTIVO';
    document.getElementById('seccion').value = boton.getAttribute('data-seccion') || '';
    document.getElementById('turno').value = boton.getAttribute('data-turno') || '';
    document.getElementById('capacidadMaxima').value = boton.getAttribute('data-capacidad') || '36';
    document.getElementById('alumnosActuales').value = '0';
    document.getElementById('horario').value = boton.getAttribute('data-horario') || '';
    document.getElementById('cursoForm').action = '/admin/curso/actualizar/' + boton.getAttribute('data-id');

    setTimeout(() => validarHorarioTiempoReal(), 100);
}

function confirmarEliminarCurso(boton) {
    const id = boton.getAttribute('data-id');
    const nombre = boton.getAttribute('data-nombre');
    if (confirm('¿Estás seguro de eliminar el curso "' + nombre + '"? Esta acción no se puede deshacer.')) {
        window.location.href = '/admin/curso/eliminar/' + id;
    }
}

function inicializarFormularioCurso() {
    const grado = document.getElementById('idGrado').value;
    const seccion = document.getElementById('seccion').value;
    const turno = document.getElementById('turno').value;
    if (grado && seccion && turno) generarHorarioAutomatico();
}

// ==================== EVENT LISTENERS ====================

document.addEventListener('DOMContentLoaded', function () {
    // Auto-cerrar alertas
    setTimeout(() => {
        document.querySelectorAll('.alert').forEach(alert => {
            new bootstrap.Alert(alert).close();
        });
    }, 5000);

    // Modal: nombre del curso
    const nombreInput = document.getElementById('nombreCurso');
    if (nombreInput) {
        nombreInput.addEventListener('input', function () {
            detectarAreaAutomatica();
            mostrarSugerenciasDinamicas();
        });
        nombreInput.addEventListener('paste', function () {
            setTimeout(() => {
                detectarAreaAutomatica();
                mostrarSugerenciasDinamicas();
            }, 100);
        });
    }

    // Modal: cambios en grado, sección, turno, área
    ['idGrado', 'seccion', 'turno', 'area'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('change', generarHorarioAutomatico);
    });

    // Modal: validar horario en tiempo real
    const idDocenteSelect = document.getElementById('idDocente');
    const horarioSelect = document.getElementById('horario');
    if (idDocenteSelect) idDocenteSelect.addEventListener('change', validarHorarioTiempoReal);
    if (horarioSelect) horarioSelect.addEventListener('change', validarHorarioTiempoReal);

    // Modal: al abrir
    const cursoModal = document.getElementById('cursoModal');
    if (cursoModal) {
        cursoModal.addEventListener('shown.bs.modal', function () {
            setTimeout(inicializarFormularioCurso, 100);
        });
    }

    // Modal: validar antes de enviar
    const form = document.getElementById('cursoForm');
    if (form) {
        form.addEventListener('submit', async function (event) {
            const nombreCurso = document.getElementById('nombreCurso').value;
            const seccion = document.getElementById('seccion').value;
            const turno = document.getElementById('turno').value;
            const horas = document.getElementById('horasSemanales').value;
            const area = document.getElementById('area').value;
            const grado = document.getElementById('idGrado').value;
            const idDocente = document.getElementById('idDocente').value;
            const horario = document.getElementById('horario').value;

            if (!nombreCurso?.trim()) { event.preventDefault(); alert(' El nombre del curso es obligatorio'); return false; }
            if (!seccion) { event.preventDefault(); alert('Debe seleccionar una sección (A, B, C o D)'); return false; }
            if (!turno) { event.preventDefault(); alert(' Debe seleccionar un turno (MAÑANA o TARDE)'); return false; }
            if (!horas) { event.preventDefault(); alert(' Debe seleccionar las horas semanales'); return false; }
            if (!area) { event.preventDefault(); alert(' Debe seleccionar el área del curso'); return false; }
            if (!grado) { event.preventDefault(); alert(' Debe seleccionar el grado'); return false; }

            if (idDocente && horario) {
                const cursoId = document.getElementById('cursoId').value || '0';
                const response = await fetch(`/admin/curso/validar-horario?idDocente=${idDocente}&horario=${encodeURIComponent(horario)}&idCurso=${cursoId}`);
                const data = await response.json();
                if (data.conflicto) {
                    event.preventDefault();
                    alert(' No se puede guardar: El docente YA tiene un curso en el horario ' + horario);
                    return false;
                }
            }
            return true;
        });
    }

    // Tabla: inicializar filtros
    inicializarFiltros();
});