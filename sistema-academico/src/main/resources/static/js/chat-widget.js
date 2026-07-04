document.addEventListener('DOMContentLoaded', function() {
    const toggleBtn = document.getElementById('chatToggleBtn');
    const chatWidget = document.getElementById('chatWidget');
    const closeBtn = document.getElementById('chatWidgetClose');
    const icon = document.getElementById('chatIcon');
    const iframe = chatWidget.querySelector('.chat-widget-iframe');
    const iframeBaseSrc = iframe.getAttribute('src'); // ej: /chat?embed=true

    function abrirChat() {
        chatWidget.classList.add('active');
        icon.className = 'fas fa-times';

        // 🔥 FIX: recargar el iframe cada vez que se abre,
        // para reiniciar la conversación (volver al mensaje de bienvenida)
        iframe.src = iframeBaseSrc + '&_=' + new Date().getTime();
    }

    function cerrarChat() {
        chatWidget.classList.remove('active');
        icon.className = 'fas fa-comment-dots';
    }

    function toggleChat() {
        if (chatWidget.classList.contains('active')) {
            cerrarChat();
        } else {
            abrirChat();
        }
    }

    toggleBtn.addEventListener('click', toggleChat);
    closeBtn.addEventListener('click', cerrarChat);

    document.addEventListener('click', function(event) {
        const isClickInside = chatWidget.contains(event.target);
        const isClickOnToggle = toggleBtn.contains(event.target);
        if (!isClickInside && !isClickOnToggle && chatWidget.classList.contains('active')) {
            cerrarChat();
        }
    });

    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && chatWidget.classList.contains('active')) {
            cerrarChat();
        }
    });
});