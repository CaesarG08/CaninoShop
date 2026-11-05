const pedidoModal = document.getElementById('pedidoModal');
if (pedidoModal) {
    // Carga en el modal los datos del pedido seleccionado para actualizarlo.
    pedidoModal.addEventListener('show.bs.modal', event => {
        const button = event.relatedTarget;
        
        const idPedido = button.getAttribute('data-id');
        const idUsuario = button.getAttribute('data-usuario');
        const total = button.getAttribute('data-total');
        const idEstado = button.getAttribute('data-estado');
        const titulo = pedidoModal.querySelector('#pedidoModalTitulo').dataset.titulo;
        const prefijoCliente = pedidoModal.querySelector('#pedidoModalTitulo').dataset.prefijoCliente;
        const moneda = pedidoModal.querySelector('#pedidoModalTitulo').dataset.moneda;
        
        pedidoModal.querySelector('.modal-title').textContent = titulo + idPedido;
        pedidoModal.querySelector('#lblCliente').textContent = prefijoCliente + idUsuario;
        pedidoModal.querySelector('#lblTotal').textContent = moneda + total;
        
        pedidoModal.querySelector('#pedidoId').value = idPedido;
        pedidoModal.querySelector('#pedidoUsuario').value = idUsuario;
        pedidoModal.querySelector('#pedidoTotal').value = total;
        
        pedidoModal.querySelector('#estadoId').value = idEstado;
    });
}
