// ==================== FECHA ACTUAL ====================
const options = { year: 'numeric', month: 'long', day: 'numeric' };
const dateElement = document.getElementById('currentDate');
if (dateElement) {
    dateElement.textContent = new Date().toLocaleDateString('es-PE', options);
}

// ==================== COLORES ====================
const COLORS = {
    blue: 'rgba(52, 152, 219, 0.7)',
    blueBorder: 'rgba(52, 152, 219, 1)',
    green: 'rgba(46, 204, 113, 0.7)',
    greenBorder: 'rgba(46, 204, 113, 1)',
    orange: 'rgba(243, 156, 18, 0.7)',
    orangeBorder: 'rgba(243, 156, 18, 1)',
    red: 'rgba(231, 76, 60, 0.7)',
    redBorder: 'rgba(231, 76, 60, 1)',
    purple: 'rgba(155, 89, 182, 0.7)',
    purpleBorder: 'rgba(155, 89, 182, 1)',
    teal: 'rgba(26, 188, 156, 0.7)',
    tealBorder: 'rgba(26, 188, 156, 1)',
    gray: 'rgba(149, 165, 166, 0.7)',
    grayBorder: 'rgba(149, 165, 166, 1)',
};

// ==================== FUNCIONES DE EXPORTACIÓN ====================

function exportarExcel(tipo) {
    window.location.href = '/admin/reportes/exportar-excel/' + tipo;
}

function exportarPDF(tipo) {
    window.location.href = '/admin/reportes/exportar-pdf/' + tipo;
}

// ==================== 1. ESTUDIANTES POR GRADO ====================
fetch('/admin/reportes/estudiantes-por-grado')
    .then(response => response.json())
    .then(data => {
        const ctx = document.getElementById('chartEstudiantesGrado');
        if (ctx) {
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Estudiantes',
                        data: data.values,
                        backgroundColor: COLORS.blue,
                        borderColor: COLORS.blueBorder,
                        borderWidth: 2,
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: { display: false },
                        title: {
                            display: true,
                            text: 'Total: ' + data.total + ' estudiantes',
                            position: 'bottom'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { stepSize: 1 }
                        }
                    }
                }
            });
        }
    })
    .catch(error => console.error('Error cargando estudiantes por grado:', error));

// ==================== 2. MATRÍCULAS POR ESTADO ====================
(function() {
    const { pendientes, activas, inactivas } = REPORTE_DATA;

    const ctx = document.getElementById('chartMatriculasEstado');
    if (ctx) {
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Pendientes', 'Activas', 'Inactivas'],
                datasets: [{
                    data: [pendientes, activas, inactivas],
                    backgroundColor: ['#f39c12', '#2ecc71', '#e74c3c'],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { position: 'bottom', labels: { padding: 15 } }
                }
            }
        });
    }
})();

// ==================== 3. MATRÍCULAS POR AÑO ====================
fetch('/admin/reportes/matriculas-por-anio')
    .then(response => response.json())
    .then(data => {
        const ctx = document.getElementById('chartMatriculasAnio');
        if (ctx) {
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Matrículas',
                        data: data.values,
                        backgroundColor: 'rgba(52, 152, 219, 0.2)',
                        borderColor: COLORS.blueBorder,
                        borderWidth: 3,
                        fill: true,
                        tension: 0.4,
                        pointBackgroundColor: COLORS.blueBorder,
                        pointRadius: 5
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { stepSize: 1 }
                        }
                    }
                }
            });
        }
    })
    .catch(error => console.error('Error cargando matrículas por año:', error));

// ==================== 4. MATRÍCULAS POR GRADO ====================
fetch('/admin/reportes/matriculas-por-grado')
    .then(response => response.json())
    .then(data => {
        const ctx = document.getElementById('chartMatriculasGrado');
        if (ctx) {
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Matrículas',
                        data: data.values,
                        backgroundColor: COLORS.green,
                        borderColor: COLORS.greenBorder,
                        borderWidth: 2,
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { stepSize: 1 }
                        }
                    }
                }
            });
        }
    })
    .catch(error => console.error('Error cargando matrículas por grado:', error));

// ==================== 5. CURSOS POR GRADO ====================
fetch('/admin/reportes/cursos-por-grado')
    .then(response => response.json())
    .then(data => {
        const ctx = document.getElementById('chartCursosGrado');
        if (ctx) {
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Cursos',
                        data: data.values,
                        backgroundColor: COLORS.orange,
                        borderColor: COLORS.orangeBorder,
                        borderWidth: 2,
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { stepSize: 1 }
                        }
                    }
                }
            });
        }
    })
    .catch(error => console.error('Error cargando cursos por grado:', error));

