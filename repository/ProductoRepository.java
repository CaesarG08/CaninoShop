package com.caninoshop.repository;

import com.caninoshop.domain.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByEstadoTrueOrderByNombreAsc();

    List<Producto> findAllByOrderByNombreAsc();

    List<Producto> findByEstadoTrueAndDestacadoTrueOrderByNombreAsc();

    List<Producto> findByStock(Integer stock);

    List<Producto> findByEstadoTrueAndStockOrderByNombreAsc(Integer stock);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdProductoNot(String nombre, Integer idProducto);

    boolean existsByCategoria_IdCategoriaAndEstadoTrue(Integer idCategoria);
}
