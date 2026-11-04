package com.caninoshop.repository;

import java.util.List;
import com.caninoshop.domain.Pedido;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository // Indica que esta interfaz accede a los datos de los pedidos.
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Consulta los pedidos registrados para un cliente.
    List<Pedido> findByIdUsuario(Long idUsuario);

    // Consulta pedidos dentro de un rango de fechas.
    List<Pedido> findByFechaPedidoBetween(Date fechaInicio, Date fechaFin);

    // Consulta pedidos que tienen el estado indicado.
    List<Pedido> findByEstadoPedido_IdEstadoPedido(Long idEstadoPedido);

    // Aplica los filtros opcionales y devuelve los resultados por página.
    @Query("SELECT p FROM Pedido p WHERE "
            + "(:fechaInicio IS NULL OR p.fechaPedido >= :fechaInicio) AND "
            + "(:fechaFin IS NULL OR p.fechaPedido < :fechaFin) AND "
            + "(:idEstado IS NULL OR p.estadoPedido.idEstadoPedido = :idEstado) "
            + "ORDER BY p.idPedido DESC")
    Page<Pedido> consultaSQLFiltrosPedidos(
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin,
            @Param("idEstado") Long idEstado,
            Pageable pageable
    );

}
