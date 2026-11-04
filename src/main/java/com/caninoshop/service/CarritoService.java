package com.caninoshop.service;

import com.caninoshop.domain.Carrito;
import com.caninoshop.domain.DetalleCarrito;
import com.caninoshop.domain.Producto;
import com.caninoshop.domain.Usuario;
import com.caninoshop.repository.CarritoRepository;
import com.caninoshop.repository.DetalleCarritoRepository;
import com.caninoshop.repository.ProductoRepository;
import com.caninoshop.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Lógica del carrito de compras de cada cliente.
@RequiredArgsConstructor
public class CarritoService {

    // Repositorios que usa el servicio.
    private final CarritoRepository carritoRepository;
    private final DetalleCarritoRepository detalleCarritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    // Devuelve el carrito abierto del cliente; si todavía no tiene, lo crea.
    @Transactional
    public Carrito getCarritoAbierto(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new CarritoException("carrito.error.usuario"));
        return carritoRepository
                .findFirstByUsuario_IdUsuarioAndEstado(usuario.getIdUsuario(), Carrito.ABIERTO)
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuario(usuario);
                    nuevo.setEstado(Carrito.ABIERTO);
                    return carritoRepository.save(nuevo);
                });
    }

    // Líneas del carrito abierto del cliente.
    @Transactional
    public List<DetalleCarrito> getDetalles(String username) {
        Carrito carrito = getCarritoAbierto(username);
        return detalleCarritoRepository
                .findByCarrito_IdCarritoOrderByIdDetalleCarritoAsc(carrito.getIdCarrito());
    }

    // Suma los subtotales de las líneas.
    public BigDecimal calcularTotal(List<DetalleCarrito> detalles) {
        return detalles.stream()
                .map(DetalleCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Unidades en el carrito, para el contador del menú. No crea el carrito si no existe.
    @Transactional(readOnly = true)
    public long contarUnidades(String username) {
        return usuarioRepository.findByUsername(username)
                .flatMap(u -> carritoRepository
                .findFirstByUsuario_IdUsuarioAndEstado(u.getIdUsuario(), Carrito.ABIERTO))
                .map(c -> detalleCarritoRepository.contarUnidades(c.getIdCarrito()))
                .orElse(0L);
    }

    // HU11: agrega un producto; si ya estaba en el carrito suma la cantidad.
    @Transactional
    public void agregar(String username, Integer idProducto, Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new CarritoException("carrito.error.cantidad");
        }
        Producto producto = productoRepository.findById(idProducto)
                .filter(Producto::isEstado)
                .orElseThrow(() -> new CarritoException("carrito.error.producto"));

        Carrito carrito = getCarritoAbierto(username);
        DetalleCarrito detalle = detalleCarritoRepository
                .findByCarrito_IdCarritoAndProducto_IdProducto(carrito.getIdCarrito(), idProducto)
                .orElse(null);

        int nuevaCantidad = cantidad + (detalle != null ? detalle.getCantidad() : 0);
        validarStock(producto, nuevaCantidad);

        if (detalle == null) {
            detalle = new DetalleCarrito();
            detalle.setCarrito(carrito);
            detalle.setProducto(producto);
        }
        detalle.setCantidad(nuevaCantidad);
        detalleCarritoRepository.save(detalle);
    }

    // HU12: cambia la cantidad de una línea del carrito.
    @Transactional
    public void actualizarCantidad(String username, Integer idDetalle, Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new CarritoException("carrito.error.cantidad");
        }
        DetalleCarrito detalle = getDetallePropio(username, idDetalle);
        validarStock(detalle.getProducto(), cantidad);
        detalle.setCantidad(cantidad);
        detalleCarritoRepository.save(detalle);
    }

    // HU12: quita una línea del carrito.
    @Transactional
    public void eliminar(String username, Integer idDetalle) {
        DetalleCarrito detalle = getDetallePropio(username, idDetalle);
        detalleCarritoRepository.delete(detalle);
    }

    // HU12: deja el carrito sin productos.
    @Transactional
    public void vaciar(String username) {
        Carrito carrito = getCarritoAbierto(username);
        detalleCarritoRepository.deleteAll(
                detalleCarritoRepository.findByCarrito_IdCarritoOrderByIdDetalleCarritoAsc(carrito.getIdCarrito()));
    }

    // Verifica que la línea exista y que pertenezca al carrito del cliente que la pide.
    private DetalleCarrito getDetallePropio(String username, Integer idDetalle) {
        Carrito carrito = getCarritoAbierto(username);
        return detalleCarritoRepository.findById(idDetalle)
                .filter(d -> d.getCarrito().getIdCarrito().equals(carrito.getIdCarrito()))
                .orElseThrow(() -> new CarritoException("carrito.error.detalle"));
    }

    private void validarStock(Producto producto, int cantidad) {
        if (producto.getStock() == null || cantidad > producto.getStock()) {
            throw new CarritoException("carrito.error.stock", producto.getNombre(), producto.getStock());
        }
    }
}
