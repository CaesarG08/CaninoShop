package com.caninoshop.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter //Agrega los Get a los atributos
@Setter //Agrega los Set a los atributos
@NoArgsConstructor //Genera automáticamente un constructor sin argumentos.
@Entity //Indica que una clase representa una entidad que se puede guardar en una base de datos.
@Table(name = "estado_pedido")
public class EstadoPedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_pedido")
    private Long idEstadoPedido;

    private String nombre;
}