package com.refugio.tienda.tienda_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transferencia")
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transferencia")
    private Integer idTransferencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cuenta_origen", nullable = false)
    private Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cuenta_destino", nullable = false)
    private Cuenta cuentaDestino;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    /*
     * Nuevo:
     * TRANSFERENCIA_BANCARIA o EFECTIVO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    /*
     * Nuevo:
     * número de comprobante, referencia bancaria,
     * número de recibo, etc.
     */
    @Column(name = "referencia_comprobante", length = 255)
    private String referenciaComprobante;

    /*
     * Se conserva para no romper registros antiguos.
     */
    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}