package com.refugio.proveedor.proveedor_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "envio")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEnvio;

    @Column(nullable = false)
    private Integer idPedido;

    @Column(name = "id_perro")
    private Integer idPerro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.PREPARANDO;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    /**
     * Los productos del pedido, guardados como JSON (lista de ItemPedidoDTO)
     * para poder reenviarlos en cada evento de seguimiento sin depender
     * de que tienda-service siga disponible.
     */
    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    public enum Estado { PREPARANDO, DESPACHADO, EN_TRANSITO, EN_REPARTO, ENTREGADO }
}