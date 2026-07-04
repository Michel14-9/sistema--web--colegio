function scrollToPosition() {
    // 1) Prioridad: si hay una respuesta del asistente, mostrar su inicio
    const lastResponse = document.getElementById('lastResponse');
    if (lastResponse) {
        lastResponse.scrollIntoView({ block: 'start', behavior: 'auto' });
        return;
    }

    // 2) Si no hay respuesta pero sí pregunta/error, mostrar ese bloque
    const lastExchange = document.getElementById('lastExchange');
    if (lastExchange) {
        lastExchange.scrollIntoView({ block: 'start', behavior: 'auto' });
        return;
    }

    // 3) Caso inicial (solo bienvenida): ir al final
    const chatBody = document.getElementById('chatBody');
    if (chatBody) {
        chatBody.scrollTop = chatBody.scrollHeight;
    }
}

document.addEventListener('DOMContentLoaded', scrollToPosition);
window.addEventListener('load', scrollToPosition);

if (document.fonts && document.fonts.ready) {
    document.fonts.ready.then(scrollToPosition);
}

setTimeout(scrollToPosition, 150);
setTimeout(scrollToPosition, 400);

document.addEventListener('DOMContentLoaded', function() {
    const chatForm = document.getElementById('chatForm');
    if (chatForm) {
        chatForm.addEventListener('submit', function(e) {
            const input = document.getElementById('preguntaInput');
            const pregunta = input.value.trim();

            if (!pregunta) {
                e.preventDefault();
                alert('Por favor, escribe una pregunta.');
                return;
            }

            const typingIndicator = document.getElementById('typingIndicator');
            const sendButton = document.getElementById('sendButton');

            typingIndicator.style.display = 'block';
            sendButton.disabled = true;
            input.disabled = false;
        });
    }

    document.querySelectorAll('.faq-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const typingIndicator = document.getElementById('typingIndicator');
            typingIndicator.style.display = 'block';
        });
    });
});