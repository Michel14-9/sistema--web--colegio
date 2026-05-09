  # Sistema Académico - Colegio

Sistema web para la gestión académica de un colegio, desarrollado con Spring Boot y PostgreSQL.

---

## Plan de Prueba de Software y Seguridad

### 1. Objetivo

Verificar que el módulo de **Gestión de Estudiantes** del Sistema Académico funcione correctamente, asegurando que las operaciones CRUD (Crear, Leer, Actualizar, Eliminar) a través del endpoint REST `/api/estudiantes` procesen los datos de forma íntegra, validen las reglas de negocio establecidas (unicidad de código y DNI) y respondan con los códigos HTTP apropiados.

### 2. Alcance de Pruebas

La prueba se centra exclusivamente en la capa de servicio (`EstudianteService`) del módulo de Estudiantes, evaluando la lógica de negocio del método `guardar()`, que es responsable de crear nuevos estudiantes y validar restricciones de unicidad.

| Aspecto            | Detalle                                                                |
|--------------------|------------------------------------------------------------------------|
| **Módulo**         | Gestión de Estudiantes                                                 |
| **Capa evaluada**  | Servicio (`EstudianteService`)                                         |
| **Método bajo prueba** | `guardar(EstudianteDTO estudianteDTO)`                             |
| **Tipo de prueba** | Prueba Unitaria                                                        |
| **Herramienta**    | JUnit 5 + Mockito                                                      |

### 3. Funcionalidades para Probar

- **Registrar un nuevo estudiante exitosamente**: Verificar que al enviar un `EstudianteDTO` con datos válidos (código y DNI únicos), el servicio persista la entidad y retorne el DTO con el ID generado.

### 4. Funcionalidades No Incluidas en las Pruebas

- Operaciones de listar, buscar por ID y eliminar estudiantes.
- CRUD completo de los módulos Curso, Docente y Matrícula.
- Autenticación y autorización (Login).
- Capa de controlador (endpoints HTTP).
- Capa de repositorio contra base de datos real.
- Interfaz de usuario (frontend).

### 5. Tipos de Pruebas

#### 5.1 Prueba de Requerimiento Funcional

| ID     | Caso de Prueba                              | Entrada                                                                                                                                                            | Resultado Esperado                                                                                     | Estado   |
|--------|---------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|----------|
| RF-001 | Registrar estudiante con datos válidos      | `EstudianteDTO` con código `"EST-2026-001"`, DNI `"72345678"`, nombres `"Juan Carlos"`, apellido paterno `"García"`, apellido materno `"López"`, estado `"Activo"` | El servicio retorna un `EstudianteDTO` con `idEstudiante` no nulo y todos los campos correctamente mapeados | Pendiente |

#### 5.2 Pruebas de Seguridad

No incluidas en esta iteración. Se recomienda evaluar en futuras pruebas:
- Inyección SQL a través de los campos de texto.
- Validación de longitud máxima de campos (DNI = 8, celular = 15, etc.).

#### 5.3 Pruebas de Integración

No incluidas en esta iteración. Se recomienda evaluar en futuras pruebas:
- Conexión del `EstudianteController` con `EstudianteService` y `EstudianteRepository` contra base de datos PostgreSQL.

#### 5.4 Pruebas de Rendimiento y Carga

No incluidas en esta iteración. Se recomienda evaluar en futuras pruebas:
- Tiempo de respuesta del endpoint `POST /api/estudiantes` bajo carga concurrente.

#### 5.5 Pruebas de Regresión

No incluidas en esta iteración. Se aplicarán cuando se realicen modificaciones futuras al módulo de Estudiantes para verificar que las funcionalidades existentes no se vean afectadas.

#### 5.6 Pruebas de Aceptación del Usuario

No incluidas en esta iteración. Se recomienda evaluar en futuras pruebas:
- Validar que el flujo completo de registro de un estudiante desde la interfaz de usuario cumpla con los criterios de aceptación definidos por el usuario final.

---

### 6. Prueba Unitaria Implementada

**Clase de prueba:** `EstudianteServiceTest.java`

**Ubicación:** `src/test/java/com/universidad/sistema_academico/service/EstudianteServiceTest.java`

**Descripción:** Se utiliza **JUnit 5** con **Mockito** para simular (mockear) el repositorio `EstudianteRepository`, aislando la lógica de negocio del servicio sin necesidad de una base de datos real.

**Caso de prueba `RF-001` — Registrar estudiante con datos válidos:**

```java
@Test
void guardar_ConDatosValidos_DebeRetornarEstudianteDTOConId()
```

- Se crea un `EstudianteDTO` con datos de prueba.
- Se configura el mock del repositorio para que:
  - `existsByCodigoEstudiante()` retorne `false` (código no duplicado).
  - `existsByDni()` retorne `false` (DNI no duplicado).
  - `save()` retorne la entidad con un ID asignado.
- Se invoca `estudianteService.guardar(dto)`.
- Se verifican las aserciones:
  - El resultado no es nulo.
  - El `idEstudiante` es el esperado (`1L`).
  - Los campos `codigoEstudiante`, `dni`, `nombres`, `apellidoPaterno` y `estado` coinciden con los datos de entrada.
- Se verifica que el repositorio fue invocado correctamente con `verify()`.