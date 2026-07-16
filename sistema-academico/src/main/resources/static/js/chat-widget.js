document.addEventListener('DOMContentLoaded', function() {
    const toggleBtn = document.getElementById('chatToggleBtn');
    const chatWidget = document.getElementById('chatWidget');
    const closeBtn = document.getElementById('chatWidgetClose');
    const minimizeBtn = document.getElementById('chatWidgetMinimize');
    const chatIcon = document.getElementById('chatIcon');
    const chatBadge = document.getElementById('chatBadge');
    const iframe = document.querySelector('.chat-widget-iframe');
    const iframeBaseSrc = iframe.getAttribute('src');

    let isOpen = false;
    let isMinimized = false;

    function abrirChat() {
        chatWidget.classList.remove('closing');
        chatWidget.classList.add('active');
        isOpen = true;
        // 🔥 SIEMPRE GRADUACIÓN - nunca cambia
        chatIcon.className = 'fas fa-graduation-cap';
        chatBadge.classList.remove('has-messages');
        chatBadge.textContent = '';

        // Recargar iframe para reiniciar conversación
        iframe.src = iframeBaseSrc + '&_=' + new Date().getTime();

        // Mostrar loading
        const body = document.getElementById('chatWidgetBody');
        body.classList.add('loading');
        setTimeout(() => {
            body.classList.remove('loading');
        }, 1000);
    }

    function cerrarChat() {
        chatWidget.classList.add('closing');
        setTimeout(() => {
            chatWidget.classList.remove('active', 'closing');
            isOpen = false;
            // 🔥 SIEMPRE GRADUACIÓN - nunca cambia
            chatIcon.className = 'fas fa-graduation-cap';
        }, 250);
    }

    function toggleChat() {
        if (isOpen) {
            cerrarChat();
        } else {
            abrirChat();
        }
    }

    function toggleMinimize() {
        isMinimized = !isMinimized;
        chatWidget.classList.toggle('minimized');
        minimizeBtn.innerHTML = isMinimized ? '<i class="fas fa-expand"></i>' : '<i class="fas fa-minus"></i>';
    }

    // Eventos
    toggleBtn.addEventListener('click', toggleChat);
    closeBtn.addEventListener('click', cerrarChat);

    if (minimizeBtn) {
        minimizeBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            toggleMinimize();
        });
    }

    // Cerrar al hacer clic fuera
    document.addEventListener('click', function(event) {
        const isClickInside = chatWidget.contains(event.target);
        const isClickOnToggle = toggleBtn.contains(event.target);
        if (!isClickInside && !isClickOnToggle && isOpen) {
            cerrarChat();
        }
    });

    // Cerrar con ESC
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && isOpen) {
            cerrarChat();
        }
    });

    // Notificaciones de nuevos mensajes
    window.addEventListener('message', function(event) {
        if (event.origin !== window.location.origin) return;

        try {
            const data = JSON.parse(event.data);
            if (data.type === 'newMessage' && !isOpen) {
                chatBadge.classList.add('has-messages');
                chatBadge.textContent = '✦';

                if (navigator.vibrate) {
                    navigator.vibrate(100);
                }
            }
        } catch (e) {
            // Ignorar mensajes no JSON
        }
    });

    console.log('Chat Widget inicializado');
});