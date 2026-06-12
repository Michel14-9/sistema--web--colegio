/**
 * Sistema de Matrícula - I.E. San Carlos
 * Validaciones profesionales y mejoras de experiencia de usuario
 */

document.addEventListener('DOMContentLoaded', function() {

    // ========== ELEMENTOS DEL DOM ==========
    const matriculaForm = document.getElementById('matriculaForm');
    const voucherInput = document.getElementById('voucher');
    const voucherPreview = document.getElementById('voucherPreview');
    const submitBtn = document.querySelector('.btn-matricular');

    // ========== 1. DETECTAR ERRORES/ÉXITO EN URL ==========
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('error')) {
        mostrarMensaje(decodeURIComponent(urlParams.get('error')), 'error');
    }
    if (urlParams.get('success')) {
        mostrarMensaje(decodeURIComponent(urlParams.get('success')), 'success');
    }

    // ========== 2. PREVISUALIZACIÓN DEL VOUCHER ==========
    if (voucherInput) {
        voucherInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                if (file.size > 5 * 1024 * 1024) {
                    mostrarMensaje('El archivo no debe superar los 5MB', 'error');
                    voucherInput.value = '';
                    voucherPreview.innerHTML = '';
                    return;
                }

                const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'application/pdf'];
                if (!allowedTypes.includes(file.type)) {
                    mostrarMensaje('Solo se permiten archivos JPG, PNG o PDF', 'error');
                    voucherInput.value = '';
                    voucherPreview.innerHTML = '';
                    return;
                }

                if (file.type.startsWith('image/')) {
                    const reader = new FileReader();
                    reader.onload = function(event) {
                        voucherPreview.innerHTML = `
                            <div class="voucher-preview-image">
                                <img src="${event.target.result}" alt="Vista previa del voucher">
                                <button type="button" class="btn-remove-preview" onclick="removerPreview()">
                                    <i class="fas fa-times-circle"></i>
                                </button>
                            </div>
                            <p class="text-muted mt-2 small">Vista previa de tu voucher</p>
                        `;
                    };
                    reader.readAsDataURL(file);
                } else if (file.type === 'application/pdf') {
                    voucherPreview.innerHTML = `
                        <div class="alert alert-info mt-2">
                            <i class="fas fa-file-pdf"></i> Archivo PDF: ${file.name}
                            <button type="button" class="btn-remove-preview-pdf" onclick="removerPreview()">
                                <i class="fas fa-times-circle"></i>
                            </button>
                        </div>
                    `;
                }
            }
        });
    }

    window.removerPreview = function() {
        voucherInput.value = '';
        voucherPreview.innerHTML = '';
    };

    // ========== FUNCIÓN PARA CONVERTIR A MAYÚSCULAS ==========
    function convertirMayusculas(input) {
        if (input && input.value) {
            const cursorPos = input.selectionStart;
            const originalValue = input.value;
            const newValue = originalValue.toUpperCase();

            if (originalValue !== newValue) {
                input.value = newValue;
                input.setSelectionRange(cursorPos, cursorPos);
            }
        }
    }

    // ========== 3. VALIDACIONES EN TIEMPO REAL ==========
    const dniInput = document.getElementById('dni');
    if (dniInput) {
        dniInput.addEventListener('input', function() {
            this.value = this.value.replace(/[^0-9]/g, '').slice(0, 8);
            validarDNI(this);
        });
        dniInput.addEventListener('blur', function() {
            if (this.value.length === 8) {
                verificarDNIUnico(this.value);
            }
        });
    }

    const nombresInput = document.getElementById('nombres');
    if (nombresInput) {
        nombresInput.addEventListener('input', function() {
            this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '');
            convertirMayusculas(this);
            validarTexto(this, 'nombres');
        });
    }

    const apellidoPaterno = document.getElementById('apellidoPaterno');
    const apellidoMaterno = document.getElementById('apellidoMaterno');
    if (apellidoPaterno) {
        apellidoPaterno.addEventListener('input', function() {
            this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '');
            convertirMayusculas(this);
            validarTexto(this, 'apellido');
        });
    }
    if (apellidoMaterno) {
        apellidoMaterno.addEventListener('input', function() {
            this.value = this.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '');
            convertirMayusculas(this);
            validarTexto(this, 'apellido');
        });
    }



    const direccionInput = document.getElementById('direccion');
    if (direccionInput) {
        direccionInput.addEventListener('input', function() {
            convertirMayusculas(this);
        });
    }

    const celularInput = document.getElementById('celular');
    if (celularInput) {
        celularInput.addEventListener('input', function() {
            this.value = this.value.replace(/[^0-9]/g, '').slice(0, 9);
            validarCelular(this);
        });
    }

    const fechaNacimiento = document.getElementById('fechaNacimiento');
    if (fechaNacimiento) {
        fechaNacimiento.addEventListener('change', function() {
            validarEdad(this);
        });
    }

    const emailInput = document.getElementById('apoderadoEmail');
    if (emailInput) {
        emailInput.addEventListener('input', function() {
            this.value = this.value.toLowerCase();
            validarEmail(this);
        });
        emailInput.addEventListener('blur', function() {
            if (this.value.trim()) {
                verificarEmailUnico(this.value);
            }
        });
    }

    const telefonoApoderado = document.getElementById('apoderadoTelefono');
    if (telefonoApoderado) {
        telefonoApoderado.addEventListener('input', function() {
            this.value = this.value.replace(/[^0-9]/g, '').slice(0, 9);
            validarTelefonoApoderado(this);
        });
    }

    const apoderadoDni = document.getElementById('apoderadoDni');
    if (apoderadoDni) {
        apoderadoDni.addEventListener('input', function() {
            this.value = this.value.replace(/[^0-9]/g, '').slice(0, 8);
            validarDNIApoderado(this);
        });
    }

    // ========== CONSULTA AUTOMÁTICA A RENIEC PARA APODERADO ==========
    const apoderadoApellidoPaternoInput = document.getElementById('apoderadoApellidoPaterno');
    const apoderadoApellidoMaternoInput = document.getElementById('apoderadoApellidoMaterno');
    const loadingApoderado = document.getElementById('loadingApoderado');

    if (apoderadoDni) {
        let timeoutId;

        apoderadoDni.addEventListener('input', function() {
            clearTimeout(timeoutId);
            this.value = this.value.replace(/[^0-9]/g, '').slice(0, 8);

            if (this.value.length !== 8) {
                limpiarCamposApoderado();
                return;
            }

            timeoutId = setTimeout(() => {
                if (this.value.length === 8) {
                    consultarReniecApoderado(this.value);
                }
            }, 500);
        });
    }

    function limpiarCamposApoderado() {
        if (apoderadoNombres) apoderadoNombres.value = '';
        if (apoderadoApellidoPaternoInput) apoderadoApellidoPaternoInput.value = '';
        if (apoderadoApellidoMaternoInput) apoderadoApellidoMaternoInput.value = '';

        [apoderadoNombres, apoderadoApellidoPaternoInput, apoderadoApellidoMaternoInput].forEach(input => {
            if (input) {
                input.classList.remove('is-valid', 'is-invalid');
                limpiarError(input);
            }
        });
    }

    function consultarReniecApoderado(dni) {
        if (loadingApoderado) loadingApoderado.style.display = 'inline-block';

        fetch(`/api/reniec/consultar/${dni}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    apoderadoNombres.value = (data.nombres || '').toUpperCase();
                    apoderadoApellidoPaternoInput.value = (data.apellidoPaterno || '').toUpperCase();
                    apoderadoApellidoMaternoInput.value = (data.apellidoMaterno || '').toUpperCase();

                    validarTexto(apoderadoNombres, 'nombres');
                    validarTexto(apoderadoApellidoPaternoInput, 'apellido');
                    validarTexto(apoderadoApellidoMaternoInput, 'apellido');

                    mostrarMensaje('Datos del apoderado cargados correctamente', 'success');
                } else {
                    mostrarMensaje('DNI no encontrado en RENIEC', 'error');
                    limpiarCamposApoderado();
                }
            })
            .catch(error => {
                console.error('Error:', error);
                mostrarMensaje('Error al consultar RENIEC', 'error');
                limpiarCamposApoderado();
            })
            .finally(() => {
                if (loadingApoderado) loadingApoderado.style.display = 'none';
            });
    }



    const terminosCheck = document.getElementById('terminosCondiciones');
    if (terminosCheck) {
        terminosCheck.addEventListener('change', function() {
            if (this.checked) {
                this.classList.remove('is-invalid');
                const invalidFeedback = this.parentElement.querySelector('.invalid-feedback');
                if (invalidFeedback) {
                    invalidFeedback.style.display = 'none';
                }
            }
        });
    }

    // ========== 4. VALIDACIÓN AL ENVIAR ==========
    if (matriculaForm) {
        matriculaForm.addEventListener('submit', function(e) {
            console.log("🔵 INICIO DE VALIDACIÓN");
            let isValid = true;
            let firstInvalidField = null;

            console.log("--- VALORES DE TODOS LOS CAMPOS ---");
            const allInputs = matriculaForm.querySelectorAll('input, select, textarea');
            allInputs.forEach(field => {
                if (field.type === 'checkbox') {
                    console.log(`${field.id || field.name}: ${field.checked}`);
                } else {
                    console.log(`${field.id || field.name}: "${field.value}"`);
                }
            });
            console.log("-----------------------------------");

            if (nombresInput) convertirMayusculas(nombresInput);
            if (apellidoPaterno) convertirMayusculas(apellidoPaterno);
            if (apellidoMaterno) convertirMayusculas(apellidoMaterno);
            if (apoderadoNombres) convertirMayusculas(apoderadoNombres);
            if (direccionInput) convertirMayusculas(direccionInput);
            if (emailInput) emailInput.value = emailInput.value.toLowerCase();

            const requiredFields = matriculaForm.querySelectorAll('[required]');
            console.log("--- VALIDANDO CAMPOS REQUERIDOS ---");
            requiredFields.forEach(field => {
                if (field.type === 'checkbox') {
                    if (!field.checked) {
                        console.log(`❌ Checkbox REQUERIDO no marcado: ${field.id || field.name}`);
                        isValid = false;
                        field.classList.add('is-invalid');
                        if (!firstInvalidField) firstInvalidField = field;
                        const invalidFeedback = field.parentElement.querySelector('.invalid-feedback');
                        if (invalidFeedback) {
                            invalidFeedback.style.display = 'block';
                            invalidFeedback.textContent = 'Debe aceptar los términos y condiciones';
                        }
                    } else {
                        console.log(`✅ Checkbox OK: ${field.id || field.name}`);
                    }
                } else if (!field.value.trim()) {
                    console.log(`❌ Campo REQUERIDO vacío: ${field.id || field.name} - valor: "${field.value}"`);
                    isValid = false;
                    field.classList.add('is-invalid');
                    if (!firstInvalidField) firstInvalidField = field;
                    mostrarErrorPersonalizado(field, 'Este campo es obligatorio');
                } else {
                    console.log(`✅ Campo OK: ${field.id || field.name} = "${field.value}"`);
                    field.classList.remove('is-invalid');
                }
            });
            console.log("-----------------------------------");

            if (dniInput && dniInput.value.trim() && !validarDNI(dniInput, true)) isValid = false;
            if (nombresInput && nombresInput.value.trim() && !validarTexto(nombresInput, 'nombres', true)) isValid = false;
            if (apellidoPaterno && apellidoPaterno.value.trim() && !validarTexto(apellidoPaterno, 'apellido', true)) isValid = false;
            if (apellidoMaterno && apellidoMaterno.value.trim() && !validarTexto(apellidoMaterno, 'apellido', true)) isValid = false;
            if (fechaNacimiento && fechaNacimiento.value && !validarEdad(fechaNacimiento, true)) isValid = false;
            if (celularInput && celularInput.value.trim() && !validarCelular(celularInput, true)) isValid = false;
            if (emailInput && emailInput.value.trim() && !validarEmail(emailInput, true)) isValid = false;
            if (telefonoApoderado && telefonoApoderado.value.trim() && !validarTelefonoApoderado(telefonoApoderado, true)) isValid = false;
            if (apoderadoDni && apoderadoDni.value.trim() && !validarDNIApoderado(apoderadoDni, true)) isValid = false;

            // Validar apellidos del apoderado
            if (apoderadoApellidoPaternoInput && apoderadoApellidoPaternoInput.value.trim() && !validarTexto(apoderadoApellidoPaternoInput, 'apellido', true)) isValid = false;
            if (apoderadoApellidoMaternoInput && apoderadoApellidoMaternoInput.value.trim() && !validarTexto(apoderadoApellidoMaternoInput, 'apellido', true)) isValid = false;

            if (voucherInput) {
                const file = voucherInput.files[0];
                if (!file) {
                    console.log("❌ Voucher: No hay archivo");
                    isValid = false;
                    voucherInput.classList.add('is-invalid');
                    let errorDiv = voucherInput.parentElement.querySelector('.invalid-feedback');
                    if (!errorDiv) {
                        errorDiv = document.createElement('div');
                        errorDiv.className = 'invalid-feedback';
                        voucherInput.parentElement.appendChild(errorDiv);
                    }
                    errorDiv.textContent = 'Debe adjuntar el voucher de pago';
                } else {
                    let extension = file.name.split('.').pop().toLowerCase();
                    const allowedExtensions = ['jpg', 'jpeg', 'png', 'pdf'];
                    if (!allowedExtensions.includes(extension)) {
                        console.log(`❌ Voucher: Extensión no válida: ${extension}`);
                        isValid = false;
                        voucherInput.classList.add('is-invalid');
                        let errorDiv = voucherInput.parentElement.querySelector('.invalid-feedback');
                        if (!errorDiv) {
                            errorDiv = document.createElement('div');
                            errorDiv.className = 'invalid-feedback';
                            voucherInput.parentElement.appendChild(errorDiv);
                        }
                        errorDiv.textContent = `Formato no válido: "${extension}". Solo se permiten JPG, PNG o PDF`;
                    } else if (file.size > 5 * 1024 * 1024) {
                        console.log(`❌ Voucher: Archivo demasiado grande: ${file.size} bytes`);
                        isValid = false;
                        voucherInput.classList.add('is-invalid');
                        let errorDiv = voucherInput.parentElement.querySelector('.invalid-feedback');
                        if (!errorDiv) {
                            errorDiv = document.createElement('div');
                            errorDiv.className = 'invalid-feedback';
                            voucherInput.parentElement.appendChild(errorDiv);
                        }
                        errorDiv.textContent = 'El archivo no debe superar los 5MB';
                    } else {
                        console.log("✅ Voucher válido");
                        voucherInput.classList.remove('is-invalid');
                    }
                }
            }

            console.log(`🔵 RESULTADO FINAL - isValid: ${isValid}`);

            if (!isValid) {
                e.preventDefault();
                console.log("❌ ENVÍO CANCELADO - Formulario inválido");
                if (firstInvalidField) {
                    console.log(`📌 Primer campo inválido: ${firstInvalidField.id || firstInvalidField.name}`);
                    firstInvalidField.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstInvalidField.focus();
                }
                mostrarMensaje('Por favor, complete todos los campos correctamente', 'error');
                return false;
            }

            console.log("✅ FORMULARIO VÁLIDO - Mostrando confirmación");

            if (!confirm('¿Está seguro que todos los datos son correctos? Esta acción no se puede deshacer.')) {
                console.log("❌ Usuario canceló el envío");
                e.preventDefault();
                return false;
            }

            console.log("✅ Usuario confirmó - Enviando formulario");
            mostrarLoading(true);
            return true;
        });
    }

    // ========== 5. FUNCIONES DE VALIDACIÓN ==========

    function validarDNI(input, showMessage = false) {
        const dni = input.value.trim();
        const isValid = dni.length === 8 && /^\d+$/.test(dni);

        if (dni.length > 0 && dni.length !== 8) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, 'El DNI debe tener 8 dígitos');
            return false;
        } else if (!isValid && dni.length > 0) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, 'Solo números permitidos');
            return false;
        } else if (isValid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            limpiarError(input);
            return true;
        } else {
            input.classList.remove('is-invalid', 'is-valid');
            limpiarError(input);
            return false;
        }
    }

    function validarTexto(input, tipo, showMessage = false) {
        const valor = input.value.trim();
        let isValid = true;
        let mensaje = '';

        if (tipo === 'nombres') {
            isValid = valor.length >= 2 && valor.length <= 50;
            mensaje = 'Los nombres deben tener entre 2 y 50 caracteres';
        } else if (tipo === 'apellido') {
            isValid = valor.length >= 2 && valor.length <= 30;
            mensaje = 'El apellido debe tener entre 2 y 30 caracteres';
        }

        if (valor && !isValid) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, mensaje);
            return false;
        } else if (valor && isValid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            limpiarError(input);
            return true;
        } else {
            input.classList.remove('is-invalid', 'is-valid');
            limpiarError(input);
            return false;
        }
    }

    function validarCelular(input, showMessage = false) {
        let celular = input.value.trim().replace(/\s/g, '');
        const isValid = celular.length === 9 && /^\d+$/.test(celular);

        if (celular.length > 0 && celular.length !== 9) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, 'El celular debe tener 9 dígitos');
            return false;
        } else if (isValid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            limpiarError(input);
            return true;
        } else if (celular.length === 0) {
            input.classList.remove('is-invalid', 'is-valid');
            limpiarError(input);
            return false;
        } else {
            input.classList.remove('is-invalid', 'is-valid');
            limpiarError(input);
            return false;
        }
    }

    function validarTelefonoApoderado(input, showMessage = false) {
        const telefono = input.value.trim();
        const isValid = (telefono.length === 9 || telefono.length === 7) && /^\d+$/.test(telefono);

        if (telefono.length > 0 && telefono.length !== 9 && telefono.length !== 7) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, 'El teléfono debe tener 7 u 9 dígitos');
            return false;
        } else if (isValid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            limpiarError(input);
            return true;
        } else {
            input.classList.remove('is-invalid', 'is-valid');
            limpiarError(input);
            return false;
        }
    }

    function validarDNIApoderado(input, showMessage = false) {
        const dni = input.value.trim();
        const isValid = dni.length === 8 && /^\d+$/.test(dni);

        if (dni.length > 0 && dni.length !== 8) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, 'El DNI del apoderado debe tener 8 dígitos');
            return false;
        } else if (isValid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            limpiarError(input);
            return true;
        } else {
            input.classList.remove('is-invalid', 'is-valid');
            limpiarError(input);
            return false;
        }
    }

    function validarEmail(input, showMessage = false) {
        const email = input.value.trim();
        const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
        const isValid = email !== '' && emailRegex.test(email);

        if (email !== '' && !emailRegex.test(email)) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, 'Ingrese un correo electrónico válido (ejemplo@dominio.com)');
            return false;
        } else if (isValid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            limpiarError(input);
            return true;
        } else {
            input.classList.remove('is-invalid', 'is-valid');
            limpiarError(input);
            return false;
        }
    }

    function validarEdad(input, showMessage = false) {
        const fechaNac = new Date(input.value);
        const hoy = new Date();
        let edad = hoy.getFullYear() - fechaNac.getFullYear();
        const mesDiff = hoy.getMonth() - fechaNac.getMonth();

        if (mesDiff < 0 || (mesDiff === 0 && hoy.getDate() < fechaNac.getDate())) {
            edad--;
        }

        const isValid = edad >= 5 && edad <= 18;

        if (input.value && !isValid) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, 'La edad debe estar entre 5 y 18 años');
            return false;
        } else if (input.value && isValid) {
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
            limpiarError(input);
            return true;
        } else {
            input.classList.remove('is-invalid', 'is-valid');
            limpiarError(input);
            return false;
        }
    }

    function validarGradoPorEdad(fechaNacimiento, idGrado) {
        if (!fechaNacimiento || !idGrado) return true;

        const fechaNac = new Date(fechaNacimiento);
        const hoy = new Date();
        let edad = hoy.getFullYear() - fechaNac.getFullYear();
        const mesDiff = hoy.getMonth() - fechaNac.getMonth();

        if (mesDiff < 0 || (mesDiff === 0 && hoy.getDate() < fechaNac.getDate())) {
            edad--;
        }

        const gradoPorEdad = {
            5: [1], 6: [2], 7: [3], 8: [4], 9: [5], 10: [6],
            11: [7], 12: [8], 13: [9], 14: [10], 15: [11], 16: [11], 17: [11], 18: [11]
        };

        const gradosPermitidos = gradoPorEdad[edad] || [];
        return gradosPermitidos.includes(parseInt(idGrado));
    }

    function verificarDNIUnico(dni) {
        console.log('Verificando DNI único:', dni);
    }

    function verificarEmailUnico(email) {
        console.log('Verificando email único:', email);
    }

    function mostrarError(input, message) {
        let errorDiv = input.parentElement.querySelector('.invalid-feedback');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback';
            input.parentElement.appendChild(errorDiv);
        }
        errorDiv.textContent = message;
    }

    function mostrarErrorPersonalizado(input, message) {
        mostrarError(input, message);
    }

    function limpiarError(input) {
        const errorDiv = input.parentElement.querySelector('.invalid-feedback');
        if (errorDiv) {
            errorDiv.remove();
        }
    }

    // ========== 6. MENSAJES FLOTANTES ==========

    function mostrarMensaje(message, type) {
        const oldMessage = document.querySelector('.floating-message');
        if (oldMessage) oldMessage.remove();

        const messageDiv = document.createElement('div');
        messageDiv.className = `floating-message ${type}`;
        messageDiv.innerHTML = `
            <i class="fas ${type === 'error' ? 'fa-exclamation-circle' : 'fa-check-circle'}"></i>
            <span>${message}</span>
            <button class="close-btn">&times;</button>
        `;

        document.body.appendChild(messageDiv);

        setTimeout(() => {
            if (messageDiv) {
                messageDiv.style.animation = 'slideOutRight 0.3s ease';
                setTimeout(() => messageDiv.remove(), 300);
            }
        }, 5000);

        const closeBtn = messageDiv.querySelector('.close-btn');
        closeBtn.onclick = () => {
            messageDiv.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => messageDiv.remove(), 300);
        };
    }

    function mostrarLoading(isLoading) {
        if (!submitBtn) return;

        if (isLoading) {
            submitBtn.setAttribute('data-original-text', submitBtn.innerHTML);
            submitBtn.innerHTML = `
                <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                Procesando solicitud...
            `;
            submitBtn.disabled = true;
        } else {
            const originalText = submitBtn.getAttribute('data-original-text');
            if (originalText) submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    }

    // ========== 7. MASCARAS DE ENTRADA ==========
    function aplicarMascaras() {
        if (celularInput) {
            celularInput.addEventListener('input', function(e) {
                let value = this.value.replace(/\D/g, '').slice(0, 9);
                this.value = value;
                validarCelular(this);
            });
        }

        if (telefonoApoderado) {
            telefonoApoderado.addEventListener('input', function(e) {
                let value = this.value.replace(/\D/g, '').slice(0, 9);
                this.value = value;
                validarTelefonoApoderado(this);
            });
        }
    }

    aplicarMascaras();

    // ========== 8. ESTILOS ADICIONALES ==========
    const style = document.createElement('style');
    style.textContent = `
        .spinner-border {
            display: inline-block;
            width: 1rem;
            height: 1rem;
            vertical-align: text-bottom;
            border: 0.15em solid currentColor;
            border-right-color: transparent;
            border-radius: 50%;
            animation: spinner-border 0.75s linear infinite;
            margin-right: 8px;
        }
        @keyframes spinner-border {
            to { transform: rotate(360deg); }
        }
        @keyframes slideInRight {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideOutRight {
            from { transform: translateX(0); opacity: 1; }
            to { transform: translateX(100%); opacity: 0; }
        }
        .voucher-preview-image {
            position: relative;
            display: inline-block;
        }
        .voucher-preview-image img {
            max-width: 200px;
            max-height: 150px;
            border-radius: 8px;
            border: 1px solid #ddd;
        }
        .btn-remove-preview, .btn-remove-preview-pdf {
            position: absolute;
            top: -10px;
            right: -10px;
            background: #dc3545;
            color: white;
            border: none;
            border-radius: 50%;
            width: 24px;
            height: 24px;
            cursor: pointer;
            font-size: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .btn-remove-preview:hover, .btn-remove-preview-pdf:hover {
            background: #c82333;
        }
        .form-control.is-valid, .form-select.is-valid {
            border-color: #28a745;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 8 8'%3e%3cpath fill='%2328a745' d='M2.3 6.73L.6 4.53c-.4-1.04.46-1.4 1.1-.8l1.1 1.4 3.4-3.8c.6-.63 1.6-.27 1.2.7l-4 4.6c-.43.5-.8.4-1.1.1z'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right calc(0.375em + 0.1875rem) center;
            background-size: calc(0.75em + 0.375rem) calc(0.75em + 0.375rem);
        }
        .form-control.is-invalid, .form-select.is-invalid {
            border-color: #dc3545;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right calc(0.375em + 0.1875rem) center;
            background-size: calc(0.75em + 0.375rem) calc(0.75em + 0.375rem);
        }
        .floating-message {
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: flex;
            align-items: center;
            gap: 12px;
            animation: slideInRight 0.3s ease;
            font-weight: 500;
        }
        .floating-message.success {
            background: #d4edda;
            color: #155724;
            border-left: 4px solid #28a745;
        }
        .floating-message.error {
            background: #f8d7da;
            color: #721c24;
            border-left: 4px solid #dc3545;
        }
        .floating-message .close-btn {
            background: none;
            border: none;
            font-size: 1.2rem;
            cursor: pointer;
            color: inherit;
            margin-left: 10px;
        }
    `;
    document.head.appendChild(style);
});