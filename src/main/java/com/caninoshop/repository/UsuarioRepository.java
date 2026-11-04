
package com.caninoshop.repository;

import com.caninoshop.domain.Usuario;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsernameAndActivoTrue(String username);

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByCorreo(String correo);

    // Evita reutilizar un codigo TOTP simultaneamente
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.username = :username and u.activo = true")
    Optional<Usuario> bloquearUsuarioActivo(@Param("username") String username);

    boolean existsByUsername(String username);

    boolean existsByCorreo(String correo);
}
