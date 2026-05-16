/**
 * Sistema de Login - I.E. San Carlos
 * Validaciones y mejoras de experiencia de usuario
 */

// Esperar a que el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', function() {

    // ========== ELEMENTOS DEL DOM ==========
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const rememberCheckbox = document.getElementById('remember');
    const submitBtn = document.querySelector('.btn-ingresar');

    // ========== 1. DETECTAR ERROR EN URL ==========
    // Si la URL tiene ?error=true, mostrar mensaje
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('error') === 'true') {
        mostrarMensajeError('Correo o contraseña incorrectos. Por favor, intente nuevamente.');
    }

    // Si la URL tiene ?logout=true, mostrar mensaje
    if (urlParams.get('logout') === 'true') {
        mostrarMensajeExito('Has cerrado sesión correctamente.');
    }

    // ========== 2. CARGAR EMAIL GUARDADO (Recordarme) ==========
    const savedEmail = localStorage.getItem('savedEmail');
    const savedRemember = localStorage.getItem('rememberMe') === 'true';

    if (savedEmail && savedRemember) {
        emailInput.value = savedEmail;
        rememberCheckbox.checked = true;
    }

    // ========== 3. VALIDACIÓN EN TIEMPO REAL ==========

    // Validar email mientras escribe
    emailInput.addEventListener('input', function() {
        validarEmail(this);
    });

    // Validar contraseña mientras escribe
    passwordInput.addEventListener('input', function() {
        validarPassword(this);
    });

    // ========== 4. VALIDACIÓN AL ENVIAR FORMULARIO ==========
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            // Validar campos antes de enviar
            const isEmailValid = validarEmail(emailInput);
            const isPasswordValid = validarPassword(passwordInput);

            if (!isEmailValid || !isPasswordValid) {
                e.preventDefault();
                mostrarMensajeError('Por favor, complete todos los campos correctamente.');
                return false;
            }

            // Guardar email si "Recordarme" está marcado
            if (rememberCheckbox.checked) {
                localStorage.setItem('savedEmail', emailInput.value);
                localStorage.setItem('rememberMe', 'true');
            } else {
                localStorage.removeItem('savedEmail');
                localStorage.setItem('rememberMe', 'false');
            }

            // Mostrar loading en el botón
            mostrarLoading(true);

            // El formulario se enviará automáticamente
            return true;
        });
    }

    // ========== 5. FUNCIONES AUXILIARES ==========

    /**
     * Valida el campo de email
     * @param {HTMLElement} input - El input de email
     * @returns {boolean} - true si es válido, false si no
     */
    function validarEmail(input) {
        const email = input.value.trim();
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const isValid = email !== '' && emailRegex.test(email);

        // Estilos visuales
        if (email === '') {
            setError(input, 'El correo es obligatorio');
            return false;
        } else if (!emailRegex.test(email)) {
            setError(input, 'Ingrese un correo válido (ejemplo@dominio.com)');
            return false;
        } else {
            setSuccess(input);
            return true;
        }
    }

    /**
     * Valida el campo de contraseña
     * @param {HTMLElement} input - El input de contraseña
     * @returns {boolean} - true si es válida, false si no
     */
    function validarPassword(input) {
        const password = input.value.trim();
        const isValid = password !== '';

        if (password === '') {
            setError(input, 'La contraseña es obligatoria');
            return false;
        } else if (password.length < 4) {
            setError(input, 'La contraseña debe tener al menos 4 caracteres');
            return false;
        } else {
            setSuccess(input);
            return true;
        }
    }

    /**
     * Muestra error en un campo
     * @param {HTMLElement} input - El input a marcar
     * @param {string} message - Mensaje de error
     */
    function setError(input, message) {
        input.classList.add('is-invalid');
        input.classList.remove('is-valid');

        // Buscar o crear el mensaje de error
        let errorDiv = input.parentElement.querySelector('.invalid-feedback');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'invalid-feedback';
            input.parentElement.appendChild(errorDiv);
        }
        errorDiv.textContent = message;
    }

    /**
     * Marca un campo como válido
     * @param {HTMLElement} input - El input a marcar
     */
    function setSuccess(input) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');

        // Eliminar mensaje de error si existe
        const errorDiv = input.parentElement.querySelector('.invalid-feedback');
        if (errorDiv) {
            errorDiv.remove();
        }
    }

    /**
     * Muestra mensaje de error flotante
     * @param {string} message - Mensaje a mostrar
     */
    function mostrarMensajeError(message) {
        mostrarMensaje(message, 'error');
    }

    /**
     * Muestra mensaje de éxito flotante
     * @param {string} message - Mensaje a mostrar
     */
    function mostrarMensajeExito(message) {
        mostrarMensaje(message, 'success');
    }

    /**
     * Muestra un mensaje flotante
     * @param {string} message - Mensaje a mostrar
     * @param {string} type - Tipo: 'error' o 'success'
     */
    function mostrarMensaje(message, type) {
        // Eliminar mensajes anteriores
        const oldMessage = document.querySelector('.floating-message');
        if (oldMessage) {
            oldMessage.remove();
        }

        // Crear nuevo mensaje
        const messageDiv = document.createElement('div');
        messageDiv.className = `floating-message ${type}`;
        messageDiv.innerHTML = `
            <i class="fas ${type === 'error' ? 'fa-exclamation-circle' : 'fa-check-circle'}"></i>
            <span>${message}</span>
            <button class="close-btn">&times;</button>
        `;

        // Estilos del mensaje
        messageDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            background: ${type === 'error' ? '#dc3545' : '#28a745'};
            color: white;
            padding: 12px 20px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            gap: 12px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            font-size: 0.9rem;
            animation: slideInRight 0.3s ease;
            max-width: 350px;
        `;

        // Estilo del botón cerrar
        const closeBtn = messageDiv.querySelector('.close-btn');
        closeBtn.style.cssText = `
            background: none;
            border: none;
            color: white;
            font-size: 1.2rem;
            cursor: pointer;
            margin-left: 10px;
        `;

        document.body.appendChild(messageDiv);

        // Auto-cerrar después de 5 segundos
        setTimeout(() => {
            if (messageDiv) {
                messageDiv.style.animation = 'slideOutRight 0.3s ease';
                setTimeout(() => messageDiv.remove(), 300);
            }
        }, 5000);

        // Cerrar al hacer clic en la X
        closeBtn.onclick = () => {
            messageDiv.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => messageDiv.remove(), 300);
        };
    }

    /**
     * Muestra loading en el botón mientras se envía
     * @param {boolean} isLoading - Si está cargando o no
     */
    function mostrarLoading(isLoading) {
        if (!submitBtn) return;

        if (isLoading) {
            // Guardar texto original
            submitBtn.setAttribute('data-original-text', submitBtn.innerHTML);
            // Mostrar loading
            submitBtn.innerHTML = `
                <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                Cargando...
            `;
            submitBtn.disabled = true;
        } else {
            // Restaurar texto original
            const originalText = submitBtn.getAttribute('data-original-text');
            if (originalText) {
                submitBtn.innerHTML = originalText;
            }
            submitBtn.disabled = false;
        }
    }

    // ========== 6. AGREGAR ANIMACIONES CSS ==========
    // Agregar estilos de animación dinámicamente
    const style = document.createElement('style');
    style.textContent = `
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

        @keyframes slideOutRight {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }

        /* Estilos para inputs válidos/inválidos */
        .form-control.is-valid {
            border-color: #28a745;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 8 8'%3e%3cpath fill='%2328a745' d='M2.3 6.73L.6 4.53c-.4-1.04.46-1.4 1.1-.8l1.1 1.4 3.4-3.8c.6-.63 1.6-.27 1.2.7l-4 4.6c-.43.5-.8.4-1.1.1z'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right 0.75rem center;
            background-size: 1rem;
        }

        .form-control.is-invalid {
            border-color: #dc3545;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right 0.75rem center;
            background-size: 1rem;
        }

        .invalid-feedback {
            display: block;
            width: 100%;
            margin-top: 0.25rem;
            font-size: 0.75rem;
            color: #dc3545;
        }

        /* Loading spinner */
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
    `;
    document.head.appendChild(style);
});