// ==================== 6. CURSOS POR ESTADO ====================
(function() {
    const { cursosActivos: activos, cursosInactivos: inactivos } = REPORTE_DATA;

    const ctx = document.getElementById('chartCursosEstado');
    if (ctx) {
        new Chart(ctx, {
            type: 'pie',
            data: {
                labels: ['Activos', 'Inactivos'],
                datasets: [{
                    data: [activos, inactivos],
                    backgroundColor: ['#2ecc71', '#e74c3c'],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { position: 'bottom', labels: { padding: 15 } }
                }
            }
        });
    }
})();

// ==================== 7. DOCENTES POR ESPECIALIDAD ====================
fetch('/admin/reportes/docentes-por-especialidad')
    .then(response => response.json())
    .then(data => {
        const ctx = document.getElementById('chartDocentesEspecialidad');
        if (ctx) {
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Docentes',
                        data: data.values,
                        backgroundColor: COLORS.purple,
                        borderColor: COLORS.purpleBorder,
                        borderWidth: 2,
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: { display: false },
                        title: {
                            display: true,
                            text: 'Total: ' + data.total + ' docentes',
                            position: 'bottom'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { stepSize: 1 }
                        }
                    }
                }
            });
        }
    })
    .catch(error => console.error('Error cargando docentes por especialidad:', error));

// ==================== 8. ESTUDIANTES POR GÉNERO ====================
fetch('/admin/reportes/estudiantes-por-genero')
    .then(response => response.json())
    .then(data => {
        const ctx = document.getElementById('chartEstudiantesGenero');
        if (ctx) {
            new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: Object.keys(data.data),
                    datasets: [{
                        data: Object.values(data.data),
                        backgroundColor: ['#3498db', '#e74c3c'],
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: { padding: 15 }
                        },
                        title: {
                            display: true,
                            text: 'Total: ' + data.total + ' estudiantes',
                            position: 'bottom'
                        }
                    }
                }
            });
        }
    })
    .catch(error => console.error('Error cargando estudiantes por género:', error));

// ==================== 9. MATRÍCULAS POR TURNO ====================
fetch('/admin/reportes/matriculas-por-turno')
    .then(response => response.json())
    .then(data => {
        const ctx = document.getElementById('chartMatriculasTurno');
        if (ctx) {
            new Chart(ctx, {
                type: 'pie',
                data: {
                    labels: Object.keys(data.data),
                    datasets: [{
                        data: Object.values(data.data),
                        backgroundColor: ['#f39c12', '#3498db'],
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: { padding: 15 }
                        }
                    }
                }
            });
        }
    })
    .catch(error => console.error('Error cargando matrículas por turno:', error));

// ==================== 10. ASISTENCIA ====================
document.getElementById('btnCargarAsistencia')?.addEventListener('click', function() {
    const cursoId = document.getElementById('selectCursoAsistencia').value;
    if (!cursoId) {
        alert('Seleccione un curso');
        return;
    }

    fetch('/admin/reportes/asistencia-curso/' + cursoId)
        .then(response => response.json())
        .then(data => {
            const ctx = document.getElementById('chartAsistencia');
            if (ctx) {
                if (window.asistenciaChart) {
                    window.asistenciaChart.destroy();
                }

                window.asistenciaChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: Object.keys(data.data),
                        datasets: [{
                            label: 'Asistencia',
                            data: Object.values(data.data),
                            backgroundColor: ['#2ecc71', '#e74c3c', '#f39c12', '#3498db'],
                            borderWidth: 2,
                            borderRadius: 4
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: true,
                        plugins: {
                            legend: { display: false },
                            title: {
                                display: true,
                                text: 'Total: ' + data.total + ' registros',
                                position: 'bottom'
                            }
                        },
                        scales: {
                            y: {
                                beginAtZero: true,
                                ticks: { stepSize: 1 }
                            }
                        }
                    }
                });
            }
        })
        .catch(error => console.error('Error cargando asistencia:', error));
});

// ==================== 11. NOTAS ====================
document.getElementById('btnCargarNotas')?.addEventListener('click', function() {
    const cursoId = document.getElementById('selectCursoNotas').value;
    if (!cursoId) {
        alert('Seleccione un curso');
        return;
    }

    fetch('/admin/reportes/notas-curso/' + cursoId)
        .then(response => response.json())
        .then(data => {
            const ctx = document.getElementById('chartNotas');
            if (ctx) {
                if (window.notasChart) {
                    window.notasChart.destroy();
                }

                window.notasChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: Object.keys(data.data),
                        datasets: [{
                            label: 'Notas',
                            data: Object.values(data.data),
                            backgroundColor: ['#2ecc71', '#f39c12', '#e74c3c'],
                            borderWidth: 2,
                            borderRadius: 4
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: true,
                        plugins: {
                            legend: { display: false },
                            title: {
                                display: true,
                                text: 'Total: ' + data.total + ' notas registradas',
                                position: 'bottom'
                            }
                        },
                        scales: {
                            y: {
                                beginAtZero: true,
                                ticks: { stepSize: 1 }
                            }
                        }
                    }
                });
            }
        })
        .catch(error => console.error('Error cargando notas:', error));
});

// ==================== 12. MEJORA: Cargar con ENTER ====================
document.addEventListener('DOMContentLoaded', function() {
    // Enter en selects
    document.getElementById('selectCursoAsistencia')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            document.getElementById('btnCargarAsistencia')?.click();
        }
    });

    document.getElementById('selectCursoNotas')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            document.getElementById('btnCargarNotas')?.click();
        }
    });
});