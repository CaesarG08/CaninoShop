package com.caninoshop.service;

import com.caninoshop.domain.Categoria;
import com.caninoshop.domain.Producto;
import com.caninoshop.repository.CategoriaRepository;
import com.caninoshop.repository.ProductoRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Producto> listar(boolean incluirInactivos, String busqueda,
            Integer idCategoria) {
        List<Producto> productos = incluirInactivos
                ? productoRepository.findAllByOrderByNombreAsc()
                : productoRepository.findByEstadoTrueOrderByNombreAsc();

        return productos.stream()
                .filter(producto -> busqueda == null || busqueda.isBlank()
                        || producto.getNombre().toLowerCase().contains(busqueda.trim().toLowerCase()))
                .filter(producto -> idCategoria == null
                        || (producto.getCategoria() != null
                        && idCategoria.equals(producto.getCategoria().getIdCategoria())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Producto> getProductos() {
        return productoRepository.findByEstadoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Producto> getDestacados() {
        return productoRepository.findByEstadoTrueAndDestacadoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Producto> buscar(String nombre) {
        String termino = nombre == null ? "" : nombre.trim().toLowerCase();
        return getProductos().stream()
                .filter(producto -> producto.getNombre().toLowerCase().contains(termino))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Producto> getAgotados() {
        return productoRepository.findByEstadoTrueAndStockOrderByNombreAsc(0);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> getProducto(Integer idProducto) {
        return productoRepository.findById(idProducto);
    }

    @Transactional
    public Producto guardar(Producto producto) {
        if (producto.getIdProducto() != null
                && !productoRepository.existsById(producto.getIdProducto())) {
            throw new IllegalArgumentException("Producto no encontrado.");
        }
        if (producto.getCategoria() == null || producto.getCategoria().getIdCategoria() == null) {
            throw new IllegalArgumentException("Seleccione una categoría activa.");
        }
        Categoria categoria = categoriaRepository.findById(
                        producto.getCategoria().getIdCategoria())
                .filter(Categoria::isActivo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seleccione una categoría activa."));

        String nombre = producto.getNombre().trim();
        boolean nombreExiste = producto.getIdProducto() == null
                ? productoRepository.existsByNombreIgnoreCase(nombre)
                : productoRepository.existsByNombreIgnoreCaseAndIdProductoNot(
                        nombre, producto.getIdProducto());
        if (nombreExiste) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre.");
        }

        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        return productoRepository.save(producto);
    }

    @Transactional
    public void desactivar(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
        producto.setEstado(false);
        productoRepository.save(producto);
    }
}
