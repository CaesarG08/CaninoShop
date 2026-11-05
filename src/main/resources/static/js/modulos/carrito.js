// Carga en el modal el producto que se va a quitar del carrito
document.addEventListener('DOMContentLoaded', function () {
    const modal = document.getElementById('quitarModal');
    if (!modal) {
        return;
    }
    modal.addEventListener('show.bs.modal', function (event) {
        const button = event.relatedTarget;
        if (!button) {
            return;
        }
        document.getElementById('quitarId').value = button.getAttribute('data-id') || '';
        document.getElementById('quitarNombre').textContent = button.getAttribute('data-nombre') || '';
    });
});

// Quita las alertas de resultado después de unos segundos
document.addEventListener('DOMContentLoaded', function () {
    setTimeout(() => {
        document.querySelectorAll('.alert-dismissible').forEach(alerta =>
            alerta.classList.remove('show'));
    }, 5000);
});
