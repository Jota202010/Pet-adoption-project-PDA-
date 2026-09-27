package com.refugio.nueva_vida.proyecto_de_aula.model;

import com.refugio.nueva_vida.proyecto_de_aula.dtos.ItemPedidoDTO;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "paquete_recepcion")
public class PaqueteRecepcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete")
    private Integer idPaquete;

    @Column(name = "id_pedido", nullable = false, unique = true)
    private Integer idPedido;

    @Column(name = "id_perro")
    private Integer idPerro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.RECIBIDO;

    @Column(name = "items_json", columnDefinition = "TEXT", nullable = false)
    private String itemsJson = "[]";

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion = LocalDateTime.now();

    @Column(name = "fecha_apertura")
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_envio_inventario")
    private LocalDateTime fechaEnvioInventario;

    @Transient
    private List<ItemPedidoDTO> items;

    public enum Estado {
        RECIBIDO,
        ABIERTO,
        ENVIADO_INVENTARIO
    }
}