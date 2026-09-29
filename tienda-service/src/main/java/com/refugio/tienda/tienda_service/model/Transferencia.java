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

    /*
     * Dinero que REALMENTE se movió entre las cuentas (monto NETO).
     * En las compras (REFUGIO -> TIENDA) es el total del pedido.
     * En las liquidaciones (TIENDA -> REFUGIO) es bruto - comisión.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    /*
     * Solo liquidaciones TIENDA -> REFUGIO. Nullable a propósito:
     * los registros antiguos y las compras no tienen comisión.
     * Se cumple siempre: montoBruto = monto (neto) + montoComision.
     */
    @Column(name = "monto_bruto", precision = 15, scale = 2)
    private BigDecimal montoBruto;

    @Column(name = "porcentaje_comision", precision = 5, scale = 2)
    private BigDecimal porcentajeComision;

    @Column(name = "monto_comision", precision = 15, scale = 2)
    private BigDecimal montoComision;

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
    @Column(name = "referencia_comprobante", length = 255, unique = true)
    private String referenciaComprobante;

    /*
     * Se conserva para no romper registros antiguos.
     */
    @Column(length = 255)
    private String descripcion;

    /*
     * "Borrar historial" NO elimina filas: solo las oculta de la lista.
     * Las liquidaciones se usan para calcular lo ya liquidado y los
     * comprobantes usados, así que borrarlas de verdad permitiría
     * liquidar dos veces lo mismo.
     */
    @Column(name = "oculta", nullable = false,
            columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean oculta = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}