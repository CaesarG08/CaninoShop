package com.caninoshop.service;

import com.caninoshop.domain.Carrito;
import com.caninoshop.domain.DetalleCarrito;
import com.caninoshop.domain.DetallePedido;
import com.caninoshop.domain.EstadoPedido;
import com.caninoshop.domain.Pedido;
import com.caninoshop.domain.Producto;
import com.caninoshop.repository.CarritoRepository;
import com.caninoshop.repository.DetallePedidoRepository;
import com.caninoshop.repository.EstadoPedidoRepository;
import com.caninoshop.repository.PedidoRepository;
import com.caninoshop.repository.ProductoRepository;
import com.caninoshop.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Convierte el carrito en un pedido (parte transaccional del sistema) y consulta el historial.
@RequiredArgsConstructor
public class CompraService {

    private final CarritoService carritoService;
    private final CarritoRepository carritoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // HU13/HU14: confirma la compra. Todo ocurre en una sola transacción: si algo falla no se guarda nada.
    @Transactional
    public Pedido confirmarCompra(String username) {
        Carrito carrito = carritoService.getCarritoAbierto(username);
        List<DetalleCarrito> detalles = carritoService.getDetalles(username);
        if (detalles.isEmpty()) {
            throw new CarritoException("carrito.error.vacio");
        }

        // 1. Revisa el stock con el producto bloqueado y lo descuenta.
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCarrito d : detalles) {
            Producto producto = d.getProducto();
            entityManager.refresh(producto, LockModeType.PESSIMISTIC_WRITE);
            if (!producto.isEstado() || producto.getStock() < d.getCantidad()) {
                throw new CarritoException("carrito.error.stock", producto.getNombre(), producto.getStock());
            }
            producto.setStock(producto.getStock() - d.getCantidad());
            total = total.add(d.getSubtotal());
        }

        // 2. Crea el pedido en estado inicial.
        Pedido pedido = new Pedido();
        pedido.setIdUsuario(carrito.getUsuario().getIdUsuario().longValue());
        pedido.setFechaPedido(new Date());
        pedido.setTotal(total.doubleValue());
        pedido.setEstadoPedido(getEstadoInicial());
        pedido = pedidoRepository.save(pedido);

        // 3. Guarda cada línea con el precio del momento de la compra.
        for (DetalleCarrito d : detalles) {
            DetallePedido linea = new DetallePedido();
            linea.setIdPedido(pedido.getIdPedido());
            linea.setIdProducto(d.getProducto().getIdProducto().longValue());
            linea.setCantidad(d.getCantidad());
            linea.setPrecio(d.getProducto().getPrecio());
            detallePedidoRepository.save(linea);
        }

        // 4. El carrito queda finalizado; la próxima compra abre uno nuevo.
        carrito.setEstado(Carrito.FINALIZADO);
        carritoRepository.save(carrito);
        return pedido;
    }

    // HU15: pedidos del cliente, del más reciente al más antiguo.
    @Transactional(readOnly = true)
    public List<Pedido> getPedidosCliente(String username) {
        return usuarioRepository.findByCorreo(username)
                .map(u -> pedidoRepository.findByIdUsuario(u.getIdUsuario().longValue()).stream()
                .sorted(Comparator.comparing(Pedido::getIdPedido).reversed())
                .toList())
                .orElse(List.of());
    }

    // Un pedido solo se muestra a su dueño; si es de otro cliente se trata como inexistente.
    @Transactional(readOnly = true)
    public Pedido getPedidoCliente(String username, Long idPedido) {
        return usuarioRepository.findByCorreo(username)
                .flatMap(u -> pedidoRepository.findById(idPedido)
                .filter(p -> p.getIdUsuario().equals(u.getIdUsuario().longValue())))
                .orElseThrow(() -> new CarritoException("pedido.cliente.error.noExiste"));
    }

    @Transactional(readOnly = true)
    public List<DetallePedido> getDetallesPedido(Long idPedido) {
        return detallePedidoRepository.findByIdPedidoOrderByIdDetallePedidoAsc(idPedido);
    }

    // Nombres de los productos de un pedido, para mostrarlos en vez del código.
    @Transactional(readOnly = true)
    public Map<Long, String> getNombresProductos(List<DetallePedido> detalles) {
        List<Integer> ids = detalles.stream().map(d -> d.getIdProducto().intValue()).toList();
        Map<Long, String> nombres = new HashMap<>();
        productoRepository.findAllById(ids)
                .forEach(p -> nombres.put(p.getIdProducto().longValue(), p.getNombre()));
        return nombres;
    }

    // El estado inicial es "Pendiente"; si no existe con ese nombre se usa el de menor código.
    private EstadoPedido getEstadoInicial() {
        List<EstadoPedido> estados = estadoPedidoRepository.findAll();
        return estados.stream()
                .filter(e -> e.getNombre() != null && e.getNombre().equalsIgnoreCase("Pendiente"))
                .findFirst()
                .or(() -> estados.stream().min(Comparator.comparing(EstadoPedido::getIdEstadoPedido)))
                .orElseThrow(() -> new CarritoException("carrito.error.estado"));
    }
}
