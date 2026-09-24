package com.refugio.tienda.tienda_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idItemPedido;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    private Integer idProducto;      // guardamos el id por referencia
    private String nombreProducto;   // y una "foto" del nombre/precio al momento de comprar
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private BigDecimal subtotal;
}
