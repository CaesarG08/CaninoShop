package com.caninoshop.repository;

import com.caninoshop.domain.DetalleCarrito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Integer> {

    // Líneas de un carrito en el orden en que se agregaron.
    List<DetalleCarrito> findByCarrito_IdCarritoOrderByIdDetalleCarritoAsc(Integer idCarrito);

    // Busca si el producto ya está en el carrito para sumar la cantidad en vez de repetir la línea.
    Optional<DetalleCarrito> findByCarrito_IdCarritoAndProducto_IdProducto(Integer idCarrito, Integer idProducto);

    // Cantidad total de unidades del carrito (para el contador del menú).
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM DetalleCarrito d WHERE d.carrito.idCarrito = :idCarrito")
    long contarUnidades(@Param("idCarrito") Integer idCarrito);
}
