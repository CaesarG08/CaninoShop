package com.caninoshop.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter //Agrega los Get a los atributos
@Setter //Agrega los Set a los atributos
@NoArgsConstructor //Genera el constructor sin argumentos que requiere JPA.
@Entity //Indica que la clase se guarda en la base de datos.
@Table(name = "carrito")
public class Carrito implements Serializable {

    private static final long serialVersionUID = 1L;

    // Estados posibles del carrito de un cliente.
    public static final String ABIERTO = "ABIERTO";
    public static final String FINALIZADO = "FINALIZADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito")
    private Integer idCarrito;

    @ManyToOne // Cliente dueño del carrito.
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private Date fechaCreacion; // La asigna la base de datos (DEFAULT CURRENT_TIMESTAMP).

    @Column(nullable = false, length = 30)
    private String estado = ABIERTO;

    // Líneas del carrito: se cargan solo cuando se necesitan.
    @OneToMany(mappedBy = "carrito", fetch = FetchType.LAZY)
    @OrderBy("idDetalleCarrito ASC")
    private List<DetalleCarrito> detalles = new ArrayList<>();
}
