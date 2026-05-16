/**
 * Sistema de Matrícula - I.E. San Carlos
 * Validaciones y mejoras de experiencia de usuario
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
            if (file && file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(event) {
                    voucherPreview.innerHTML = `
                        <img src="${event.target.result}" alt="Vista previa del voucher">
                        <p class="text-muted mt-2 small">Vista previa de tu voucher</p>
                    `;
                };
                reader.readAsDataURL(file);
            } else if (file) {
                voucherPreview.innerHTML = `
                    <div class="alert alert-info mt-2">
                        <i class="fas fa-file-pdf"></i> Archivo: ${file.name}
                    </div>
                `;
            } else {
                voucherPreview.innerHTML = '';
            }
        });
    }

    // ========== 3. VALIDACIÓN EN TIEMPO REAL ==========
    const dniInput = document.getElementById('dni');
    const celularInput = document.getElementById('celular');
    const emailInput = document.getElementById('apoderadoEmail');

    if (dniInput) {
        dniInput.addEventListener('input', function() {
            this.value = this.value.replace(/[^0-9]/g, '').slice(0, 8);
            validarDNI(this);
        });
    }

    if (celularInput) {
        celularInput.addEventListener('input', function() {
            this.value = this.value.replace(/[^0-9]/g, '').slice(0, 9);
            validarCelular(this);
        });
    }

    if (emailInput) {
        emailInput.addEventListener('input', function() {
            validarEmail(this);
        });
    }

    // ========== 4. VALIDACIÓN AL ENVIAR ==========
    if (matriculaForm) {
        matriculaForm.addEventListener('submit', function(e) {
            // Validar todos los campos requeridos
            let isValid = true;

            const requiredFields = matriculaForm.querySelectorAll('[required]');
            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.classList.add('is-invalid');
                } else {
                    field.classList.remove('is-invalid');
                }
            });

            // Validaciones específicas
            if (dniInput && !validarDNI(dniInput)) isValid = false;
            if (celularInput && !validarCelular(celularInput)) isValid = false;
            if (emailInput && !validarEmail(emailInput)) isValid = false;

            // Validar voucher
            if (voucherInput && !voucherInput.files.length) {
                isValid = false;
                mostrarMensaje('Debe adjuntar el voucher de pago', 'error');
            }

            if (!isValid) {
                e.preventDefault();
                mostrarMensaje('Por favor, complete todos los campos correctamente', 'error');
                return false;
            }

            // Mostrar loading en el botón
            mostrarLoading(true);
            return true;
        });
    }

    // ========== 5. FUNCIONES DE VALIDACIÓN ==========

    function validarDNI(input) {
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

    function validarCelular(input) {
        const celular = input.value.trim();
        const isValid = celular.length === 9 && /^\d+$/.test(celular);

        if (celular.length > 0 && celular.length !== 9) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, 'El celular debe tener 9 dígitos');
            return false;
        } else if (!isValid && celular.length > 0) {
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

    function validarEmail(input) {
        const email = input.value.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const isValid = email !== '' && emailRegex.test(email);

        if (email !== '' && !emailRegex.test(email)) {
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
            mostrarError(input, 'Ingrese un correo válido');
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

    function mostrarError(input, message) {
        let errorDiv = input.parentElement.querySelector('.invalid-feedback');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback';
            input.parentElement.appendChild(errorDiv);
        }
        errorDiv.textContent = message;
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
                Procesando...
            `;
            submitBtn.disabled = true;
        } else {
            const originalText = submitBtn.getAttribute('data-original-text');
            if (originalText) submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    }

    // ========== 7. AGREGAR ESTILOS DE SPINNER ==========
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
    `;
    document.head.appendChild(style);
});