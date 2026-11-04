package com.caninoshop.repository;

import com.caninoshop.domain.Carrito;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    // Consulta derivada: el carrito de un cliente según su estado (solo hay uno ABIERTO a la vez).
    Optional<Carrito> findFirstByUsuario_IdUsuarioAndEstado(Integer idUsuario, String estado);
}
