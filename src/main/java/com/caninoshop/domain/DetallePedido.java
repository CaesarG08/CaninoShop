package com.caninoshop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter // Genera los métodos para consultar los atributos.
@Setter // Genera los métodos para modificar los atributos.
@NoArgsConstructor // Genera el constructor sin argumentos requerido por JPA.
@Entity // Indica que la clase representa una entidad de la base de datos.
@Table(name = "detalle_pedido") // Relaciona la entidad con la tabla detalle_pedido.
public class DetallePedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id // Identificador único de la línea del pedido.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_pedido")
    private Long idDetallePedido;

    @Column(name = "id_pedido", nullable = false) // Pedido al que pertenece esta línea.
    private Long idPedido;

    @Column(name = "id_producto", nullable = false) // Código del producto incluido.
    private Long idProducto;

    @Column(nullable = false) // Unidades solicitadas del producto.
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2) // Precio unitario al realizar el pedido.
    private BigDecimal precio;

    // Calcula el subtotal de la línea sin guardar un valor duplicado en la tabla.
    public BigDecimal getSubtotal() {
        if (precio == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}
