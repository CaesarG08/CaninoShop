package com.caninoshop.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@Getter //Agrega los Get a los atributos
@Setter //Agrega los Set a los atributos
@NoArgsConstructor //Genera automáticamente un constructor sin argumentos.
@Entity //Indica que una clase representa una entidad que se puede guardar en una base de datos.
@Table(name = "pedido")
public class Pedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "fecha_pedido")
    private Date fechaPedido;

    private double total;

    @ManyToOne
    @JoinColumn(name = "id_estado_pedido")
    private EstadoPedido estadoPedido;

}
