package com.refugio.tienda.tienda_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class LiquidacionDTO {

    private BigDecimal ventasMes;

    private BigDecimal comisiones;

    private BigDecimal ajustes;

    private BigDecimal totalARecibir;
}