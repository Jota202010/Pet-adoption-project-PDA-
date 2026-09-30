package com.refugio.tienda.tienda_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor
public class VentanaLiquidacionDTO {

 
    private boolean restringida;

    
    private boolean habilitada;

    
    private boolean liquidadaHoy;

    private long dias;

    
    private String desde;

    
    private String hasta;

    
    private String etiqueta;

    private String titulo;

    private String detalle;
}
