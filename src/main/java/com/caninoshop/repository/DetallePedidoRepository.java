package com.caninoshop.repository;

import com.caninoshop.domain.DetallePedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Indica que esta interfaz accede a los datos de las líneas del pedido.
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    // Consulta los productos de un pedido y los ordena por código de detalle.
    List<DetallePedido> findByIdPedidoOrderByIdDetallePedidoAsc(Long idPedido);
}
