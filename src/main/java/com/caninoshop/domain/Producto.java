package com.caninoshop.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// NOTA: entidad mínima mapeada a la tabla producto del script común.
// El módulo de Catálogo (Génesis) es el dueño de esta clase; si ya existe en main se usa la suya.
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "producto")
public class Producto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "id_categoria", nullable = false)
    private Integer idCategoria;

    @Column(nullable = false, unique = true, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean estado;

    @Column(name = "ruta_imagen", length = 1024)
    private String rutaImagen;

    @Column(nullable = false)
    private boolean destacado;
}
