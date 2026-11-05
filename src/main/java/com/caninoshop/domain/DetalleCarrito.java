package com.caninoshop.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "detalle_carrito")
public class DetalleCarrito implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_carrito")
    private Integer idDetalleCarrito;

    @ManyToOne // Carrito al que pertenece la línea.
    @JoinColumn(name = "id_carrito", nullable = false)
    private Carrito carrito;

    @ManyToOne // Producto agregado al carrito.
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Min(1)
    @Column(nullable = false)
    private Integer cantidad;

    // El subtotal se calcula con el precio actual del producto; no se guarda en la tabla.
    public BigDecimal getSubtotal() {
        if (producto == null || producto.getPrecio() == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }
}
