package com.caninoshop.service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import com.caninoshop.domain.DetallePedido;
import com.caninoshop.domain.Pedido;
import com.caninoshop.repository.DetallePedidoRepository;
import com.caninoshop.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Indica que esta clase contiene la lógica de pedidos.
@RequiredArgsConstructor // Genera el constructor con los repositorios requeridos.
public class PedidoService {

    // Repositorios que consultan los pedidos y sus detalles.
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    // Obtiene los pedidos filtrados y paginados.
    @Transactional(readOnly = true)
    public Page<Pedido> getPedidosFiltrados(LocalDate fechaInicio, LocalDate fechaFin, Long idEstado, Pageable pageable) {
        Date start = (fechaInicio != null) ? java.sql.Date.valueOf(fechaInicio) : null;
        // La fecha final se maneja como límite exclusivo para incluir todo ese día.
        Date end = (fechaFin != null) ? java.sql.Date.valueOf(fechaFin.plusDays(1)) : null;

        return pedidoRepository.consultaSQLFiltrosPedidos(start, end, idEstado, pageable);
    }

    // Busca un pedido por su identificador.
    @Transactional(readOnly = true)
    public Pedido getPedido(Pedido pedido) {
        return pedidoRepository.findById(pedido.getIdPedido()).orElse(null);
    }

    // Obtiene las líneas asociadas a un pedido, ordenadas por su código.
    @Transactional(readOnly = true)
    public List<DetallePedido> getDetallesPedido(Long idPedido) {
        return detallePedidoRepository.findByIdPedidoOrderByIdDetallePedidoAsc(idPedido);
    }

    // Guarda los cambios realizados en el pedido.
    @Transactional
    public void guardar(Pedido pedido) {
        pedidoRepository.save(pedido);
    }
}
