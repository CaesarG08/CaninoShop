package com.caninoshop.repository;

import com.caninoshop.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByCorreoAndActivoTrue(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByCedula(String cedula);
}
