package com.caninoshop.service;

import com.caninoshop.domain.Categoria;
import com.caninoshop.repository.CategoriaRepository;
import com.caninoshop.repository.ProductoRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Categoria> getCategorias(boolean incluirInactivas) {
        return incluirInactivas
                ? categoriaRepository.findAllByOrderByNombreAsc()
                : categoriaRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> getCategoria(Integer idCategoria) {
        return categoriaRepository.findById(idCategoria);
    }

    @Transactional
    public Categoria guardar(Categoria categoria) {
        if (categoria.getIdCategoria() != null
                && !categoriaRepository.existsById(categoria.getIdCategoria())) {
            throw new IllegalArgumentException("Categoría no encontrada.");
        }
        String nombre = categoria.getNombre().trim();
        boolean nombreExiste = categoria.getIdCategoria() == null
                ? categoriaRepository.existsByNombreIgnoreCase(nombre)
                : categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot(
                        nombre, categoria.getIdCategoria());
        if (nombreExiste) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void desactivar(Integer idCategoria) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada."));
        if (productoRepository.existsByCategoria_IdCategoriaAndEstadoTrue(idCategoria)) {
            throw new IllegalStateException(
                    "No se puede desactivar una categoría que todavía tiene productos activos.");
        }
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }
}
