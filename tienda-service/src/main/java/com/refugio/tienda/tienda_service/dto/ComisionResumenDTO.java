package com.refugio.tienda.tienda_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Resumen de la comisión que la Tienda va reteniendo en cada liquidación
 * y de cuánto de ella ya se gastó en stock.
 */
@Getter
@Builder
@AllArgsConstructor
public class ComisionResumenDTO {

    /** Porcentaje de comisión configurado. */
    private BigDecimal porcentaje;

    /** Comisión de las liquidaciones ya hechas. */
    private BigDecimal liquidada;

    /** Comisión de las ventas que todavía no se liquidan (se queda en la Tienda al liquidar). */
    private BigDecimal pendiente;

    /** liquidada + pendiente. */
    private BigDecimal acumulada;

    /** Total gastado en reponer stock. */
    private BigDecimal gastadaEnStock;

    /** Lo que hoy se puede gastar en stock sin afectar la próxima liquidación. */
    private BigDecimal disponible;
}