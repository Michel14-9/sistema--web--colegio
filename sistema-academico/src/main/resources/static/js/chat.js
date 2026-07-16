document.addEventListener('DOMContentLoaded', function () {
    const toggleBtn = document.getElementById('chatToggleBtn');
    const chatWidget = document.getElementById('chatWidget');
    const closeBtn = document.getElementById('chatWidgetClose');
    const minimizeBtn = document.getElementById('chatWidgetMinimize');
    const chatIcon = document.getElementById('chatIcon');
    const chatBadge = document.getElementById('chatBadge');
    const iframe = document.querySelector('.chat-widget-iframe');

    if (!toggleBtn || !chatWidget || !iframe) return; // evita romper la página si falta el widget

    let isOpen = false;
    let isMinimized = false;

    // ===== STORAGE SEGURO (puede fallar en incógnito/privado) =====
    function safeStorage(action, key, value) {
        try {
            if (action === 'get') return localStorage.getItem(key);
            localStorage.setItem(key, value);
        } catch (e) {
            return null;
        }
    }

    // ===== ABRIR/CERRAR CHAT =====
    function toggleChat() {
        isOpen = !isOpen;

        if (isOpen) {
            chatWidget.classList.remove('closed');
            chatWidget.classList.add('open');
            toggleBtn.classList.add('open');
            chatIcon.className = 'fas fa-times';
            chatBadge.classList.add('hidden');
            toggleBtn.setAttribute('aria-expanded', 'true');

            if (!iframe.src || iframe.src === 'about:blank') {
                iframe.src = iframe.dataset.src || '/chat?embed=true';
            }

            const body = chatWidget.querySelector('.chat-widget-body');
            body.classList.add('loading');
            iframe.addEventListener('load', () => body.classList.remove('loading'), { once: true });

            // Foco accesible al input dentro del iframe (mismo origen)
            iframe.addEventListener('load', () => {
                try {
                    iframe.contentWindow.document.getElementById('preguntaInput')?.focus();
                } catch (e) { /* cross-origin, ignorar */ }
            }, { once: true });

        } else {
            chatWidget.classList.remove('open');
            chatWidget.classList.add('closed');
            toggleBtn.classList.remove('open');
            chatIcon.className = 'fas fa-comment-dots';
            toggleBtn.setAttribute('aria-expanded', 'false');
        }

        saveState();
    }

    // ===== MINIMIZAR =====
    function toggleMinimize() {
        isMinimized = !isMinimized;
        chatWidget.classList.toggle('minimized');
        minimizeBtn.innerHTML = isMinimized
            ? '<i class="fas fa-expand"></i>'
            : '<i class="fas fa-minus"></i>';
        minimizeBtn.setAttribute('aria-label', isMinimized ? 'Expandir chat' : 'Minimizar chat');
        saveState();
    }

    // ===== EVENTOS =====
    toggleBtn.addEventListener('click', toggleChat);

    closeBtn?.addEventListener('click', function (e) {
        e.stopPropagation();
        if (isOpen) toggleChat();
    });

    minimizeBtn?.addEventListener('click', function (e) {
        e.stopPropagation();
        toggleMinimize();
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && isOpen) toggleChat();
    });

    // ===== PERSISTENCIA DE ESTADO =====
    function saveState() {
        safeStorage('set', 'chatWidgetOpen', isOpen);
        safeStorage('set', 'chatWidgetMinimized', isMinimized);
    }

    function loadState() {
        const savedOpen = safeStorage('get', 'chatWidgetOpen');
        const savedMinimized = safeStorage('get', 'chatWidgetMinimized');

        if (savedOpen === 'true') {
            setTimeout(toggleChat, 1000);
        }
        if (savedMinimized === 'true') {
            setTimeout(() => {
                isMinimized = true;
                chatWidget.classList.add('minimized');
                if (minimizeBtn) minimizeBtn.innerHTML = '<i class="fas fa-expand"></i>';
            }, 1200);
        }
    }

    // ===== NOTIFICACIÓN DE NUEVO MENSAJE =====
    window.addEventListener('message', function (event) {
        if (event.origin !== window.location.origin) return;

        let data;
        try {
            data = JSON.parse(event.data);
        } catch (e) {
            return; // no era JSON, ignorar en silencio
        }

        if (data.type === 'newMessage' && !isOpen) {
            chatBadge.classList.remove('hidden');
            chatBadge.textContent = '✦';
            if (navigator.vibrate) navigator.vibrate(100);
        }
    });

    loadState();
});