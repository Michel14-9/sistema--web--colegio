package com.universidad.sistema_academico.service;

import com.universidad.sistema_academico.dto.EstudianteDTO;
import com.universidad.sistema_academico.model.Estudiante;
import com.universidad.sistema_academico.repository.EstudianteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Prueba unitaria para EstudianteService.
 * 
 * Caso de prueba RF-001: Registrar estudiante con datos válidos.
 * Se utiliza Mockito para simular el repositorio y aislar la lógica de negocio.
 */
@ExtendWith(MockitoExtension.class)
class EstudianteServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    @InjectMocks
    private EstudianteService estudianteService;

    @Test
    @DisplayName("RF-001: Registrar estudiante con datos válidos debe retornar DTO con ID generado")
    void guardar_ConDatosValidos_DebeRetornarEstudianteDTOConId() {
        // ============================================================
        // ARRANGE - Preparar los datos de entrada y configurar los mocks
        // ============================================================

        // Crear el DTO de entrada con datos de prueba
        EstudianteDTO dtoEntrada = new EstudianteDTO();
        dtoEntrada.setCodigoEstudiante("EST-2026-001");
        dtoEntrada.setDni("72345678");
        dtoEntrada.setNombres("Juan Carlos");
        dtoEntrada.setApellidoPaterno("García");
        dtoEntrada.setApellidoMaterno("López");
        dtoEntrada.setFechaNacimiento(LocalDate.of(2010, 5, 15));
        dtoEntrada.setGenero("M");
        dtoEntrada.setEmailInstitucional("juan.garcia@colegio.edu.pe");
        dtoEntrada.setCelular("987654321");
        dtoEntrada.setIdGrado(3);
        dtoEntrada.setSeccion("A");
        dtoEntrada.setTurno("Mañana");
        dtoEntrada.setEstado("Activo");
        dtoEntrada.setFechaIngreso(LocalDate.of(2026, 3, 1));

        // Simular la entidad que retornaría el repositorio después de guardar
        Estudiante estudianteGuardado = new Estudiante();
        estudianteGuardado.setIdEstudiante(1L); // ID generado por la base de datos
        estudianteGuardado.setCodigoEstudiante("EST-2026-001");
        estudianteGuardado.setDni("72345678");
        estudianteGuardado.setNombres("Juan Carlos");
        estudianteGuardado.setApellidoPaterno("García");
        estudianteGuardado.setApellidoMaterno("López");
        estudianteGuardado.setFechaNacimiento(LocalDate.of(2010, 5, 15));
        estudianteGuardado.setGenero("M");
        estudianteGuardado.setEmailInstitucional("juan.garcia@colegio.edu.pe");
        estudianteGuardado.setCelular("987654321");
        estudianteGuardado.setIdGrado(3);
        estudianteGuardado.setSeccion("A");
        estudianteGuardado.setTurno("Mañana");
        estudianteGuardado.setEstado("Activo");
        estudianteGuardado.setFechaIngreso(LocalDate.of(2026, 3, 1));

        // Configurar comportamiento del mock:
        // - El código del estudiante NO existe previamente en la BD
        when(estudianteRepository.existsByCodigoEstudiante("EST-2026-001")).thenReturn(false);
        // - El DNI NO existe previamente en la BD
        when(estudianteRepository.existsByDni("72345678")).thenReturn(false);
        // - Al guardar cualquier entidad Estudiante, retornar la entidad con ID asignado
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudianteGuardado);

        // ============================================================
        // ACT - Ejecutar el método bajo prueba
        // ============================================================
        EstudianteDTO resultado = estudianteService.guardar(dtoEntrada);

        // ============================================================
        // ASSERT - Verificar que el resultado sea el esperado
        // ============================================================

        // Verificar que el resultado no es nulo
        assertNotNull(resultado, "El resultado no debe ser nulo");

        // Verificar que el ID fue asignado correctamente
        assertEquals(1L, resultado.getIdEstudiante(),
                "El ID del estudiante debe ser 1 (generado por la BD)");

        // Verificar que los campos se mapearon correctamente
        assertEquals("EST-2026-001", resultado.getCodigoEstudiante(),
                "El código del estudiante debe coincidir");
        assertEquals("72345678", resultado.getDni(),
                "El DNI debe coincidir");
        assertEquals("Juan Carlos", resultado.getNombres(),
                "Los nombres deben coincidir");
        assertEquals("García", resultado.getApellidoPaterno(),
                "El apellido paterno debe coincidir");
        assertEquals("Activo", resultado.getEstado(),
                "El estado debe ser 'Activo'");

        // Verificar que los métodos del repositorio fueron invocados
        verify(estudianteRepository, times(1)).existsByCodigoEstudiante("EST-2026-001");
        verify(estudianteRepository, times(1)).existsByDni("72345678");
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }
}
