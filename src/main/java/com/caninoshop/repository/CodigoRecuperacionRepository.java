package com.caninoshop.repository;

import com.caninoshop.domain.CodigoRecuperacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Long> {
    @Query("select c from CodigoRecuperacion c where c.usuario.idUsuario = :usuarioId")
    List<CodigoRecuperacion> buscarPorUsuario(@Param("usuarioId") Integer usuarioId);
}
