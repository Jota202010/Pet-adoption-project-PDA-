package com.refugio.tienda.tienda_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferenciaRequestDTO {

    private BigDecimal monto;
    private String descripcion;
}