package com.refugio.tienda.tienda_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Resultado de la liquidación calculado 100 % en el backend.
 *
 * totalBruto = ventasMes - yaLiquidado
 * comisiones = totalBruto * porcentajeComision / 100
 * totalARecibir (neto) = totalBruto - comisiones
 */
@Getter
@Builder
@AllArgsConstructor
public class LiquidacionDTO {

    /** Total vendido en el mes en curso. */
    private BigDecimal ventasMes;

    /** Parte de las ventas del mes que ya fue liquidada al Refugio. */
    private BigDecimal yaLiquidado;

    /** Ventas pendientes de liquidar (base sobre la que se cobra la comisión). */
    private BigDecimal totalBruto;

    /** Porcentaje de comisión aplicado (viene de la configuración). */
    private BigDecimal porcentajeComision;

    /** Valor de la comisión que retiene la Tienda. */
    private BigDecimal comisiones;

    /** Neto que recibirá el Refugio (y que sale de la cuenta TIENDA). */
    private BigDecimal totalARecibir;
}
